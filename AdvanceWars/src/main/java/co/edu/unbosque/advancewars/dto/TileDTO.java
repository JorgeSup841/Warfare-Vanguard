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

    public TileDTO(Tile tile, Property property) {
        this.x = tile.getPosition().getX();
        this.y = tile.getPosition().getY();
        this.terrain = tile.getTerrainType().name();

        if (property != null) {
            this.propertyType = property.getDefinition().getType().name();
            if (property.getOwner() != null) {
                this.ownerOrder = property.getOwner().getTurnOrder();
            }
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public String getTerrain() { return terrain; }
    public String getPropertyType() { return propertyType; }
    public Integer getOwnerOrder() { return ownerOrder; }
}
