package co.edu.unbosque.advancewars.dto;

import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;

/** Datos de una partida para mostrar en Angular y Swagger. */
public class GameDTO {

	private Long id;
	private String status;
	private int playerCount;
	private int width;
	private int height;
	private Long seed;
	private int currentRound;
	private int currentPlayerOrder;
	private Integer winnerPlayerOrder;
	private List<String> playerNames = new ArrayList<>();
	private List<PlayerStateDTO> players = new ArrayList<>();

	public GameDTO(GameState game) {
		id = game.getId();
		status = game.getStatus().name();
		playerCount = game.getConfiguration().getPlayerCount();
		width = game.getConfiguration().getWidth();
		height = game.getConfiguration().getHeight();
		seed = game.getSeed();
		currentRound = game.getCurrentRound();
		currentPlayerOrder = game.getCurrentPlayerOrder();
		winnerPlayerOrder = game.getWinnerPlayerOrder();

		for (Player player : game.getPlayers()) {
			playerNames.add(player.getName());
			players.add(new PlayerStateDTO(player));
		}
	}

	public Long getId() {
		return id;
	}

	public String getStatus() {
		return status;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Long getSeed() {
		return seed;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public int getCurrentPlayerOrder() {
		return currentPlayerOrder;
	}

	public Integer getWinnerPlayerOrder() {
		return winnerPlayerOrder;
	}

	public List<String> getPlayerNames() {
		return playerNames;
	}

	public List<PlayerStateDTO> getPlayers() {
		return players;
	}
}
