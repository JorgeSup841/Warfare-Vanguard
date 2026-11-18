
package co.edu.unbosque.advancewars.model;



import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.*;

/** Representa una unidad en partida y sus recursos y acciones de turno. */
@Entity
@Table(name = "game_unit")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo de unidad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_definition_id", nullable = false)
    private UnitDefinition definition;

    // Jugador dueño de la unidad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Player owner;

    // Casilla donde esta la unidad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tile_id", nullable = false, unique = true)
    private Tile tile;

    // Vida y combustible actuales.
    private int currentHealth;
    private int currentFuel;

    // Municion disponible para cada arma.
    @ElementCollection
    @CollectionTable(
        name = "unit_ammunition",
        joinColumns = @JoinColumn(name = "unit_id")
    )
    @MapKeyColumn(name = "weapon_code")
    @Column(name = "remaining_ammunition")
    private Map<String, Integer> ammunition = new HashMap<>();

    // Acciones realizadas durante el turno.
    private boolean movedThisTurn;
    private boolean attackedThisTurn;
    private boolean capturedThisTurn;

    // Constructor necesario para JPA.
    public Unit() {
    }

    // Crea una unidad con sus valores iniciales.
    public Unit(UnitDefinition definition, Player owner, Tile tile) {
        this.definition = definition;
        this.owner = owner;
        this.tile = tile;

        this.currentHealth = definition.getMaxHealth();
        this.currentFuel = definition.isUsesFuel()
                ? definition.getMaxFuel() : 0;

        // Carga la municion inicial de cada arma.
        for (Weapon weapon : definition.getWeapons()) {
            if (weapon.isUsesAmmunition()) {
                ammunition.put(
                    weapon.getCode(),
                    weapon.getAmmunitionCapacity()
                );
            }
        }
    }

    // Mueve la unidad y consume combustible.
    public void moveTo(Tile destination, int fuelCost) {
        consumeFuel(fuelCost);
        this.tile = destination;
        this.movedThisTurn = true;
    }

    // Descuenta combustible.
    public void consumeFuel(int amount) {
        this.currentFuel -= amount;
    }

    // Recupera combustible sin superar el maximo.
    public void refuel(int amount) {
        this.currentFuel = Math.min(
            definition.getMaxFuel(),
            currentFuel + amount
        );
    }

    // Descuenta municion de un arma.
    public void consumeAmmunition(String weaponCode, int amount) {
        ammunition.put(
            weaponCode,
            ammunition.getOrDefault(weaponCode, 0) - amount
        );
    }

    // Reduce la vida sin bajar de cero.
    public void receiveDamage(int damage) {
        this.currentHealth = Math.max(0, currentHealth - damage);
    }

    // Recupera vida sin superar el maximo.
    public void repair(int amount) {
        this.currentHealth = Math.min(
            definition.getMaxHealth(),
            currentHealth + amount
        );
    }

    // Marca que la unidad ya ataco.
    public void markAttacked() {
        this.attackedThisTurn = true;
    }

    // Marca que la unidad ya capturo.
    public void markCaptured() {
        this.capturedThisTurn = true;
    }

    // Reinicia las acciones para el siguiente turno.
    public void resetTurnActions() {
        movedThisTurn = false;
        attackedThisTurn = false;
        capturedThisTurn = false;
    }

    // Comprueba si la unidad fue destruida.
    public boolean isDestroyed() {
        return currentHealth <= 0;
    }

    // Devuelve la posicion actual.
    public Position getPosition() {
        return tile == null ? null : tile.getPosition();
    }

    // Getters y setters.
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
        this.ammunition = ammunition == null
                ? new HashMap<>()
                : new HashMap<>(ammunition);
    }

    public boolean isMovedThisTurn() {
        return movedThisTurn;
    }

    public void setMovedThisTurn(boolean movedThisTurn) {
        this.movedThisTurn = movedThisTurn;
    }

    public boolean isAttackedThisTurn() {
        return attackedThisTurn;
    }

    public void setAttackedThisTurn(boolean attackedThisTurn) {
        this.attackedThisTurn = attackedThisTurn;
    }

    public boolean isCapturedThisTurn() {
        return capturedThisTurn;
    }

    public void setCapturedThisTurn(boolean capturedThisTurn) {
        this.capturedThisTurn = capturedThisTurn;
    }
}
