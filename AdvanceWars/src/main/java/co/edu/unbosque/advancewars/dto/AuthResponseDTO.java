package co.edu.unbosque.advancewars.dto;

/**
 * Devuelve un token de acceso y los datos públicos de la cuenta.
 */
public record AuthResponseDTO(String accessToken, String tokenType, UserResponseDTO user) {}
