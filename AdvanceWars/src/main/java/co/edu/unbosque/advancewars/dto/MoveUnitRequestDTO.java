package co.edu.unbosque.advancewars.dto;

/** Coordenadas de destino y turno que ve el jugador en pantalla. */
public class MoveUnitRequestDTO {

	private Integer x;
	private Integer y;
	private Integer currentRound;
	private Integer currentPlayerOrder;

	public MoveUnitRequestDTO() {
	}

	public MoveUnitRequestDTO(Integer x, Integer y, Integer currentRound, Integer currentPlayerOrder) {
		this.x = x;
		this.y = y;
		this.currentRound = currentRound;
		this.currentPlayerOrder = currentPlayerOrder;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
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
