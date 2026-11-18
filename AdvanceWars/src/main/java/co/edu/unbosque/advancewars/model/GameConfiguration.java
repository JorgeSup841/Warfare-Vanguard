package co.edu.unbosque.advancewars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Guarda los datos basicos de configuracion de una partida. */
@Embeddable
public class GameConfiguration {

	@Column(name = "player_count")
	private int playerCount;

	@Column(name = "map_width")
	private int width;

	@Column(name = "map_height")
	private int height;

	// Constructor vacio requerido para guardar la configuracion con JPA.
	protected GameConfiguration() {
	}

	/** Crea la configuracion con el numero de jugadores y tamano del mapa. */
	public GameConfiguration(int playerCount, int width, int height) {
		this.playerCount = playerCount;
		this.width = width;
		this.height = height;
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
}
