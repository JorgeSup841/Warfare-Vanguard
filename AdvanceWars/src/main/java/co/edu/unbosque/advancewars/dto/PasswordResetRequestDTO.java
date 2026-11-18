package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicita recuperar el acceso mediante una dirección de correo.
 */
public record PasswordResetRequestDTO(@NotBlank @Email String email) {}
