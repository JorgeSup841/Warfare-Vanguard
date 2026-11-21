package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import co.edu.unbosque.advancewars.enums.UnitType;

/** Datos necesarios para comprar una unidad en una fabrica. */
public class ProduceUnitRequestDTO {

    @NotNull

    @Min(1)

    private Long propertyId;
    @NotNull
    private UnitType unitType;
    @NotNull
    @Min(1)
    private Integer currentRound;
    @NotNull
    @Min(1)
    private Integer currentPlayerOrder;

    /** Crea una solicitud vacía para deserialización JSON. */
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
