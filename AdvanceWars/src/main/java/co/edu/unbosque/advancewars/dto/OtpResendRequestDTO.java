package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Identifica el desafío para solicitar un nuevo OTP.
 */
public class OtpResendRequestDTO {

    @NotBlank
    private String challengeId;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public OtpResendRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param challengeId identificador del desafío
     */
    public OtpResendRequestDTO(String challengeId) {
        this.challengeId = challengeId;
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
     * Compara los valores de los campos de este DTO.
     * @param object objeto que se compara
     * @return true cuando ambos DTO contienen los mismos valores
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof OtpResendRequestDTO other)) {
            return false;
        }
        return Objects.equals(challengeId, other.challengeId);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(challengeId);
    }

    /**
     * Describe el DTO sin mostrar datos sensibles.
     * @return nombre del tipo DTO
     */
    @Override
    public String toString() {
        return "OtpResendRequestDTO";
    }

}
