package co.edu.unbosque.advancewars.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.AttackResultDTO;
import co.edu.unbosque.advancewars.dto.AttackUnitRequestDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Combate terrestre entre infanterias y tanques, con contraataque. */
@Service
public class UnitCombatService {

	private final GameStateRepository gameRepository;
	private final UnitRepository unitRepository;
	private final PropertyRepository propertyRepository;
	private final GameVictoryService victoryService;
	private final GameEventService events;

	public UnitCombatService(GameStateRepository gameRepository, UnitRepository unitRepository,
			PropertyRepository propertyRepository, GameVictoryService victoryService, GameEventService events) {
		this.gameRepository = gameRepository;
		this.unitRepository = unitRepository;
		this.propertyRepository = propertyRepository;
		this.victoryService = victoryService;
		this.events = events;
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

		Optional<Unit> foundAttacker = unitRepository.findById(unitId);
		if (foundAttacker.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Atacante no encontrado");
		}
		Unit attacker = foundAttacker.get();
		Optional<Unit> foundTarget = unitRepository.findById(request.getTargetUnitId());
		if (foundTarget.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Objetivo no encontrado");
		}
		Unit target = foundTarget.get();

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
		UnitType attackerType = attacker.getDefinition().getUnitType();
		UnitType targetType = target.getDefinition().getUnitType();
		if (!isGroundCombatUnit(attackerType) || !isGroundCombatUnit(targetType)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Por ahora solo combaten INFANTRY y TANK");
		}

		int distance = Math.abs(attacker.getPosition().getX() - target.getPosition().getX())
				+ Math.abs(attacker.getPosition().getY() - target.getPosition().getY());
		if (distance != 1) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El enemigo debe estar en una casilla adyacente");
		}

		// Regla sencilla: el ataque resta hasta 4 HP.
		int damage = Math.min(baseDamage(attackerType, targetType), target.getCurrentHealth());
		target.receiveDamage(damage);
		attacker.markAttacked();

		// Si el defensor sobrevive, responde automaticamente con hasta 2 HP de dano.
		int counterDamage = 0;
		if (!target.isDestroyed()) {
			counterDamage = Math.min(counterDamage(targetType, attackerType), attacker.getCurrentHealth());
			attacker.receiveDamage(counterDamage);
		}

		// Conservamos los dueños para evaluar derrota despues de borrar las tropas.
		Player attackerOwner = attacker.getOwner();
		Player targetOwner = target.getOwner();

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

		events.record(game, attackerOwner, GameLogType.UNIT_ATTACKED,
				"Unidad #" + attacker.getId() + " (" + attackerType + ") ataco a #" + target.getId() + " (" + targetType
						+ "): dano " + damage + ", contraataque " + counterDamage);

		if (target.isDestroyed() || attacker.isDestroyed()) {
			// Forzamos el DELETE antes de contar las tropas que quedan en MySQL.
			unitRepository.flush();
			if (target.isDestroyed()) {
				victoryService.checkArmyDefeat(game, targetOwner);
			}
			if (attacker.isDestroyed()) {
				victoryService.checkArmyDefeat(game, attackerOwner);
			}
		}

		return new AttackResultDTO(attacker.getId(), target.getId(), damage, counterDamage, attacker.getCurrentHealth(),
				target.getCurrentHealth());
	}

	private boolean isGroundCombatUnit(UnitType type) {
		return type == UnitType.INFANTRY || type == UnitType.TANK;
	}

	/** Tabla sencilla de dano. No modifica el contrato JSON de ataque. */
	private int baseDamage(UnitType attacker, UnitType defender) {
		if (attacker == UnitType.INFANTRY && defender == UnitType.INFANTRY)
			return 4;
		if (attacker == UnitType.INFANTRY && defender == UnitType.TANK)
			return 2;
		if (attacker == UnitType.TANK && defender == UnitType.INFANTRY)
			return 6;
		return 5; // TANK contra TANK
	}

	/** Un defensor que sobrevive devuelve menos dano que en un ataque normal. */
	private int counterDamage(UnitType defender, UnitType attacker) {
		if (defender == UnitType.INFANTRY && attacker == UnitType.INFANTRY)
			return 2;
		if (defender == UnitType.INFANTRY && attacker == UnitType.TANK)
			return 1;
		if (defender == UnitType.TANK && attacker == UnitType.INFANTRY)
			return 3;
		return 3; // TANK contra TANK
	}

	// Una tropa destruida no puede dejar una captura pendiente.
	private void cancelCapture(Unit unit) {
		Optional<Property> found = propertyRepository.findByTile_Id(unit.getTile().getId());
		if (found.isPresent()) {
			Property property = found.get();
			if (property.getCapturingPlayer() != null
					&& property.getCapturingPlayer().getId().equals(unit.getOwner().getId())) {
				property.resetCaptureProgress();
				propertyRepository.save(property);
			}
		}
	}

	private boolean belongsToGame(Unit unit, Long gameId) {
		return unit.getOwner() != null && unit.getOwner().getGameState() != null
				&& gameId.equals(unit.getOwner().getGameState().getId());
	}
}
