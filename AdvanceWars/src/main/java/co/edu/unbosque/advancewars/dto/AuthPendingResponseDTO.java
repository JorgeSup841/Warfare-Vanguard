package co.edu.unbosque.advancewars.dto;

import java.time.LocalDateTime;

public record AuthPendingResponseDTO(String status, String challengeId, LocalDateTime expiresAt) {}
