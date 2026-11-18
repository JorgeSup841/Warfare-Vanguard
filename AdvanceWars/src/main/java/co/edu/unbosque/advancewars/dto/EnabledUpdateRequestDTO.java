package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.NotNull;

public record EnabledUpdateRequestDTO(@NotNull Boolean enabled) {}
