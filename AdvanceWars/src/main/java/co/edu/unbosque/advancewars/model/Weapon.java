
package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.UnitType;
import java.util.HashMap;
import java.util.Map;
import jakarta.persistence.*;

@Entity
@Table(name = "weapon")
public class Weapon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Codigo unico y nombre del arma.
    @Column(unique = true, nullable = false)
    private String code;

    private String name;

    // Alcance minimo y maximo.
    private int minRange;
    private int maxRange;

    // Municion del arma.
    private boolean usesAmmunition;
    private int ammunitionCapacity;

    // Daño que causa a cada tipo de unidad.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "weapon_damage",
        joinColumns = @JoinColumn(name = "weapon_id")
    )
    @MapKeyColumn(name = "target_unit_type")
    @MapKeyEnumerated(EnumType.STRING)
    @Column(name = "base_damage")
    private Map<UnitType, Integer> damageByTarget = new HashMap<>();

    // Constructor necesario para JPA.
    public Weapon() {
    }

    // Crea un arma con sus caracteristicas.
    public Weapon(String code, String name, int minRange,
                  int maxRange, boolean usesAmmunition,
                  int ammunitionCapacity) {

        this.code = code;
        this.name = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.usesAmmunition = usesAmmunition;
        this.ammunitionCapacity = ammunitionCapacity;
    }

    // Establece el daño contra un tipo de unidad.
    public void defineDamage(UnitType targetType, int damage) {
        damageByTarget.put(targetType, damage);
    }

    // Obtiene el daño contra un tipo de unidad.
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
        return new HashMap<>(damageByTarget);
    }

    public void setDamageByTarget(Map<UnitType, Integer> damageByTarget) {
        this.damageByTarget = damageByTarget == null
                ? new HashMap<>()
                : new HashMap<>(damageByTarget);
    }
}
