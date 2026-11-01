package co.edu.unbosque.advancewars.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "commander")
public class Commander {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faction_id", nullable = false)
    private Faction faction;

    @Column(name = "passive_ability_name", nullable = false, length = 60)
    private String passiveAbilityName;

    @Column(name = "passive_ability_description", nullable = false, length = 500)
    private String passiveAbilityDescription;

    @Column(name = "passive_effect_code", nullable = false, length = 50)
    private String passiveEffectCode;

    @Column(name = "power_name", nullable = false, length = 60)
    private String powerName;

    @Column(name = "power_description", nullable = false, length = 500)
    private String powerDescription;

    @Column(name = "power_effect_code", nullable = false, length = 50)
    private String powerEffectCode;

    @Column(name = "power_cost", nullable = false)
    private int powerCost;

    protected Commander() {
    }

    public Commander(
        String code,
        String name,
        String description,
        Faction faction,
        String passiveAbilityName,
        String passiveAbilityDescription,
        String passiveEffectCode,
        String powerName,
        String powerDescription,
        String powerEffectCode,
        int powerCost
    ) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.faction = faction;
        this.passiveAbilityName = passiveAbilityName;
        this.passiveAbilityDescription = passiveAbilityDescription;
        this.passiveEffectCode = passiveEffectCode;
        this.powerName = powerName;
        this.powerDescription = powerDescription;
        this.powerEffectCode = powerEffectCode;
        this.powerCost = powerCost;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Faction getFaction() {
        return faction;
    }

    public void setFaction(Faction faction) {
        this.faction = faction;
    }

    public String getPassiveAbilityName() {
        return passiveAbilityName;
    }

    public void setPassiveAbilityName(String passiveAbilityName) {
        this.passiveAbilityName = passiveAbilityName;
    }

    public String getPassiveAbilityDescription() {
        return passiveAbilityDescription;
    }

    public void setPassiveAbilityDescription(String passiveAbilityDescription) {
        this.passiveAbilityDescription = passiveAbilityDescription;
    }

    public String getPassiveEffectCode() {
        return passiveEffectCode;
    }

    public void setPassiveEffectCode(String passiveEffectCode) {
        this.passiveEffectCode = passiveEffectCode;
    }

    public String getPowerName() {
        return powerName;
    }

    public void setPowerName(String powerName) {
        this.powerName = powerName;
    }

    public String getPowerDescription() {
        return powerDescription;
    }

    public void setPowerDescription(String powerDescription) {
        this.powerDescription = powerDescription;
    }

    public String getPowerEffectCode() {
        return powerEffectCode;
    }

    public void setPowerEffectCode(String powerEffectCode) {
        this.powerEffectCode = powerEffectCode;
    }

    public int getPowerCost() {
        return powerCost;
    }

    public void setPowerCost(int powerCost) {
        this.powerCost = powerCost;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Commander other)) {
            return false;
        }

        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return Commander.class.hashCode();
    }

    @Override
    public String toString() {
        return "Commander{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", description='" + description + '\'' +
            ", passiveAbilityName='" + passiveAbilityName + '\'' +
            ", passiveEffectCode='" + passiveEffectCode + '\'' +
            ", powerName='" + powerName + '\'' +
            ", powerEffectCode='" + powerEffectCode + '\'' +
            ", powerCost=" + powerCost +
            '}';
    }
}
