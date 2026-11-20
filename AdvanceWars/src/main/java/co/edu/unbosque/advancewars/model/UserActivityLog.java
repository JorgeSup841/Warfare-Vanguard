package co.edu.unbosque.advancewars.model;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Evento realizado por una cuenta autenticada, distinto del jugador HOT-SEAT.
 */
@Entity
@Table(name = "user_activity_log")
public class UserActivityLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 180)
	private String action;

	@Column(nullable = false)
	private Instant createdAt;

	protected UserActivityLog() {
	}

	public UserActivityLog(User user, String action) {
		this.user = user;
		this.action = action;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getAction() {
		return action;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
