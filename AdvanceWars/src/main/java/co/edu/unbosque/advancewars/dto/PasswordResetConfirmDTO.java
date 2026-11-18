package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Transporta la verificación OTP y la contraseña nueva.
 */
public class PasswordResetConfirmDTO {

    @NotBlank
    private String challengeId;

    @NotBlank @Pattern(regexp = "[0-9]{6}")
    private String code;

    @NotBlank @Size(min = 12, max = 64)
    private String newPassword;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public PasswordResetConfirmDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param challengeId identificador del desafío
     * @param code código OTP
     * @param newPassword contraseña nueva
     */
    public PasswordResetConfirmDTO(String challengeId, String code, String newPassword) {
        this.challengeId = challengeId;
        this.code = code;
        this.newPassword = newPassword;
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
     * Obtiene contraseña nueva.
     * @return contraseña nueva
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Establece contraseña nueva.
     * @param newPassword contraseña nueva
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
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
        if (!(object instanceof PasswordResetConfirmDTO other)) {
            return false;
        }
        return Objects.equals(challengeId, other.challengeId)
                && Objects.equals(code, other.code)
                && Objects.equals(newPassword, other.newPassword);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(challengeId, code, newPassword);
    }

    /**
     * Describe el DTO sin mostrar datos sensibles.
     * @return nombre del tipo DTO
     */
    @Override
    public String toString() {
        return "PasswordResetConfirmDTO";
    }

}
