package co.edu.unbosque.advancewars.dto;

import java.time.Instant;
import co.edu.unbosque.advancewars.model.UserActivityLog;

public class UserActivityDTO {
	private final Long id;
	private final Long userId;
	private final String username;
	private final String action;
	private final Instant createdAt;

	public UserActivityDTO(UserActivityLog event) {
		this.id = event.getId();
		this.userId = event.getUser().getId();
		this.username = event.getUser().getUsername();
		this.action = event.getAction();
		this.createdAt = event.getCreatedAt();
	}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getAction() {
		return action;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
