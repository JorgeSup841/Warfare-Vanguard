package co.edu.unbosque.advancewars.controller;

import java.util.Map;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.security.JwtUtil;
import co.edu.unbosque.advancewars.service.UserSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

	public SessionController(UserSessionService sessions, JwtUtil jwt) {
		this.sessions = sessions;
		this.jwt = jwt;
	}

	@PostMapping("/heartbeat")
	public Map<String, String> heartbeat(@AuthenticationPrincipal User user,
			@RequestHeader("Authorization") String authorization) {
		sessions.touch(user, jwt.parse(authorization.substring(7)).getId());
		return Map.of("status", "OK");
	}

	@PostMapping("/logout")
	public Map<String, String> logout(@AuthenticationPrincipal User user,
			@RequestHeader("Authorization") String authorization) {
		sessions.logout(user, jwt.parse(authorization.substring(7)).getId());
		return Map.of("status", "LOGGED_OUT");
	}
}
