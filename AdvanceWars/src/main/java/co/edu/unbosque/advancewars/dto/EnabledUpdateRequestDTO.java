package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Recibe el nuevo estado de habilitación de una cuenta.
 */
public record EnabledUpdateRequestDTO(@NotNull Boolean enabled) {}
