package co.edu.unbosque.advancewars.exception;

/**
 * Indica que una operación recibió datos de negocio no válidos.
 */
public class BadRequestException extends RuntimeException {
    /**
     * Crea una excepción para una solicitud de negocio inválida.
     */
    public BadRequestException(String message) { super(message); }
}
