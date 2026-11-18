package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.Role;
import jakarta.validation.constraints.NotNull;

/**
 * Recibe el rol autorizado para actualizar una cuenta.
 */
public record RoleUpdateRequestDTO(@NotNull Role role) {}
