package co.edu.unbosque.advancewars.exception;

/**
 * Indica un conflicto entre la solicitud y el estado persistido.
 */
public class ConflictException extends RuntimeException {
    /**
     * Crea una excepción para un conflicto de negocio.
     */
    public ConflictException(String message) { super(message); }
}
