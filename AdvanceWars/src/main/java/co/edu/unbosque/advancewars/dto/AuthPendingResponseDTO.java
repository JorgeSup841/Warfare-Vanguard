package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Indica el desafío OTP pendiente de validación.
 */
public class AuthPendingResponseDTO {

    private String status;

    private String challengeId;

    private LocalDateTime expiresAt;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public AuthPendingResponseDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param status estado de autenticación
     * @param challengeId identificador del desafío
     * @param expiresAt fecha de expiración
     */
    public AuthPendingResponseDTO(String status, String challengeId, LocalDateTime expiresAt) {
        this.status = status;
        this.challengeId = challengeId;
        this.expiresAt = expiresAt;
    }

    /**
     * Obtiene estado de autenticación.
     * @return estado de autenticación
     */
    public String getStatus() {
        return status;
    }

    /**
     * Establece estado de autenticación.
     * @param status estado de autenticación
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Obtiene identificador del desafío.
     * @return identificador del desafío
     */
    public String getChallengeId() {
        return challengeId;
    }

    /**
     * Establece identificador del desafío.
     * @param challengeId identificador del desafío
     */
    public void setChallengeId(String challengeId) {
        this.challengeId = challengeId;
    }

    /**
     * Obtiene fecha de expiración.
     * @return fecha de expiración
     */
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    /**
     * Establece fecha de expiración.
     * @param expiresAt fecha de expiración
     */
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Compara los valores de los campos de este DTO.
     * @param object objeto que se compara
     * @return true cuando ambos DTO contienen los mismos valores
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AuthPendingResponseDTO other)) {
            return false;
        }
        return Objects.equals(status, other.status)
                && Objects.equals(challengeId, other.challengeId)
                && Objects.equals(expiresAt, other.expiresAt);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, challengeId, expiresAt);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "AuthPendingResponseDTO [" + "status=" + status + ", " + "expiresAt=" + expiresAt + "]";
    }

}
