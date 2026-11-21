package co.edu.unbosque.advancewars.dto;

import java.util.List;
import java.util.ArrayList;

/** Contrato tipado del catalogo del juego, sin entidades JPA en las respuestas REST. */
public class UnitCatalogDTO {

    private String unitType;
    private String name;
    private String category;
    private String movementType;
    private int productionCost;
    private int movementPoints;
    private int maxHealth;
    private int maxFuel;
    private boolean captureCapable;
    private boolean usesFuel;
    private int fuelConsumptionPerTurn;
    private int fuelPerMovementPoint;
    private List<WeaponCatalogDTO> weapons = new ArrayList<>();

    /** Constructor requerido por serializadores y herramientas de mapeo. */
    public UnitCatalogDTO() {
    }

    /** Crea un DTO de catalogo con los valores de la capa de servicio. */
    public UnitCatalogDTO(String unitType, String name, String category, String movementType, int productionCost, int movementPoints, int maxHealth, int maxFuel, boolean captureCapable, boolean usesFuel, int fuelConsumptionPerTurn, int fuelPerMovementPoint, List<WeaponCatalogDTO> weapons) {
        this.unitType = unitType;
        this.name = name;
        this.category = category;
        this.movementType = movementType;
        this.productionCost = productionCost;
        this.movementPoints = movementPoints;
        this.maxHealth = maxHealth;
        this.maxFuel = maxFuel;
        this.captureCapable = captureCapable;
        this.usesFuel = usesFuel;
        this.fuelConsumptionPerTurn = fuelConsumptionPerTurn;
        this.fuelPerMovementPoint = fuelPerMovementPoint;
        this.weapons = weapons == null ? new ArrayList<>() : new ArrayList<>(weapons);
    }

    /** Obtiene unitType. */
    public String getUnitType() {
        return unitType;
    }

    /** Actualiza unitType. */
    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    /** Obtiene name. */
    public String getName() {
        return name;
    }

    /** Actualiza name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Obtiene category. */
    public String getCategory() {
        return category;
    }

    /** Actualiza category. */
    public void setCategory(String category) {
        this.category = category;
    }

    /** Obtiene movementType. */
    public String getMovementType() {
        return movementType;
    }

    /** Actualiza movementType. */
    public void setMovementType(String movementType) {
        this.movementType = movementType;
    }

    /** Obtiene productionCost. */
    public int getProductionCost() {
        return productionCost;
    }

    /** Actualiza productionCost. */
    public void setProductionCost(int productionCost) {
        this.productionCost = productionCost;
    }

    /** Obtiene movementPoints. */
    public int getMovementPoints() {
        return movementPoints;
    }

    /** Actualiza movementPoints. */
    public void setMovementPoints(int movementPoints) {
        this.movementPoints = movementPoints;
    }

    /** Obtiene maxHealth. */
    public int getMaxHealth() {
        return maxHealth;
    }

    /** Actualiza maxHealth. */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    /** Obtiene maxFuel. */
    public int getMaxFuel() {
        return maxFuel;
    }

    /** Actualiza maxFuel. */
    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    /** Obtiene captureCapable. */
    public boolean isCaptureCapable() {
        return captureCapable;
    }

    /** Actualiza captureCapable. */
    public void setCaptureCapable(boolean captureCapable) {
        this.captureCapable = captureCapable;
    }

    /** Obtiene usesFuel. */
    public boolean isUsesFuel() {
        return usesFuel;
    }

    /** Actualiza usesFuel. */
    public void setUsesFuel(boolean usesFuel) {
        this.usesFuel = usesFuel;
    }

    /** Obtiene fuelConsumptionPerTurn. */
    public int getFuelConsumptionPerTurn() {
        return fuelConsumptionPerTurn;
    }

    /** Actualiza fuelConsumptionPerTurn. */
    public void setFuelConsumptionPerTurn(int fuelConsumptionPerTurn) {
        this.fuelConsumptionPerTurn = fuelConsumptionPerTurn;
    }

    /** Obtiene fuelPerMovementPoint. */
    public int getFuelPerMovementPoint() {
        return fuelPerMovementPoint;
    }

    /** Actualiza fuelPerMovementPoint. */
    public void setFuelPerMovementPoint(int fuelPerMovementPoint) {
        this.fuelPerMovementPoint = fuelPerMovementPoint;
    }

    /** Obtiene weapons. */
    public List<WeaponCatalogDTO> getWeapons() {
        return weapons == null ? new ArrayList<>() : new ArrayList<>(weapons);
    }

    /** Actualiza weapons. */
    public void setWeapons(List<WeaponCatalogDTO> weapons) {
        this.weapons = weapons == null ? new ArrayList<>() : new ArrayList<>(weapons);
    }

}
