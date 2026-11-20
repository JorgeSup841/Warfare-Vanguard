package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.UnitType;

/** Datos necesarios para comprar una unidad en una fabrica. */
public class ProduceUnitRequestDTO {

    private Long propertyId;
    private UnitType unitType;
    private Integer currentRound;
    private Integer currentPlayerOrder;

    public ProduceUnitRequestDTO() {
    }

    public Long getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    public Integer getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(Integer currentRound) {
        this.currentRound = currentRound;
    }

    public Integer getCurrentPlayerOrder() {
        return currentPlayerOrder;
    }

    public void setCurrentPlayerOrder(Integer currentPlayerOrder) {
        this.currentPlayerOrder = currentPlayerOrder;
    }
}
