
package co.edu.unbosque.advancewars.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import co.edu.unbosque.advancewars.enums.GameStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "game_state")
public class GameState {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private GameStatus status = GameStatus.SETUP;

	private int currentRound;
	private int currentPlayerOrder;
	private Integer winnerPlayerOrder;
	private Long seed;

	// Configuracion de la partida.
	@Embedded
	private GameConfiguration configuration;

	// Usuario que creo la partida.
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by")
	private User createdBy;

	// Fechas de la partida.
	private LocalDateTime createdAt;
	private LocalDateTime startedAt;
	private LocalDateTime finishedAt;

	// Mapa de la partida.
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "game_map_id", unique = true)
	private GameMap gameMap;

	// Jugadores de la partida.
	@OneToMany(mappedBy = "gameState", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Player> players = new ArrayList<>();

	// Evita sobrescribir cambios simultaneos.
	@Version
	private Long version;

	// Constructor necesario para JPA.
	public GameState() {
		this.configuration = new GameConfiguration(2, 20, 15);
	}

	// Crea una partida con una semilla.
	public GameState(Long seed) {
		this();
		this.seed = seed;
	}

	// Guarda la fecha al crear la partida.
	@PrePersist
	private void initialize() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
	}

	// Agrega un jugador y lo relaciona con la partida.
	public void addPlayer(Player player) {
		if (player != null && !players.contains(player)) {
			players.add(player);
			player.assignGameState(this);
		}
	}

	// Elimina un jugador de la partida.
	public void removePlayer(Player player) {
		if (players.remove(player)) {
			player.removeGameState();
		}
	}

	// Inicia la partida.
	public void startGame(int firstPlayerOrder) {
		status = GameStatus.IN_PROGRESS;
		currentRound = 1;
		currentPlayerOrder = firstPlayerOrder;
		startedAt = LocalDateTime.now();
	}

	// Cambia el turno al jugador indicado.
	public void changeCurrentPlayer(int playerOrder) {
		currentPlayerOrder = playerOrder;
	}

	// Avanza a la siguiente ronda.
	public void incrementRound() {
		currentRound++;
	}

	// Finaliza la partida y declara un ganador.
	public void finishGame(int winnerPlayerOrder) {
		Player winner = findPlayerByOrder(winnerPlayerOrder);

		if (winner != null) {
			winner.declareWinner();
		}

		this.winnerPlayerOrder = winnerPlayerOrder;
		status = GameStatus.FINISHED;
		finishedAt = LocalDateTime.now();
	}

	// Busca un jugador por su orden de turno.
	public Player findPlayerByOrder(int order) {
		for (Player player : players) {
			if (player.getTurnOrder() == order) {
				return player;
			}
		}
		return null;
	}

	public Long getId() {
		return id;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public int getCurrentPlayerOrder() {
		return currentPlayerOrder;
	}

	public void setCurrentPlayerOrder(int currentPlayerOrder) {
		this.currentPlayerOrder = currentPlayerOrder;
	}

	public Integer getWinnerPlayerOrder() {
		return winnerPlayerOrder;
	}

	public void setWinnerPlayerOrder(Integer winnerPlayerOrder) {
		this.winnerPlayerOrder = winnerPlayerOrder;
	}

	public Long getSeed() {
		return seed;
	}

	public void setSeed(Long seed) {
		this.seed = seed;
	}

	public GameConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(GameConfiguration configuration) {
		this.configuration = configuration;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public LocalDateTime getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(LocalDateTime finishedAt) {
		this.finishedAt = finishedAt;
	}

	public GameMap getGameMap() {
		return gameMap;
	}

	public void setGameMap(GameMap gameMap) {
		this.gameMap = gameMap;
	}

	public List<Player> getPlayers() {
		return Collections.unmodifiableList(players);
	}

	// Reemplaza los jugadores manteniendo sus relaciones.
	public void setPlayers(List<Player> newPlayers) {
		List<Player> copy = newPlayers == null ? new ArrayList<>() : new ArrayList<>(newPlayers);

		for (Player player : players) {
			player.removeGameState();
		}

		players.clear();

		for (Player player : copy) {
			addPlayer(player);
		}
	}

	public Long getVersion() {
		return version;
	}
}
