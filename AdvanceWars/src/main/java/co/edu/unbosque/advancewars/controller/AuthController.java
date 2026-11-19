package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.dto.AuthPendingResponseDTO;
import co.edu.unbosque.advancewars.dto.AuthResponseDTO;
import co.edu.unbosque.advancewars.dto.LoginRequestDTO;
import co.edu.unbosque.advancewars.dto.OtpResendRequestDTO;
import co.edu.unbosque.advancewars.dto.OtpVerificationRequestDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetConfirmDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetRequestDTO;
import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.security.PublicRequestRateLimiter;
import co.edu.unbosque.advancewars.service.AuthService;
import co.edu.unbosque.advancewars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro, verificación de correo, inicio de sesión con OTP y recuperación")
public class AuthController {
    private final AuthService auth;
    private final UserService users;
    private final PublicRequestRateLimiter publicRequestRateLimiter;

    public AuthController(AuthService auth, UserService users,
            PublicRequestRateLimiter publicRequestRateLimiter) {
        this.auth = auth;
        this.users = users;
        this.publicRequestRateLimiter = publicRequestRateLimiter;
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar el usuario autenticado", description = "Requiere JWT Bearer válido")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<UserResponseDTO> me(Principal principal) {
        return ResponseEntity.ok(users.me(principal.getName()));
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar usuario", description = "Crea una cuenta USER deshabilitada y envía OTP de verificación al correo")
    @SecurityRequirements
    public ResponseEntity<AuthPendingResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Valida usuario y contraseña y envía un OTP. Aún no devuelve un JWT")
    @SecurityRequirements
    public ResponseEntity<AuthPendingResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(auth.login(request));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verificar correo electrónico", description = "Valida el código OTP de activación de la cuenta")
    @SecurityRequirements
    public ResponseEntity<Map<String, String>> verifyEmail(@Valid @RequestBody OtpVerificationRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        if (!auth.verifyEmail(request.getChallengeId(), request.getCode())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Correo verificado"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Confirmar segundo factor", description = "Valida el OTP de login y solo entonces entrega el JWT")
    @SecurityRequirements
    public ResponseEntity<?> verifyLoginOtp(@Valid @RequestBody OtpVerificationRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        AuthResponseDTO response = auth.verifyLoginOtp(request.getChallengeId(), request.getCode());
        if (response == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Reenviar código OTP", description = "Emite un nuevo desafío respetando las restricciones de frecuencia")
    @SecurityRequirements
    public ResponseEntity<AuthPendingResponseDTO> resend(@Valid @RequestBody OtpResendRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(auth.resend(request.getChallengeId()));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña", description = "Envía instrucciones de recuperación sin revelar si existe el usuario")
    @SecurityRequirements
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody PasswordResetRequestDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        auth.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Si el correo está registrado, recibirás instrucciones"));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Cambiar contraseña con OTP", description = "Comprueba OTP de recuperación y almacena la contraseña mediante BCrypt")
    @SecurityRequirements
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody PasswordResetConfirmDTO request,
            HttpServletRequest httpRequest) {
        publicRequestRateLimiter.check(httpRequest.getRemoteAddr());
        if (!auth.resetPassword(request)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Código inválido o vencido"));
        }
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada"));
    }
}
