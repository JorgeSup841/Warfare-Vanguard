package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.OtpPurpose;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/** Representa un desafío de un solo uso para los flujos de verificación y autenticación. */
@Entity
@Table(name = "otp_challenge")
public class OtpChallenge {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	@Column(unique = true, nullable = false)
	private String challengeId;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 100)
	private String codeHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OtpPurpose purpose;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	private LocalDateTime consumedAt;

	@Column(nullable = false)
	private int attempts;

	@Column(nullable = false)
	private int maxAttempts;

	@Version
	private Long version; // skipcq: JAVA-E1065

	/** Construye un desafío OTP asociado a un usuario, propósito, hash y vencimiento. */
	public OtpChallenge() {
		this.attempts = 0;
		this.maxAttempts = 5;
	}

	/** Construye un desafío OTP asociado a un usuario, propósito, hash y vencimiento. */
	public OtpChallenge(User user, String codeHash, OtpPurpose purpose, LocalDateTime expiresAt) {
		this();
		this.challengeId = UUID.randomUUID().toString();
		this.user = user;
		this.codeHash = codeHash;
		this.purpose = purpose;
		this.createdAt = LocalDateTime.now();
		this.expiresAt = expiresAt;
	}
	/** Indica si el desafío OTP ya fue consumido. */
	public boolean isConsumed() {
		return consumedAt != null;
	}

	/** Registra un intento fallido de validación del código OTP. */
	public void registerFailedAttempt() {
		attempts++;
	}

	/** Marca el desafío OTP como consumido y registra la fecha correspondiente. */
	public void consume() {
		this.consumedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getChallengeId() {
		return challengeId;
	}

	public void setChallengeId(String challengeId) {
		this.challengeId = challengeId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getCodeHash() {
		return codeHash;
	}

	public void setCodeHash(String codeHash) {
		this.codeHash = codeHash;
	}

	public OtpPurpose getPurpose() {
		return purpose;
	}

	public void setPurpose(OtpPurpose purpose) {
		this.purpose = purpose;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

	public LocalDateTime getConsumedAt() {
		return consumedAt;
	}

	public void setConsumedAt(LocalDateTime consumedAt) {
		this.consumedAt = consumedAt;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public int getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public Long getVersion() {
		return version;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof OtpChallenge other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return OtpChallenge.class.hashCode();
	}

	@Override
	public String toString() {
		return "OtpChallenge{" + "id=" + id + ", purpose=" + purpose + ", createdAt=" + createdAt + ", expiresAt="
				+ expiresAt + ", consumedAt=" + consumedAt + ", attempts=" + attempts + ", maxAttempts=" + maxAttempts
				+ '}';
	}
}
