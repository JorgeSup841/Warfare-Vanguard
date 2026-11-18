package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;

/**
 * Indica una autenticación pendiente de verificación por código OTP.
 */
public record AuthPendingResponseDTO(String status, String challengeId, LocalDateTime expiresAt) {}
