package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.CreateGameDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.model.GameConfiguration;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Faction;
import co.edu.unbosque.advancewars.model.Commander;
import co.edu.unbosque.advancewars.repository.FactionRepository;
import co.edu.unbosque.advancewars.repository.CommanderRepository;
import co.edu.unbosque.advancewars.dto.PlayerSelectionRequestDTO;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Crea partidas y permite consultar unicamente las del usuario autenticado. */
@Service
public class GameService {

	private final GameStateRepository gameRepository;
	private final MapGenerationService mapGenerationService;
	private final PropertyGenerationService propertyGenerationService;
	private final PropertyRepository propertyRepository;
	private final UnitGenerationService unitGenerationService;
	private final UnitRepository unitRepository;
	private final GameEventService events;
	private final FactionRepository factions;
	private final CommanderRepository commanders;

	private static final int STARTING_MONEY = 5000;

	/** Inyecta repositorios y servicios del motor de partidas. */
	public GameService(GameStateRepository gameRepository, MapGenerationService mapGenerationService,
			PropertyGenerationService propertyGenerationService, PropertyRepository propertyRepository,
			UnitGenerationService unitGenerationService, UnitRepository unitRepository, GameEventService events,
			FactionRepository factions, CommanderRepository commanders) {
		this.gameRepository = gameRepository;
		this.mapGenerationService = mapGenerationService;
		this.propertyGenerationService = propertyGenerationService;
		this.propertyRepository = propertyRepository;
		this.unitGenerationService = unitGenerationService;
		this.unitRepository = unitRepository;
		this.events = events;
		this.factions = factions;
		this.commanders = commanders;
	}

	/** Crea una partida, su mapa y sus propiedades en una sola transaccion. */
	@Transactional
	public GameDTO createGame(CreateGameDTO data, User creator) {
		checkUser(creator);

		if (data == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos de la partida");
		}

		List<String> names = validateNames(data.getPlayerNames());

		// Por seguridad, limitamos el tamano del mapa antes de generar sus casillas.
		if (data.getWidth() < 5 || data.getWidth() > 50 || data.getHeight() < 5 || data.getHeight() > 50) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El mapa debe medir entre 5 y 50 casillas por lado");
		}

		long seed = data.getSeed() == null ? ThreadLocalRandom.current().nextLong() : data.getSeed();
		GameState game = new GameState(seed);
		game.setCreatedBy(creator);
		game.setConfiguration(new GameConfiguration(names.size(), data.getWidth(), data.getHeight()));

		// Asignamos el orden 1, 2, 3 o 4 a cada jugador.
		for (int i = 0; i < names.size(); i++) {
			Player player = new Player(names.get(i), i + 1, 0);
			String factionCode = new String[] {"RED", "BLUE", "GREEN", "YELLOW"}[i];
			Faction faction = factions.findByCode(factionCode).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo de facciones no inicializado"));
			Commander commander = commanders.findByCode("CMD_" + factionCode).orElseThrow(() ->
				new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Catalogo de comandantes no inicializado"));
			player.assignSelection(faction, commander);
			game.addPlayer(player);
		}
		game.setGameMap(mapGenerationService.generateMap(data.getWidth(), data.getHeight(), seed));

		// Guardamos la partida, jugadores, mapa y casillas.
		GameState saved = gameRepository.save(game);

		// Luego guardamos los edificios en casillas ya persistidas.
		List<Property> properties = propertyGenerationService.generateProperties(saved);
		mapGenerationService.connectPropertiesToRoads(saved.getGameMap(), properties);
		propertyRepository.saveAll(properties);
		events.record(saved, null, GameLogType.GAME_CREATED, "Partida creada con " + names.size() + " jugadores");

		return new GameDTO(saved);
	}

	/** Devuelve las partidas del usuario actual, sin ver las de los demas. */
	@Transactional(readOnly = true)
	public List<GameDTO> getMyGames(User creator) {

		checkUser(creator);

		List<GameDTO> result = new ArrayList<>();

		for (GameState game : gameRepository.findAllByCreatedBy_IdOrderByCreatedAtDesc(creator.getId())) {

			result.add(new GameDTO(game));
		}

		return result;
	}

	/** Busca una partida por id y comprueba que pertenece al usuario actual. */
	@Transactional(readOnly = true)
	public GameDTO getMyGame(Long gameId, User creator) {

		checkUser(creator);

		Optional<GameState> foundGame = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId());
		if (foundGame.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		GameState game = foundGame.get();

		return new GameDTO(game);
	}

	/** Devuelve el mapa completo, solo si pertenece al usuario autenticado. */
	@Transactional(readOnly = true)
	public MapDTO getMyMap(Long gameId, User creator) {
		checkUser(creator);
		Optional<GameState> foundGame = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId());
		if (foundGame.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		GameState game = foundGame.get();
		return new MapDTO(game, propertyRepository.findAllByTile_GameMap_Id(game.getGameMap().getId()));
	}

	/** Actualiza una seleccion unicamente durante SETUP y para el dueno de la partida. */
	@Transactional
	public GameDTO selectPlayer(Long gameId, int turnOrder, PlayerSelectionRequestDTO request, User creator) {
		checkUser(creator);
		if (request == null || request.getFactionId() == null || request.getCommanderId() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seleccion de faccion y comandante incompleta");
		}
		GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
		if (game.getStatus() != GameStatus.SETUP) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo se pueden editar jugadores antes de iniciar");
		}
		Player player = game.findPlayerByOrder(turnOrder);
		if (player == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Jugador no encontrado");
		}
		Faction faction = factions.findById(request.getFactionId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faccion no valida"));
		Commander commander = commanders.findByIdAndFaction_Id(request.getCommanderId(), faction.getId())
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comandante incompatible"));
		for (Player other : game.getPlayers()) {
			if (other.getTurnOrder() != turnOrder && other.getFaction() != null
					&& faction.getId().equals(other.getFaction().getId())) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "La faccion ya pertenece a otro jugador");
			}
		}
		player.assignSelection(faction, commander);
		return new GameDTO(game);
	}

	/** Inicia una partida una sola vez y prepara sus unidades y dinero. */
	@Transactional
	public GameDTO startGame(Long gameId, User creator) {
		checkUser(creator);
		Optional<GameState> foundGame = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId());
		if (foundGame.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		GameState game = foundGame.get();

		if (game.getStatus() != GameStatus.SETUP) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida ya fue iniciada o finalizada");
		}
		if (game.getGameMap() == null || !game.getGameMap().isComplete() || game.getPlayers().size() < 2
				|| game.getPlayers().size() > 4) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El mapa o los jugadores no estan listos");
		}
		if (unitRepository.countByOwner_GameState_Id(gameId) != 0) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida ya tiene unidades iniciales");
		}

		List<Property> properties = propertyRepository.findAllByTile_GameMap_Id(game.getGameMap().getId());
		List<Unit> units = unitGenerationService.generateInitialUnits(game, properties);
		for (Player player : game.getPlayers()) {
			player.setMoney(STARTING_MONEY);
		}
		unitRepository.saveAll(units);
		game.startGame(1);
		events.record(game, game.findPlayerByOrder(1), GameLogType.GAME_STARTED, "Partida iniciada");
		return new GameDTO(game);
	}

	/**
	 * Finaliza el turno visto por el cliente y valida su versión bajo bloqueo.
	 * Aplica ingresos, acciones y el siguiente jugador en una sola transacción.
	 */
	@Transactional
	public GameDTO endTurn(Long gameId, EndTurnRequestDTO expected, User creator) {
		checkUser(creator);
		validateTurnRequest(expected);
		GameState game = findGameForTurn(gameId, expected, creator);
		Player previous = game.findPlayerByOrder(game.getCurrentPlayerOrder());
		if (previous == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida tiene un turno actual invalido");
		}
		endCommanderPower(previous);
		List<Player> active = findActivePlayers(game);
		if (active.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay jugadores activos en la partida");
		}
		Player first = firstActivePlayer(active);
		if (active.size() == 1) {
			game.finishGame(first.getTurnOrder());
			events.record(game, first, GameLogType.GAME_FINISHED,
					"Partida terminada. Ganador: " + first.getName());
			return new GameDTO(game);
		}
		Player next = nextActivePlayer(active, game.getCurrentPlayerOrder(), first);
		advanceToNextPlayer(game, previous, next);
		return new GameDTO(game);
	}

	/** Verifica que la solicitud aporta ronda y asiento válidos. */
	private void validateTurnRequest(EndTurnRequestDTO expected) {
		if (expected == null || expected.getCurrentRound() == null || expected.getCurrentPlayerOrder() == null
				|| expected.getCurrentRound() < 1 || expected.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"Debes indicar currentRound y currentPlayerOrder validos");
		}
	}

	/** Bloquea la partida y comprueba que el turno coincida con el esperado. */
	private GameState findGameForTurn(Long gameId, EndTurnRequestDTO expected, User creator) {
		GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Solo se pueden terminar turnos de partidas en curso");
		}
		if (game.getCurrentRound() != expected.getCurrentRound()
				|| game.getCurrentPlayerOrder() != expected.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"El turno ya cambio; consulta la partida antes de volver a intentar");
		}
		return game;
	}

	/** Finaliza los poderes de un turno, salvo la tormenta activa hasta la próxima ronda. */
	private void endCommanderPower(Player previous) {
		if (previous.getCommander() == null
				|| !"ENEMY_MOVE_MINUS_1".equals(previous.getCommander().getPowerEffectCode())) {
			previous.clearPowerActive();
		}
	}

	/** Enumera a los jugadores que no han sido eliminados. */
	private List<Player> findActivePlayers(GameState game) {
		List<Player> active = new ArrayList<>();
		for (Player player : game.getPlayers()) {
			if (player.isActive()) {
				active.add(player);
			}
		}
		return active;
	}

	/** Encuentra al jugador activo de menor orden. */
	private Player firstActivePlayer(List<Player> active) {
		Player first = null;
		for (Player player : active) {
			if (first == null || player.getTurnOrder() < first.getTurnOrder()) {
				first = player;
			}
		}
		return first;
	}

	/** Busca el siguiente jugador activo, regresando al primero tras el último asiento. */
	private Player nextActivePlayer(List<Player> active, int currentOrder, Player first) {
		Player next = null;
		for (Player player : active) {
			if (player.getTurnOrder() > currentOrder
					&& (next == null || player.getTurnOrder() < next.getTurnOrder())) {
				next = player;
			}
		}
		return next == null ? first : next;
	}

	/** Actualiza ronda, jugador activo, acciones, ingresos e historial. */
	private void advanceToNextPlayer(GameState game, Player previous, Player next) {
		events.record(game, previous, GameLogType.TURN_ENDED,
				"Termino el turno del jugador " + previous.getName());
		if (next.getTurnOrder() <= game.getCurrentPlayerOrder()) {
			game.incrementRound();
		}
		game.changeCurrentPlayer(next.getTurnOrder());
		next.clearPowerActive();
		events.record(game, next, GameLogType.TURN_STARTED, "Comenzo el turno del jugador " + next.getName());
		for (Unit unit : unitRepository.findAllByOwner_Id(next.getId())) {
			unit.resetTurnActions();
		}
		for (Property property : propertyRepository.findAllByOwner_Id(next.getId())) {
			if (property.isOperational() && property.getDefinition().getIncome() > 0) {
				next.addMoney(property.getDefinition().getIncome());
			}
		}
	}

	/** Consulta las unidades de una partida del usuario autenticado. */
	@Transactional(readOnly = true)
	public List<UnitDTO> getMyUnits(Long gameId, User creator) {
		checkUser(creator);
		if (gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId()).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}

		List<UnitDTO> result = new ArrayList<>();
		for (Unit unit : unitRepository.findAllByOwner_GameState_Id(gameId)) {
			result.add(new UnitDTO(unit));
		}
		return result;
	}

	/** Rechaza solicitudes que no tengan un usuario autenticado. */
	private void checkUser(User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
	}

	/** Revisa que haya entre 2 y 4 jugadores con nombres diferentes. */
	private List<String> validateNames(List<String> playerNames) {
		if (playerNames == null || playerNames.size() < 2 || playerNames.size() > 4) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se necesitan entre 2 y 4 jugadores");
		}

		List<String> names = new ArrayList<>();
		Set<String> used = new HashSet<>();

		for (String name : playerNames) {
			if (name == null || name.isBlank() || name.trim().length() > 40) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
						"Nombre de jugador invalido (maximo 40 caracteres)");
			}
			String clean = name.trim();
			if (!used.add(clean.toLowerCase(java.util.Locale.ROOT))) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los nombres deben ser diferentes");
			}
			names.add(clean);
		}
		return names;
	}
}
