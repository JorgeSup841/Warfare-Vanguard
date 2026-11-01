package co.edu.unbosque.advancewars.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "game_log")
public class GameLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_state_id", nullable = false)
    private GameState gameState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private GameLogType eventType;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "turn_order", nullable = false)
    private int turnOrder;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected GameLog() {
    }

    public GameLog(
        GameState gameState,
        Player player,
        GameLogType eventType,
        int roundNumber,
        int turnOrder,
        String description
    ) {
        this.gameState = gameState;
        this.player = player;
        this.eventType = eventType;
        this.roundNumber = roundNumber;
        this.turnOrder = turnOrder;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public GameLogType getEventType() {
        return eventType;
    }

    public void setEventType(GameLogType eventType) {
        this.eventType = eventType;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        this.roundNumber = roundNumber;
    }

    public int getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(int turnOrder) {
        this.turnOrder = turnOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof GameLog other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return GameLog.class.hashCode();
    }

    @Override
    public String toString() {
        return "GameLog{" +
            "id=" + id +
            ", eventType=" + eventType +
            ", roundNumber=" + roundNumber +
            ", turnOrder=" + turnOrder +
            ", description='" + description + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}
