package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Transporta el cambio solicitado de habilitación de una cuenta.
 */
public class EnabledUpdateRequestDTO {

    @NotNull
    private Boolean enabled;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public EnabledUpdateRequestDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param enabled estado de habilitación
     */
    public EnabledUpdateRequestDTO(Boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Obtiene estado de habilitación.
     * @return estado de habilitación
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * Establece estado de habilitación.
     * @param enabled estado de habilitación
     */
    public void setEnabled(Boolean enabled) {
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
        if (!(object instanceof EnabledUpdateRequestDTO other)) {
            return false;
        }
        return Objects.equals(enabled, other.enabled);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(enabled);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "EnabledUpdateRequestDTO [" + "enabled=" + enabled + "]";
    }

}
