package co.edu.unbosque.advancewars.security;

import co.edu.unbosque.advancewars.service.OtpService;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Limita las solicitudes a los endpoints públicos de seguridad por dirección de origen. */
@Component
public class PublicRequestRateLimiter {
    private final long windowSeconds;
    private final int maxRequests;
    private final int maxEntries;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    /** Configura la ventana de solicitudes por dirección de origen. */
    public PublicRequestRateLimiter(
            @Value("${app.security.public.window-seconds}") long windowSeconds,
            @Value("${app.security.public.max-requests}") int maxRequests,
            @Value("${app.security.public.max-entries}") int maxEntries) {
        if (windowSeconds < 60 || maxRequests < 1 || maxEntries < 100) {
            throw new IllegalArgumentException("Configuración de protección pública inválida");
        }
        this.windowSeconds = windowSeconds;
        this.maxRequests = maxRequests;
        this.maxEntries = maxEntries;
    }

    /** Rechaza solicitudes que superan el límite configurado. */
    public void check(String remoteAddress) {
        Instant now = Instant.now();
        String key = remoteAddress == null ? "unknown" : remoteAddress;
        if (windows.size() >= maxEntries && !windows.containsKey(key)) {
            windows.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().startedAt.plusSeconds(windowSeconds)));
            if (windows.size() >= maxEntries) {
                throw new OtpService.OtpLimitException("Demasiadas solicitudes. Intenta más tarde");
            }
        }
        Window window = windows.compute(key, (address, previous) -> {
            if (previous == null || !now.isBefore(previous.startedAt.plusSeconds(windowSeconds))) {
                return new Window(now, 1);
            }
            return new Window(previous.startedAt, previous.count + 1);
        });
        if (window.count > maxRequests) {
            throw new OtpService.OtpLimitException("Demasiadas solicitudes. Intenta más tarde");
        }
    }

    /** Representa las solicitudes contadas en una ventana temporal. */
    private record Window(Instant startedAt, int count) {}
}
