package co.edu.unbosque.advancewars.service;


import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/**
 * Captura propiedades con unidades habilitadas; cada acción aporta puntos según
 * la vida de la tropa.
 */
@Service
public class UnitCaptureService {

	private final GameStateRepository gameRepository;
	private final UnitRepository unitRepository;
	private final PropertyRepository propertyRepository;
	private final GameVictoryService victoryService;
	private final GameEventService events;

	/** Inyecta las dependencias del servicio mediante constructor. */
    public UnitCaptureService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository, GameVictoryService victoryService, GameEventService events) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
		this.victoryService = victoryService;
		this.events = events;
	}

	/** Valida y aplica un avance de captura en una transacción. */
	@Transactional
	public CaptureResultDTO capture(Long gameId, Long unitId, EndTurnRequestDTO request, User creator) {
		validateRequest(request, creator);
		GameState game = findCurrentGame(gameId, request, creator);
		Unit unit = findCapturingUnit(game, gameId, unitId);
		Player player = unit.getOwner();
		Property property = findCapturableProperty(unit, player);

		if (property.getCapturingPlayer() != null
				&& !property.getCapturingPlayer().getId().equals(player.getId())) {
			property.resetCaptureProgress();
		}
		int progress = property.getCaptureProgress() + Math.min(10, unit.getCurrentHealth());
		property.updateCaptureState(player, progress);
		unit.markCaptured();

		boolean completed = property.isCaptureComplete();
		if (completed) {
			property.completeCapture(player);
		}
		propertyRepository.save(property);
		unitRepository.save(unit);
		recordCapture(game, player, unit, property, completed, progress);
		if (completed && property.getDefinition().getType() == PropertyType.HEADQUARTERS) {
			victoryService.checkHeadquartersCapture(game, property, player);
		}
		return new CaptureResultDTO(unit, property, completed);
	}

	/** Valida identidad y datos del turno antes de consultar la partida. */
	private void validateRequest(EndTurnRequestDTO request, User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		if (request == null || request.getCurrentRound() == null || request.getCurrentPlayerOrder() == null
				|| request.getCurrentRound() < 1 || request.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del turno incompletos");
		}
	}

	/** Bloquea la partida y evita cruces de turno y capturas simultáneas. */
	private GameState findCurrentGame(Long gameId, EndTurnRequestDTO request, User creator) {
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

	/** Comprueba partida, turno y capacidad de captura de la unidad. */
	private Unit findCapturingUnit(GameState game, Long gameId, Long unitId) {
		Unit unit = unitRepository.findById(unitId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
		Player player = unit.getOwner();
		if (player == null || player.getGameState() == null || !gameId.equals(player.getGameState().getId())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada en esta partida");
		}
		if (!player.isActive() || player.getTurnOrder() != game.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No es el turno de esta unidad");
		}
		if (unit.isDestroyed() || unit.isAttackedThisTurn() || unit.isCapturedThisTurn()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad ya ataco o capturo en este turno");
		}
		if (!unit.getDefinition().isCaptureCapable()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Este tipo de unidad no puede capturar");
		}
		return unit;
	}

	/** Impide capturar una propiedad propia, inexistente o inoperativa. */
	private Property findCapturableProperty(Unit unit, Player player) {
		Property property = propertyRepository.findByTile_Id(unit.getTile().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
						"No hay una propiedad en la casilla de esta unidad"));
		if (!property.isOperational()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La propiedad no esta operativa");
		}
		if (property.getOwner() != null && property.getOwner().getId().equals(player.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta propiedad ya es tuya");
		}
		return property;
	}

	/** Registra el avance o la captura definitiva con el mismo evento del motor. */
	private void recordCapture(GameState game, Player player, Unit unit, Property property,
			boolean completed, int progress) {
		if (completed) {
			events.record(game, player, GameLogType.PROPERTY_CAPTURED,
					"Propiedad #" + property.getId() + " (" + property.getDefinition().getType() + ") capturada");
		} else {
			events.record(game, player, GameLogType.UNIT_CAPTURE_STARTED,
					"Unidad #" + unit.getId() + " captura propiedad #" + property.getId()
							+ ": " + progress + " puntos");
		}
	}
}
