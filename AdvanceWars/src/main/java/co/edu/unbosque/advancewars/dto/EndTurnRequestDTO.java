package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Estado del turno que el frontend intenta finalizar. */
public class EndTurnRequestDTO {
    @NotNull
    @Min(1)
    private Integer currentRound;
    @NotNull
    @Min(1)
    private Integer currentPlayerOrder;

    /** Crea una solicitud vacía para deserialización JSON. */
    public EndTurnRequestDTO() {
    }

    /** Crea una solicitud con el turno que conoce el cliente. */
    public EndTurnRequestDTO(Integer currentRound, Integer currentPlayerOrder) {
        this.currentRound = currentRound;
        this.currentPlayerOrder = currentPlayerOrder;
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
