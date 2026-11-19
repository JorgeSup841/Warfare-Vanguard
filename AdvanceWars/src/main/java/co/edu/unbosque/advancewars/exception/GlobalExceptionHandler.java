package co.edu.unbosque.advancewars.exception;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.dto.ApiErrorDTO;
import co.edu.unbosque.advancewars.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Transforma excepciones de la API en respuestas HTTP seguras.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Devuelve HTTP 401 ante un fallo de autenticación.
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorDTO> authenticationFailed(AuthenticationFailedException ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    /**
     * Devuelve HTTP 400 para una solicitud de negocio inválida.
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorDTO> badRequest(BadRequestException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    /**
     * Devuelve HTTP 404 cuando el recurso no existe.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    /**
     * Devuelve HTTP 409 para un conflicto o una restricción de datos.
     */
    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorDTO> conflict(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "El recurso ya existe o incumple una restricción", req);
    }

    /**
     * Devuelve HTTP 429 cuando se excede un límite OTP.
     */
    @ExceptionHandler(OtpService.OtpLimitException.class)
    public ResponseEntity<ApiErrorDTO> otpLimit(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    /**
     * Devuelve HTTP 400 para errores de validación o JSON inválido.
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorDTO> validation(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida", req);
    }

    /**
     * Devuelve HTTP 503 si el envío de correo falla.
     */
    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiErrorDTO> mail(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible enviar el correo; vuelve a solicitar un código", req);
    }

    /**
     * Devuelve HTTP 403 cuando no hay autorización suficiente.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> accessDenied(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "No tienes permiso", req);
    }

    /**
     * Respeta el código HTTP de las excepciones de Spring.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorDTO> responseStatus(
            ResponseStatusException ex,
            HttpServletRequest req) {

        HttpStatusCode status = ex.getStatusCode();

        return ResponseEntity.status(status).body(
            new ApiErrorDTO(
                Instant.now(),
                status.value(),
                HttpStatus.valueOf(status.value()).getReasonPhrase(),
                ex.getReason(),
                req.getRequestURI()
            )
        );
    }
    
    /**
     * Devuelve una respuesta genérica HTTP 500 sin divulgar detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> unexpected(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", req);
    }

    /**
     * Construye el formato uniforme de las respuestas de error.
     */
    private ResponseEntity<ApiErrorDTO> error(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiErrorDTO(Instant.now(), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
