package co.edu.unbosque.advancewars.dto;

/** Resultado del combate para mostrar en Angular y Phaser. */
public class AttackResultDTO {

	private final Long attackerUnitId;
	private final Long targetUnitId;
	private final int damageDealt;
	private final int counterattackDamage;
	private final int attackerHealth;
	private final int targetHealth;
	private final boolean attackerDestroyed;
	private final boolean targetDestroyed;

	public AttackResultDTO(Long attackerUnitId, Long targetUnitId, int damageDealt, int counterattackDamage,
			int attackerHealth, int targetHealth) {
		this.attackerUnitId = attackerUnitId;
		this.targetUnitId = targetUnitId;
		this.damageDealt = damageDealt;
		this.counterattackDamage = counterattackDamage;
		this.attackerHealth = attackerHealth;
		this.targetHealth = targetHealth;
		this.attackerDestroyed = attackerHealth == 0;
		this.targetDestroyed = targetHealth == 0;
	}

	public Long getAttackerUnitId() {
		return attackerUnitId;
	}

	public Long getTargetUnitId() {
		return targetUnitId;
	}

	public int getDamageDealt() {
		return damageDealt;
	}

	public int getCounterattackDamage() {
		return counterattackDamage;
	}

	public int getAttackerHealth() {
		return attackerHealth;
	}

	public int getTargetHealth() {
		return targetHealth;
	}

	public boolean isAttackerDestroyed() {
		return attackerDestroyed;
	}

	public boolean isTargetDestroyed() {
		return targetDestroyed;
	}
}
