package co.edu.unbosque.advancewars.exception;

/**
 * Indica que la autenticación de una cuenta no fue aceptada.
 */
public class AuthenticationFailedException extends RuntimeException {
    /**
     * Crea una excepción con un mensaje de fallo de autenticación.
     */
    public AuthenticationFailedException(String message) { super(message); }
}
