package co.edu.unbosque.advancewars.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Controla intentos fallidos de autenticación durante una ventana temporal.
 */
@Component
public class LoginRateLimiter {
    private final long windowSeconds;
    private final int maxFailures;
    private final int maxEntries;
    private final ConcurrentHashMap<String, Window> failures = new ConcurrentHashMap<>();

    /**
     * Inicializa los límites de intentos desde las propiedades de la aplicación.
     */
    public LoginRateLimiter(@Value("${app.security.login.window-seconds}") long windowSeconds,
            @Value("${app.security.login.max-failures}") int maxFailures,
            @Value("${app.security.login.max-entries}") int maxEntries) {
        if (windowSeconds < 60 || maxFailures < 1 || maxEntries < 100) {
            throw new IllegalArgumentException("Configuración de limitación de login inválida");
        }
        this.windowSeconds = windowSeconds;
        this.maxFailures = maxFailures;
        this.maxEntries = maxEntries;
    }

    /**
     * Comprueba si la cuenta puede realizar otro intento de autenticación.
     */
    public boolean allowed(String username) {
        Window window = failures.get(username);
        if (window == null) return true;
        if (Instant.now().isAfter(window.startedAt.plusSeconds(windowSeconds))) {
            failures.remove(username, window);
            return true;
        }
        return window.count < maxFailures;
    }

    /**
     * Registra un intento fallido dentro de la ventana temporal.
     */
    public void failed(String username) {
        failures.compute(username, (key, window) -> {
            if (window == null || Instant.now().isAfter(window.startedAt.plusSeconds(windowSeconds))) {
                return new Window(Instant.now(), 1);
            }
            return new Window(window.startedAt, window.count + 1);
        });
        if (failures.size() > maxEntries) {
            failures.entrySet().removeIf(entry -> Instant.now()
                    .isAfter(entry.getValue().startedAt.plusSeconds(windowSeconds)));
        }
    }

    /**
     * Limpia los intentos acumulados después de una autenticación correcta.
     */
    public void succeeded(String username) {
        failures.remove(username);
    }

    /**
     * Conserva el comienzo y el número de intentos de una ventana.
     */
    private record Window(Instant startedAt, int count) {}
}
