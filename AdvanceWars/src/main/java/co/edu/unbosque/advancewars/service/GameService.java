package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.CreateGameDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameConfiguration;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
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

	private static final int STARTING_MONEY = 5000;

	public GameService(GameStateRepository gameRepository, MapGenerationService mapGenerationService,
			PropertyGenerationService propertyGenerationService, PropertyRepository propertyRepository,
			UnitGenerationService unitGenerationService, UnitRepository unitRepository) {
		this.gameRepository = gameRepository;
		this.mapGenerationService = mapGenerationService;
		this.propertyGenerationService = propertyGenerationService;
		this.propertyRepository = propertyRepository;
		this.unitGenerationService = unitGenerationService;
		this.unitRepository = unitRepository;
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
			game.addPlayer(new Player(names.get(i), i + 1, 0));
		}
		game.setGameMap(mapGenerationService.generateMap(data.getWidth(), data.getHeight(), seed));

		// Guardamos la partida, jugadores, mapa y casillas.
		GameState saved = gameRepository.save(game);

		// Luego guardamos los edificios en casillas ya persistidas.
		propertyRepository.saveAll(propertyGenerationService.generateProperties(saved));

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

		GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

		return new GameDTO(game);
	}

	/** Devuelve el mapa completo, solo si pertenece al usuario autenticado. */
	@Transactional(readOnly = true)
	public MapDTO getMyMap(Long gameId, User creator) {
		checkUser(creator);
		GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
		return new MapDTO(game, propertyRepository.findAllByTile_GameMap_Id(game.getGameMap().getId()));
	}

	/** Inicia una partida una sola vez y prepara sus unidades y dinero. */
	@Transactional
	public GameDTO startGame(Long gameId, User creator) {
		checkUser(creator);
		GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

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
		return new GameDTO(game);
	}

	/** Consulta las unidades de una partida del usuario autenticado. */
	@Transactional(readOnly = true)
	public List<UnitDTO> getMyUnits(Long gameId, User creator) {
		checkUser(creator);
		gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

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