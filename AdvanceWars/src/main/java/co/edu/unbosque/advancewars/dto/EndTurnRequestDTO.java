package co.edu.unbosque.advancewars.dto;

/** Estado del turno que el frontend intenta finalizar. */
public class EndTurnRequestDTO {
    private Integer currentRound;
    private Integer currentPlayerOrder;

    public EndTurnRequestDTO() {
    }

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
