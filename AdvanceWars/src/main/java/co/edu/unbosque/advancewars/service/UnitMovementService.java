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

	/** Inyecta las dependencias del servicio mediante constructor. */
	public UnitMovementService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository, GameEventService events) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
		this.events = events;
	}

	/** Aplica el movimiento validado de la unidad. */
	@Transactional
	public UnitDTO move(Long gameId, Long unitId, MoveUnitRequestDTO request, User creator) {
		requireMoveRequest(request, creator);
		GameState game = requireLockedGame(gameId, request, creator);
		Unit unit = findGameUnit(gameId, unitId);
		requireMovableUnit(game, unit);
		Tile[][] tiles = requireMovementTiles(game, request);
		Tile destination = tiles[request.getY()][request.getX()];
		int startX = unit.getPosition().getX();
		int startY = unit.getPosition().getY();
		if (startX == request.getX() && startY == request.getY()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La unidad ya esta en esa casilla");
		}
		Set<Long> occupied = occupiedTiles(gameId, unitId);
		if (occupied.contains(destination.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La casilla esta ocupada");
		}
		int movementCost = shortestPathCost(tiles, occupied, startX, startY, request.getX(), request.getY(),
				unit.getDefinition().getMovementType(), movementPoints(unit));
		if (movementCost < 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Destino inaccesible o fuera del alcance de movimiento");
		}
		int fuelCost = fuelCost(unit, movementCost);
		if (fuelCost > unit.getCurrentFuel()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Combustible insuficiente");
		}
		cancelPreviousCapture(unit);
		unit.moveTo(destination, fuelCost);
		unitRepository.save(unit);
		events.record(game, unit.getOwner(), GameLogType.UNIT_MOVED, "Unidad #" + unit.getId() + " movida de (" + startX
				+ "," + startY + ") a (" + request.getX() + "," + request.getY() + ")");
		return new UnitDTO(unit);
	}

	/**
	 * Consulta las casillas permitidas sin gastar combustible ni mover la unidad.
	 */
	@Transactional(readOnly = true)
	public List<ReachableTileDTO> reachableTiles(Long gameId, Long unitId, User creator) {
		Long userId = authenticatedId(creator);
		GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			return List.of();
		}
		Unit unit = findGameUnit(gameId, unitId);
		if (!isMovableUnit(game, unit)) {
			return List.of();
		}
		GameMap map = game.getGameMap();
		if (map == null || map.getHeight() == 0 || map.getWidth() == 0) {
			return List.of();
		}
		Tile[][] tiles = indexTiles(map);
		Set<Long> occupied = occupiedTiles(gameId, unitId);
		int[][] costs = reachableCosts(tiles, occupied, unit.getPosition().getX(), unit.getPosition().getY(),
				unit.getDefinition().getMovementType(), movementPoints(unit));
		return reachableWithFuel(map, tiles, costs, unit);
	}

	/** Valida el usuario que realiza la acción. */
	private Long authenticatedId(User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		return creator.getId();
	}

	/** Comprueba las coordenadas y el turno enviados con el movimiento. */
	private void requireMoveRequest(MoveUnitRequestDTO request, User creator) {
		authenticatedId(creator);
		if (request == null || request.getX() == null || request.getY() == null || request.getCurrentRound() == null
				|| request.getCurrentPlayerOrder() == null || request.getCurrentRound() < 1
				|| request.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos de movimiento incompletos");
		}
	}

	/** Aplica el bloqueo de partida y rechaza turnos desactualizados. */
	private GameState requireLockedGame(Long gameId, MoveUnitRequestDTO request, User creator) {
		GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
		}
		if (game.getCurrentRound() != request.getCurrentRound()
				|| game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
		}
		return game;
	}

	/** Carga la unidad y comprueba que pertenece a la partida solicitada. */
	private Unit findGameUnit(Long gameId, Long unitId) {
		Unit unit = unitRepository.findById(unitId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
		if (unit.getOwner() == null || unit.getOwner().getGameState() == null
				|| !gameId.equals(unit.getOwner().getGameState().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada en esta partida");
		}
		return unit;
	}

	/** Indica si la unidad aún puede moverse en el turno activo. */
	private boolean isMovableUnit(GameState game, Unit unit) {
		return unit.getOwner().isActive() && unit.getOwner().getTurnOrder() == game.getCurrentPlayerOrder()
				&& !unit.isDestroyed() && !unit.isMovedThisTurn() && !unit.isAttackedThisTurn()
				&& !unit.isCapturedThisTurn();
	}

	/** Distingue acciones agotadas del intento de mover fuera del turno. */
	private void requireMovableUnit(GameState game, Unit unit) {
		if (!unit.getOwner().isActive() || unit.getOwner().getTurnOrder() != game.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No es el turno de esta unidad");
		}
		if (!isMovableUnit(game, unit)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta unidad no puede moverse en este turno");
		}
	}

	/** Comprueba si la casilla de destino pertenece al mapa de la partida. */
	private Tile[][] requireMovementTiles(GameState game, MoveUnitRequestDTO request) {
		GameMap map = game.getGameMap();
		if (map == null || !map.isInsideMap(request.getX(), request.getY())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Destino fuera del mapa");
		}
		Tile[][] tiles = indexTiles(map);
		Tile destination = tiles[request.getY()][request.getX()];
		if (destination == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Falta la casilla de destino");
		}
		return tiles;
	}

	/** Construye una matriz de casillas para utilizar en el algoritmo de rutas. */
	private Tile[][] indexTiles(GameMap map) {
		Tile[][] tiles = new Tile[map.getHeight()][map.getWidth()];
		for (Tile tile : map.getTiles()) {
			tiles[tile.getPosition().getY()][tile.getPosition().getX()] = tile;
		}
		return tiles;
	}

	/** Obtiene los identificadores de casillas ocupadas por otras unidades. */
	private Set<Long> occupiedTiles(Long gameId, Long excludedUnitId) {
		Set<Long> occupied = new HashSet<>();
		for (Unit other : unitRepository.findAllByOwner_GameState_Id(gameId)) {
			if (!other.getId().equals(excludedUnitId)) {
				occupied.add(other.getTile().getId());
			}
		}
		return occupied;
	}

	/** Convierte los puntos de movimiento a combustible consumido. */
	private int fuelCost(Unit unit, int cost) {
		return unit.getDefinition().isUsesFuel() ? cost * unit.getDefinition().getFuelPerMovementPoint() : 0;
	}

	/** Cancela la captura parcial si una unidad sale del edificio. */
	private void cancelPreviousCapture(Unit unit) {
		Optional<Property> previousProperty = propertyRepository.findByTile_Id(unit.getTile().getId());
		if (previousProperty.isPresent()) {
			Property property = previousProperty.get();
			if (property.getCapturingPlayer() != null
					&& property.getCapturingPlayer().getId().equals(unit.getOwner().getId())) {
				property.resetCaptureProgress();
				propertyRepository.save(property);
			}
		}
	}

	/** Filtra rutas por combustible sin alterar el estado del juego. */
	private List<ReachableTileDTO> reachableWithFuel(GameMap map, Tile[][] tiles, int[][] costs, Unit unit) {
		List<ReachableTileDTO> result = new ArrayList<>();
		for (int y = 0; y < map.getHeight(); y++) {
			for (int x = 0; x < map.getWidth(); x++) {
				int cost = costs[y][x];
				if (cost == 0 || cost == Integer.MAX_VALUE || tiles[y][x] == null) {
					continue;
				}
				if (fuelCost(unit, cost) <= unit.getCurrentFuel()) {
					result.add(new ReachableTileDTO(x, y, cost));
				}
			}
		}
		return result;
	}

	/** El movimiento y la vista previa comparten exactamente el mismo algoritmo. */
	private int shortestPathCost(Tile[][] tiles, Set<Long> occupied, int startX, int startY, int endX, int endY,
			MovementType movementType, int maximumPoints) {
		int[][] costs = reachableCosts(tiles, occupied, startX, startY, movementType, maximumPoints);
		int cost = costs[endY][endX];
		return cost == Integer.MAX_VALUE ? -1 : cost;
	}

	/**
	 * Dijkstra: rutas ortogonales y costos iguales a los usados por el movimiento
	 * real.
	 */
	private int[][] reachableCosts(Tile[][] tiles, Set<Long> occupied, int startX, int startY,
			MovementType movementType, int maximumPoints) {
		int height = tiles.length;
		int width = tiles[0].length;
		int[][] best = new int[height][width];
		for (int[] row : best) {
			Arrays.fill(row, Integer.MAX_VALUE);
		}
		best[startY][startX] = 0;
		PriorityQueue<int[]> pending = new PriorityQueue<>(Comparator.comparingInt(step -> step[2]));
		pending.add(new int[] { startX, startY, 0 });
		int[][] directions = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };

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
					pending.add(new int[] { nx, ny, newCost });
				}
			}
		}
		return best;
	}

	/** Bonificaciones de poder autoritativas y de duracion de turno. */
	private int movementPoints(Unit unit) {
		int points = unit.getDefinition().getMovementPoints();
		if (unit.getOwner().getCommander() != null && unit.getOwner().isPowerActive()
				&& "MOVE_PLUS_1".equals(unit.getOwner().getCommander().getPowerEffectCode()))
			points++;
		for (co.edu.unbosque.advancewars.model.Player other : unit.getOwner().getGameState().getPlayers()) {
			if (other.getId() != null && !other.getId().equals(unit.getOwner().getId()) && other.isPowerActive()
					&& other.getCommander() != null
					&& "ENEMY_MOVE_MINUS_1".equals(other.getCommander().getPowerEffectCode()))
				points--;
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
