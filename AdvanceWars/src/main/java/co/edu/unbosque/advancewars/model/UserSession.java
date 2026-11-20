package co.edu.unbosque.advancewars.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Una sesion JWT identificada por su jti. No almacena el token completo. */
@Entity
@Table(name = "user_session")
public class UserSession {
	@Id
	@Column(length = 36)
	private String id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Instant loginAt;

	@Column(nullable = false)
	private Instant lastSeen;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private boolean revoked;

	@Column(length = 180)
	private String lastAction;

	protected UserSession() {
	}

	public UserSession(String id, User user, Instant expiresAt) {
		this.id = id;
		this.user = user;
		this.loginAt = Instant.now();
		this.lastSeen = loginAt;
		this.expiresAt = expiresAt;
		this.revoked = false;
		this.lastAction = "LOGIN";
	}

	public String getId() {
		return id;
	}

	public User getUser() {
		return user;
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

	public String getLastAction() {
		return lastAction;
	}

	public void touch(Instant when) {
		this.lastSeen = when;
	}

	public void recordAction(String action, Instant when) {
		this.lastAction = action;
		this.lastSeen = when;
	}

	public void revoke() {
		this.revoked = true;
	}

	/** 'Conectado' es una aproximacion basada en actividad reciente. */
	public boolean isOnline(Instant now) {
		return !revoked && expiresAt.isAfter(now) && lastSeen.isAfter(now.minusSeconds(90)) && user.isEnabled();
	}
}
