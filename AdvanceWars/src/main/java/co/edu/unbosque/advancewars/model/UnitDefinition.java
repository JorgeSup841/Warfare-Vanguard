package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "unit_definition")
public class UnitDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, unique = true, length = 40)
	private UnitType unitType;

	@Column(nullable = false, length = 80)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UnitCategory category;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MovementType movementType;

	@Column(nullable = false)
	private int productionCost;

	@Column(nullable = false)
	private int maxHealth;

	@Column(nullable = false)
	private int movementPoints;

	@Column(nullable = false)
	private boolean usesFuel;

	@Column(nullable = false)
	private int maxFuel;

	@Column(nullable = false)
	private int fuelConsumptionPerTurn;

	@Column(nullable = false)
	private int fuelPerMovementPoint;

	@Column(nullable = false)
	private boolean captureCapable;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "unit_definition_weapon", joinColumns = @JoinColumn(name = "unit_definition_id"), inverseJoinColumns = @JoinColumn(name = "weapon_id"))
	private Set<Weapon> weapons = new HashSet<>();

	protected UnitDefinition() {
	}

	public UnitDefinition(UnitType unitType, String name, UnitCategory category, MovementType movementType,
			int productionCost, int maxHealth, int movementPoints, boolean usesFuel, int maxFuel,
			int fuelConsumptionPerTurn, int fuelPerMovementPoint, boolean captureCapable) {
		this.unitType = unitType;
		this.name = name;
		this.category = category;
		this.movementType = movementType;
		this.productionCost = productionCost;
		this.maxHealth = maxHealth;
		this.movementPoints = movementPoints;
		this.usesFuel = usesFuel;
		this.maxFuel = maxFuel;
		this.fuelConsumptionPerTurn = fuelConsumptionPerTurn;
		this.fuelPerMovementPoint = fuelPerMovementPoint;
		this.captureCapable = captureCapable;
	}

	public void addWeapon(Weapon weapon) {
		weapons.add(weapon);
	}

	public void removeWeapon(Weapon weapon) {
		weapons.remove(weapon);
	}

	public Long getId() {
		return id;
	}

	public UnitType getUnitType() {
		return unitType;
	}

	public void setUnitType(UnitType unitType) {
		this.unitType = unitType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UnitCategory getCategory() {
		return category;
	}

	public void setCategory(UnitCategory category) {
		this.category = category;
	}

	public MovementType getMovementType() {
		return movementType;
	}

	public void setMovementType(MovementType movementType) {
		this.movementType = movementType;
	}

	public int getProductionCost() {
		return productionCost;
	}

	public void setProductionCost(int productionCost) {
		this.productionCost = productionCost;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public int getMovementPoints() {
		return movementPoints;
	}

	public void setMovementPoints(int movementPoints) {
		this.movementPoints = movementPoints;
	}

	public boolean isUsesFuel() {
		return usesFuel;
	}

	public void setUsesFuel(boolean usesFuel) {
		this.usesFuel = usesFuel;
	}

	public int getMaxFuel() {
		return maxFuel;
	}

	public void setMaxFuel(int maxFuel) {
		this.maxFuel = maxFuel;
	}

	public int getFuelConsumptionPerTurn() {
		return fuelConsumptionPerTurn;
	}

	public void setFuelConsumptionPerTurn(int fuelConsumptionPerTurn) {
		this.fuelConsumptionPerTurn = fuelConsumptionPerTurn;
	}

	public int getFuelPerMovementPoint() {
		return fuelPerMovementPoint;
	}

	public void setFuelPerMovementPoint(int fuelPerMovementPoint) {
		this.fuelPerMovementPoint = fuelPerMovementPoint;
	}

	public boolean isCaptureCapable() {
		return captureCapable;
	}

	public void setCaptureCapable(boolean captureCapable) {
		this.captureCapable = captureCapable;
	}

	public Set<Weapon> getWeapons() {
		return Collections.unmodifiableSet(weapons);
	}

	public void setWeapons(Set<Weapon> weapons) {
		this.weapons = weapons == null ? new HashSet<>() : new HashSet<>(weapons);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof UnitDefinition other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return UnitDefinition.class.hashCode();
	}

	@Override
	public String toString() {
		return "UnitDefinition{" + "id=" + id + ", unitType=" + unitType + ", name='" + name + '\'' + ", category="
				+ category + ", movementType=" + movementType + ", productionCost=" + productionCost + ", maxHealth="
				+ maxHealth + ", movementPoints=" + movementPoints + ", usesFuel=" + usesFuel + ", maxFuel=" + maxFuel
				+ ", fuelConsumptionPerTurn=" + fuelConsumptionPerTurn + ", fuelPerMovementPoint="
				+ fuelPerMovementPoint + ", captureCapable=" + captureCapable + '}';
	}
}
