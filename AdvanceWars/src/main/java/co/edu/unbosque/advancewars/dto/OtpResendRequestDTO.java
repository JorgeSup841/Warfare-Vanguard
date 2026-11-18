package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;

public record OtpResendRequestDTO(@NotBlank String challengeId) {}
