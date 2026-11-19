package co.edu.unbosque.advancewars.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.advancewars.dto.CreateGameDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository; // Ajusta el nombre si tu repositorio se llama distinto
import co.edu.unbosque.advancewars.service.GameService;

/** Rutas REST para crear partidas y consultar las propias. */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;
    private final UserRepository userRepository; // Temporal para pruebas

    public GameController(GameService gameService, UserRepository userRepository) {
        this.gameService = gameService;
        this.userRepository = userRepository;
    }

    /** Crea una partida para el usuario que inicio sesion. */
    @PostMapping
    public GameDTO create(@RequestBody CreateGameDTO data, @AuthenticationPrincipal User creator) {
        return gameService.createGame(data, resolveUser(creator));
    }

    /** Consulta las partidas creadas por el usuario autenticado. */
    @GetMapping
    public List<GameDTO> getMyGames(@AuthenticationPrincipal User creator) {
        return gameService.getMyGames(resolveUser(creator));
    }

    /** Devuelve el mapa con sus casillas y propiedades. */
    @GetMapping("/{id}/map")
    public MapDTO getMyMap(@PathVariable Long id, @AuthenticationPrincipal User creator) {
        return gameService.getMyMap(id, resolveUser(creator));
    }

    /** Consulta una partida especifica del usuario autenticado. */
    @GetMapping("/{id}")
    public GameDTO getMyGame(@PathVariable Long id, @AuthenticationPrincipal User creator) {
        return gameService.getMyGame(id, resolveUser(creator));
    }

    /** Temporal: devuelve un usuario de la BD si la peticion no trae JWT. */
    private User resolveUser(User creator) {
        if (creator != null) {
            return creator;
        }
        return userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Debe existir al menos un usuario en la tabla useraccount"));
    }
}