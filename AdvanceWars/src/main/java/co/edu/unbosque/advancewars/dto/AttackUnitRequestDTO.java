package co.edu.unbosque.advancewars.dto;

/** Envia la unidad enemiga y el turno actual para atacar. */
public class AttackUnitRequestDTO {

	private Long targetUnitId;
	private Integer currentRound;
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
