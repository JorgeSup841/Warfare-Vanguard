package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Unit;

/** Resultado de la accion de captura. */
public class CaptureResultDTO {
	private final Long unitId;
	private final Long propertyId;
	private final String propertyType;
	private final int x;
	private final int y;
	private final int captureProgress;
	private final int requiredProgress;
	private final Integer ownerOrder;
	private final Integer capturingPlayerOrder;
	private final boolean captured;

	public CaptureResultDTO(Unit unit, Property property, boolean captured) {
		this.unitId = unit.getId();
		this.propertyId = property.getId();
		this.propertyType = property.getDefinition().getType().name();
		this.x = property.getTile().getPosition().getX();
		this.y = property.getTile().getPosition().getY();
		this.captureProgress = property.getCaptureProgress();
		this.requiredProgress = property.getDefinition().getCaptureThreshold();
		this.ownerOrder = property.getOwner() == null ? null : property.getOwner().getTurnOrder();
		this.capturingPlayerOrder = property.getCapturingPlayer() == null ? null
				: property.getCapturingPlayer().getTurnOrder();
		this.captured = captured;
	}

	public Long getUnitId() {
		return unitId;
	}

	public Long getPropertyId() {
		return propertyId;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getCaptureProgress() {
		return captureProgress;
	}

	public int getRequiredProgress() {
		return requiredProgress;
	}

	public Integer getOwnerOrder() {
		return ownerOrder;
	}

	public Integer getCapturingPlayerOrder() {
		return capturingPlayerOrder;
	}

	public boolean isCaptured() {
		return captured;
	}
}
