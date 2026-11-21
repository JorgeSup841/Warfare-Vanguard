package co.edu.unbosque.advancewars.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/**
 * Captura propiedades usando infanterias; cada accion aporta tantos puntos como
 * vida.
 */
@Service
public class UnitCaptureService {

	private final GameStateRepository gameRepository;
	private final UnitRepository unitRepository;
	private final PropertyRepository propertyRepository;
	private final GameVictoryService victoryService;
	private final GameEventService events;

	public UnitCaptureService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository, GameVictoryService victoryService, GameEventService events) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
		this.victoryService = victoryService;
		this.events = events;
	}

	@Transactional
	public CaptureResultDTO capture(Long gameId, Long unitId, EndTurnRequestDTO request, User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		if (request == null || request.getCurrentRound() == null || request.getCurrentPlayerOrder() == null
				|| request.getCurrentRound() < 1 || request.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del turno incompletos");
		}

		// Bloquea la partida para que turnos, movimientos y capturas no se crucen.
		Optional<GameState> foundGame = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId());
		if (foundGame.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		GameState game = foundGame.get();

		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
		}
		if (game.getCurrentRound() != request.getCurrentRound()
				|| game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
		}

		Optional<Unit> foundUnit = unitRepository.findById(unitId);
		if (foundUnit.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada");
		}
		Unit unit = foundUnit.get();
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

		Optional<Property> foundProperty = propertyRepository.findByTile_Id(unit.getTile().getId());
		if (foundProperty.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No hay una propiedad en la casilla de esta unidad");
		}
		Property property = foundProperty.get();
		if (!property.isOperational()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La propiedad no esta operativa");
		}
		if (property.getOwner() != null && property.getOwner().getId().equals(player.getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta propiedad ya es tuya");
		}

		// Si otro jugador habia comenzado la captura, su progreso no se conserva.
		if (property.getCapturingPlayer() != null && !property.getCapturingPlayer().getId().equals(player.getId())) {
			property.resetCaptureProgress();
		}
		int progress = property.getCaptureProgress() + Math.min(10, unit.getCurrentHealth());
		property.updateCaptureState(player, progress);
		unit.markCaptured();

		boolean completed = property.isCaptureComplete();
		if (completed) {
			property.completeCapture(player); // Cambia el dueno y reinicia el contador.
		}
		propertyRepository.save(property);
		unitRepository.save(unit);
		if (completed) {
			events.record(game, player, GameLogType.PROPERTY_CAPTURED,
					"Propiedad #" + property.getId() + " (" + property.getDefinition().getType() + ") capturada");
		} else {
			events.record(game, player, GameLogType.UNIT_CAPTURE_STARTED,
					"Unidad #" + unit.getId() + " captura propiedad #" + property.getId()
					+ ": " + progress + " puntos");
		}
		if (completed && property.getDefinition().getType() == PropertyType.HEADQUARTERS) {
			victoryService.checkHeadquartersCapture(game, property, player);
		}
		return new CaptureResultDTO(unit, property, completed);
	}
}
