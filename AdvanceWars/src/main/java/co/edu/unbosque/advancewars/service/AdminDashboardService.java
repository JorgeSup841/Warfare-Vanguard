package co.edu.unbosque.advancewars.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.advancewars.dto.AdminDashboardDTO;
import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.repository.GameStateRepository;
import co.edu.unbosque.advancewars.repository.UserRepository;

/**
 * Construye indicadores de administración sin acceder a repositorios desde el
 * controlador.
 */
@Service
public class AdminDashboardService {
	private final UserRepository users;
	private final GameStateRepository games;
	private final UserSessionService sessions;

	/** Recibe las consultas necesarias para el panel administrativo. */
	public AdminDashboardService(UserRepository users, GameStateRepository games, UserSessionService sessions) {
		this.users = users;
		this.games = games;
		this.sessions = sessions;
	}

	/**
	 * Calcula los indicadores del panel dentro de una operación de solo lectura.
	 */
	@Transactional(readOnly = true)
	public AdminDashboardDTO summary() {
		return new AdminDashboardDTO(users.count(), sessions.countOnlineUsers(), games.count(),
				games.countByStatus(GameStatus.IN_PROGRESS), games.countByStatus(GameStatus.FINISHED));
	}
}
