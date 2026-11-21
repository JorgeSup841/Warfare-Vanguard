package co.edu.unbosque.advancewars.controller;

import java.security.Principal;
import java.util.List;
import co.edu.unbosque.advancewars.dto.AdminDashboardDTO;
import co.edu.unbosque.advancewars.dto.AdminGameDTO;
import co.edu.unbosque.advancewars.dto.AdminGameLogDTO;
import co.edu.unbosque.advancewars.dto.AuthPendingResponseDTO;
import co.edu.unbosque.advancewars.dto.EnabledUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.dto.RegisterRequestDTO;
import co.edu.unbosque.advancewars.dto.RoleUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.UnitDTO;
import co.edu.unbosque.advancewars.dto.UserActivityDTO;
import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.dto.UserSessionDTO;
import co.edu.unbosque.advancewars.service.AdminGameService;
import co.edu.unbosque.advancewars.service.AdminDashboardService;
import co.edu.unbosque.advancewars.service.AuthenticatedUserService;
import co.edu.unbosque.advancewars.service.AuthService;
import co.edu.unbosque.advancewars.service.UserService;
import co.edu.unbosque.advancewars.service.UserSessionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Administracion central: solo ADMIN, controlado por Spring Security. */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administracion")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
	private final UserService userService;
	private final AuthService auth;
	private final AdminGameService games;
	private final UserSessionService sessions;
	private final AdminDashboardService dashboardService;
	private final AuthenticatedUserService authenticatedUsers;

	/** Inyecta los servicios de usuarios, partidas, sesiones y autorización. */
	public AdminController(UserService userService, AuthService auth, AdminGameService games,
			UserSessionService sessions, AdminDashboardService dashboardService,
			AuthenticatedUserService authenticatedUsers) {
		this.userService = userService;
		this.auth = auth;
		this.games = games;
		this.sessions = sessions;
		this.dashboardService = dashboardService;
		this.authenticatedUsers = authenticatedUsers;
	}

	/** Resume indicadores administrativos de la aplicación. */
	@GetMapping("/dashboard")
	public AdminDashboardDTO dashboard() {
		return dashboardService.summary();
	}

	/** Lista los usuarios del sistema para administración. */
	@GetMapping("/users")
	public List<UserResponseDTO> users() {
		return userService.list();
	}

	/** Consulta una cuenta por identificador. */
	@GetMapping("/users/{id}")
	public UserResponseDTO user(@PathVariable Long id) {
		return userService.find(id);
	}

	/** Reutiliza el registro con email + OTP, siempre crea USER. */
	@PostMapping("/users")
	public ResponseEntity<AuthPendingResponseDTO> createUser(@Valid @RequestBody RegisterRequestDTO request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request));
	}

	/** Actualiza el rol de una cuenta autorizada. */
	@PatchMapping("/users/{id}/role")
	public UserResponseDTO role(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequestDTO request,
			Principal principal) {
		return userService.setRole(id, request.getRole(), principal.getName());
	}

	/** Cambia el estado de habilitación de una cuenta. */
	@PatchMapping("/users/{id}/enabled")
	public UserResponseDTO enabled(@PathVariable Long id, @Valid @RequestBody EnabledUpdateRequestDTO request,
			Principal principal) {
		return userService.setEnabled(id, request.getEnabled(), principal.getName());
	}

	/** Lista las partidas del sistema para administración. */
	@GetMapping("/games")
	public List<AdminGameDTO> games() {
		return games.list();
	}

	/** Consulta el detalle administrativo de una partida. */
	@GetMapping("/games/{id}")
	public AdminGameDTO game(@PathVariable Long id) {
		return games.find(id);
	}

	/** Consulta el mapa de una partida. */
	@GetMapping("/games/{id}/map")
	public MapDTO map(@PathVariable Long id) {
		return games.map(id);
	}

	/** Consulta las unidades presentes en una partida. */
	@GetMapping("/games/{id}/units")
	public List<UnitDTO> units(@PathVariable Long id) {
		return games.units(id);
	}

	/** Consulta los eventos de una partida. */
	@GetMapping("/games/{id}/history")
	public List<AdminGameLogDTO> history(@PathVariable Long id) {
		return games.history(id);
	}

	/** Elimina una partida mediante el servicio administrativo. */
	@DeleteMapping("/games/{id}")
	public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
		games.delete(id);
		return ResponseEntity.noContent().build();
	}

	/** Lista las sesiones registradas en el sistema. */
	@GetMapping("/sessions")
	public List<UserSessionDTO> sessions() {
		return sessions.getSessions(false);
	}

	/** Lista únicamente las sesiones activas. */
	@GetMapping("/sessions/active")
	public List<UserSessionDTO> online() {
		return sessions.getSessions(true);
	}

	/** Revoca una sesión, validando al administrador autenticado. */
	@DeleteMapping("/sessions/{id}")
	public ResponseEntity<Void> revoke(@PathVariable String id, Principal principal) {
		sessions.revoke(authenticatedUsers.requireUser(principal), id);
		return ResponseEntity.noContent().build();
	}

	/** Consulta la actividad general de usuarios. */
	@GetMapping("/activity")
	public List<UserActivityDTO> activity() {
		return sessions.getActivity(null);
	}

	/** Consulta la actividad de una cuenta existente. */
	@GetMapping("/users/{id}/activity")
	public List<UserActivityDTO> userActivity(@PathVariable Long id) {
		userService.find(id);
		return sessions.getActivity(id);
	}
}
