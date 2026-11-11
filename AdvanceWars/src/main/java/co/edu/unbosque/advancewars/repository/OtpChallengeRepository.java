package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.OtpPurpose;
import co.edu.unbosque.advancewars.model.OtpChallenge;
import co.edu.unbosque.advancewars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, Long> {

    Optional<OtpChallenge> findByChallengeId(String challengeId);

    List<OtpChallenge> findAllByUserAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            User user, OtpPurpose purpose);

    long countByUserAndPurposeAndCreatedAtAfter(
            User user, OtpPurpose purpose, LocalDateTime createdAt);

    long deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
