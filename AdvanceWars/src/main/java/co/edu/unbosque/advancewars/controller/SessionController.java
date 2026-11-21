package co.edu.unbosque.advancewars.controller;

import java.security.Principal;
import java.util.Map;
import co.edu.unbosque.advancewars.security.JwtUtil;
import co.edu.unbosque.advancewars.service.AuthenticatedUserService;
import co.edu.unbosque.advancewars.service.UserSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Solicitudes sencillas de presencia y cierre de sesion para Angular. */
@RestController
@RequestMapping("/api/sessions")
@Tag(name = "Sesiones")
@SecurityRequirement(name = "bearerAuth")
public class SessionController {
	private final UserSessionService sessions;
	private final JwtUtil jwt;
	private final AuthenticatedUserService authenticatedUsers;

	/** Inyecta el servicio de sesiones y la consulta de identidad autenticada. */
	public SessionController(UserSessionService sessions, JwtUtil jwt, AuthenticatedUserService authenticatedUsers) {
		this.sessions = sessions;
		this.jwt = jwt;
		this.authenticatedUsers = authenticatedUsers;
	}

	/** Registra la actividad reciente de una sesión JWT autorizada. */
	@PostMapping("/heartbeat")
	public Map<String, String> heartbeat(Principal principal,
			@RequestHeader("Authorization") String authorization) {
		sessions.touch(authenticatedUsers.requireUser(principal), jwt.parse(bearerToken(authorization)).getId());
		return Map.of("status", "OK");
	}

	/** Cierra únicamente la sesión representada por el token recibido. */
	@PostMapping("/logout")
	public Map<String, String> logout(Principal principal,
			@RequestHeader("Authorization") String authorization) {
		sessions.logout(authenticatedUsers.requireUser(principal), jwt.parse(bearerToken(authorization)).getId());
		return Map.of("status", "LOGGED_OUT");
	}
    /** Valida el formato Bearer antes de extraer el token firmado. */
    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() <= 7) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Token Bearer requerido");
        }
        return authorization.substring(7);
    }
}
