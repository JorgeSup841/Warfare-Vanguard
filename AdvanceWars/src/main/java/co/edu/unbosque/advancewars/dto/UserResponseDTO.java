package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.Role;
import java.util.Objects;

/**
 * Expone únicamente los datos públicos de un usuario, nunca credenciales.
 */
public class UserResponseDTO {

    private Long id;

    private String username;

    private String email;

    private Role role;

    private boolean emailVerified;

    private boolean enabled;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public UserResponseDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param id identificador
     * @param username nombre de usuario
     * @param email correo electrónico
     * @param role rol
     * @param emailVerified estado de verificación de correo
     * @param enabled estado de habilitación
     */
    public UserResponseDTO(Long id, String username, String email, Role role, boolean emailVerified, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.emailVerified = emailVerified;
        this.enabled = enabled;
    }

    /**
     * Obtiene identificador.
     * @return identificador
     */
    public Long getId() {
        return id;
    }

    /**
     * Establece identificador.
     * @param id identificador
     */
    public void setId(Long id) {
        this.id = id;
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
     * Obtiene rol.
     * @return rol
     */
    public Role getRole() {
        return role;
    }

    /**
     * Establece rol.
     * @param role rol
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Obtiene estado de verificación de correo.
     * @return estado de verificación de correo
     */
    public boolean isEmailVerified() {
        return emailVerified;
    }

    /**
     * Establece estado de verificación de correo.
     * @param emailVerified estado de verificación de correo
     */
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    /**
     * Obtiene estado de habilitación.
     * @return estado de habilitación
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Establece estado de habilitación.
     * @param enabled estado de habilitación
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        if (!(object instanceof UserResponseDTO other)) {
            return false;
        }
        return Objects.equals(id, other.id)
                && Objects.equals(username, other.username)
                && Objects.equals(email, other.email)
                && Objects.equals(role, other.role)
                && emailVerified == other.emailVerified
                && enabled == other.enabled;
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, username, email, role, emailVerified, enabled);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "UserResponseDTO [" + "id=" + id + ", " + "username=" + username + ", " + "role=" + role + ", " + "emailVerified=" + emailVerified + ", " + "enabled=" + enabled + "]";
    }

}
