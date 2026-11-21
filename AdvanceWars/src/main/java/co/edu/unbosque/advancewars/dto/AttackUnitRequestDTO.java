package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Envia la unidad enemiga y el turno actual para atacar. */
public class AttackUnitRequestDTO {

	@NotNull
	@Min(1)

	private Long targetUnitId;
	@NotNull
	@Min(1)
	private Integer currentRound;
	@NotNull
	@Min(1)
	private Integer currentPlayerOrder;

	public AttackUnitRequestDTO() {
	}

	public AttackUnitRequestDTO(Long targetUnitId, Integer currentRound, Integer currentPlayerOrder) {
		this.targetUnitId = targetUnitId;
		this.currentRound = currentRound;
		this.currentPlayerOrder = currentPlayerOrder;
	}

	public Long getTargetUnitId() {
		return targetUnitId;
	}

	public void setTargetUnitId(Long targetUnitId) {
		this.targetUnitId = targetUnitId;
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
