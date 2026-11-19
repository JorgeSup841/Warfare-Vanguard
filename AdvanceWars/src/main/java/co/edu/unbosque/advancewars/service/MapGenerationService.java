package co.edu.unbosque.advancewars.service;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

import org.springframework.stereotype.Service;

import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Tile;

@Service
public class MapGenerationService {

    // Crea un mapa aleatorio. La misma semilla genera el mismo terreno.
    public GameMap generateMap(int width, int height, long seed) {
        if (width < 5 || width > 50 || height < 5 || height > 50) {
            throw new IllegalArgumentException("El mapa debe medir entre 5 y 50 casillas por lado");
        }

        Random random = new Random(seed);
        TerrainType[][] terrain = new TerrainType[height][width];

        // Primero llenamos el mapa con terrenos transitables.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int number = random.nextInt(100);

                if (number < 20) {
                    terrain[y][x] = TerrainType.FOREST;
                } else if (number < 30) {
                    terrain[y][x] = TerrainType.MOUNTAIN;
                } else if (number < 40) {
                    terrain[y][x] = TerrainType.ROAD;
                } else {
                    terrain[y][x] = TerrainType.PLAIN;
                }
            }
        }

        // Agregamos aproximadamente un 12 % de agua, sin cortar caminos terrestres.
        int waterGoal = width * height / 8;
        int waterPlaced = 0;

        for (int attempt = 0; attempt < width * height * 10 && waterPlaced < waterGoal; attempt++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // Reservamos las cuatro posiciones de inicio y la esquina de busqueda.
            if (terrain[y][x] == TerrainType.WATER || isReserved(x, y, width, height)) {
                continue;
            }

            TerrainType previous = terrain[y][x];
            terrain[y][x] = TerrainType.WATER;

            if (isConnected(terrain)) {
                waterPlaced++;
            } else {
                terrain[y][x] = previous;
            }
        }

        // Convertimos la matriz en los modelos que ya tenemos.
        GameMap map = new GameMap(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                map.addTile(new Tile(new Position(x, y), terrain[y][x]));
            }
        }

        return map;
    }

    // No colocamos agua en las futuras posiciones de inicio de los jugadores.
    private boolean isReserved(int x, int y, int width, int height) {
        return (x == 0 && y == 0)
                || (x == 1 && y == 1)
                || (x == width - 2 && y == 1)
                || (x == 1 && y == height - 2)
                || (x == width - 2 && y == height - 2);
    }

    // Comprueba con una busqueda sencilla que toda la tierra este conectada.
    private boolean isConnected(TerrainType[][] terrain) {
        int height = terrain.length;
        int width = terrain[0].length;
        boolean[][] visited = new boolean[height][width];
        int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
        Queue<int[]> queue = new ArrayDeque<>();

        queue.add(new int[] { 0, 0 });
        visited[0][0] = true;

        while (!queue.isEmpty()) {
            int[] current = queue.poll();

            for (int[] direction : directions) {
                int x = current[0] + direction[0];
                int y = current[1] + direction[1];

                if (x >= 0 && x < width && y >= 0 && y < height
                        && !visited[y][x] && terrain[y][x] != TerrainType.WATER) {
                    visited[y][x] = true;
                    queue.add(new int[] { x, y });
                }
            }
        }

        // Si alguna casilla de tierra quedo aislada, rechazamos ese cambio.
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
