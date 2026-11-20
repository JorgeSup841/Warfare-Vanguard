package co.edu.unbosque.advancewars.dto;

import java.time.Instant;
import co.edu.unbosque.advancewars.model.UserSession;

/** Informacion de sesion visible al ADMIN; nunca contiene el JWT. */
public class UserSessionDTO {
	private final String id;
	private final Long userId;
	private final String username;
	private final Instant loginAt;
	private final Instant lastSeen;
	private final Instant expiresAt;
	private final boolean revoked;
	private final boolean online;
	private final String lastAction;

	public UserSessionDTO(UserSession session, Instant now) {
		this.id = session.getId();
		this.userId = session.getUser().getId();
		this.username = session.getUser().getUsername();
		this.loginAt = session.getLoginAt();
		this.lastSeen = session.getLastSeen();
		this.expiresAt = session.getExpiresAt();
		this.revoked = session.isRevoked();
		this.online = session.isOnline(now);
		this.lastAction = session.getLastAction();
	}

	public String getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public Instant getLoginAt() {
		return loginAt;
	}

	public Instant getLastSeen() {
		return lastSeen;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public boolean isRevoked() {
		return revoked;
	}

	public boolean isOnline() {
		return online;
	}

	public String getLastAction() {
		return lastAction;
	}
}
