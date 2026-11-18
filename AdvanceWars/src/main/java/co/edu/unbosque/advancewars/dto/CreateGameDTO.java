package co.edu.unbosque.advancewars.dto;

import java.util.List;

/** Datos que envia Angular para crear una partida. */
public class CreateGameDTO {

	private List<String> playerNames;
	private int width = 20;
	private int height = 15;
	private Long seed;

	public List<String> getPlayerNames() {
		return playerNames;
	}

	public void setPlayerNames(List<String> playerNames) {
		this.playerNames = playerNames;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Long getSeed() {
		return seed;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
	}
}
