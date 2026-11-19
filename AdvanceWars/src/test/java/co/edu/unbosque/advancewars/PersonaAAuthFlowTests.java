package co.edu.unbosque.advancewars;

import co.edu.unbosque.advancewars.dto.AuthPendingResponseDTO;
import co.edu.unbosque.advancewars.dto.AuthResponseDTO;
import co.edu.unbosque.advancewars.dto.LoginRequestDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetConfirmDTO;
import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.repository.UserRepository;
import co.edu.unbosque.advancewars.service.AuthService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Comprueba el flujo de autenticación de Persona A en una base de datos de prueba. */
@SpringBootTest
@ActiveProfiles("test")
class PersonaAAuthFlowTests {
    @Autowired
    private AuthService auth;

    @Autowired
    private UserRepository users;

    @MockitoBean
    private JavaMailSender mailSender;

    /** Recorre registro, verificación, segundo factor y recuperación de contraseña. */
    @Test
    void registroLoginYRecuperacion() {
        String username = "usuarioflujo21";
        String email = "usuarioflujo21@example.invalid";
        String originalPassword = "ClaveOriginal!123";
        String newPassword = "NuevaClave!3214";

        AuthPendingResponseDTO registration = auth.register(
                new RegisterRequestDTO(username, email, originalPassword));
        assertEquals("EMAIL_VERIFICATION_REQUIRED", registration.getStatus());
        assertFalse(users.findByUsername(username).orElseThrow().isEmailVerified());
        String emailCode = lastCode(1);
        assertFalse(auth.verifyEmail(registration.getChallengeId(), "999999".equals(emailCode) ? "000000" : "999999"));
        assertTrue(auth.verifyEmail(registration.getChallengeId(), emailCode));
        assertFalse(auth.verifyEmail(registration.getChallengeId(), emailCode));

        AuthPendingResponseDTO pendingLogin = auth.login(new LoginRequestDTO(username, originalPassword));
        assertEquals("LOGIN_OTP_REQUIRED", pendingLogin.getStatus());
        String loginCode = lastCode(2);
        AuthResponseDTO access = auth.verifyLoginOtp(pendingLogin.getChallengeId(), loginCode);
        assertNotNull(access);
        assertNotNull(access.getAccessToken());
        assertEquals("Bearer", access.getTokenType());
        assertNull(auth.verifyLoginOtp(pendingLogin.getChallengeId(), loginCode));

        auth.forgotPassword(email);
        String resetCode = lastCode(3);
        ArgumentCaptor<SimpleMailMessage> capture = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(capture.capture());
        String resetMessage = capture.getAllValues().get(2).getText();
        assertNotNull(resetMessage);
        Matcher identifier = Pattern.compile("Identificador para restablecer contraseña: ([0-9a-f-]{36})")
                .matcher(resetMessage);
        assertTrue(identifier.find());
        assertTrue(auth.resetPassword(new PasswordResetConfirmDTO(identifier.group(1), resetCode, newPassword)));
        assertFalse(auth.resetPassword(new PasswordResetConfirmDTO(identifier.group(1), resetCode, newPassword)));
        assertThrows(RuntimeException.class, () -> auth.login(new LoginRequestDTO(username, originalPassword)));
        assertNotNull(auth.login(new LoginRequestDTO(username, newPassword)));
    }

    /** Extrae del mensaje capturado el OTP recibido en el correo de prueba. */
    private String lastCode(int expectedCount) {
        ArgumentCaptor<SimpleMailMessage> capture = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(expectedCount)).send(capture.capture());
        String text = capture.getAllValues().get(expectedCount - 1).getText();
        assertNotNull(text);
        Matcher match = Pattern.compile("Tu código de AdvanceWars es: ([0-9]{6})").matcher(text);
        assertTrue(match.find());
        return match.group(1);
    }
}
