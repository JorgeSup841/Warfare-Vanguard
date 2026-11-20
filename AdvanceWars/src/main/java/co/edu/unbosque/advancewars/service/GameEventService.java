package co.edu.unbosque.advancewars.service;

import org.springframework.stereotype.Service;
import co.edu.unbosque.advancewars.enums.GameLogType;
import co.edu.unbosque.advancewars.model.GameLog;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.repository.GameLogRepository;

/** Almacena eventos en la misma transaccion que la accion de juego. */
@Service
public class GameEventService {
	private final GameLogRepository logs;

	public GameEventService(GameLogRepository logs) {
		this.logs = logs;
	}

	public void record(GameState game, Player player, GameLogType type, String description) {
		int round = game.getCurrentRound();
		int order = player == null ? 0 : player.getTurnOrder();
		logs.save(new GameLog(game, player, type, round, order, description));
	}
}
