package co.edu.unbosque.advancewars.dto;

import java.util.Map;
import java.util.HashMap;
import co.edu.unbosque.advancewars.enums.UnitType;

/** Contrato tipado del catalogo del juego, sin entidades JPA en las respuestas REST. */
public class WeaponCatalogDTO {

    private String code;
    private String name;
    private int minRange;
    private int maxRange;
    private boolean usesAmmunition;
    private int ammunitionCapacity;
    private Map<UnitType, Integer> damageByTarget = new HashMap<>();

    /** Constructor requerido por serializadores y herramientas de mapeo. */
    public WeaponCatalogDTO() {
    }

    /** Crea un DTO de catalogo con los valores de la capa de servicio. */
    public WeaponCatalogDTO(String code, String name, int minRange, int maxRange, boolean usesAmmunition, int ammunitionCapacity, Map<UnitType, Integer> damageByTarget) {
        this.code = code;
        this.name = name;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.usesAmmunition = usesAmmunition;
        this.ammunitionCapacity = ammunitionCapacity;
        this.damageByTarget = damageByTarget == null ? new HashMap<>() : new HashMap<>(damageByTarget);
    }

    /** Obtiene code. */
    public String getCode() {
        return code;
    }

    /** Actualiza code. */
    public void setCode(String code) {
        this.code = code;
    }

    /** Obtiene name. */
    public String getName() {
        return name;
    }

    /** Actualiza name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene minRange. */
    public int getMinRange() {
        return minRange;
    }

    /** Actualiza minRange. */
    public void setMinRange(int minRange) {
        this.minRange = minRange;
    }

    /** Obtiene maxRange. */
    public int getMaxRange() {
        return maxRange;
    }

    /** Actualiza maxRange. */
    public void setMaxRange(int maxRange) {
        this.maxRange = maxRange;
    }

    /** Obtiene usesAmmunition. */
    public boolean isUsesAmmunition() {
        return usesAmmunition;
    }

    /** Actualiza usesAmmunition. */
    public void setUsesAmmunition(boolean usesAmmunition) {
        this.usesAmmunition = usesAmmunition;
    }

    /** Obtiene ammunitionCapacity. */
    public int getAmmunitionCapacity() {
        return ammunitionCapacity;
    }

    /** Actualiza ammunitionCapacity. */
    public void setAmmunitionCapacity(int ammunitionCapacity) {
        this.ammunitionCapacity = ammunitionCapacity;
    }

    /** Obtiene damageByTarget. */
    public Map<UnitType, Integer> getDamageByTarget() {
        return damageByTarget == null ? new HashMap<>() : new HashMap<>(damageByTarget);
    }

    /** Actualiza damageByTarget. */
    public void setDamageByTarget(Map<UnitType, Integer> damageByTarget) {
        this.damageByTarget = damageByTarget == null ? new HashMap<>() : new HashMap<>(damageByTarget);
    }

}
