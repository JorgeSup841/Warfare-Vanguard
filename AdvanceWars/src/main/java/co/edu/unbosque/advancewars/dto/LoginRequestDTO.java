package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Recibe las credenciales iniciales del inicio de sesión.
 */
public record LoginRequestDTO(@NotBlank String username, @NotBlank String password) {}
