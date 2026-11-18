package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.Role;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Transporta el rol solicitado para una actualización autorizada.
 */
public class RoleUpdateRequestDTO {

    @NotNull
    private Role role;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public RoleUpdateRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param role rol
     */
    public RoleUpdateRequestDTO(Role role) {
        this.role = role;
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
     * Compara los valores de los campos de este DTO.
     * @param object objeto que se compara
     * @return true cuando ambos DTO contienen los mismos valores
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof RoleUpdateRequestDTO other)) {
            return false;
        }
        return Objects.equals(role, other.role);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(role);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "RoleUpdateRequestDTO [" + "role=" + role + "]";
    }

}
