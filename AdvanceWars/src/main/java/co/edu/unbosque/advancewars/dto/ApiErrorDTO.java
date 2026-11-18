package co.edu.unbosque.advancewars.dto;

import java.time.Instant;

/**
 * Representa el cuerpo público y uniforme de una respuesta de error.
 */
public record ApiErrorDTO(Instant timestamp, int status, String error, String message, String path) {}
