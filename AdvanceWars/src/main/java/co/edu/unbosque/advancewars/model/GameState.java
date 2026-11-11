package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.GameStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "game_state")
public class GameState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;

    @Column(nullable = false)
    private int currentRound;

    @Column(nullable = false)
    private int currentPlayerOrder;

    @Column
    private Integer winnerPlayerOrder;

    private Long seed;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "game_map_id", unique = true)
    private GameMap gameMap;

    @OneToMany(mappedBy = "gameState", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    @Version
    private Long version;

    public GameState() {
        this.status = GameStatus.SETUP;
        this.currentRound = 0;
        this.currentPlayerOrder = 0;
    }

    public GameState(Long seed) {
        this();
        this.seed = seed;
    }

    @PrePersist
    private void initialize() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = GameStatus.SETUP;
        }
    }

    public void addPlayer(Player player) {
        if (!players.contains(player)) {
            players.add(player);
            player.assignGameState(this);
        }
    }

    public void removePlayer(Player player) {
        if (players.remove(player)) {
            player.removeGameState();
        }
    }

    public void startGame(int firstPlayerOrder) {
        status = GameStatus.IN_PROGRESS;
        currentRound = 1;
        currentPlayerOrder = firstPlayerOrder;
        startedAt = LocalDateTime.now();
    }

    public void changeCurrentPlayer(int playerOrder) {
        currentPlayerOrder = playerOrder;
    }

    public void incrementRound() {
        currentRound++;
    }

    public void finishGame(int winnerPlayerOrder) {
        Player winner = findPlayerByOrder(winnerPlayerOrder);

        if (winner != null) {
            winner.declareWinner();
        }

        this.winnerPlayerOrder = winnerPlayerOrder;
        this.status = GameStatus.FINISHED;
        this.finishedAt = LocalDateTime.now();
    }

    public Player findPlayerByOrder(int playerOrder) {
        return players.stream()
                .filter(player -> player.getTurnOrder() == playerOrder)
                .findFirst()
                .orElse(null);
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

    public void setPlayers(List<Player> players) {
        for (Player existing : this.players) {
            existing.removeGameState();
        }

        this.players.clear();

        if (players != null) {
            for (Player player : players) {
                addPlayer(player);
            }
        }
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof GameState other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return GameState.class.hashCode();
    }

    @Override
    public String toString() {
        return "GameState{" +
                "id=" + id +
                ", status=" + status +
                ", currentRound=" + currentRound +
                ", currentPlayerOrder=" + currentPlayerOrder +
                ", winnerPlayerOrder=" + winnerPlayerOrder +
                ", seed=" + seed +
                ", createdAt=" + createdAt +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                '}';
    }
}