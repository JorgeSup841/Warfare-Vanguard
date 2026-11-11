package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import co.edu.unbosque.advancewars.model.OtpChallenge;
import co.edu.unbosque.advancewars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para desafíos OTP. */
public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    /** Busca un desafío OTP mediante su identificador público. */
    Optional<OtpChallenge> findByChallengeId(String challengeId);

    /** Lista los desafíos OTP sin consumir de un usuario y propósito, del más reciente al más antiguo. */
    List<OtpChallenge> findAllByUserAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            User user, OtpPurpose purpose);

    /** Cuenta los desafíos OTP creados después de la fecha indicada para un usuario y propósito. */
    long countByUserAndPurposeAndCreatedAtAfter(
            User user, OtpPurpose purpose, LocalDateTime createdAt);

    /** Elimina los desafíos OTP cuya fecha de expiración sea anterior a la indicada. */
    long deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
