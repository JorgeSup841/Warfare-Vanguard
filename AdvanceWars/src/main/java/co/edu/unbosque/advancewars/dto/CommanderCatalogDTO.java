package co.edu.unbosque.advancewars.dto;


/** Contrato tipado del catalogo del juego, sin entidades JPA en las respuestas REST. */
public class CommanderCatalogDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String factionCode;
    private String powerName;
    private int powerCost;
    private String powerDescription;
    private String powerEffectCode;
    private String passiveEffectCode;
    private String passiveAbilityName;
    private String passiveAbilityDescription;

    /** Constructor requerido por serializadores y herramientas de mapeo. */
    public CommanderCatalogDTO() {
    }

    /** Crea un DTO de catalogo con los valores de la capa de servicio. */
    public CommanderCatalogDTO(Long id, String code, String name, String description, String factionCode, String powerName, int powerCost, String powerDescription, String powerEffectCode, String passiveEffectCode, String passiveAbilityName, String passiveAbilityDescription) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.factionCode = factionCode;
        this.powerName = powerName;
        this.powerCost = powerCost;
        this.powerDescription = powerDescription;
        this.powerEffectCode = powerEffectCode;
        this.passiveEffectCode = passiveEffectCode;
        this.passiveAbilityName = passiveAbilityName;
        this.passiveAbilityDescription = passiveAbilityDescription;
    }

    /** Obtiene id. */
    public Long getId() {
        return id;
    }

    /** Actualiza id. */
    public void setId(Long id) {
        this.id = id;
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

    /** Obtiene description. */
    public String getDescription() {
        return description;
    }

    /** Actualiza description. */
    public void setDescription(String description) {
        this.description = description;
    }

    /** Obtiene factionCode. */
    public String getFactionCode() {
        return factionCode;
    }

    /** Actualiza factionCode. */
    public void setFactionCode(String factionCode) {
        this.factionCode = factionCode;
    }

    /** Obtiene powerName. */
    public String getPowerName() {
        return powerName;
    }

    /** Actualiza powerName. */
    public void setPowerName(String powerName) {
        this.powerName = powerName;
    }

    /** Obtiene powerCost. */
    public int getPowerCost() {
        return powerCost;
    }

    /** Actualiza powerCost. */
    public void setPowerCost(int powerCost) {
        this.powerCost = powerCost;
    }

    /** Obtiene powerDescription. */
    public String getPowerDescription() {
        return powerDescription;
    }

    /** Actualiza powerDescription. */
    public void setPowerDescription(String powerDescription) {
        this.powerDescription = powerDescription;
    }

    /** Obtiene powerEffectCode. */
    public String getPowerEffectCode() {
        return powerEffectCode;
    }

    /** Actualiza powerEffectCode. */
    public void setPowerEffectCode(String powerEffectCode) {
        this.powerEffectCode = powerEffectCode;
    }

    /** Obtiene passiveEffectCode. */
    public String getPassiveEffectCode() {
        return passiveEffectCode;
    }

    /** Actualiza passiveEffectCode. */
    public void setPassiveEffectCode(String passiveEffectCode) {
        this.passiveEffectCode = passiveEffectCode;
    }

    /** Obtiene passiveAbilityName. */
    public String getPassiveAbilityName() {
        return passiveAbilityName;
    }

    /** Actualiza passiveAbilityName. */
    public void setPassiveAbilityName(String passiveAbilityName) {
        this.passiveAbilityName = passiveAbilityName;
    }

    /** Obtiene passiveAbilityDescription. */
    public String getPassiveAbilityDescription() {
        return passiveAbilityDescription;
    }

    /** Actualiza passiveAbilityDescription. */
    public void setPassiveAbilityDescription(String passiveAbilityDescription) {
        this.passiveAbilityDescription = passiveAbilityDescription;
    }

}
