package co.edu.unbosque.advancewars.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import co.edu.unbosque.advancewars.dto.GameLogDTO;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.GameLogRepository;
import co.edu.unbosque.advancewars.repository.GameStateRepository;

@Service
public class GameHistoryService {
	private final GameStateRepository games;
	private final GameLogRepository logs;

	public GameHistoryService(GameStateRepository games, GameLogRepository logs) {
		this.games = games;
		this.logs = logs;
	}

	@Transactional(readOnly = true)
	public List<GameLogDTO> getHistory(Long gameId, User user) {
		if (user == null || user.getId() == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debes iniciar sesion");
		}
		if (games.findByIdAndCreatedBy_Id(gameId, user.getId()).isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada");
		}
		return logs.findAllByGameState_IdOrderByCreatedAtAscIdAsc(gameId).stream().map(GameLogDTO::new).toList();
	}
}
