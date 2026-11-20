package co.edu.unbosque.advancewars.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.AttackResultDTO;
import co.edu.unbosque.advancewars.dto.AttackUnitRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Combate basico: infanteria vs infanteria, con contraataque. */
@Service
public class UnitCombatService {

	private final GameStateRepository gameRepository;
	private final UnitRepository unitRepository;
	private final PropertyRepository propertyRepository;

	public UnitCombatService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
	}

	@Transactional
	public AttackResultDTO attack(Long gameId, Long unitId, AttackUnitRequestDTO request, User creator) {
		if (creator == null || creator.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		if (request == null || request.getTargetUnitId() == null || request.getCurrentRound() == null
				|| request.getCurrentPlayerOrder() == null || request.getCurrentRound() < 1
				|| request.getCurrentPlayerOrder() < 1) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos del ataque incompletos");
		}
		if (unitId.equals(request.getTargetUnitId())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una unidad no puede atacarse a si misma");
		}

		// El mismo bloqueo usado por los turnos y movimientos evita ataques
		// simultaneos.
		GameState game = gameRepository.findWithLockByIdAndCreatedBy_Id(gameId, creator.getId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));

		if (game.getStatus() != GameStatus.IN_PROGRESS) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La partida no esta en curso");
		}
		if (game.getCurrentRound() != request.getCurrentRound()
				|| game.getCurrentPlayerOrder() != request.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El turno cambio; consulta la partida");
		}

		Unit attacker = unitRepository.findById(unitId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Atacante no encontrado"));
		Unit target = unitRepository.findById(request.getTargetUnitId())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo no encontrado"));

		// Ambos deben pertenecer a esta partida, nunca a otra partida del sistema.
		if (!belongsToGame(attacker, gameId) || !belongsToGame(target, gameId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada en esta partida");
		}
		if (!attacker.getOwner().isActive() || attacker.getOwner().getTurnOrder() != game.getCurrentPlayerOrder()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "No es el turno de esta unidad");
		}
		if (!target.getOwner().isActive() || attacker.getOwner().getId().equals(target.getOwner().getId())) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Solo puedes atacar unidades enemigas activas");
		}
		if (attacker.isDestroyed() || target.isDestroyed()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La unidad esta destruida");
		}
		if (attacker.isAttackedThisTurn() || attacker.isCapturedThisTurn()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"La unidad ya realizo una accion de ataque o captura");
		}
		// Version inicial: solo existe infanteria en las partidas creadas.
		if (attacker.getDefinition().getUnitType() != UnitType.INFANTRY
				|| target.getDefinition().getUnitType() != UnitType.INFANTRY) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
					"Por ahora solo se permite combate entre infanterias");
		}

		int distance = Math.abs(attacker.getPosition().getX() - target.getPosition().getX())
				+ Math.abs(attacker.getPosition().getY() - target.getPosition().getY());
		if (distance != 1) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El enemigo debe estar en una casilla adyacente");
		}

		// Regla sencilla: el ataque resta hasta 4 HP.
		int damage = Math.min(4, target.getCurrentHealth());
		target.receiveDamage(damage);
		attacker.markAttacked();

		// Si el defensor sobrevive, responde automaticamente con hasta 2 HP de dano.
		int counterDamage = 0;
		if (!target.isDestroyed()) {
			counterDamage = Math.min(2, attacker.getCurrentHealth());
			attacker.receiveDamage(counterDamage);
		}

		// La transaccion guarda la vida y libera la casilla de las tropas destruidas.
		if (target.isDestroyed()) {
			cancelCapture(target);
			unitRepository.delete(target);
		} else {
			unitRepository.save(target);
		}
		if (attacker.isDestroyed()) {
			cancelCapture(attacker);
			unitRepository.delete(attacker);
		} else {
			unitRepository.save(attacker);
		}

		return new AttackResultDTO(attacker.getId(), target.getId(), damage, counterDamage, attacker.getCurrentHealth(),
				target.getCurrentHealth());
	}

	// Una tropa destruida no puede dejar una captura pendiente.
	private void cancelCapture(Unit unit) {
		propertyRepository.findByTile_Id(unit.getTile().getId()).ifPresent(property -> {
			if (property.getCapturingPlayer() != null
					&& property.getCapturingPlayer().getId().equals(unit.getOwner().getId())) {
				property.resetCaptureProgress();
				propertyRepository.save(property);
			}
		});
	}

	private boolean belongsToGame(Unit unit, Long gameId) {
		return unit.getOwner() != null && unit.getOwner().getGameState() != null
				&& gameId.equals(unit.getOwner().getGameState().getId());
	}
}
