package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.model.GameLog;

/** Evento del juego; player es un participante local, no una cuenta. */
public class AdminGameLogDTO {
	private final Long id;
	private final GameLogType eventType;
	private final int roundNumber;
	private final int turnOrder;
	private final String description;
	private final LocalDateTime createdAt;

	public AdminGameLogDTO(GameLog log) {
		id = log.getId();
		eventType = log.getEventType();
		roundNumber = log.getRoundNumber();
		turnOrder = log.getTurnOrder();
		description = log.getDescription();
		createdAt = log.getCreatedAt();
	}

	public Long getId() {
		return id;
	}

	public GameLogType getEventType() {
		return eventType;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getTurnOrder() {
		return turnOrder;
	}

	public String getDescription() {
		return description;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
