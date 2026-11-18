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
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.service.GameService;

/** Rutas REST para crear partidas y consultar las propias. */
@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    /** Crea una partida para el usuario que inicio sesion. */
    @PostMapping
    public GameDTO create(@RequestBody CreateGameDTO data, @AuthenticationPrincipal User creator) {
        return gameService.createGame(data, creator);
    }

    /** Consulta las partidas creadas por el usuario autenticado. */
    @GetMapping
    public List<GameDTO> getMyGames(@AuthenticationPrincipal User creator) {
        return gameService.getMyGames(creator);
    }

    /** Consulta una partida especifica del usuario autenticado. */
    @GetMapping("/{id}")
    public GameDTO getMyGame(@PathVariable Long id, @AuthenticationPrincipal User creator) {
        return gameService.getMyGame(id, creator);
    }
}
