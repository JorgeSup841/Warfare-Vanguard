package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Transporta los datos para verificar un desafío OTP.
 */
public class OtpVerificationRequestDTO {

    @NotBlank
    private String challengeId;

    @NotBlank @Pattern(regexp = "[0-9]{6}")
    private String code;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public OtpVerificationRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param challengeId identificador del desafío
     * @param code código OTP
     */
    public OtpVerificationRequestDTO(String challengeId, String code) {
        this.challengeId = challengeId;
        this.code = code;
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
     * Obtiene código OTP.
     * @return código OTP
     */
    public String getCode() {
        return code;
    }

    /**
     * Establece código OTP.
     * @param code código OTP
     */
    public void setCode(String code) {
        this.code = code;
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
        if (!(object instanceof OtpVerificationRequestDTO other)) {
            return false;
        }
        return Objects.equals(challengeId, other.challengeId)
                && Objects.equals(code, other.code);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(challengeId, code);
    }

    /**
     * Describe el DTO sin mostrar datos sensibles.
     * @return nombre del tipo DTO
     */
    @Override
    public String toString() {
        return "OtpVerificationRequestDTO";
    }

}
