package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;
import co.edu.unbosque.advancewars.model.GameState;

/** Datos de partida con su propietario, solo para administracion. */
public class AdminGameDTO {
	private final GameDTO game;
	private final Long creatorId;
	private final String creatorUsername;
	private final LocalDateTime createdAt;

	public AdminGameDTO(GameState state) {
		this.game = new GameDTO(state);
		this.creatorId = state.getCreatedBy() == null ? null : state.getCreatedBy().getId();
		this.creatorUsername = state.getCreatedBy() == null ? null : state.getCreatedBy().getUsername();
		this.createdAt = state.getCreatedAt();
	}

	public GameDTO getGame() {
		return game;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public String getCreatorUsername() {
		return creatorUsername;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
