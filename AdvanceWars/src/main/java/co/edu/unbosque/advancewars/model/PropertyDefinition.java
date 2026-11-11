package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.UnitCategory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/** Define las características económicas y de producción de un tipo de propiedad. */
@Entity
@Table(name = "property_definition")
public class PropertyDefinition {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	@Enumerated(EnumType.STRING)
	@Column(name = "property_type", nullable = false, unique = true, length = 30)
	private PropertyType type;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false)
	private int income;

	@Column(name = "defense_bonus", nullable = false)
	private int defenseBonus;

	@Column(name = "capture_threshold", nullable = false)
	private int captureThreshold;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "property_production_category", joinColumns = @JoinColumn(name = "property_definition_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "unit_category", nullable = false, length = 20)
	private Set<UnitCategory> productionCategories = new HashSet<>();

	protected PropertyDefinition() {
	}

	/** Construye la definición de una propiedad con sus valores y categorías de producción. */
	public PropertyDefinition(PropertyType type, String name, int income, int defenseBonus, int captureThreshold,
			Set<UnitCategory> productionCategories) {
		this.type = type;
		this.name = name;
		this.income = income;
		this.defenseBonus = defenseBonus;
		this.captureThreshold = captureThreshold;
		setProductionCategories(productionCategories);
	}

	/** Habilita una categoría de unidades para producirse en esta propiedad. */
	public void allowProduction(UnitCategory category) {
		productionCategories.add(category);
	}

	/** Deshabilita una categoría de unidades para producirse en esta propiedad. */
	public void disallowProduction(UnitCategory category) {
		productionCategories.remove(category);
	}

	/** Actualiza los valores económicos y defensivos de la definición. */
	public void updateValues(int income, int defenseBonus, int captureThreshold) {
		this.income = income;
		this.defenseBonus = defenseBonus;
		this.captureThreshold = captureThreshold;
	}

	public Long getId() {
		return id;
	}

	public PropertyType getType() {
		return type;
	}

	public void setType(PropertyType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIncome() {
		return income;
	}

	public void setIncome(int income) {
		this.income = income;
	}

	public int getDefenseBonus() {
		return defenseBonus;
	}

	public void setDefenseBonus(int defenseBonus) {
		this.defenseBonus = defenseBonus;
	}

	public int getCaptureThreshold() {
		return captureThreshold;
	}

	public void setCaptureThreshold(int captureThreshold) {
		this.captureThreshold = captureThreshold;
	}

	public Set<UnitCategory> getProductionCategories() {
		return Collections.unmodifiableSet(productionCategories);
	}

	public void setProductionCategories(Set<UnitCategory> productionCategories) {
		this.productionCategories = productionCategories == null ? new HashSet<>()
				: new HashSet<>(productionCategories);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof PropertyDefinition other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return PropertyDefinition.class.hashCode();
	}

	@Override
	public String toString() {
		return "PropertyDefinition{" + "id=" + id + ", type=" + type + ", name='" + name + '\'' + ", income=" + income
				+ ", defenseBonus=" + defenseBonus + ", captureThreshold=" + captureThreshold
				+ ", productionCategories=" + productionCategories + '}';
	}
}
