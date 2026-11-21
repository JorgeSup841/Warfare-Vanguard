package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.advancewars.dto.FactionCatalogDTO;
import co.edu.unbosque.advancewars.dto.CommanderCatalogDTO;
import co.edu.unbosque.advancewars.dto.UnitCatalogDTO;
import co.edu.unbosque.advancewars.dto.WeaponCatalogDTO;
import co.edu.unbosque.advancewars.model.Commander;
import co.edu.unbosque.advancewars.model.Faction;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.model.Weapon;
import co.edu.unbosque.advancewars.repository.CommanderRepository;
import co.edu.unbosque.advancewars.repository.FactionRepository;
import co.edu.unbosque.advancewars.repository.UnitDefinitionRepository;
import co.edu.unbosque.advancewars.repository.WeaponRepository;
import co.edu.unbosque.advancewars.service.GameCatalogData.FactionData;
import co.edu.unbosque.advancewars.service.GameCatalogData.CommanderData;
import co.edu.unbosque.advancewars.service.GameCatalogData.UnitData;
import co.edu.unbosque.advancewars.service.GameCatalogData.WeaponData;

/** Gestiona y publica los catalogos del juego. Los datos estan en catalogo.txt. */
@Service
public class GameCatalogService {

	private final FactionRepository factions;
	private final CommanderRepository commanders;
	private final UnitDefinitionRepository units;
	private final WeaponRepository weapons;
	private final GameCatalogData data;

	public GameCatalogService(FactionRepository factions, CommanderRepository commanders,
			UnitDefinitionRepository units, WeaponRepository weapons) {
		this.factions = factions;
		this.commanders = commanders;
		this.units = units;
		this.weapons = weapons;
		this.data = GameCatalogData.load();
	}

	/** Sincroniza las definiciones del TXT con las entidades existentes, sin duplicarlas. */
	@Transactional
	public void seed() {
		seedFactions();
		seedCommanders();
		seedUnits();
	}

	private void seedFactions() {
		for (FactionData item : data.factions()) {
			Faction faction = factions.findByCode(item.code())
					.orElseGet(() -> new Faction(item.code(), item.name(), item.description()));
			faction.setName(item.name());
			faction.setDescription(item.description());
			factions.save(faction);
		}
	}

	private void seedCommanders() {
		for (CommanderData item : data.commanders()) {
			Faction faction = factions.findByCode(item.factionCode()).orElseThrow(
					() -> new IllegalStateException("Faccion de comandante inexistente: " + item.factionCode()));
			Commander commander = commanders.findByCode(item.code()).orElseGet(() -> new Commander(
					item.code(), item.name(), item.description(), faction,
					item.passiveAbilityName(), item.passiveAbilityDescription(), item.passiveEffectCode(),
					item.powerName(), item.powerDescription(), item.powerEffectCode(), item.powerCost()));
			commander.setName(item.name());
			commander.setDescription(item.description());
			commander.setFaction(faction);
			commander.setPassiveAbilityName(item.passiveAbilityName());
			commander.setPassiveAbilityDescription(item.passiveAbilityDescription());
			commander.setPassiveEffectCode(item.passiveEffectCode());
			commander.setPowerName(item.powerName());
			commander.setPowerDescription(item.powerDescription());
			commander.setPowerEffectCode(item.powerEffectCode());
			commander.setPowerCost(item.powerCost());
			commanders.save(commander);
		}
	}

	private void seedUnits() {
		for (UnitData definition : data.units()) {
			UnitDefinition unit = units.findByUnitType(definition.type())
					.orElseGet(() -> new UnitDefinition(definition.type(), definition.name(), definition.category(), definition.movementType(),
							definition.productionCost(), definition.maxHealth(), definition.movementPoints(), definition.usesFuel(),
							definition.maxFuel(), definition.fuelConsumptionPerTurn(), definition.fuelPerMovementPoint(),
							definition.captureCapable()));
			unit.setName(definition.name());
			unit.setCategory(definition.category());
			unit.setMovementType(definition.movementType());
			unit.setProductionCost(definition.productionCost());
			unit.setMaxHealth(definition.maxHealth());
			unit.setMovementPoints(definition.movementPoints());
			unit.setUsesFuel(definition.usesFuel());
			unit.setMaxFuel(definition.maxFuel());
			unit.setFuelConsumptionPerTurn(definition.fuelConsumptionPerTurn());
			unit.setFuelPerMovementPoint(definition.fuelPerMovementPoint());
			unit.setCaptureCapable(definition.captureCapable());

			Set<Weapon> armament = new HashSet<>();
			for (WeaponData weaponData : definition.weapons()) {
				Weapon weapon = weapons.findByCode(weaponData.code())
						.orElseGet(() -> new Weapon(weaponData.code(), weaponData.name(), weaponData.minRange(),
								weaponData.maxRange(), weaponData.usesAmmunition(), weaponData.ammunitionCapacity()));
				weapon.setName(weaponData.name());
				weapon.setMinRange(weaponData.minRange());
				weapon.setMaxRange(weaponData.maxRange());
				weapon.setUsesAmmunition(weaponData.usesAmmunition());
				weapon.setAmmunitionCapacity(weaponData.ammunitionCapacity());
				weapon.setDamageByTarget(weaponData.damages());
				armament.add(weapons.save(weapon));
			}
			// Actualiza la relación sin reemplazar la colección persistente de Hibernate.
			for (Weapon previous : new HashSet<>(unit.getWeapons())) {
				boolean stillUsed = armament.stream()
						.anyMatch(candidate -> candidate.getCode().equals(previous.getCode()));
				if (!stillUsed) {
					unit.removeWeapon(previous);
				}
			}
			for (Weapon current : armament) {
				boolean alreadyLinked = unit.getWeapons().stream()
						.anyMatch(previous -> previous.getCode().equals(current.getCode()));
				if (!alreadyLinked) {
					unit.addWeapon(current);
				}
			}
			units.save(unit);
		}
	}

	private String factionColor(String code) {
		for (FactionData data : data.factions()) {
			if (data.code().equals(code)) {
				return data.color();
			}
		}
		return "#819198";
	}

	@Transactional(readOnly = true)
	public List<FactionCatalogDTO> getFactions() {
		List<FactionCatalogDTO> result = new ArrayList<>();
		for (Faction faction : factions.findAllByOrderByNameAsc()) {
			String color = factionColor(faction.getCode());
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
			result.add(new CommanderCatalogDTO(commander.getId(), commander.getCode(), commander.getName(),
					commander.getDescription(), commander.getFaction().getCode(), commander.getPowerName(),
					commander.getPowerCost(), commander.getPowerDescription(), commander.getPowerEffectCode(),
					commander.getPassiveEffectCode(), commander.getPassiveAbilityName(),
					commander.getPassiveAbilityDescription()));
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
				armament.add(new WeaponCatalogDTO(weapon.getCode(), weapon.getName(), weapon.getMinRange(),
						weapon.getMaxRange(), weapon.isUsesAmmunition(), weapon.getAmmunitionCapacity(),
						weapon.getDamageByTarget()));
			}
			result.add(new UnitCatalogDTO(unit.getUnitType().name(), unit.getName(), unit.getCategory().name(),
					unit.getMovementType().name(), unit.getProductionCost(), unit.getMovementPoints(),
					unit.getMaxHealth(), unit.getMaxFuel(), unit.isCaptureCapable(), unit.isUsesFuel(),
					unit.getFuelConsumptionPerTurn(), unit.getFuelPerMovementPoint(), armament));
		}
		return result;
	}
}
