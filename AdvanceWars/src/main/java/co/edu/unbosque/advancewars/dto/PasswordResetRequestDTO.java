package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Transporta el correo de una solicitud de recuperación de contraseña.
 */
public class PasswordResetRequestDTO {

    @NotBlank @Email
    private String email;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public PasswordResetRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param email correo electrónico
     */
    public PasswordResetRequestDTO(String email) {
        this.email = email;
    }

    /**
     * Obtiene correo electrónico.
     * @return correo electrónico
     */
    public String getEmail() {
        return email;
    }

    /**
     * Establece correo electrónico.
     * @param email correo electrónico
     */
    public void setEmail(String email) {
        this.email = email;
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
        if (!(object instanceof PasswordResetRequestDTO other)) {
            return false;
        }
        return Objects.equals(email, other.email);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    /**
     * Describe el DTO sin mostrar datos sensibles.
     * @return nombre del tipo DTO
     */
    @Override
    public String toString() {
        return "PasswordResetRequestDTO";
    }

}
