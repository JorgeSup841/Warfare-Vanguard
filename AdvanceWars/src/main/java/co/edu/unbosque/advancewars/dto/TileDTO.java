package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Tile;

// Datos de una casilla para mostrar el mapa en Angular o Swagger.
public class TileDTO {

	private int x;
	private int y;
	private String terrain;
	private String propertyType;
	private Integer ownerOrder;
	private int captureProgress;
	private Integer capturingPlayerOrder;

	public TileDTO(Tile tile, Property property) {
		this.x = tile.getPosition().getX();
		this.y = tile.getPosition().getY();
		this.terrain = tile.getTerrainType().name();

		if (property != null) {
			this.propertyType = property.getDefinition().getType().name();
			this.captureProgress = property.getCaptureProgress();
			if (property.getCapturingPlayer() != null) {
				this.capturingPlayerOrder = property.getCapturingPlayer().getTurnOrder();
			}
			if (property.getOwner() != null) {
				this.ownerOrder = property.getOwner().getTurnOrder();
			}
		}
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getTerrain() {
		return terrain;
	}

	public String getPropertyType() {
		return propertyType;
	}

	public Integer getOwnerOrder() {
		return ownerOrder;
	}

	public int getCaptureProgress() {
		return captureProgress;
	}

	public Integer getCapturingPlayerOrder() {
		return capturingPlayerOrder;
	}
}