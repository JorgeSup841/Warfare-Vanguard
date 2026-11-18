package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Objects;

/**
 * Transporta únicamente los datos necesarios para registrar un usuario.
 */
public class RegisterRequestDTO {

    @NotBlank @Size(min = 3, max = 50)
    private String username;

    @NotBlank @Email @Size(max = 254)
    private String email;

    @NotBlank @Size(min = 12, max = 64)
    private String password;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public RegisterRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param password contraseña
     */
    public RegisterRequestDTO(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /**
     * Obtiene nombre de usuario.
     * @return nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    /**
     * Establece nombre de usuario.
     * @param username nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
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
     * Obtiene contraseña.
     * @return contraseña
     */
    public String getPassword() {
        return password;
    }

    /**
     * Establece contraseña.
     * @param password contraseña
     */
    public void setPassword(String password) {
        this.password = password;
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
        if (!(object instanceof RegisterRequestDTO other)) {
            return false;
        }
        return Objects.equals(username, other.username)
                && Objects.equals(email, other.email)
                && Objects.equals(password, other.password);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, email, password);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "RegisterRequestDTO [" + "username=" + username + "]";
    }

}
