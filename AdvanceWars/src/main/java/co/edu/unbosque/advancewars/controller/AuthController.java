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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ResponseEntity<AuthPendingResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthPendingResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(auth.login(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody OtpVerificationRequestDTO request) {
        if (!auth.verifyEmail(request.challengeId(), request.code())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Correo verificado"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody OtpVerificationRequestDTO request) {
        AuthResponseDTO response = auth.verifyLoginOtp(request.challengeId(), request.code());
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<AuthPendingResponseDTO> resend(@Valid @RequestBody OtpResendRequestDTO request) {
        return ResponseEntity.ok(auth.resend(request.challengeId()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody PasswordResetRequestDTO request) {
        auth.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás instrucciones"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody PasswordResetConfirmDTO request) {
        if (!auth.resetPassword(request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }
}
