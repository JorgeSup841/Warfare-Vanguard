package co.edu.unbosque.advancewars.dto;

import java.time.Instant;
import java.util.Objects;

/**
 * Transporta una respuesta uniforme de error HTTP.
 */
public class ApiErrorDTO {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    /**
     * Crea un DTO vacío para la deserialización JSON.
     */
    public ApiErrorDTO() {
    }

    /**
     * Construye el DTO a partir de sus campos.
     * @param timestamp fecha del error
     * @param status estado HTTP
     * @param error descripción del estado
     * @param message mensaje
     * @param path ruta solicitada
     */
    public ApiErrorDTO(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    /**
     * Obtiene fecha del error.
     * @return fecha del error
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Establece fecha del error.
     * @param timestamp fecha del error
     */
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Obtiene estado HTTP.
     * @return estado HTTP
     */
    public int getStatus() {
        return status;
    }

    /**
     * Establece estado HTTP.
     * @param status estado HTTP
     */
    public void setStatus(int status) {
        this.status = status;
    }

    /**
     * Obtiene descripción del estado.
     * @return descripción del estado
     */
    public String getError() {
        return error;
    }

    /**
     * Establece descripción del estado.
     * @param error descripción del estado
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Obtiene mensaje.
     * @return mensaje
     */
    public String getMessage() {
        return message;
    }

    /**
     * Establece mensaje.
     * @param message mensaje
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Obtiene ruta solicitada.
     * @return ruta solicitada
     */
    public String getPath() {
        return path;
    }

    /**
     * Establece ruta solicitada.
     * @param path ruta solicitada
     */
    public void setPath(String path) {
        this.path = path;
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
        if (!(object instanceof ApiErrorDTO other)) {
            return false;
        }
        return Objects.equals(timestamp, other.timestamp)
                && status == other.status
                && Objects.equals(error, other.error)
                && Objects.equals(message, other.message)
                && Objects.equals(path, other.path);
    }

    /**
     * Calcula la huella de igualdad de este DTO.
     * @return hash de los campos del DTO
     */
    @Override
    public int hashCode() {
        return Objects.hash(timestamp, status, error, message, path);
    }

    /**
     * Describe el DTO sin mostrar valores secretos ni códigos de verificación.
     * @return resumen seguro para depuración
     */
    @Override
    public String toString() {
        return "ApiErrorDTO [" + "timestamp=" + timestamp + ", " + "status=" + status + ", " + "error=" + error + "]";
    }

}
