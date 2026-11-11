package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.PlayerStatus;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "game_player",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_player_game_order",
            columnNames = {"game_state_id", "turn_order"}
        ),
        @UniqueConstraint(
            name = "uk_player_game_name",
            columnNames = {"game_state_id", "player_name"}
        )
    }
)
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_name", nullable = false, length = 40)
    private String name;

    @Column(name = "turn_order", nullable = false)
    private int turnOrder;

    @Column(nullable = false)
    private int money;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlayerStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_state_id", nullable = false)
    private GameState gameState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "faction_id")
    private Faction faction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commander_id")
    private Commander commander;

    protected Player() {
    }

    public Player(String name, int turnOrder, int money) {
        this.name = name;
        this.turnOrder = turnOrder;
        this.money = money;
        this.status = PlayerStatus.ACTIVE;
    }

    void assignGameState(GameState gameState) {
        this.gameState = gameState;
    }

    void removeGameState() {
        this.gameState = null;
    }

    public void assignSelection(Faction faction, Commander commander) {
        this.faction = faction;
        this.commander = commander;
    }

    public void clearSelection() {
        this.faction = null;
        this.commander = null;
    }

    public void eliminate() {
        this.status = PlayerStatus.ELIMINATED;
    }

    public void addMoney(int amount) {
        this.money += amount;
    }

    public void subtractMoney(int amount) {
        this.money -= amount;
    }

    void declareWinner() {
        this.status = PlayerStatus.WINNER;
    }

    public boolean isActive() {
        return status == PlayerStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTurnOrder() {
        return turnOrder;
    }

    public void setTurnOrder(int turnOrder) {
        this.turnOrder = turnOrder;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public PlayerStatus getStatus() {
        return status;
    }

    public void setStatus(PlayerStatus status) {
        this.status = status;
    }

    public GameState getGameState() {
        return gameState;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public Commander getCommander() {
        return commander;
    }

    public void setCommander(Commander commander) {
        this.commander = commander;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Player other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Player.class.hashCode();
    }

    @Override
    public String toString() {
        return "Player{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", turnOrder=" + turnOrder +
            ", money=" + money +
            ", status=" + status +
            '}';
    }
}
