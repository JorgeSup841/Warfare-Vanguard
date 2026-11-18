package co.edu.unbosque.advancewars.dto;

public record AuthResponseDTO(String accessToken, String tokenType, UserResponseDTO user) {}
