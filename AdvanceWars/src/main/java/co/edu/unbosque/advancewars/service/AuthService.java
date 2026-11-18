package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.dto.AuthPendingResponseDTO;
import co.edu.unbosque.advancewars.dto.AuthResponseDTO;
import co.edu.unbosque.advancewars.dto.LoginRequestDTO;
import co.edu.unbosque.advancewars.dto.PasswordResetConfirmDTO;
import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.enums.OtpPurpose;
import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.exception.BadRequestException;
import co.edu.unbosque.advancewars.exception.ConflictException;
import co.edu.unbosque.advancewars.model.OtpChallenge;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import co.edu.unbosque.advancewars.security.JwtUtil;
import co.edu.unbosque.advancewars.security.LoginRateLimiter;
import java.util.Locale;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwt;
    private final OtpService otp;
    private final EmailService email;
    private final UserService userService;
    private final LoginRateLimiter limiter;

    public AuthService(UserRepository users, PasswordEncoder passwords,
            AuthenticationManager authenticationManager, JwtUtil jwt, OtpService otp,
            EmailService email, UserService userService, LoginRateLimiter limiter) {
        this.users = users;
        this.passwords = passwords;
        this.authenticationManager = authenticationManager;
        this.jwt = jwt;
        this.otp = otp;
        this.email = email;
        this.userService = userService;
        this.limiter = limiter;
    }

    @Transactional
    public AuthPendingResponseDTO register(RegisterRequestDTO request) {
        String username = normalize(request.username());
        String address = normalize(request.email());
        if (username.length() < 3 || address.length() > 254 || username.length() > 50) {
            throw new BadRequestException("Nombre o correo inválido");
        }
        if (users.existsByUsername(username) || users.existsByEmailIgnoreCase(address)) {
            throw new ConflictException("Nombre de usuario o correo no disponible");
        }
        User user = users.save(new User(username, passwords.encode(request.password()), address, Role.USER));
        OtpService.IssuedOtp issued = otp.issue(user, OtpPurpose.EMAIL_VERIFICATION);
        email.sendOtp(issued);
        return pending("EMAIL_VERIFICATION_REQUIRED", issued);
    }

    @Transactional
    public AuthPendingResponseDTO login(LoginRequestDTO request) {
        String username = normalize(request.username());
        if (!limiter.allowed(username)) throw new OtpService.OtpLimitException("Demasiados intentos");
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username, request.password()));
        } catch (AuthenticationException exception) {
            limiter.failed(username);
            throw new co.edu.unbosque.advancewars.exception.AuthenticationFailedException("Credenciales inválidas o cuenta no verificada");
        }
        limiter.succeeded(username);
        User user = users.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("Credenciales inválidas"));
        if (!user.isEmailVerified() || !user.isEnabled()) {
            throw new co.edu.unbosque.advancewars.exception.AuthenticationFailedException("Credenciales inválidas o cuenta no verificada");
        }
        OtpService.IssuedOtp issued = otp.issue(user, OtpPurpose.LOGIN_2FA);
        email.sendOtp(issued);
        return pending("LOGIN_OTP_REQUIRED", issued);
    }

    @Transactional
    public boolean verifyEmail(String challengeId, String code) {
        OtpService.CheckResult checked = otp.verify(challengeId, code, OtpPurpose.EMAIL_VERIFICATION);
        if (!checked.valid()) return false;
        checked.user().verifyEmail();
        return true;
    }

    @Transactional
    public AuthResponseDTO verifyLoginOtp(String challengeId, String code) {
        OtpService.CheckResult checked = otp.verify(challengeId, code, OtpPurpose.LOGIN_2FA);
        if (!checked.valid()) return null;
        User user = checked.user();
        if (!user.isEnabled() || !user.isEmailVerified() || !user.isAccountNonLocked()
                || !user.isAccountNonExpired() || !user.isCredentialsNonExpired()) {
            return null;
        }
        return new AuthResponseDTO(jwt.generateToken(user), "Bearer", userService.toDto(user));
    }

    @Transactional
    public boolean resetPassword(PasswordResetConfirmDTO request) {
        OtpService.CheckResult checked = otp.verify(request.challengeId(), request.code(), OtpPurpose.PASSWORD_RESET);
        if (!checked.valid()) return false;
        checked.user().setPassword(passwords.encode(request.newPassword()));
        return true;
    }

    @Transactional(noRollbackFor = {OtpService.OtpLimitException.class, MailException.class})
    public void forgotPassword(String emailAddress) {
        users.findByEmail(normalize(emailAddress)).ifPresent(user -> {
            if (!user.isEmailVerified()) return;
            try {
                OtpService.IssuedOtp issued = otp.issue(user, OtpPurpose.PASSWORD_RESET);
                email.sendOtp(issued);
            } catch (OtpService.OtpLimitException | MailException ignored) {
                return;
            }
        });
    }

    @Transactional
    public AuthPendingResponseDTO resend(String challengeId) {
        OtpChallenge previous = otp.getChallenge(challengeId);
        if (previous == null || previous.isConsumed()) {
            throw new BadRequestException("Desafío inválido o vencido");
        }
        User user = previous.getUser();
        if (previous.getPurpose() == OtpPurpose.EMAIL_VERIFICATION && user.isEmailVerified()) {
            throw new BadRequestException("Cuenta ya verificada");
        }
        OtpService.IssuedOtp issued = otp.issue(user, previous.getPurpose());
        email.sendOtp(issued);
        return pending("OTP_SENT", issued);
    }

    private AuthPendingResponseDTO pending(String status, OtpService.IssuedOtp issued) {
        return new AuthPendingResponseDTO(status, issued.challengeId(), issued.expiresAt());
    }

    private String normalize(String value) {
        return value.strip().toLowerCase(Locale.ROOT);
    }
}
