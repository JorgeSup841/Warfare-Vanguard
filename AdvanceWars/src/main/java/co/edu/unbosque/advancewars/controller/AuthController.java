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
import co.edu.unbosque.advancewars.service.UserService;
import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.security.PublicRequestRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final UserService users;
    private final PublicRequestRateLimiter publicRequestRateLimiter;

    /** Inyecta los servicios de autenticación, usuarios y protección de la API. */
    public AuthController(AuthService auth, UserService users,
            PublicRequestRateLimiter publicRequestRateLimiter) {
        this.auth = auth;
        this.users = users;
        this.publicRequestRateLimiter = publicRequestRateLimiter;
    }

    /** Devuelve los datos de la cuenta autenticada. */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Principal principal) {
        return ResponseEntity.ok(users.me(principal.getName()));
    }

    /**
     * Registra una cuenta y devuelve el desafío de verificación.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthPendingResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    /**
     * Comprueba las credenciales y solicita el segundo factor.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthPendingResponseDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(auth.login(request));
    }

    /**
     * Comprueba el OTP de verificación de correo.
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody OtpVerificationRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        if (!auth.verifyEmail(request.getChallengeId(), request.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Correo verificado"));
    }

    /**
     * Completa el segundo factor y devuelve la sesión JWT.
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody OtpVerificationRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
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
    public ResponseEntity<AuthPendingResponseDTO> resend(@Valid @RequestBody OtpResendRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(auth.resend(request.getChallengeId()));
    }

    /**
     * Solicita el restablecimiento de contraseña sin revelar la existencia de la cuenta.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody PasswordResetRequestDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        auth.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás instrucciones"));
    }

    /**
     * Actualiza la contraseña cuando el OTP es válido.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody PasswordResetConfirmDTO request, HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        if (!auth.resetPassword(request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }
}
