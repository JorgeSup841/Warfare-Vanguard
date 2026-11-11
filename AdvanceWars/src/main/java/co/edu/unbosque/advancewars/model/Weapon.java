package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.UnitType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.MapKeyEnumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "weapon")
public class Weapon {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String code;

	@Column(nullable = false, length = 80)
	private String name;

	@Column(nullable = false)
	private int minRange;

	@Column(nullable = false)
	private int maxRange;

	@Column(nullable = false)
	private boolean usesAmmunition;

	@Column(nullable = false)
	private int ammunitionCapacity;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "weapon_damage", joinColumns = @JoinColumn(name = "weapon_id"))
	@MapKeyColumn(name = "target_unit_type")
	@MapKeyEnumerated(EnumType.STRING)
	@Column(name = "base_damage", nullable = false)
	private Map<UnitType, Integer> damageByTarget = new HashMap<>();

	protected Weapon() {
	}

	public Weapon(String code, String name, int minRange, int maxRange, boolean usesAmmunition,
			int ammunitionCapacity) {
		this.code = code;
		this.name = name;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.usesAmmunition = usesAmmunition;
		this.ammunitionCapacity = ammunitionCapacity;
	}

	public void defineDamage(UnitType targetType, int damage) {
		damageByTarget.put(targetType, damage);
	}

	public int getDamageAgainst(UnitType targetType) {
		return damageByTarget.getOrDefault(targetType, 0);
	}

	public Long getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinRange() {
		return minRange;
	}

	public void setMinRange(int minRange) {
		this.minRange = minRange;
	}

	public int getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(int maxRange) {
		this.maxRange = maxRange;
	}

	public boolean isUsesAmmunition() {
		return usesAmmunition;
	}

	public void setUsesAmmunition(boolean usesAmmunition) {
		this.usesAmmunition = usesAmmunition;
	}

	public int getAmmunitionCapacity() {
		return ammunitionCapacity;
	}

	public void setAmmunitionCapacity(int ammunitionCapacity) {
		this.ammunitionCapacity = ammunitionCapacity;
	}

	public Map<UnitType, Integer> getDamageByTarget() {
		return Collections.unmodifiableMap(damageByTarget);
	}

	public void setDamageByTarget(Map<UnitType, Integer> damageByTarget) {
		this.damageByTarget = damageByTarget == null ? new HashMap<>() : new HashMap<>(damageByTarget);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Weapon other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Weapon.class.hashCode();
	}

	@Override
	public String toString() {
		return "Weapon{" + "id=" + id + ", code='" + code + '\'' + ", name='" + name + '\'' + ", minRange=" + minRange
				+ ", maxRange=" + maxRange + ", usesAmmunition=" + usesAmmunition + ", ammunitionCapacity="
				+ ammunitionCapacity + ", damageByTarget=" + damageByTarget + '}';
	}
}
