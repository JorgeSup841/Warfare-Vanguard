package co.edu.unbosque.advancewars.dto;

import co.edu.unbosque.advancewars.enums.Role;

/**
 * Representa los datos públicos de un usuario sin exponer credenciales.
 */
public record UserResponseDTO(Long id, String username, String email, Role role,
                              boolean emailVerified, boolean enabled) {}
