package co.edu.unbosque.advancewars.controller;

import java.security.Principal;
import java.util.List;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.unbosque.advancewars.dto.AttackResultDTO;
import co.edu.unbosque.advancewars.dto.AttackUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
import co.edu.unbosque.advancewars.dto.CreateGameDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.dto.GameDTO;
import co.edu.unbosque.advancewars.dto.PlayerSelectionRequestDTO;
import co.edu.unbosque.advancewars.dto.GameLogDTO;
import co.edu.unbosque.advancewars.service.GameHistoryService;
import co.edu.unbosque.advancewars.service.CommanderPowerService;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.MoveUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.ProduceUnitRequestDTO;
import co.edu.unbosque.advancewars.dto.ReachableTileDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.service.AuthenticatedUserService;
import co.edu.unbosque.advancewars.service.GameService;
import co.edu.unbosque.advancewars.service.UnitCaptureService;
import co.edu.unbosque.advancewars.service.UnitCombatService;
import co.edu.unbosque.advancewars.service.UnitMovementService;
import co.edu.unbosque.advancewars.service.UnitProductionService;

/** Rutas REST para crear partidas y consultar las propias. */
@RestController
@RequestMapping("/api/games")
public class GameController {

	private final GameService gameService;
	private final UnitMovementService unitMovementService;
	private final UnitCombatService unitCombatService;
	private final UnitCaptureService unitCaptureService;
	private final UnitProductionService unitProductionService;
	private final GameHistoryService historyService;
	private final CommanderPowerService commanderPowerService;
	private final AuthenticatedUserService authenticatedUsers;

	/** Inyecta servicios del motor y el resolvedor de la cuenta autenticada. */
	public GameController(GameService gameService, UnitMovementService unitMovementService,
			UnitCombatService unitCombatService, UnitCaptureService unitCaptureService,
			UnitProductionService unitProductionService, GameHistoryService historyService,
			CommanderPowerService commanderPowerService, AuthenticatedUserService authenticatedUsers) {
		this.gameService = gameService;
		this.unitMovementService = unitMovementService;
		this.unitCombatService = unitCombatService;
		this.unitCaptureService = unitCaptureService;
		this.unitProductionService = unitProductionService;
		this.historyService = historyService;
		this.commanderPowerService = commanderPowerService;
		this.authenticatedUsers = authenticatedUsers;
	}

	/** Crea una partida para el usuario que inicio sesion. */
	@PostMapping
	public GameDTO create(@Valid @RequestBody CreateGameDTO data, Principal principal) {
		return gameService.createGame(data, authenticatedUsers.requireUser(principal));
	}

	/** Consulta las partidas creadas por el usuario autenticado. */
	@GetMapping
	public List<GameDTO> getMyGames(Principal principal) {
		return gameService.getMyGames(authenticatedUsers.requireUser(principal));
	}

	/** Devuelve el mapa con sus casillas y propiedades. */
	@GetMapping("/{id}/map")
	public MapDTO getMyMap(@PathVariable("id") Long id, Principal principal) {
		return gameService.getMyMap(id, authenticatedUsers.requireUser(principal));
	}

	/** Consulta una partida especifica del usuario autenticado. */
	@GetMapping("/{id}")
	public GameDTO getMyGame(@PathVariable("id") Long id, Principal principal) {
		return gameService.getMyGame(id, authenticatedUsers.requireUser(principal));
	}

	/** Actualiza la faccion y comandante de un jugador local durante SETUP. */
	@PutMapping("/{id}/players/{order}/selection")
	public GameDTO selectPlayer(@PathVariable("id") Long id, @PathVariable("order") int order,
			@Valid @RequestBody PlayerSelectionRequestDTO selection, Principal principal) {
		return gameService.selectPlayer(id, order, selection, authenticatedUsers.requireUser(principal));
	}

	/** Devuelve el historial autorizado de acciones de la partida. */
	@GetMapping("/{id}/history")
	public List<GameLogDTO> history(@PathVariable("id") Long id, Principal principal) {
		return historyService.getHistory(id, authenticatedUsers.requireUser(principal));
	}

	/** Solicita la activación del poder del comandante del jugador activo. */
	@PostMapping("/{id}/power")
	public GameDTO usePower(@PathVariable("id") Long id, @Valid @RequestBody EndTurnRequestDTO request,
			Principal principal) {
		return commanderPowerService.activate(id, request, authenticatedUsers.requireUser(principal));
	}

	/** Comienza la partida y crea los soldados iniciales. */
	@PostMapping("/{id}/start")
	public GameDTO startGame(@PathVariable("id") Long id, Principal principal) {
		return gameService.startGame(id, authenticatedUsers.requireUser(principal));
	}

	/** Cambia al siguiente jugador y, cuando corresponde, inicia otra ronda. */
	@PostMapping("/{id}/end-turn")
	public GameDTO endTurn(@PathVariable("id") Long id, @Valid @RequestBody EndTurnRequestDTO expected,
			Principal principal) {
		return gameService.endTurn(id, expected, authenticatedUsers.requireUser(principal));
	}

	/** Lista las unidades que hay sobre el tablero. */
	@GetMapping("/{id}/units")
	public List<UnitDTO> getMyUnits(@PathVariable("id") Long id, Principal principal) {
		return gameService.getMyUnits(id, authenticatedUsers.requireUser(principal));
	}

	/** Vista previa autorizada y de solo lectura de las casillas alcanzables. */
	@GetMapping("/{id}/units/{unitId}/reachable")
	public List<ReachableTileDTO> reachableTiles(@PathVariable("id") Long id, @PathVariable("unitId") Long unitId,
			Principal principal) {
		return unitMovementService.reachableTiles(id, unitId, authenticatedUsers.requireUser(principal));
	}

	/** Mueve una tropa una sola vez por turno a una casilla alcanzable. */
	@PostMapping("/{id}/units/{unitId}/move")
	public UnitDTO moveUnit(@PathVariable("id") Long id, @PathVariable("unitId") Long unitId,
			@Valid @RequestBody MoveUnitRequestDTO request, Principal principal) {
		return unitMovementService.move(id, unitId, request, authenticatedUsers.requireUser(principal));
	}

	/** Solicita un ataque validado por armamento, alcance, munición y turno. */
	@PostMapping("/{id}/units/{unitId}/attack")
	public AttackResultDTO attackUnit(@PathVariable("id") Long id, @PathVariable("unitId") Long unitId,
			@Valid @RequestBody AttackUnitRequestDTO request, Principal principal) {
		return unitCombatService.attack(id, unitId, request, authenticatedUsers.requireUser(principal));
	}

	/** Solicita una captura con cualquier unidad habilitada para capturar. */
	@PostMapping("/{id}/units/{unitId}/capture")
	public CaptureResultDTO captureProperty(@PathVariable("id") Long id, @PathVariable("unitId") Long unitId,
			@Valid @RequestBody EndTurnRequestDTO request, Principal principal) {
		return unitCaptureService.capture(id, unitId, request, authenticatedUsers.requireUser(principal));
	}

	/** Compra una unidad en una fabrica propia que este desocupada. */
	@PostMapping("/{id}/units/produce")
	public UnitDTO produceUnit(@PathVariable("id") Long id, @Valid @RequestBody ProduceUnitRequestDTO request,
			Principal principal) {
		return unitProductionService.produce(id, request, authenticatedUsers.requireUser(principal));
	}

}
