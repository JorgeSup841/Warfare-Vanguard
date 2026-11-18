package co.edu.unbosque.advancewars.dto;

import java.time.Instant;

public record ApiErrorDTO(Instant timestamp, int status, String error, String message, String path) {}
