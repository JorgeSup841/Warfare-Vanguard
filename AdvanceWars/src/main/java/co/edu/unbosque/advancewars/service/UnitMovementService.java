package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.MoveUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.ReachableTileDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Tile;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Valida y realiza movimientos de unidades en el mapa. */
@Service
public class UnitMovementService {

    private final GameStateRepository gameRepository;
    private final UnitRepository unitRepository;
    private final PropertyRepository propertyRepository;
    private final GameEventService events;

    public UnitMovementService(GameStateRepository gameRepository, UnitRepository unitRepository,
            PropertyRepository propertyRepository, GameEventService events) {
        this.gameRepository = gameRepository;
        this.unitRepository = unitRepository;
        this.propertyRepository = propertyRepository;
        this.events = events;
    }

    @Transactional
    public UnitDTO move(Long gameId, Long unitId, MoveUnitRequestDTO request, User creator) {
        if (creator == null || creator.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
        }
        if (request == null || request.getX() == null || request.getY() == null
                || request.getCurrentRound() == null || request.getCurrentPlayerOrder() == null
                || request.getCurrentRound() < 1 || request.getCurrentPlayerOrder() < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de movimiento incompletos");
        }

        // El bloqueo evita que dos movimientos simultaneos modifiquen la misma partida.
        GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
        }
        if (game.getCurrentRound() != request.getCurrentRound()
                || game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
        }

        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        if (unit.getOwner() == null || unit.getOwner().getGameState() == null
                || !gameId.equals(unit.getOwner().getGameState().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada en esta partida");
        }
        if (!unit.getOwner().isActive()
                || unit.getOwner().getTurnOrder() != game.getCurrentPlayerOrder()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No es el turno de esta unidad");
        }
        if (unit.isDestroyed() || unit.isMovedThisTurn()
                || unit.isAttackedThisTurn() || unit.isCapturedThisTurn()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta unidad no puede moverse en este turno");
        }

        GameMap map = game.getGameMap();
        if (map == null || !map.isInsideMap(request.getX(), request.getY())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destino fuera del mapa");
        }
        int startX = unit.getPosition().getX();
        int startY = unit.getPosition().getY();
        int endX = request.getX();
        int endY = request.getY();
        if (startX == endX && startY == endY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad ya esta en esa casilla");
        }

        // Cargamos el tablero y las unidades una sola vez.
        Tile[][] tiles = new Tile[map.getHeight()][map.getWidth()];
        for (Tile tile : map.getTiles()) {
            tiles[tile.getPosition().getY()][tile.getPosition().getX()] = tile;
        }
        Tile destination = tiles[endY][endX];
        if (destination == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Falta la casilla de destino");
        }

        Set<Long> occupied = new HashSet<>();
        for (Unit other : unitRepository.findAllByOwner_GameState_Id(gameId)) {
            if (!other.getId().equals(unitId)) {
                occupied.add(other.getTile().getId());
            }
        }
        if (occupied.contains(destination.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La casilla esta ocupada");
        }

        int movementCost = shortestPathCost(tiles, occupied, startX, startY, endX, endY,
                unit.getDefinition().getMovementType(), movementPoints(unit));
        if (movementCost < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Destino inaccesible o fuera del alcance de movimiento");
        }

        int fuelCost = unit.getDefinition().isUsesFuel()
                ? movementCost * unit.getDefinition().getFuelPerMovementPoint() : 0;
        if (fuelCost > unit.getCurrentFuel()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Combustible insuficiente");
        }

        // Al abandonar un edificio, pierde la captura que llevaba acumulada.
        Optional<Property> previousProperty =
                propertyRepository.findByTile_Id(unit.getTile().getId());
        if (previousProperty.isPresent()) {
            Property property = previousProperty.get();
            if (property.getCapturingPlayer() != null
                    && property.getCapturingPlayer().getId().equals(unit.getOwner().getId())) {
                property.resetCaptureProgress();
                propertyRepository.save(property);
            }
        }

        unit.moveTo(destination, fuelCost);
        unitRepository.save(unit);
        events.record(game, unit.getOwner(), GameLogType.UNIT_MOVED, "Unidad #" + unit.getId()
                + " movida de (" + startX + "," + startY + ") a (" + endX + "," + endY + ")");
        return new UnitDTO(unit);
    }

    /**
     * Consulta las casillas permitidas sin cambiar posiciones ni gastar combustible.
     * Emplea el mismo cálculo de rutas que move(), con autorización por partida.
     */
    @Transactional(readOnly = true)
    public List<ReachableTileDTO> reachableTiles(Long gameId, Long unitId, User creator) {
        if (creator == null || creator.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
        }
        GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            return List.of();
        }
        Unit unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
        if (unit.getOwner() == null || unit.getOwner().getGameState() == null
                || !gameId.equals(unit.getOwner().getGameState().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada en esta partida");
        }
        if (!unit.getOwner().isActive() || unit.getOwner().getTurnOrder() != game.getCurrentPlayerOrder()
                || unit.isDestroyed() || unit.isMovedThisTurn() || unit.isAttackedThisTurn()
                || unit.isCapturedThisTurn()) {
            return List.of();
        }
        GameMap map = game.getGameMap();
        if (map == null || map.getHeight() == 0 || map.getWidth() == 0) {
            return List.of();
        }
        Tile[][] tiles = new Tile[map.getHeight()][map.getWidth()];
        for (Tile tile : map.getTiles()) {
            tiles[tile.getPosition().getY()][tile.getPosition().getX()] = tile;
        }
        Set<Long> occupied = new HashSet<>();
        for (Unit other : unitRepository.findAllByOwner_GameState_Id(gameId)) {
            if (!other.getId().equals(unitId)) {
                occupied.add(other.getTile().getId());
            }
        }
        int[][] costs = reachableCosts(tiles, occupied, unit.getPosition().getX(),
                unit.getPosition().getY(), unit.getDefinition().getMovementType(),
                movementPoints(unit));
        List<ReachableTileDTO> result = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                int cost = costs[y][x];
                if (cost == 0 || cost == Integer.MAX_VALUE || tiles[y][x] == null) {
                    continue;
                }
                int fuelCost = unit.getDefinition().isUsesFuel()
                        ? cost * unit.getDefinition().getFuelPerMovementPoint() : 0;
                if (fuelCost <= unit.getCurrentFuel()) {
                    result.add(new ReachableTileDTO(x, y, cost));
                }
            }
        }
        return result;
    }

    /** El movimiento y la vista previa comparten exactamente el mismo algoritmo. */
    private int shortestPathCost(Tile[][] tiles, Set<Long> occupied,
            int startX, int startY, int endX, int endY,
            MovementType movementType, int maximumPoints) {
        int[][] costs = reachableCosts(tiles, occupied, startX, startY, movementType, maximumPoints);
        int cost = costs[endY][endX];
        return cost == Integer.MAX_VALUE ? -1 : cost;
    }

    /** Dijkstra: rutas ortogonales y costos iguales a los usados por el movimiento real. */
    private int[][] reachableCosts(Tile[][] tiles, Set<Long> occupied,
            int startX, int startY, MovementType movementType, int maximumPoints) {
        int height = tiles.length;
        int width = tiles[0].length;
        int[][] best = new int[height][width];
        for (int[] row : best) {
            Arrays.fill(row, Integer.MAX_VALUE);
        }
        best[startY][startX] = 0;
        PriorityQueue<int[]> pending = new PriorityQueue<>(Comparator.comparingInt(step -> step[2]));
        pending.add(new int[] {startX, startY, 0});
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!pending.isEmpty()) {
            int[] current = pending.poll();
            int x = current[0];
            int y = current[1];
            int total = current[2];
            if (total != best[y][x]) {
                continue;
            }
            for (int[] direction : directions) {
                int nx = x + direction[0];
                int ny = y + direction[1];
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
                    continue;
                }
                Tile neighbor = tiles[ny][nx];
                if (neighbor == null || occupied.contains(neighbor.getId())) {
                    continue;
                }
                int stepCost = terrainCost(movementType, neighbor.getTerrainType());
                if (stepCost < 0) {
                    continue;
                }
                int newCost = total + stepCost;
                if (newCost <= maximumPoints && newCost < best[ny][nx]) {
                    best[ny][nx] = newCost;
                    pending.add(new int[] {nx, ny, newCost});
                }
            }
        }
        return best;
    }

    /** Bonificaciones de poder autoritativas y de duracion de turno. */
    private int movementPoints(Unit unit) {
        int points = unit.getDefinition().getMovementPoints();
        if (unit.getOwner().getCommander() != null && unit.getOwner().isPowerActive()
                && "MOVE_PLUS_1".equals(unit.getOwner().getCommander().getPowerEffectCode())) points++;
        for (co.edu.unbosque.advancewars.model.Player other : unit.getOwner().getGameState().getPlayers()) {
            if (other.getId() != null && !other.getId().equals(unit.getOwner().getId())
                    && other.isPowerActive() && other.getCommander() != null
                    && "ENEMY_MOVE_MINUS_1".equals(other.getCommander().getPowerEffectCode())) points--;
        }
        return Math.max(1, points);
    }

    /** -1 significa que ese terreno es intransitable para la unidad. */
    private int terrainCost(MovementType movementType, TerrainType terrain) {
        if (movementType == MovementType.AIR) {
            return 1;
        }
        if (movementType == MovementType.SHIP) {
            return terrain == TerrainType.WATER ? 1 : -1;
        }
        if (terrain == TerrainType.WATER) {
            return -1;
        }
        if (terrain == TerrainType.MOUNTAIN) {
            return (movementType == MovementType.FOOT || movementType == MovementType.MECH) ? 2 : -1;
        }
        if (terrain == TerrainType.FOREST) {
            return switch (movementType) {
                case FOOT, TREAD -> 2;
                case WHEEL -> 3;
                default -> 1;
            };
        }
        return 1; // PLAIN y ROAD
    }
}
