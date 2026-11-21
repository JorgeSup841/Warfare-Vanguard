package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;
import co.edu.unbosque.advancewars.model.GameLog;

/** DTO público del historial de partida, sin exponer la entidad JPA. */
public class GameLogDTO {
	private final Long id;
	private final String eventType;
	private final String description;
	private final LocalDateTime occurredAt;
	private final int roundNumber;
	private final int turnOrder;

	/** Copia los campos públicos de un evento persistido. */
	public GameLogDTO(GameLog log) {
		this.id = log.getId();
		this.eventType = log.getEventType().name();
		this.description = log.getDescription();
		this.occurredAt = log.getCreatedAt();
		this.roundNumber = log.getRoundNumber();
		this.turnOrder = log.getTurnOrder();
	}

	public Long getId() {
		return id;
	}

	public String getEventType() {
		return eventType;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getOccurredAt() {
		return occurredAt;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getTurnOrder() {
		return turnOrder;
	}
}
