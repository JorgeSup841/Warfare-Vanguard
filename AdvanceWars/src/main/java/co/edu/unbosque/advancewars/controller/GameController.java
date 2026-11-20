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
import co.edu.unbosque.advancewars.dto.AttackUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.AttackResultDTO;
import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.MoveUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository; // Ajusta el nombre si tu repositorio se llama distinto
import co.edu.unbosque.advancewars.service.GameService;
import co.edu.unbosque.advancewars.service.UnitCombatService;
import co.edu.unbosque.advancewars.service.UnitCaptureService;
import co.edu.unbosque.advancewars.service.UnitMovementService;

/** Rutas REST para crear partidas y consultar las propias. */
@RestController
@RequestMapping("/api/games")
public class GameController {

	private final GameService gameService;
	private final UnitMovementService unitMovementService;
	private final UnitCombatService unitCombatService;
	private final UnitCaptureService unitCaptureService;
	private final UserRepository userRepository; // Temporal para pruebas

	public GameController(GameService gameService, UserRepository userRepository,
			UnitMovementService unitMovementService, UnitCombatService unitCombatService,
			UnitCaptureService unitCaptureService) {
		this.gameService = gameService;
		this.unitMovementService = unitMovementService;
		this.unitCombatService = unitCombatService;
		this.unitCaptureService = unitCaptureService;
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

	/** Comienza la partida y crea los soldados iniciales. */
	@PostMapping("/{id}/start")
	public GameDTO startGame(@PathVariable Long id, @AuthenticationPrincipal User creator) {
		return gameService.startGame(id, resolveUser(creator));
	}

	/** Cambia al siguiente jugador y, cuando corresponde, inicia otra ronda. */
	@PostMapping("/{id}/end-turn")
	public GameDTO endTurn(@PathVariable Long id, @RequestBody EndTurnRequestDTO expected,
			@AuthenticationPrincipal User creator) {
		return gameService.endTurn(id, expected, resolveUser(creator));
	}

	/** Lista las unidades que hay sobre el tablero. */
	@GetMapping("/{id}/units")
	public List<UnitDTO> getMyUnits(@PathVariable Long id, @AuthenticationPrincipal User creator) {
		return gameService.getMyUnits(id, resolveUser(creator));
	}

	/** Mueve una tropa una sola vez por turno a una casilla alcanzable. */
	@PostMapping("/{id}/units/{unitId}/move")
	public UnitDTO moveUnit(@PathVariable Long id, @PathVariable Long unitId, @RequestBody MoveUnitRequestDTO request,
			@AuthenticationPrincipal User creator) {
		return unitMovementService.move(id, unitId, request, resolveUser(creator));
	}

	/** Una infanteria ataca a otra infanteria enemiga adyacente. */
	@PostMapping("/{id}/units/{unitId}/attack")
	public AttackResultDTO attackUnit(@PathVariable Long id, @PathVariable Long unitId,
			@RequestBody AttackUnitRequestDTO request, @AuthenticationPrincipal User creator) {
		return unitCombatService.attack(id, unitId, request, resolveUser(creator));
	}

	/** Captura la propiedad donde se encuentra una infanteria. */
	@PostMapping("/{id}/units/{unitId}/capture")
	public CaptureResultDTO captureProperty(@PathVariable Long id, @PathVariable Long unitId,
			@RequestBody EndTurnRequestDTO request, @AuthenticationPrincipal User creator) {
		return unitCaptureService.capture(id, unitId, request, resolveUser(creator));
	}

	/** Temporal: devuelve un usuario de la BD si la peticion no trae JWT. */
	private User resolveUser(User creator) {
		if (creator != null) {
			return creator;
		}
		return userRepository.findAll().stream().findFirst().orElseThrow(
				() -> new IllegalStateException("Debe existir al menos un usuario en la tabla useraccount"));
	}
}