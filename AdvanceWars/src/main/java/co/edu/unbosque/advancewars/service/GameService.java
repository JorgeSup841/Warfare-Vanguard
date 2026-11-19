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
import co.edu.unbosque.advancewars.model.GameConfiguration;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;

/** Crea partidas y permite consultar unicamente las del usuario autenticado. */
@Service
public class GameService {

    private final GameStateRepository gameRepository;
    private final MapGenerationService mapGenerationService;

    public GameService(GameStateRepository gameRepository, MapGenerationService mapGenerationService) {
        this.gameRepository = gameRepository;
        this.mapGenerationService = mapGenerationService;
    }

    /** Crea una partida en configuracion, con jugadores y sin mapa por ahora. */
    public GameDTO createGame(CreateGameDTO data, User creator) {
        checkUser(creator);

        if (data == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos de la partida");
        }

        List<String> names = validateNames(data.getPlayerNames());

        // Por seguridad, limitamos el tamano del mapa antes de generar sus casillas.
        if (data.getWidth() < 5 || data.getWidth() > 50
                || data.getHeight() < 5 || data.getHeight() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mapa debe medir entre 5 y 50 casillas por lado");
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

        // JPA guarda los jugadores porque GameState ya tiene CascadeType.ALL.
        return new GameDTO(gameRepository.save(game));
    }

    /** Devuelve las partidas del usuario actual, sin ver las de los demas. */
    public List<GameDTO> getMyGames(User creator) {
        checkUser(creator);
        List<GameDTO> result = new ArrayList<>();

        for (GameState game : gameRepository.findAllByCreatedBy_IdOrderByCreatedAtDesc(creator.getId())) {
            result.add(new GameDTO(game));
        }
        return result;
    }

    /** Busca una partida por id y comprueba que pertenece al usuario actual. */
    public GameDTO getMyGame(Long gameId, User creator) {
        checkUser(creator);
        GameState game = gameRepository.findByIdAndCreatedBy_Id(gameId, creator.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        return new GameDTO(game);
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de jugador invalido (maximo 40 caracteres)");
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
