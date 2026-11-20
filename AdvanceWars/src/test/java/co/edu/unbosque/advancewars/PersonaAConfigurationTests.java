//package co.edu.unbosque.advancewars;
//
//import co.edu.unbosque.advancewars.security.JwtUtil;
//import co.edu.unbosque.advancewars.security.LoginRateLimiter;
//import co.edu.unbosque.advancewars.security.SecurityConfig;
//import org.junit.jupiter.api.Test;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.mock;
//
///**
// * Comprueba las restricciones configurables de BCrypt y del limitador de login.
// */
//class PersonaAConfigurationTests {
//    /** Comprueba el factor de BCrypt y el rechazo de valores inseguros. */
//    @Test
//    void bcryptConfigurable() {
//        SecurityConfig config = new SecurityConfig(mock(JwtUtil.class), mock(UserDetailsService.class));
//        PasswordEncoder encoder = config.passwordEncoder(12);
//        assertTrue(encoder.matches("password-de-prueba", encoder.encode("password-de-prueba")));
//        assertThrows(IllegalArgumentException.class, () -> config.passwordEncoder(8));
//    }
//
//    /** Comprueba el bloqueo y restablecimiento tras varios intentos fallidos. */
//    @Test
//    void loginLimiterConfigurable() {
//        LoginRateLimiter limiter = new LoginRateLimiter(900, 2, 1000);
//        assertTrue(limiter.allowed("test"));
//        limiter.failed("test");
//        assertTrue(limiter.allowed("test"));
//        limiter.failed("test");
//        assertFalse(limiter.allowed("test"));
//        limiter.succeeded("test");
//        assertTrue(limiter.allowed("test"));
//    }
//}
