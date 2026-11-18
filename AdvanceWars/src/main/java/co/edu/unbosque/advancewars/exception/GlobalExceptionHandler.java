package co.edu.unbosque.advancewars.exception;

import co.edu.unbosque.advancewars.dto.ApiErrorDTO;
import co.edu.unbosque.advancewars.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ApiErrorDTO> authenticationFailed(AuthenticationFailedException ex, HttpServletRequest req) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage(), req);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorDTO> badRequest(BadRequestException ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), req);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorDTO> notFound(ResourceNotFoundException ex, HttpServletRequest req) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), req);
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiErrorDTO> conflict(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.CONFLICT, "El recurso ya existe o incumple una restricción", req);
    }

    @ExceptionHandler(OtpService.OtpLimitException.class)
    public ResponseEntity<ApiErrorDTO> otpLimit(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), req);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ApiErrorDTO> validation(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST, "Solicitud inválida", req);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<ApiErrorDTO> mail(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "No fue posible enviar el correo; vuelve a solicitar un código", req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorDTO> accessDenied(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.FORBIDDEN, "No tienes permiso", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDTO> unexpected(Exception ex, HttpServletRequest req) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", req);
    }

    private ResponseEntity<ApiErrorDTO> error(HttpStatus status, String message, HttpServletRequest req) {
        return ResponseEntity.status(status).body(new ApiErrorDTO(Instant.now(), status.value(),
                status.getReasonPhrase(), message, req.getRequestURI()));
    }
}
