package co.edu.unbosque.advancewars.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.CaptureResultDTO;
import co.edu.unbosque.advancewars.dto.EndTurnRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
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

	public UnitCaptureService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
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
		GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
		}
		if (game.getCurrentRound() != request.getCurrentRound()
				|| game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
		}

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
		if (unit.getDefinition().getUnitType() != UnitType.INFANTRY) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo la infanteria puede capturar por ahora");
		}

		Property property = propertyRepository.findByTile_Id(unit.getTile().getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
						"No hay una propiedad en la casilla de esta unidad"));
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
		return new CaptureResultDTO(unit, property, completed);
	}
}
