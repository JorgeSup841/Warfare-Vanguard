package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Unit;

/** Informacion de una unidad que puede mostrar Angular. */
public class UnitDTO {

	private Long id;
	private String unitType;
	private int ownerOrder;
	private int x;
	private int y;
	private int health;
	private int fuel;
	private boolean movedThisTurn;
	private boolean attackedThisTurn;
	private boolean capturedThisTurn;

	public UnitDTO(Unit unit) {
		id = unit.getId();
		unitType = unit.getDefinition().getUnitType().name();
		ownerOrder = unit.getOwner().getTurnOrder();
		x = unit.getPosition().getX();
		y = unit.getPosition().getY();
		health = unit.getCurrentHealth();
		fuel = unit.getCurrentFuel();
		movedThisTurn = unit.isMovedThisTurn();
		attackedThisTurn = unit.isAttackedThisTurn();
		capturedThisTurn = unit.isCapturedThisTurn();
	}

	public Long getId() {
		return id;
	}

	public String getUnitType() {
		return unitType;
	}

	public int getOwnerOrder() {
		return ownerOrder;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getHealth() {
		return health;
	}

	public int getFuel() {
		return fuel;
	}

	public boolean isMovedThisTurn() {
		return movedThisTurn;
	}

	public boolean isAttackedThisTurn() {
		return attackedThisTurn;
	}

	public boolean isCapturedThisTurn() {
		return capturedThisTurn;
	}
}
