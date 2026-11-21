package co.edu.unbosque.advancewars.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * Gestiona los catálogos del juego desde definiciones Java y los persiste por
 * JPA. La inicialización se invoca desde LoadDatabase, no mediante eventos
 * duplicados.
 */
@Service
public class GameCatalogService {

	private record FactionData(String code, String name, String description, String color) {
	}

	private record CommanderData(String code, String name, String description, String factionCode,
			String passiveAbilityName, String passiveAbilityDescription, String passiveEffectCode, String powerName,
			String powerDescription, String powerEffectCode, int powerCost) {
	}

	private record WeaponData(String code, String name, int minRange, int maxRange, boolean usesAmmunition,
			int ammunitionCapacity, Map<UnitType, Integer> damages) {
	}

	private record UnitData(UnitType type, String name, UnitCategory category, MovementType movementType,
			int productionCost, int maxHealth, int movementPoints, boolean usesFuel, int maxFuel,
			int fuelConsumptionPerTurn, int fuelPerMovementPoint, boolean captureCapable, List<WeaponData> weapons) {
	}

	/** Los datos anteriormente almacenados en Facciones.json. */
	private static final List<FactionData> FACTION_DATA = List.of(
			new FactionData("RED", "Ejército Carmesí", "Fuerza equilibrada.", "#d63031"),
			new FactionData("BLUE", "Legión Azur", "Especialistas en defensa.", "#0984e3"),
			new FactionData("GREEN", "Frente Esmeralda", "Dominan el aire.", "#00b894"),
			new FactionData("YELLOW", "Guardia Ámbar", "Maestros de la artillería.", "#fdcb6e"));

	/** Los datos anteriormente almacenados en Comandantes.json. */
	private static final List<CommanderData> COMMANDER_DATA = List.of(
			new CommanderData("CMD_RED", "Comandante Rojo", "Líder del Ejército Carmesí.", "RED", "Disciplina",
					"+10% de ataque a la infantería.", "INFANTRY_ATTACK_10", "Carga",
					"Todas las unidades +1 de movimiento este turno.", "MOVE_PLUS_1", 3),
			new CommanderData("CMD_BLUE", "Comandante Azul", "Líder de la Legión Azur.", "BLUE", "Muralla",
					"+10% de defensa a todas las unidades.", "DEFENSE_10", "Reparación",
					"Todas las unidades recuperan 20 de vida.", "HEAL_20", 3),
			new CommanderData("CMD_GREEN", "Comandante Verde", "Líder del Frente Esmeralda.", "GREEN", "Alas",
					"+15% de ataque a unidades aéreas.", "AIR_ATTACK_15", "Tormenta",
					"Las unidades enemigas pierden 1 de movimiento.", "ENEMY_MOVE_MINUS_1", 4),
			new CommanderData("CMD_YELLOW", "Comandante Amarillo", "Líder de la Guardia Ámbar.", "YELLOW", "Precisión",
					"+1 de alcance a la artillería.", "ARTILLERY_RANGE_1", "Bombardeo",
					"+20% de ataque a distancia este turno.", "RANGED_ATTACK_20", 4));

	/**
	 * Los datos anteriormente almacenados en Unidades.json: dieciséis unidades y
	 * veinte armas.
	 */
	private static final List<UnitData> UNIT_DATA = List.of(new UnitData(UnitType.INFANTRY, "Infantería",
			UnitCategory.LAND, MovementType.FOOT, 1000, 100, 3, true, 99, 0, 1, true,

			List.of(new WeaponData("INFANTRY_MG", "Ametralladora", 1, 1, false, 0, Map.ofEntries(
					Map.entry(UnitType.INFANTRY, 55), Map.entry(UnitType.MECH, 45), Map.entry(UnitType.RECON, 12),
					Map.entry(UnitType.TANK, 5), Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 15),
					Map.entry(UnitType.ANTI_AIR, 5), Map.entry(UnitType.ROCKET, 25), Map.entry(UnitType.MISSILE, 25),
					Map.entry(UnitType.BATTLE_COPTER, 7), Map.entry(UnitType.TRANSPORT_COPTER, 30))))),
			new UnitData(UnitType.MECH, "Mecanizada", UnitCategory.LAND, MovementType.MECH, 3000, 100, 2, true, 70, 0,
					1, true,

					List.of(new WeaponData("MECH_BAZOOKA", "Bazuca", 1, 1, true, 3,
							Map.ofEntries(Map.entry(UnitType.RECON, 85), Map.entry(UnitType.TANK, 55),
									Map.entry(UnitType.MEDIUM_TANK, 15), Map.entry(UnitType.ARTILLERY, 70),
									Map.entry(UnitType.ANTI_AIR, 65), Map.entry(UnitType.ROCKET, 85),
									Map.entry(UnitType.MISSILE, 85))),
							new WeaponData("MECH_MG", "Ametralladora", 1, 1, false, 0,
									Map.ofEntries(Map.entry(UnitType.INFANTRY, 65), Map.entry(UnitType.MECH, 55),
											Map.entry(UnitType.RECON, 18), Map.entry(UnitType.TANK, 6),
											Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 32),
											Map.entry(UnitType.ANTI_AIR, 6), Map.entry(UnitType.ROCKET, 35),
											Map.entry(UnitType.MISSILE, 35), Map.entry(UnitType.BATTLE_COPTER, 9),
											Map.entry(UnitType.TRANSPORT_COPTER, 35))))),
			new UnitData(UnitType.RECON, "Reconocimiento", UnitCategory.LAND, MovementType.WHEEL, 4000, 100, 8, true,
					80, 0, 1, false,

					List.of(new WeaponData("RECON_MG", "Ametralladora", 1, 1, false, 0,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 70), Map.entry(UnitType.MECH, 65),
									Map.entry(UnitType.RECON, 35), Map.entry(UnitType.TANK, 6),
									Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 45),
									Map.entry(UnitType.ANTI_AIR, 4), Map.entry(UnitType.ROCKET, 55),
									Map.entry(UnitType.MISSILE, 28), Map.entry(UnitType.BATTLE_COPTER, 10),
									Map.entry(UnitType.TRANSPORT_COPTER, 35))))),
			new UnitData(UnitType.TANK, "Tanque", UnitCategory.LAND, MovementType.TREAD, 7000, 100, 6, true, 70, 0, 1,
					false,

					List.of(new WeaponData("TANK_CANNON", "Cañón", 1, 1, true, 9,
							Map.ofEntries(Map.entry(UnitType.RECON, 85), Map.entry(UnitType.TANK, 55),
									Map.entry(UnitType.MEDIUM_TANK, 15), Map.entry(UnitType.ARTILLERY, 70),
									Map.entry(UnitType.ANTI_AIR, 65), Map.entry(UnitType.ROCKET, 85),
									Map.entry(UnitType.MISSILE, 85), Map.entry(UnitType.BATTLESHIP, 1),
									Map.entry(UnitType.CRUISER, 5), Map.entry(UnitType.SUBMARINE, 1))),
							new WeaponData("TANK_MG", "Ametralladora", 1, 1, false, 0,
									Map.ofEntries(Map.entry(UnitType.INFANTRY, 75), Map.entry(UnitType.MECH, 70),
											Map.entry(UnitType.RECON, 40), Map.entry(UnitType.TANK, 6),
											Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 45),
											Map.entry(UnitType.ANTI_AIR, 5), Map.entry(UnitType.ROCKET, 55),
											Map.entry(UnitType.MISSILE, 30), Map.entry(UnitType.BATTLE_COPTER, 10),
											Map.entry(UnitType.TRANSPORT_COPTER, 40))))),
			new UnitData(UnitType.MEDIUM_TANK, "Tanque mediano", UnitCategory.LAND, MovementType.TREAD, 16000, 100, 5,
					true, 50, 0, 1, false,

					List.of(new WeaponData("MEDIUM_TANK_CANNON", "Cañón pesado", 1, 1, true, 8,
							Map.ofEntries(Map.entry(UnitType.RECON, 105), Map.entry(UnitType.TANK, 85),
									Map.entry(UnitType.MEDIUM_TANK, 55), Map.entry(UnitType.ARTILLERY, 105),
									Map.entry(UnitType.ANTI_AIR, 105), Map.entry(UnitType.ROCKET, 105),
									Map.entry(UnitType.MISSILE, 105), Map.entry(UnitType.BATTLESHIP, 10),
									Map.entry(UnitType.CRUISER, 45), Map.entry(UnitType.SUBMARINE, 10))),
							new WeaponData("MEDIUM_TANK_MG", "Ametralladora", 1, 1, false, 0,
									Map.ofEntries(Map.entry(UnitType.INFANTRY, 105), Map.entry(UnitType.MECH, 95),
											Map.entry(UnitType.RECON, 45), Map.entry(UnitType.TANK, 8),
											Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 45),
											Map.entry(UnitType.ANTI_AIR, 7), Map.entry(UnitType.ROCKET, 55),
											Map.entry(UnitType.MISSILE, 35), Map.entry(UnitType.BATTLE_COPTER, 12),
											Map.entry(UnitType.TRANSPORT_COPTER, 45))))),
			new UnitData(UnitType.ARTILLERY, "Artillería", UnitCategory.LAND, MovementType.TREAD, 6000, 100, 5, true,
					50, 0, 1, false,

					List.of(new WeaponData("ARTILLERY_CANNON", "Cañón de artillería", 2, 3, true, 9,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 90), Map.entry(UnitType.MECH, 85),
									Map.entry(UnitType.RECON, 80), Map.entry(UnitType.TANK, 70),
									Map.entry(UnitType.MEDIUM_TANK, 45), Map.entry(UnitType.ARTILLERY, 75),
									Map.entry(UnitType.ANTI_AIR, 75), Map.entry(UnitType.ROCKET, 80),
									Map.entry(UnitType.MISSILE, 80), Map.entry(UnitType.BATTLESHIP, 40),
									Map.entry(UnitType.CRUISER, 50), Map.entry(UnitType.SUBMARINE, 60))))),
			new UnitData(UnitType.ANTI_AIR, "Antiaéreo", UnitCategory.LAND, MovementType.TREAD, 8000, 100, 6, true, 60,
					0, 1, false,

					List.of(new WeaponData("ANTI_AIR_VULCAN", "Cañón Vulcan", 1, 1, true, 9,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 105), Map.entry(UnitType.MECH, 105),
									Map.entry(UnitType.RECON, 60), Map.entry(UnitType.TANK, 25),
									Map.entry(UnitType.MEDIUM_TANK, 10), Map.entry(UnitType.ARTILLERY, 50),
									Map.entry(UnitType.ANTI_AIR, 45), Map.entry(UnitType.ROCKET, 55),
									Map.entry(UnitType.MISSILE, 55), Map.entry(UnitType.FIGHTER, 65),
									Map.entry(UnitType.BOMBER, 75), Map.entry(UnitType.BATTLE_COPTER, 120),
									Map.entry(UnitType.TRANSPORT_COPTER, 120))))),
			new UnitData(UnitType.ROCKET, "Lanzacohetes", UnitCategory.LAND, MovementType.WHEEL, 15000, 100, 5, true,
					50, 0, 1, false,

					List.of(new WeaponData("ROCKET_LAUNCHER", "Cohetes", 3, 5, true, 6,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 95), Map.entry(UnitType.MECH, 90),
									Map.entry(UnitType.RECON, 90), Map.entry(UnitType.TANK, 80),
									Map.entry(UnitType.MEDIUM_TANK, 55), Map.entry(UnitType.ARTILLERY, 80),
									Map.entry(UnitType.ANTI_AIR, 85), Map.entry(UnitType.ROCKET, 85),
									Map.entry(UnitType.MISSILE, 90), Map.entry(UnitType.BATTLESHIP, 55),
									Map.entry(UnitType.CRUISER, 60), Map.entry(UnitType.SUBMARINE, 85))))),
			new UnitData(UnitType.MISSILE, "Lanzamisiles", UnitCategory.LAND, MovementType.WHEEL, 12000, 100, 4, true,
					50, 0, 1, false,

					List.of(new WeaponData("MISSILE_LAUNCHER", "Misiles antiaéreos", 3, 5, true, 6,
							Map.ofEntries(Map.entry(UnitType.FIGHTER, 100), Map.entry(UnitType.BOMBER, 100),
									Map.entry(UnitType.BATTLE_COPTER, 120),
									Map.entry(UnitType.TRANSPORT_COPTER, 120))))),
			new UnitData(UnitType.FIGHTER, "Caza", UnitCategory.AIR, MovementType.AIR, 20000, 100, 9, true, 99, 5, 1,
					false,

					List.of(new WeaponData("FIGHTER_MISSILES", "Misiles aire-aire", 1, 1, true, 9,
							Map.ofEntries(Map.entry(UnitType.FIGHTER, 55), Map.entry(UnitType.BOMBER, 100),
									Map.entry(UnitType.BATTLE_COPTER, 100),
									Map.entry(UnitType.TRANSPORT_COPTER, 100))))),
			new UnitData(UnitType.BOMBER, "Bombardero", UnitCategory.AIR, MovementType.AIR, 22000, 100, 7, true, 99, 5,
					1, false,

					List.of(new WeaponData("BOMBER_BOMBS", "Bombas", 1, 1, true, 9,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 110), Map.entry(UnitType.MECH, 110),
									Map.entry(UnitType.RECON, 105), Map.entry(UnitType.TANK, 105),
									Map.entry(UnitType.MEDIUM_TANK, 95), Map.entry(UnitType.ARTILLERY, 105),
									Map.entry(UnitType.ANTI_AIR, 95), Map.entry(UnitType.ROCKET, 105),
									Map.entry(UnitType.MISSILE, 105), Map.entry(UnitType.BATTLESHIP, 75),
									Map.entry(UnitType.CRUISER, 85), Map.entry(UnitType.SUBMARINE, 95))))),
			new UnitData(UnitType.BATTLE_COPTER, "Helicóptero de combate", UnitCategory.AIR, MovementType.AIR, 9000,
					100, 6, true, 99, 2, 1, false,

					List.of(new WeaponData("COPTER_MISSILES", "Misiles", 1, 1, true, 6,
							Map.ofEntries(Map.entry(UnitType.RECON, 55), Map.entry(UnitType.TANK, 55),
									Map.entry(UnitType.MEDIUM_TANK, 25), Map.entry(UnitType.ARTILLERY, 65),
									Map.entry(UnitType.ANTI_AIR, 25), Map.entry(UnitType.ROCKET, 65),
									Map.entry(UnitType.MISSILE, 65), Map.entry(UnitType.BATTLESHIP, 25),
									Map.entry(UnitType.CRUISER, 55), Map.entry(UnitType.SUBMARINE, 25))),
							new WeaponData("COPTER_MG", "Ametralladora", 1, 1, false, 0,
									Map.ofEntries(Map.entry(UnitType.INFANTRY, 75), Map.entry(UnitType.MECH, 75),
											Map.entry(UnitType.RECON, 30), Map.entry(UnitType.TANK, 6),
											Map.entry(UnitType.MEDIUM_TANK, 1), Map.entry(UnitType.ARTILLERY, 25),
											Map.entry(UnitType.ANTI_AIR, 6), Map.entry(UnitType.ROCKET, 35),
											Map.entry(UnitType.MISSILE, 35), Map.entry(UnitType.BATTLE_COPTER, 65),
											Map.entry(UnitType.TRANSPORT_COPTER, 95))))),
			new UnitData(UnitType.TRANSPORT_COPTER, "Helicóptero de transporte", UnitCategory.AIR, MovementType.AIR,
					5000, 100, 6, true, 99, 2, 1, false,

					List.of()),
			new UnitData(UnitType.BATTLESHIP, "Acorazado", UnitCategory.NAVAL, MovementType.SHIP, 28000, 100, 5, true,
					99, 1, 1, false,

					List.of(new WeaponData("BATTLESHIP_CANNON", "Cañón naval", 2, 6, true, 9,
							Map.ofEntries(Map.entry(UnitType.INFANTRY, 95), Map.entry(UnitType.MECH, 90),
									Map.entry(UnitType.RECON, 90), Map.entry(UnitType.TANK, 80),
									Map.entry(UnitType.MEDIUM_TANK, 55), Map.entry(UnitType.ARTILLERY, 80),
									Map.entry(UnitType.ANTI_AIR, 85), Map.entry(UnitType.ROCKET, 85),
									Map.entry(UnitType.MISSILE, 90), Map.entry(UnitType.BATTLESHIP, 50),
									Map.entry(UnitType.CRUISER, 95), Map.entry(UnitType.SUBMARINE, 95))))),
			new UnitData(UnitType.CRUISER, "Crucero", UnitCategory.NAVAL, MovementType.SHIP, 18000, 100, 6, true, 99, 1,
					1, false,

					List.of(new WeaponData("CRUISER_TORPEDOES", "Torpedos", 1, 1, true, 9,
							Map.ofEntries(Map.entry(UnitType.SUBMARINE, 90))),
							new WeaponData("CRUISER_AA", "Antiaéreo", 1, 1, false, 0,
									Map.ofEntries(Map.entry(UnitType.FIGHTER, 55), Map.entry(UnitType.BOMBER, 65),
											Map.entry(UnitType.BATTLE_COPTER, 115),
											Map.entry(UnitType.TRANSPORT_COPTER, 115))))),
			new UnitData(UnitType.SUBMARINE, "Submarino", UnitCategory.NAVAL, MovementType.SHIP, 20000, 100, 5, true,
					60, 1, 1, false,

					List.of(new WeaponData("SUBMARINE_TORPEDOES", "Torpedos", 1, 1, true, 6,
							Map.ofEntries(Map.entry(UnitType.BATTLESHIP, 55), Map.entry(UnitType.CRUISER, 25),
									Map.entry(UnitType.SUBMARINE, 55))))));

	private final FactionRepository factions;
	private final CommanderRepository commanders;
	private final UnitDefinitionRepository units;
	private final WeaponRepository weapons;

	public GameCatalogService(FactionRepository factions, CommanderRepository commanders,
			UnitDefinitionRepository units, WeaponRepository weapons) {
		this.factions = factions;
		this.commanders = commanders;
		this.units = units;
		this.weapons = weapons;
	}

	/**
	 * Inicializa los catálogos dentro de una transacción, manteniendo las claves de
	 * datos ya existentes. Facciones y comandantes se insertan solo cuando faltan;
	 * las unidades y armas se sincronizan con sus definiciones Java, igual que con
	 * el catálogo original.
	 */
	@Transactional
	public void seed() {
		seedFactions();
		seedCommanders();
		seedUnits();
	}

	private void seedFactions() {
		for (FactionData data : FACTION_DATA) {
			if (factions.findByCode(data.code()).isEmpty()) {
				factions.save(new Faction(data.code(), data.name(), data.description()));
			}
		}
	}

	private void seedCommanders() {
		for (CommanderData data : COMMANDER_DATA) {
			if (commanders.findByCode(data.code()).isPresent()) {
				continue;
			}
			Faction faction = factions.findByCode(data.factionCode()).orElseThrow(
					() -> new IllegalStateException("Faccion de comandante inexistente: " + data.factionCode()));
			commanders.save(new Commander(data.code(), data.name(), data.description(), faction,
					data.passiveAbilityName(), data.passiveAbilityDescription(), data.passiveEffectCode(),
					data.powerName(), data.powerDescription(), data.powerEffectCode(), data.powerCost()));
		}
	}

	private void seedUnits() {
		for (UnitData data : UNIT_DATA) {
			UnitDefinition unit = units.findByUnitType(data.type())
					.orElseGet(() -> new UnitDefinition(data.type(), data.name(), data.category(), data.movementType(),
							data.productionCost(), data.maxHealth(), data.movementPoints(), data.usesFuel(),
							data.maxFuel(), data.fuelConsumptionPerTurn(), data.fuelPerMovementPoint(),
							data.captureCapable()));
			unit.setName(data.name());
			unit.setCategory(data.category());
			unit.setMovementType(data.movementType());
			unit.setProductionCost(data.productionCost());
			unit.setMaxHealth(data.maxHealth());
			unit.setMovementPoints(data.movementPoints());
			unit.setUsesFuel(data.usesFuel());
			unit.setMaxFuel(data.maxFuel());
			unit.setFuelConsumptionPerTurn(data.fuelConsumptionPerTurn());
			unit.setFuelPerMovementPoint(data.fuelPerMovementPoint());
			unit.setCaptureCapable(data.captureCapable());

			Set<Weapon> armament = new HashSet<>();
			for (WeaponData weaponData : data.weapons()) {
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

	private static String factionColor(String code) {
		for (FactionData data : FACTION_DATA) {
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
