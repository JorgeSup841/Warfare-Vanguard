
package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.PlayerStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "game_player")
public class Player {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	private String name;
	private int turnOrder;
	private int money;
	private int powerCharge;
	private boolean powerActive;

	@Enumerated(EnumType.STRING)
	private PlayerStatus status;

	// Partida a la que pertenece el jugador.
	@ManyToOne
	@JoinColumn(name = "game_state_id")
	private GameState gameState;

	// Faccion seleccionada por el jugador.
	@ManyToOne
	@JoinColumn(name = "faction_id")
	private Faction faction;

	// Comandante seleccionado.
	@ManyToOne
	@JoinColumn(name = "commander_id")
	private Commander commander;

	// Constructor vacio necesario para JPA.
	public Player() {
	}

	// Crea un jugador con sus datos iniciales.
	public Player(String name, int turnOrder, int money) {
		this.name = name;
		this.turnOrder = turnOrder;
		this.money = money;
		this.status = PlayerStatus.ACTIVE;
	}

	// Asocia el jugador con una partida.
	void assignGameState(GameState gameState) {
		this.gameState = gameState;
	}

	// Elimina la asociacion con la partida.
	void removeGameState() {
		this.gameState = null;
	}

	// Asigna la faccion y el comandante.
	public void assignSelection(Faction faction, Commander commander) {
		this.faction = faction;
		this.commander = commander;
	}

	// Elimina la seleccion actual.
	public void clearSelection() {
		this.faction = null;
		this.commander = null;
	}

	// Elimina al jugador de la partida.
	public void eliminate() {
		this.status = PlayerStatus.ELIMINATED;
	}

	// Agrega dinero al jugador.
	public void addMoney(int amount) {
		this.money += amount;
	}

	// Descuenta dinero al jugador.
	public void subtractMoney(int amount) {
		this.money -= amount;
	}

	// Declara al jugador ganador.
	void declareWinner() {
		this.status = PlayerStatus.WINNER;
	}

	// Comprueba si el jugador sigue activo.
	public boolean isActive() {
		return status == PlayerStatus.ACTIVE;
	}

	/** Devuelve el nivel de carga acumulada del comandante. */
	public int getPowerCharge() {
		return powerCharge;
	}

	/** Suma carga de poder sin superar nueve estrellas. */
	public void chargePower(int amount) {
		powerCharge = Math.min(9, Math.max(0, powerCharge + amount));
	}

	/** Descuenta la carga validada y activa el efecto del comandante. */
	public void spendPower(int amount) {
		powerCharge -= amount;
		powerActive = true;
	}

	/** Indica si el poder del comandante sigue activo. */
	public boolean isPowerActive() {
		return powerActive;
	}

	/** Finaliza un efecto de poder al terminar su duración. */
	public void clearPowerActive() {
		powerActive = false;
	}

	// Getters y setters.
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
}
