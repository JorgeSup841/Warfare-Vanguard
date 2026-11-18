package co.edu.unbosque.advancewars;

import co.edu.unbosque.advancewars.security.LoginRateLimiter;
import co.edu.unbosque.advancewars.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PersonaAConfigurationTests {
    @Test
    void bcryptConfigurable() {
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder(12);
        assertTrue(encoder.matches("password-de-prueba", encoder.encode("password-de-prueba")));
        assertThrows(IllegalArgumentException.class, () -> new SecurityConfig().passwordEncoder(8));
    }

    @Test
    void loginLimiterConfigurable() {
        LoginRateLimiter limiter = new LoginRateLimiter(900, 2, 1000);
        assertTrue(limiter.allowed("test"));
        limiter.failed("test");
        assertTrue(limiter.allowed("test"));
        limiter.failed("test");
        assertFalse(limiter.allowed("test"));
        limiter.succeeded("test");
        assertTrue(limiter.allowed("test"));
    }
}
