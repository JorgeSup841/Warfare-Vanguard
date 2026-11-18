package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import co.edu.unbosque.advancewars.model.OtpChallenge;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.OtpChallengeRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona emisión, caducidad, intentos y consumo de desafíos OTP.
 */
@Service
public class OtpService {
    private final OtpChallengeRepository repository;
    private final OtpHashService hashService;
    private final long ttlSeconds;
    private final long resendCooldownSeconds;
    private final long issuanceWindowSeconds;
    private final int maxIssuedPerWindow;
    private final int maxAttempts;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Configura expiración, frecuencia y límites de intentos OTP.
     */
    public OtpService(OtpChallengeRepository repository, OtpHashService hashService,
            @Value("${app.otp.ttl-seconds}") long ttlSeconds,
            @Value("${app.otp.resend-cooldown-seconds}") long resendCooldownSeconds,
            @Value("${app.otp.issue-window-seconds}") long issuanceWindowSeconds,
            @Value("${app.otp.max-issued-per-window}") int maxIssuedPerWindow,
            @Value("${app.otp.max-attempts}") int maxAttempts) {
        if (ttlSeconds < 60 || ttlSeconds > 900 || resendCooldownSeconds < 10
                || issuanceWindowSeconds <= resendCooldownSeconds || maxIssuedPerWindow < 1
                || maxAttempts < 1 || maxAttempts > 10) {
            throw new IllegalArgumentException("Configuración OTP inválida");
        }
        this.repository = repository;
        this.hashService = hashService;
        this.ttlSeconds = ttlSeconds;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.issuanceWindowSeconds = issuanceWindowSeconds;
        this.maxIssuedPerWindow = maxIssuedPerWindow;
        this.maxAttempts = maxAttempts;
    }

    /**
     * Crea un desafío de uso único, guarda su huella y devuelve sus datos de envío.
     */
    @Transactional(noRollbackFor = OtpLimitException.class)
    public IssuedOtp issue(User user, OtpPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();
        List<OtpChallenge> active = repository
                .findAllByUserAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(user, purpose);
        if (!active.isEmpty() && active.get(0).getCreatedAt()
                .isAfter(now.minusSeconds(resendCooldownSeconds))) {
            throw new OtpLimitException("Espera antes de solicitar otro código");
        }
        if (repository.countByUserAndPurposeAndCreatedAtAfter(user, purpose,
                now.minusSeconds(issuanceWindowSeconds)) >= maxIssuedPerWindow) {
            throw new OtpLimitException("Demasiados códigos solicitados. Intenta más tarde");
        }
        for (OtpChallenge previous : active) {
            previous.consume();
        }
        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        OtpChallenge challenge = new OtpChallenge(user, "pending", purpose,
                now.plusSeconds(ttlSeconds));
        challenge.setMaxAttempts(maxAttempts);
        challenge.setCodeHash(hashService.hash(challenge.getChallengeId(), purpose, code));
        repository.save(challenge);
        return new IssuedOtp(challenge.getChallengeId(), code, challenge.getExpiresAt(),
                user.getEmail(), purpose);
    }

    /**
     * Comprueba el propósito, expiración, intentos y huella del OTP antes de consumirlo.
     */
    @Transactional
    public CheckResult verify(String challengeId, String code, OtpPurpose expectedPurpose) {
        OtpChallenge challenge = repository.findByChallengeId(challengeId).orElse(null);
        if (challenge == null || challenge.getPurpose() != expectedPurpose) return CheckResult.invalid();
        LocalDateTime now = LocalDateTime.now();
        if (challenge.isConsumed() || !challenge.getExpiresAt().isAfter(now)
                || challenge.getAttempts() >= challenge.getMaxAttempts()) {
            return CheckResult.invalid();
        }
        if (!hashService.matches(challenge.getChallengeId(), expectedPurpose,
                code, challenge.getCodeHash())) {
            challenge.registerFailedAttempt();
            repository.save(challenge);
            return CheckResult.invalid();
        }
        challenge.consume();
        repository.save(challenge);
        return CheckResult.success(challenge.getUser());
    }

    /**
     * Busca un desafío existente por su identificador.
     */
    @Transactional(readOnly = true)
    public OtpChallenge getChallenge(String challengeId) {
        return repository.findByChallengeId(challengeId).orElse(null);
    }

    /**
     * Agrupa los datos de un código OTP emitido para su envío por correo.
     */
    public record IssuedOtp(String challengeId, String code, LocalDateTime expiresAt,
                            String email, OtpPurpose purpose) {}
    /**
     * Expresa el resultado de la comprobación de un desafío OTP.
     */
    public static final class CheckResult {
        private final boolean valid;
        private final User user;

        /**
         * Crea un resultado inmutable con su estado y el usuario asociado.
         */
        private CheckResult(boolean valid, User user) {
            this.valid = valid;
            this.user = user;
        }

        /**
         * Indica si el desafío OTP fue verificado correctamente.
         */
        public boolean valid() {
            return valid;
        }

        /**
         * Devuelve el usuario verificado o null si no fue válido.
         */
        public User user() {
            return user;
        }

        /**
         * Crea el resultado correspondiente a un código OTP válido.
         */
        public static CheckResult success(User user) {
            return new CheckResult(true, java.util.Objects.requireNonNull(user));
        }

        /**
         * Crea el resultado correspondiente a un código OTP inválido.
         */
        public static CheckResult invalid() {
            return new CheckResult(false, null);
        }
    }
    /**
     * Indica que se superó un límite de emisión de códigos OTP.
     */
    public static class OtpLimitException extends RuntimeException {
        /**
         * Crea un error indicando que se superó un límite de emisión OTP.
         */
        public OtpLimitException(String message) { super(message); }
    }
}
