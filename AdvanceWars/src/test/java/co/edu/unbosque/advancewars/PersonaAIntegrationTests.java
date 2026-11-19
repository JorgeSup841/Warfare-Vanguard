package co.edu.unbosque.advancewars;

import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.security.PublicRequestRateLimiter;
import co.edu.unbosque.advancewars.service.OtpService;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

/** Pruebas aisladas de protección de peticiones y de DTO. */
class PersonaAIntegrationTests {

    /** Comprueba el límite de peticiones públicas sin acceder a MySQL. */
    @Test
    void publicRequestLimit() {
        PublicRequestRateLimiter rateLimiter = new PublicRequestRateLimiter(900, 2, 100);
        rateLimiter.check("127.0.0.1");
        rateLimiter.check("127.0.0.1");
        assertThrows(OtpService.OtpLimitException.class, () -> rateLimiter.check("127.0.0.1"));
        rateLimiter.check("127.0.0.2");
    }

    /** Comprueba el formato convencional de los DTO y la codificación BCrypt. */
    @Test
    void dtoWithPasswordEncoder() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("persona");
        dto.setEmail("persona@example.invalid");
        dto.setPassword("Clave-de-prueba-12345");
        assertEquals("persona", dto.getUsername());
        assertEquals("persona@example.invalid", dto.getEmail());
        var bcrypt = new BCryptPasswordEncoder(10);
        assertTrue(bcrypt.matches(dto.getPassword(), bcrypt.encode(dto.getPassword())));
        assertFalse(dto.toString().contains(dto.getPassword()));
    }
}
