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
import co.edu.unbosque.advancewars.model.Weapon;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.PropertyRepository;
import co.edu.unbosque.advancewars.repository.UnitRepository;

/** Combate autoritativo con catálogo de armas, alcances, munición y defensa. */
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
		int distance = Math.abs(attacker.getPosition().getX() - target.getPosition().getX())
				+ Math.abs(attacker.getPosition().getY() - target.getPosition().getY());
		Weapon selected = selectWeapon(attacker, targetType, distance);
		if (selected == null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Sin arma, alcance o municion para este objetivo");
		}
		if (attacker.isMovedThisTurn() && selected.getMinRange() > 1) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "La artilleria indirecta no ataca despues de moverse");
		}
		int damage = effectiveDamage(attacker, target, selected);
		target.receiveDamage(damage);
		if (selected.isUsesAmmunition()) attacker.consumeAmmunition(selected.getCode(), 1);
		attacker.markAttacked();
		attacker.getOwner().chargePower(1);
		int counterDamage = 0;
		if (!target.isDestroyed() && distance == 1) {
			Weapon response = selectWeapon(target, attackerType, distance);
			if (response != null && response.getMinRange() == 1) {
				counterDamage = effectiveDamage(target, attacker, response);
				attacker.receiveDamage(counterDamage);
				if (response.isUsesAmmunition()) target.consumeAmmunition(response.getCode(), 1);
			}
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

	private Weapon selectWeapon(Unit attacker, UnitType targetType, int distance) {
		Weapon best = null;
		for (Weapon weapon : attacker.getDefinition().getWeapons()) {
			int range = weapon.getMaxRange();
			if (attacker.getOwner().getCommander() != null && attacker.getDefinition().getUnitType() == UnitType.ARTILLERY
					&& "ARTILLERY_RANGE_1".equals(attacker.getOwner().getCommander().getPassiveEffectCode())) range++;
			if (distance < weapon.getMinRange() || distance > range || weapon.getDamageAgainst(targetType) <= 0) continue;
			if (weapon.isUsesAmmunition() && attacker.getAmmunition().getOrDefault(weapon.getCode(), 0) <= 0) continue;
			if (best == null || weapon.getDamageAgainst(targetType) > best.getDamageAgainst(targetType)) best = weapon;
		}
		return best;
	}

	private int effectiveDamage(Unit attacker, Unit defender, Weapon weapon) {
		int base = weapon.getDamageAgainst(defender.getDefinition().getUnitType());
		if (base <= 0) return 0;
		double attack = base * attacker.getCurrentHealth() / 100.0;
		Player owner = attacker.getOwner();
		if (owner.getCommander() != null) {
			String passive = owner.getCommander().getPassiveEffectCode();
			if ("INFANTRY_ATTACK_10".equals(passive) && attacker.getDefinition().getUnitType() == UnitType.INFANTRY) attack *= 1.10;
			if ("AIR_ATTACK_15".equals(passive) && attacker.getDefinition().getCategory() == co.edu.unbosque.advancewars.enums.UnitCategory.AIR) attack *= 1.15;
			if (owner.isPowerActive() && "RANGED_ATTACK_20".equals(owner.getCommander().getPowerEffectCode())
					&& weapon.getMinRange() > 1) attack *= 1.20;
		}
		int cover = switch (defender.getTile().getTerrainType()) {
			case FOREST -> 2;
			case MOUNTAIN -> 4;
			default -> 0;
		};
		Property property = propertyRepository.findByTile_Id(defender.getTile().getId()).orElse(null);
		if (property != null) cover = Math.max(cover, property.getDefinition().getDefenseBonus());
		if (defender.getDefinition().getCategory() == co.edu.unbosque.advancewars.enums.UnitCategory.AIR) cover = 0;
		if (defender.getOwner().getCommander() != null
				&& "DEFENSE_10".equals(defender.getOwner().getCommander().getPassiveEffectCode())) attack *= 0.90;
		return Math.min(defender.getCurrentHealth(), Math.max(1, (int) Math.round(attack * Math.max(0.5, 1 - 0.08 * cover))));
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
