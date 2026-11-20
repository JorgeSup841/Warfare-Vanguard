package co.edu.unbosque.advancewars.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

import org.springframework.stereotype.Service;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Tile;
import co.edu.unbosque.advancewars.util.RoadNode;

@Service
public class MapGenerationService {

	private static final int MIN_MAP_SIZE = 5;
	private static final int MAX_MAP_SIZE = 50;
	private static final int FOREST_PERCENT = 22;
	private static final int MOUNTAIN_PERCENT = 8;
	private static final int WATER_PERCENT = 10;
	private static final int PERCENT_BASE = 100;
	private static final int NO_DIRECTION = 4;

	private static final int ROAD_COST = 1;
	private static final int PLAIN_COST = 5;
	private static final int FOREST_COST = 9;
	private static final int MOUNTAIN_COST = 30;
	private static final int BUILDING_PENALTY = 1000;
	private static final int EDGE_PENALTY = 6;
	private static final int TURN_PENALTY = 2;

	// Los movimientos se realizan solo en horizontal y vertical.
	private static final int[][] DIRECTIONS = { { 1, 0 }, { 0, 1 }, { -1, 0 }, { 0, -1 } };

	/**
	 * Crea bosques, montanas y lagos por grupos en lugar de casillas dispersas. Una
	 * misma semilla siempre produce el mismo mapa.
	 */
	public GameMap generateMap(int width, int height, long seed) {
		if (width < MIN_MAP_SIZE || width > MAX_MAP_SIZE || height < MIN_MAP_SIZE || height > MAX_MAP_SIZE) {
			throw new IllegalArgumentException("El mapa debe medir entre 5 y 50 casillas por lado");
		}

		Random random = new Random(seed);
		TerrainType[][] terrain = new TerrainType[height][width];

		for (int y = 0; y < height; y++) {
			Arrays.fill(terrain[y], TerrainType.PLAIN);
		}

		int area = width * height;
		paintRegions(terrain, TerrainType.FOREST, area * FOREST_PERCENT / PERCENT_BASE, random);
		paintRegions(terrain, TerrainType.MOUNTAIN, area * MOUNTAIN_PERCENT / PERCENT_BASE, random);
		paintWater(terrain, Math.max(1, area * WATER_PERCENT / PERCENT_BASE), random);
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
	 * Se ejecuta despues de ubicar las propiedades de una partida nueva. Conecta
	 * cuarteles, fabricas, ciudades, puertos y aeropuertos mediante carreteras
	 * reales, sin pasar por encima de los edificios ni del agua.
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

		List<Position> bases = new ArrayList<>();
		List<Position> others = new ArrayList<>();
		for (Property property : properties) {
			int x = property.getTile().getPosition().getX();
			int y = property.getTile().getPosition().getY();
			buildings[y][x] = true;
			terrain[y][x] = TerrainType.PLAIN;
			if (property.getDefinition().getType() == PropertyType.HEADQUARTERS) {
				bases.add(new Position(x, y));
			} else {
				others.add(new Position(x, y));
			}
		}

		drawRoadNetwork(terrain, bases, buildings);
		// Cada edificio adicional recibe un ramal hasta la carretera mas cercana.
		for (Position position : others) {
			connectToExistingRoad(terrain, buildings, position.getX(), position.getY());
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
		for (Position base : basePositions(width, height)) {
			terrain[base.getY()][base.getX()] = TerrainType.PLAIN;
			for (int[] direction : DIRECTIONS) {
				int x = base.getX() + direction[0];
				int y = base.getY() + direction[1];
				if (x >= 0 && x < width && y >= 0 && y < height) {
					terrain[y][x] = TerrainType.PLAIN;
				}
			}
		}
	}

	private List<Position> basePositions(int width, int height) {
		List<Position> result = new ArrayList<>();
		result.add(new Position(1, 1));
		result.add(new Position(width - 2, height - 2));
		result.add(new Position(width - 2, 1));
		result.add(new Position(1, height - 2));
		return result;
	}

	private boolean isNearBase(int x, int y, int width, int height) {
		return isNearBase(x, y, width, height, 1);
	}

	private boolean isNearBase(int x, int y, int width, int height, int radius) {
		for (Position base : basePositions(width, height)) {
			if (Math.abs(x - base.getX()) + Math.abs(y - base.getY()) <= radius) {
				return true;
			}
		}
		return false;
	}

	private void drawRoadNetwork(TerrainType[][] terrain, List<Position> bases, boolean[][] buildings) {
		int width = terrain[0].length;
		int height = terrain.length;
		if (buildings == null) {
			buildings = new boolean[height][width];
			for (Position base : bases) {
				buildings[base.getY()][base.getX()] = true;
			}
		}
		Position hub = findHub(terrain, buildings);
		if (hub == null) {
			return;
		}
		terrain[hub.getY()][hub.getX()] = TerrainType.ROAD;

		for (Position base : bases) {
			connectToExistingRoad(terrain, buildings, base.getX(), base.getY());
		}
	}

	// Busca la casilla central mas apropiada para la interseccion de caminos.
	private Position findHub(TerrainType[][] terrain, boolean[][] buildings) {
		int height = terrain.length;
		int width = terrain[0].length;
		int centerX = width / 2;
		int centerY = height / 2;
		Position best = null;
		int bestScore = Integer.MAX_VALUE;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (terrain[y][x] == TerrainType.WATER || (buildings != null && buildings[y][x])) {
					continue;
				}
				int score = (Math.abs(x - centerX) + Math.abs(y - centerY)) * 10;
				if (terrain[y][x] == TerrainType.MOUNTAIN) {
					score += 12;
				}
				if (score < bestScore) {
					bestScore = score;
					best = new Position(x, y);
				}
			}
		}
		return best;
	}

	// Dijkstra: evita el agua y prefiere pasar alrededor de los edificios.
	private void connectToExistingRoad(TerrainType[][] terrain, boolean[][] buildings, int startX, int startY) {
		int height = terrain.length;
		int width = terrain[0].length;
		int[][][] best = new int[height][width][DIRECTIONS.length + 1];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Arrays.fill(best[y][x], Integer.MAX_VALUE);
			}
		}

		PriorityQueue<RoadNode> queue = new PriorityQueue<>();

		queue.add(new RoadNode(startX, startY, NO_DIRECTION, 0, null));

		best[startY][startX][NO_DIRECTION] = 0;

		RoadNode destination = null;

		while (!queue.isEmpty()) {

			RoadNode current = queue.poll();

			if (current.getCost() != best[current.getY()][current.getX()][current.getDirection()]) {
				continue;
			}

			if ((current.getX() != startX || current.getY() != startY)
					&& terrain[current.getY()][current.getX()] == TerrainType.ROAD) {

				destination = current;
				break;
			}

			for (int direction = 0; direction < DIRECTIONS.length; direction++) {

				int x = current.getX() + DIRECTIONS[direction][0];
				int y = current.getY() + DIRECTIONS[direction][1];

				if (x < 0 || y < 0 || x >= width || y >= height) {
					continue;
				}

				if (terrain[y][x] == TerrainType.WATER) {
					continue;
				}

				int cost = current.getCost() + terrainCost(terrain[y][x]);

				// Penaliza atravesar edificios.
				if (buildings != null && buildings[y][x]) {
					cost += BUILDING_PENALTY;
				}

				// Prefiere construir carreteras lejos de los bordes.
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					cost += EDGE_PENALTY;
				}

				// Penaliza los cambios de direccion.
				if (current.getDirection() != NO_DIRECTION && current.getDirection() != direction) {

					cost += TURN_PENALTY;
				}

				if (cost < best[y][x][direction]) {

					best[y][x][direction] = cost;

					queue.add(new RoadNode(x, y, direction, cost, current));
				}
			}
		}

		// Reconstruir la carretera desde el destino hasta el origen.
		while (destination != null && destination.getParent() != null) {

			if (buildings == null || !buildings[destination.getY()][destination.getX()]) {

				terrain[destination.getY()][destination.getX()] = TerrainType.ROAD;
			}

			destination = destination.getParent();
		}
	}

	private int terrainCost(TerrainType terrain) {
		if (terrain == TerrainType.ROAD) {
			return ROAD_COST;
		}
		if (terrain == TerrainType.PLAIN) {
			return PLAIN_COST;
		}
		if (terrain == TerrainType.FOREST) {
			return FOREST_COST;
		}
		return MOUNTAIN_COST; // Montanas: solo se cruzan si no existe una ruta mejor.
	}

	// Comprueba que no haya islas de tierra separadas por el agua.
	private boolean isConnected(TerrainType[][] terrain) {
		int height = terrain.length;
		int width = terrain[0].length;
		boolean[][] visited = new boolean[height][width];
		Queue<Position> queue = new ArrayDeque<>();
		boolean foundLand = false;
		for (int y = 0; y < height && !foundLand; y++) {
			for (int x = 0; x < width && !foundLand; x++) {
				if (terrain[y][x] != TerrainType.WATER) {
					queue.add(new Position(x, y));
					visited[y][x] = true;
					foundLand = true;
				}
			}
		}
		if (!foundLand) {
			return false;
		}

		while (!queue.isEmpty()) {
			Position current = queue.poll();
			for (int[] direction : DIRECTIONS) {
				int x = current.getX() + direction[0];
				int y = current.getY() + direction[1];
				if (x >= 0 && x < width && y >= 0 && y < height && !visited[y][x]
						&& terrain[y][x] != TerrainType.WATER) {
					visited[y][x] = true;
					queue.add(new Position(x, y));
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
