package co.edu.unbosque.advancewars.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Datos que envia Angular para crear una partida. */
public class CreateGameDTO {

	@NotNull
	@Size(min = 2, max = 4)
	private List<String> playerNames;
	@Min(5)
	@Max(50)
	private int width = 20;
	@Min(5)
	@Max(50)
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
