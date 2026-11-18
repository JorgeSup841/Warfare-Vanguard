package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Objects;

/**
 * Transporta las credenciales iniciales para iniciar sesión.
 */
public class LoginRequestDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public LoginRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param username nombre de usuario
     * @param password contraseña
     */
    public LoginRequestDTO(String username, String password) {
        this.username = username;
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
        if (!(object instanceof LoginRequestDTO other)) {
            return false;
        }
        return Objects.equals(username, other.username)
                && Objects.equals(password, other.password);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "LoginRequestDTO [" + "username=" + username + "]";
    }

}
