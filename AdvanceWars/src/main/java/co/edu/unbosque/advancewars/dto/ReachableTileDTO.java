package co.edu.unbosque.advancewars.dto;

/** Casilla que el servidor permite alcanzar y costo mínimo de movimiento. */
public record ReachableTileDTO(int x, int y, int cost) {
}
