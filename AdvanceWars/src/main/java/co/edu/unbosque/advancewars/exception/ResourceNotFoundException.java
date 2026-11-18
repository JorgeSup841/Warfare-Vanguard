package co.edu.unbosque.advancewars.exception;

/**
 * Indica que el recurso solicitado no existe.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Crea una excepción cuando un recurso no se encuentra.
     */
    public ResourceNotFoundException(String message) { super(message); }
}
