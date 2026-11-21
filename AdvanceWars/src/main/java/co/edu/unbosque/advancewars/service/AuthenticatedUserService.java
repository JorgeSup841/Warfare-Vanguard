package co.edu.unbosque.advancewars.service;

import java.security.Principal;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;

/** Resuelve la cuenta autenticada sin exponer entidades JPA en firmas REST. */
@Service
public class AuthenticatedUserService {

	private final UserRepository users;

	/** Recibe el repositorio mediante inyección por constructor. */
	public AuthenticatedUserService(UserRepository users) {
		this.users = users;
	}

	/**
	 * Obtiene del almacenamiento la cuenta del JWT validado por Spring Security.
	 */
	@Transactional(readOnly = true)
	public User requireUser(Principal principal) {
		if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión");
		}
		return users.findByUsername(principal.getName())
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión no válida"));
	}
}
