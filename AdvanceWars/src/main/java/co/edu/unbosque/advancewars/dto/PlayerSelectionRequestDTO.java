package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Identificadores de facción y comandante seleccionados durante SETUP. */
public class PlayerSelectionRequestDTO {
	@NotNull
	@Min(1)
	private Long factionId;
	@NotNull
	@Min(1)
	private Long commanderId;

	public Long getFactionId() {
		return factionId;
	}

	public void setFactionId(Long factionId) {
		this.factionId = factionId;
	}

	public Long getCommanderId() {
		return commanderId;
	}

	public void setCommanderId(Long commanderId) {
		this.commanderId = commanderId;
	}
}
