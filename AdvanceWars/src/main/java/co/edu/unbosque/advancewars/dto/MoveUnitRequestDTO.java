package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Coordenadas de destino y turno que ve el jugador en pantalla. */
public class MoveUnitRequestDTO {

	@NotNull
	@Min(0)

	private Integer x;
	@NotNull
	@Min(0)
	private Integer y;
	@NotNull
	@Min(1)
	private Integer currentRound;
	@NotNull
	@Min(1)
	private Integer currentPlayerOrder;

	/** Crea una solicitud vacía para deserialización JSON. */
	public MoveUnitRequestDTO() {
	}

	/** Crea una solicitud con destino y versión del turno. */
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
