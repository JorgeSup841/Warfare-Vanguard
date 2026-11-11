package co.edu.unbosque.advancewars.model;



import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Representa una unidad en partida y sus recursos y acciones de turno. */
@Entity
@Table(name = "game_unit", uniqueConstraints = { @UniqueConstraint(name = "uk_unit_tile", columnNames = "tile_id") })
public class Unit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_definition_id", nullable = false)
	private UnitDefinition definition;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "owner_id", nullable = false)
	private Player owner;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tile_id", nullable = false, unique = true)
	private Tile tile;

	@Column(nullable = false)
	private int currentHealth;

	@Column(nullable = false)
	private int currentFuel;

	@ElementCollection
	@CollectionTable(name = "unit_ammunition", joinColumns = @JoinColumn(name = "unit_id"))
	@MapKeyColumn(name = "weapon_code")
	@Column(name = "remaining_ammunition", nullable = false)
	private Map<String, Integer> ammunition = new HashMap<>();

	@Column(nullable = false)
	private boolean movedThisTurn;

	@Column(nullable = false)
	private boolean attackedThisTurn;

	@Column(nullable = false)
	private boolean capturedThisTurn;

	/** Constructor requerido por JPA para materializar la entidad. */
	protected Unit() {
	}

	/** Construye una unidad con su definición, dueño y casilla inicial. */
	public Unit(UnitDefinition definition, Player owner, Tile tile) {
		this.definition = definition;
		this.owner = owner;
		this.tile = tile;
		this.currentHealth = definition.getMaxHealth();
		this.currentFuel = definition.isUsesFuel() ? definition.getMaxFuel() : 0;
		this.ammunition = new HashMap<>();

		for (Weapon weapon : definition.getWeapons()) {
			if (weapon.isUsesAmmunition()) {
				ammunition.put(weapon.getCode(), weapon.getAmmunitionCapacity());
			}
		}

		this.movedThisTurn = false;
		this.attackedThisTurn = false;
		this.capturedThisTurn = false;
	}

	/** Mueve la unidad a una casilla y registra el combustible consumido. */
	public void moveTo(Tile destination, int fuelCost) {
		consumeFuel(fuelCost);
		this.tile = destination;
		this.movedThisTurn = true;
	}

	/** Descuenta del combustible actual la cantidad indicada. */
	public void consumeFuel(int amount) {
		this.currentFuel -= amount;
	}

	/** Recupera combustible sin superar la capacidad máxima de la unidad. */
	public void refuel(int amount) {
		this.currentFuel = Math.min(definition.getMaxFuel(), currentFuel + amount);
	}

	/** Descuenta la munición utilizada de un arma de la unidad. */
	public void consumeAmmunition(String weaponCode, int amount) {
		ammunition.put(weaponCode, ammunition.getOrDefault(weaponCode, 0) - amount);
	}

	/** Aplica daño a la unidad sin permitir que su salud sea negativa. */
	public void receiveDamage(int damage) {
		this.currentHealth = Math.max(0, currentHealth - damage);
	}

	/** Recupera salud sin superar la salud máxima de la unidad. */
	public void repair(int amount) {
		this.currentHealth = Math.min(definition.getMaxHealth(), currentHealth + amount);
	}

	/** Registra que la unidad ya realizó un ataque durante el turno. */
	public void markAttacked() {
		this.attackedThisTurn = true;
	}

	/** Registra que la unidad ya realizó una acción de captura durante el turno. */
	public void markCaptured() {
		this.capturedThisTurn = true;
	}

	/** Restablece las acciones realizadas por la unidad para el nuevo turno. */
	public void resetTurnActions() {
		this.movedThisTurn = false;
		this.attackedThisTurn = false;
		this.capturedThisTurn = false;
	}

	/** Indica si la salud actual de la unidad llegó a cero. */
	public boolean isDestroyed() {
		return currentHealth <= 0;
	}

	public Long getId() {
		return id;
	}

	public UnitDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(UnitDefinition definition) {
		this.definition = definition;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	/** Obtiene la posición actual de la unidad a partir de su casilla. */
	public Position getPosition() {
		return tile == null ? null : tile.getPosition();
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = currentHealth;
	}

	public int getCurrentFuel() {
		return currentFuel;
	}

	public void setCurrentFuel(int currentFuel) {
		this.currentFuel = currentFuel;
	}

	public Map<String, Integer> getAmmunition() {
		return Collections.unmodifiableMap(ammunition);
	}

	public void setAmmunition(Map<String, Integer> ammunition) {
		this.ammunition = ammunition == null ? new HashMap<>() : new HashMap<>(ammunition);
	}

	/** Indica si la unidad ya se movió durante el turno actual. */
	public boolean isMovedThisTurn() {
		return movedThisTurn;
	}

	public void setMovedThisTurn(boolean movedThisTurn) {
		this.movedThisTurn = movedThisTurn;
	}

	/** Indica si la unidad ya atacó durante el turno actual. */
	public boolean isAttackedThisTurn() {
		return attackedThisTurn;
	}

	public void setAttackedThisTurn(boolean attackedThisTurn) {
		this.attackedThisTurn = attackedThisTurn;
	}

	/** Indica si la unidad ya intentó capturar durante el turno actual. */
	public boolean isCapturedThisTurn() {
		return capturedThisTurn;
	}

	public void setCapturedThisTurn(boolean capturedThisTurn) {
		this.capturedThisTurn = capturedThisTurn;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Unit other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Unit.class.hashCode();
	}

	@Override
	public String toString() {
		return "Unit{" + "id=" + id + ", currentHealth=" + currentHealth + ", currentFuel=" + currentFuel
				+ ", movedThisTurn=" + movedThisTurn + ", attackedThisTurn=" + attackedThisTurn + ", capturedThisTurn="
				+ capturedThisTurn + '}';
	}
}
