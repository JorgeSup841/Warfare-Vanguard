package co.edu.unbosque.advancewars.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.unbosque.advancewars.dto.FactionCatalogDTO;
import co.edu.unbosque.advancewars.dto.CommanderCatalogDTO;
import co.edu.unbosque.advancewars.dto.UnitCatalogDTO;
import co.edu.unbosque.advancewars.dto.WeaponCatalogDTO;
import co.edu.unbosque.advancewars.enums.MovementType;
import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.Commander;
import co.edu.unbosque.advancewars.model.Faction;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.model.Weapon;
import co.edu.unbosque.advancewars.repository.CommanderRepository;
import co.edu.unbosque.advancewars.repository.FactionRepository;
import co.edu.unbosque.advancewars.repository.UnitDefinitionRepository;
import co.edu.unbosque.advancewars.repository.WeaponRepository;

/** Gestiona el contenido de facciones, comandantes y unidades del servidor. */
@Service
public class GameCatalogService {
    private final ObjectMapper json = new ObjectMapper();
    private final FactionRepository factions;
    private final CommanderRepository commanders;
    private final UnitDefinitionRepository units;
    private final WeaponRepository weapons;

    /** Inyecta las dependencias del servicio mediante constructor. */
    public GameCatalogService(FactionRepository factions,
            CommanderRepository commanders, UnitDefinitionRepository units, WeaponRepository weapons) {
        this.factions = factions;
        this.commanders = commanders;
        this.units = units;
        this.weapons = weapons;
    }

    /** Lee un archivo JSON del catálogo incluido en los recursos. */

    private JsonNode read(String name) {
        try (var input = new ClassPathResource("game-catalog/" + name).getInputStream()) {
            return json.readTree(input);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer el catalogo " + name, error);
        }
    }

    /** Idempotente: conserva identificadores y referencias de partidas existentes. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        for (JsonNode item : read("Facciones.json")) {
            String code = item.path("code").asText();
            if (factions.findByCode(code).isEmpty()) {
                factions.save(new Faction(code, item.path("name").asText(), item.path("description").asText()));
            }
        }
        for (JsonNode item : read("Comandantes.json")) {
            String code = item.path("code").asText();
            Faction faction = factions.findByCode(item.path("factionCode").asText())
                .orElseThrow(() -> new IllegalStateException("Faccion de comandante inexistente"));
            if (commanders.findByCode(code).isEmpty()) {
                commanders.save(new Commander(code, item.path("name").asText(), item.path("description").asText(),
                    faction, item.path("passiveAbilityName").asText(), item.path("passiveAbilityDescription").asText(),
                    item.path("passiveEffectCode").asText(), item.path("powerName").asText(),
                    item.path("powerDescription").asText(), item.path("powerEffectCode").asText(),
                    item.path("powerCost").asInt()));
            }
        }
        for (JsonNode item : read("Unidades.json")) {
            UnitType type = UnitType.valueOf(item.path("unitType").asText());
            UnitDefinition unit = units.findByUnitType(type).orElseGet(() -> new UnitDefinition(type,
                item.path("name").asText(), UnitCategory.valueOf(item.path("category").asText()),
                MovementType.valueOf(item.path("movementType").asText()), item.path("productionCost").asInt(),
                item.path("maxHealth").asInt(), item.path("movementPoints").asInt(),
                item.path("usesFuel").asBoolean(), item.path("maxFuel").asInt(),
                item.path("fuelConsumptionPerTurn").asInt(), item.path("fuelPerMovementPoint").asInt(),
                item.path("captureCapable").asBoolean()));
            unit.setName(item.path("name").asText());
            unit.setCategory(UnitCategory.valueOf(item.path("category").asText()));
            unit.setMovementType(MovementType.valueOf(item.path("movementType").asText()));
            unit.setProductionCost(item.path("productionCost").asInt());
            unit.setMaxHealth(item.path("maxHealth").asInt());
            unit.setMovementPoints(item.path("movementPoints").asInt());
            unit.setUsesFuel(item.path("usesFuel").asBoolean());
            unit.setMaxFuel(item.path("maxFuel").asInt());
            unit.setFuelConsumptionPerTurn(item.path("fuelConsumptionPerTurn").asInt());
            unit.setFuelPerMovementPoint(item.path("fuelPerMovementPoint").asInt());
            unit.setCaptureCapable(item.path("captureCapable").asBoolean());
            for (JsonNode itemWeapon : item.path("weapons")) {
                String code = itemWeapon.path("code").asText();
                Weapon weapon = weapons.findByCode(code).orElseGet(() -> new Weapon(code,
                    itemWeapon.path("name").asText(), itemWeapon.path("minRange").asInt(),
                    itemWeapon.path("maxRange").asInt(), itemWeapon.path("usesAmmunition").asBoolean(),
                    itemWeapon.path("ammunitionCapacity").asInt()));
                weapon.setName(itemWeapon.path("name").asText());
                weapon.setMinRange(itemWeapon.path("minRange").asInt());
                weapon.setMaxRange(itemWeapon.path("maxRange").asInt());
                weapon.setUsesAmmunition(itemWeapon.path("usesAmmunition").asBoolean());
                weapon.setAmmunitionCapacity(itemWeapon.path("ammunitionCapacity").asInt());
                var damages = new java.util.HashMap<UnitType, Integer>();
                itemWeapon.path("damageByTarget").fields().forEachRemaining(entry ->
                    damages.put(UnitType.valueOf(entry.getKey()), entry.getValue().asInt()));
                weapon.setDamageByTarget(damages);
                unit.addWeapon(weapons.save(weapon));
            }
            units.save(unit);
        }
    }

    @Transactional(readOnly = true)
    public List<FactionCatalogDTO> getFactions() {
        List<FactionCatalogDTO> result = new ArrayList<>();
        for (Faction faction : factions.findAllByOrderByNameAsc()) {
            String color = switch (faction.getCode()) {
                case "RED" -> "#d63031";
                case "BLUE" -> "#0984e3";
                case "GREEN" -> "#00b894";
                case "YELLOW" -> "#fdcb6e";
                default -> "#819198";
            };
            result.add(new FactionCatalogDTO(faction.getId(), faction.getCode(), faction.getName(),
                    faction.getDescription(), color));
        }
        return result;
    }

    /** Devuelve los comandantes como DTO, conservando los mismos nombres JSON. */
    @Transactional(readOnly = true)
    public List<CommanderCatalogDTO> getCommanders() {
        List<CommanderCatalogDTO> result = new ArrayList<>();
        for (Commander commander : commanders.findAllByOrderByNameAsc()) {
            result.add(new CommanderCatalogDTO(commander.getId(), commander.getCode(),
                    commander.getName(), commander.getDescription(), commander.getFaction().getCode(),
                    commander.getPowerName(), commander.getPowerCost(), commander.getPowerDescription(),
                    commander.getPowerEffectCode(), commander.getPassiveEffectCode(),
                    commander.getPassiveAbilityName(), commander.getPassiveAbilityDescription()));
        }
        return result;
    }

    /** Devuelve unidades y armas como DTO, sin exponer relaciones JPA. */
    @Transactional(readOnly = true)
    public List<UnitCatalogDTO> getUnits() {
        List<UnitCatalogDTO> result = new ArrayList<>();
        for (UnitDefinition unit : units.findAllByOrderByNameAsc()) {
            List<WeaponCatalogDTO> armament = new ArrayList<>();
            for (Weapon weapon : unit.getWeapons()) {
                armament.add(new WeaponCatalogDTO(weapon.getCode(), weapon.getName(),
                        weapon.getMinRange(), weapon.getMaxRange(), weapon.isUsesAmmunition(),
                        weapon.getAmmunitionCapacity(), weapon.getDamageByTarget()));
            }
            result.add(new UnitCatalogDTO(unit.getUnitType().name(), unit.getName(),
                    unit.getCategory().name(), unit.getMovementType().name(), unit.getProductionCost(),
                    unit.getMovementPoints(), unit.getMaxHealth(), unit.getMaxFuel(), unit.isCaptureCapable(),
                    unit.isUsesFuel(), unit.getFuelConsumptionPerTurn(), unit.getFuelPerMovementPoint(), armament));
        }
        return result;
    }
}
