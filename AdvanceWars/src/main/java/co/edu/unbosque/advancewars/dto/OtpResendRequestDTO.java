package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Identifica el desafío cuyo código debe reenviarse.
 */
public record OtpResendRequestDTO(@NotBlank String challengeId) {}
