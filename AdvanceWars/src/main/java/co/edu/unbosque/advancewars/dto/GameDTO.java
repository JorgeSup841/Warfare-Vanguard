package co.edu.unbosque.advancewars.dto;

import java.util.ArrayList;
import java.util.List;

import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;

/** Datos seguros y sencillos de una partida para enviar a Angular. */
public class GameDTO {

	private Long id;
	private String status;
	private int playerCount;
	private int width;
	private int height;
	private Long seed;
	private List<String> playerNames;

	/** Convierte la partida guardada en un objeto facil de enviar como JSON. */
	public GameDTO(GameState game) {
		this.id = game.getId();
		this.status = game.getStatus().name();
		this.playerCount = game.getConfiguration().getPlayerCount();
		this.width = game.getConfiguration().getWidth();
		this.height = game.getConfiguration().getHeight();
		this.seed = game.getSeed();
		this.playerNames = new ArrayList<>();

		for (Player player : game.getPlayers()) {
			playerNames.add(player.getName());
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

	public List<String> getPlayerNames() {
		return playerNames;
	}
}
