package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.dto.AuthPendingResponseDTO;
import co.edu.unbosque.advancewars.dto.AuthResponseDTO;
import co.edu.unbosque.advancewars.dto.LoginRequestDTO;
import co.edu.unbosque.advancewars.dto.OtpResendRequestDTO;
import co.edu.unbosque.advancewars.dto.OtpVerificationRequestDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetConfirmDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetRequestDTO;
import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone los endpoints REST para registro, acceso y recuperación de cuentas.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    /**
     * Inyecta el servicio de autenticación.
     */
    public AuthController(AuthService auth) { this.auth = auth; }

    /**
     * Registra una cuenta y devuelve el desafío de verificación.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthPendingResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    /**
     * Comprueba las credenciales y solicita el segundo factor.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthPendingResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(auth.login(request));
    }

    /**
     * Comprueba el OTP de verificación de correo.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody OtpVerificationRequestDTO request) {
        if (!auth.verifyEmail(request.getChallengeId(), request.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Correo verificado"));
    }

    /**
     * Completa el segundo factor y devuelve la sesión JWT.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody OtpVerificationRequestDTO request) {
        AuthResponseDTO response = auth.verifyLoginOtp(request.getChallengeId(), request.getCode());
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Reenvía un desafío OTP según los límites configurados.
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<AuthPendingResponseDTO> resend(@Valid @RequestBody OtpResendRequestDTO request) {
        return ResponseEntity.ok(auth.resend(request.getChallengeId()));
    }

    /**
     * Solicita el restablecimiento de contraseña sin revelar la existencia de la cuenta.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody PasswordResetRequestDTO request) {
        auth.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás instrucciones"));
    }

    /**
     * Actualiza la contraseña cuando el OTP es válido.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody PasswordResetConfirmDTO request) {
        if (!auth.resetPassword(request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }
}
