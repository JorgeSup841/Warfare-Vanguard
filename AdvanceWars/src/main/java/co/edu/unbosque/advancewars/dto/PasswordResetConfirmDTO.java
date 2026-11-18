package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Recibe el desafío, el código y la contraseña nueva.
 */
public record PasswordResetConfirmDTO(@NotBlank String challengeId,
        @NotBlank @Pattern(regexp = "[0-9]{6}") String code,
        @NotBlank @Size(min = 12, max = 64) String newPassword) {}
