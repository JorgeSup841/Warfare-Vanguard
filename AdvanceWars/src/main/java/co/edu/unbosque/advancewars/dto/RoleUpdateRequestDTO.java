package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequestDTO(@NotNull Role role) {}
