package co.edu.unbosque.advancewars.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.springframework.stereotype.Service;

import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Tile;

@Service
public class MapGenerationService {

    // Las carreteras solo se conectan por los cuatro lados de una casilla.
    private static final int[][] DIRECTIONS = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

    /**
     * Crea bosques, montanas y lagos por grupos en lugar de casillas dispersas.
     * Una misma semilla siempre produce el mismo mapa.
     */
    public GameMap generateMap(int width, int height, long seed) {
        if (width < 5 || width > 50 || height < 5 || height > 50) {
            throw new IllegalArgumentException("El mapa debe medir entre 5 y 50 casillas por lado");
        }

        Random random = new Random(seed);
        TerrainType[][] terrain = new TerrainType[height][width];

        for (int y = 0; y < height; y++) {
            Arrays.fill(terrain[y], TerrainType.PLAIN);
        }

        int area = width * height;
        paintRegions(terrain, TerrainType.FOREST, area * 22 / 100, random);
        paintRegions(terrain, TerrainType.MOUNTAIN, area * 8 / 100, random);
        paintWater(terrain, Math.max(1, area / 10), random);
        clearBases(terrain);

        // Las carreteras ya tienen continuidad aunque se consulte el mapa solo.
        // Al poner los edificios se recalcularan para conectarlos tambien.
        drawRoadNetwork(terrain, basePositions(width, height), null);

        GameMap map = new GameMap(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                map.addTile(new Tile(new Position(x, y), terrain[y][x]));
            }
        }
        return map;
    }

    /**
     * Se ejecuta despues de ubicar las propiedades de una partida nueva.
     * Conecta cuarteles, fabricas, ciudades, puertos y aeropuertos mediante
     * carreteras reales, sin pasar por encima de los edificios ni del agua.
     */
    public void connectPropertiesToRoads(GameMap map, List<Property> properties) {
        if (map == null || properties == null || properties.isEmpty()) {
            return;
        }

        int width = map.getWidth();
        int height = map.getHeight();
        TerrainType[][] terrain = new TerrainType[height][width];
        boolean[][] buildings = new boolean[height][width];

        for (Tile tile : map.getTiles()) {
            int x = tile.getPosition().getX();
            int y = tile.getPosition().getY();
            terrain[y][x] = tile.getTerrainType();
            if (terrain[y][x] == TerrainType.ROAD) {
                terrain[y][x] = TerrainType.PLAIN;
            }
        }

        List<int[]> bases = new ArrayList<>();
        List<int[]> others = new ArrayList<>();
        for (Property property : properties) {
            int x = property.getTile().getPosition().getX();
            int y = property.getTile().getPosition().getY();
            buildings[y][x] = true;
            terrain[y][x] = TerrainType.PLAIN;
            if (property.getDefinition().getType().name().equals("HEADQUARTERS")) {
                bases.add(new int[] { x, y });
            } else {
                others.add(new int[] { x, y });
            }
        }

        drawRoadNetwork(terrain, bases, buildings);
        // Cada edificio adicional recibe un ramal hasta la carretera mas cercana.
        for (int[] position : others) {
            connectToExistingRoad(terrain, buildings, position[0], position[1]);
        }

        for (Tile tile : map.getTiles()) {
            int x = tile.getPosition().getX();
            int y = tile.getPosition().getY();
            tile.setTerrainType(terrain[y][x]);
        }
    }

    // Agrupa los biomas con pequenos paseos aleatorios.
    private void paintRegions(TerrainType[][] terrain, TerrainType type, int desired, Random random) {
        int height = terrain.length;
        int width = terrain[0].length;
        int painted = 0;
        int attempts = 0;

        while (painted < desired && attempts < width * height * 25) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int length = 3 + random.nextInt(5);

            for (int step = 0; step < length && painted < desired; step++) {
                attempts++;
                if (!isNearBase(x, y, width, height) && terrain[y][x] == TerrainType.PLAIN) {
                    terrain[y][x] = type;
                    painted++;
                }
                int direction = random.nextInt(4);
                x = Math.max(0, Math.min(width - 1, x + DIRECTIONS[direction][0]));
                y = Math.max(0, Math.min(height - 1, y + DIRECTIONS[direction][1]));
            }
        }
    }

    // Agua en lagos pequenos; nunca aislamos una parte del mapa terrestre.
    private void paintWater(TerrainType[][] terrain, int desired, Random random) {
        int height = terrain.length;
        int width = terrain[0].length;
        int painted = 0;
        int attempts = 0;

        while (painted < desired && attempts < width * height * 35) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int length = 3 + random.nextInt(5);

            for (int step = 0; step < length && painted < desired; step++) {
                attempts++;
                if (!isNearBase(x, y, width, height, 2) && terrain[y][x] != TerrainType.WATER) {
                    TerrainType previous = terrain[y][x];
                    terrain[y][x] = TerrainType.WATER;
                    if (isConnected(terrain)) {
                        painted++;
                    } else {
                        terrain[y][x] = previous;
                    }
                }
                int direction = random.nextInt(4);
                x = Math.max(0, Math.min(width - 1, x + DIRECTIONS[direction][0]));
                y = Math.max(0, Math.min(height - 1, y + DIRECTIONS[direction][1]));
            }
        }
    }

    // Deja una pequena zona transitable alrededor de las cuatro posibles bases.
    private void clearBases(TerrainType[][] terrain) {
        int height = terrain.length;
        int width = terrain[0].length;
        for (int[] base : basePositions(width, height)) {
            terrain[base[1]][base[0]] = TerrainType.PLAIN;
            for (int[] direction : DIRECTIONS) {
                int x = base[0] + direction[0];
                int y = base[1] + direction[1];
                if (x >= 0 && x < width && y >= 0 && y < height) {
                    terrain[y][x] = TerrainType.PLAIN;
                }
            }
        }
    }

    private List<int[]> basePositions(int width, int height) {
        List<int[]> result = new ArrayList<>();
        result.add(new int[] { 1, 1 });
        result.add(new int[] { width - 2, height - 2 });
        result.add(new int[] { width - 2, 1 });
        result.add(new int[] { 1, height - 2 });
        return result;
    }

    private boolean isNearBase(int x, int y, int width, int height) {
        return isNearBase(x, y, width, height, 1);
    }

    private boolean isNearBase(int x, int y, int width, int height, int radius) {
        if (x == 0 && y == 0) {
            return true;
        }
        for (int[] base : basePositions(width, height)) {
            if (Math.abs(x - base[0]) + Math.abs(y - base[1]) <= radius) {
                return true;
            }
        }
        return false;
    }

    private void drawRoadNetwork(TerrainType[][] terrain, List<int[]> bases, boolean[][] buildings) {
        int width = terrain[0].length;
        int height = terrain.length;
        if (buildings == null) {
            buildings = new boolean[height][width];
            for (int[] base : bases) {
                buildings[base[1]][base[0]] = true;
            }
        }
        int[] hub = findHub(terrain, buildings);
        if (hub == null) {
            return;
        }
        terrain[hub[1]][hub[0]] = TerrainType.ROAD;

        for (int[] base : bases) {
            connectToExistingRoad(terrain, buildings, base[0], base[1]);
        }
    }

    // Busca la casilla central mas apropiada para la interseccion de caminos.
    private int[] findHub(TerrainType[][] terrain, boolean[][] buildings) {
        int height = terrain.length;
        int width = terrain[0].length;
        int centerX = width / 2;
        int centerY = height / 2;
        int[] best = null;
        int bestScore = Integer.MAX_VALUE;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (terrain[y][x] == TerrainType.WATER
                        || (buildings != null && buildings[y][x])) {
                    continue;
                }
                int score = (Math.abs(x - centerX) + Math.abs(y - centerY)) * 10;
                if (terrain[y][x] == TerrainType.MOUNTAIN) {
                    score += 12;
                }
                if (score < bestScore) {
                    bestScore = score;
                    best = new int[] { x, y };
                }
            }
        }
        return best;
    }

    // Dijkstra: evita el agua y prefiere pasar alrededor de los edificios.
    private void connectToExistingRoad(TerrainType[][] terrain, boolean[][] buildings,
                                       int startX, int startY) {
        int height = terrain.length;
        int width = terrain[0].length;
        int[][][] best = new int[height][width][5];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Arrays.fill(best[y][x], Integer.MAX_VALUE);
            }
        }

        PriorityQueue<RoadNode> queue = new PriorityQueue<>();
        queue.add(new RoadNode(startX, startY, 4, 0, null));
        best[startY][startX][4] = 0;

        RoadNode destination = null;
        while (!queue.isEmpty()) {
            RoadNode current = queue.poll();
            if (current.cost != best[current.y][current.x][current.direction]) {
                continue;
            }
            if ((current.x != startX || current.y != startY)
                    && terrain[current.y][current.x] == TerrainType.ROAD) {
                destination = current;
                break;
            }

            for (int direction = 0; direction < DIRECTIONS.length; direction++) {
                int x = current.x + DIRECTIONS[direction][0];
                int y = current.y + DIRECTIONS[direction][1];
                if (x < 0 || y < 0 || x >= width || y >= height) {
                    continue;
                }
                if (terrain[y][x] == TerrainType.WATER) {
                    continue;
                }

                int cost = current.cost + terrainCost(terrain[y][x]);
                // Los edificios funcionan como cruces, sin borrar su casilla.
                // Se evitan si hay otra ruta razonable.
                if (buildings != null && buildings[y][x]) {
                    cost += 1000;
                }
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                    cost += 6; // Los caminos prefieren el interior del mapa.
                }
                if (current.direction != 4 && current.direction != direction) {
                    cost += 2; // Evita escaleras y giros innecesarios.
                }
                if (cost < best[y][x][direction]) {
                    best[y][x][direction] = cost;
                    queue.add(new RoadNode(x, y, direction, cost, current));
                }
            }
        }

        // No modifica el edificio inicial ni las propiedades que estan en medio.
        while (destination != null && destination.parent != null) {
            if (buildings == null || !buildings[destination.y][destination.x]) {
                terrain[destination.y][destination.x] = TerrainType.ROAD;
            }
            destination = destination.parent;
        }
    }

    private int terrainCost(TerrainType terrain) {
        if (terrain == TerrainType.ROAD) {
            return 1;
        }
        if (terrain == TerrainType.PLAIN) {
            return 5;
        }
        if (terrain == TerrainType.FOREST) {
            return 9;
        }
        return 30; // Montanas: solo se cruzan si no existe una ruta mejor.
    }

    private static class RoadNode implements Comparable<RoadNode> {
        private final int x;
        private final int y;
        private final int direction;
        private final int cost;
        private final RoadNode parent;

        RoadNode(int x, int y, int direction, int cost, RoadNode parent) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.cost = cost;
            this.parent = parent;
        }

        @Override
        public int compareTo(RoadNode other) {
            return Integer.compare(cost, other.cost);
        }
    }

    // Comprueba que no haya islas de tierra separadas por el agua.
    private boolean isConnected(TerrainType[][] terrain) {
        int height = terrain.length;
        int width = terrain[0].length;
        boolean[][] visited = new boolean[height][width];
        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] { 0, 0 });
        visited[0][0] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            for (int[] direction : DIRECTIONS) {
                int x = current[0] + direction[0];
                int y = current[1] + direction[1];
                if (x >= 0 && x < width && y >= 0 && y < height
                        && !visited[y][x] && terrain[y][x] != TerrainType.WATER) {
                    visited[y][x] = true;
                    queue.add(new int[] { x, y });
                }
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (terrain[y][x] != TerrainType.WATER && !visited[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }
}
