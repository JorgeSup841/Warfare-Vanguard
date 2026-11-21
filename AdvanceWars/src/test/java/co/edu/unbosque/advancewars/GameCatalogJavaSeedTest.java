package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.Commander;
import co.edu.unbosque.advancewars.model.Faction;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import co.edu.unbosque.advancewars.model.Weapon;
import co.edu.unbosque.advancewars.repository.CommanderRepository;
import co.edu.unbosque.advancewars.repository.FactionRepository;
import co.edu.unbosque.advancewars.repository.UnitDefinitionRepository;
import co.edu.unbosque.advancewars.repository.WeaponRepository;
import co.edu.unbosque.advancewars.service.GameCatalogService;

/** Comprueba que la inicialización de catálogos Java no necesita archivos JSON. */
class GameCatalogJavaSeedTest {

    /** Verifica el catálogo completo y que reiniciarlo no duplica registros. */
    @Test
    void seedsAllCatalogsAndPreservesExistingObjects() {
        FactionRepository factionRepository = mock(FactionRepository.class);
        CommanderRepository commanderRepository = mock(CommanderRepository.class);
        UnitDefinitionRepository unitRepository = mock(UnitDefinitionRepository.class);
        WeaponRepository weaponRepository = mock(WeaponRepository.class);

        Map<String, Faction> factions = new HashMap<>();
        Map<String, Commander> commanders = new HashMap<>();
        Map<UnitType, UnitDefinition> units = new HashMap<>();
        Map<String, Weapon> weapons = new HashMap<>();

        when(factionRepository.findAllByOrderByNameAsc())
                .thenAnswer(call -> new ArrayList<>(factions.values()));
        when(factionRepository.findByCode(anyString()))
                .thenAnswer(call -> Optional.ofNullable(factions.get(call.getArgument(0))));
        when(commanderRepository.findByCode(anyString()))
                .thenAnswer(call -> Optional.ofNullable(commanders.get(call.getArgument(0))));
        when(unitRepository.findByUnitType(any(UnitType.class)))
                .thenAnswer(call -> Optional.ofNullable(units.get(call.getArgument(0))));
        when(weaponRepository.findByCode(anyString()))
                .thenAnswer(call -> Optional.ofNullable(weapons.get(call.getArgument(0))));

        when(factionRepository.save(any(Faction.class))).thenAnswer(call -> {
            Faction value = call.getArgument(0);
            factions.put(value.getCode(), value);
            return value;
        });
        when(commanderRepository.save(any(Commander.class))).thenAnswer(call -> {
            Commander value = call.getArgument(0);
            commanders.put(value.getCode(), value);
            return value;
        });
        when(unitRepository.save(any(UnitDefinition.class))).thenAnswer(call -> {
            UnitDefinition value = call.getArgument(0);
            units.put(value.getUnitType(), value);
            return value;
        });
        when(weaponRepository.save(any(Weapon.class))).thenAnswer(call -> {
            Weapon value = call.getArgument(0);
            weapons.put(value.getCode(), value);
            return value;
        });

        GameCatalogService service = new GameCatalogService(factionRepository, commanderRepository,
                unitRepository, weaponRepository);
        service.seed();

        assertEquals(4, factions.size());
        assertEquals(4, commanders.size());
        assertEquals(16, units.size());
        assertEquals(20, weapons.size());
        assertEquals(EnumSet.allOf(UnitType.class), units.keySet());
        assertEquals("#d63031", service.getFactions().stream()
                .filter(faction -> "RED".equals(faction.getCode()))
                .findFirst().orElseThrow().getColor());
        assertEquals("HEAL_20", commanders.get("CMD_BLUE").getPowerEffectCode());
        assertEquals(3, commanders.get("CMD_BLUE").getPowerCost());
        assertEquals(55, weapons.get("INFANTRY_MG").getDamageAgainst(UnitType.INFANTRY));
        assertTrue(units.get(UnitType.TRANSPORT_COPTER).getWeapons().isEmpty());

        Faction factionBefore = factions.get("RED");
        Commander commanderBefore = commanders.get("CMD_RED");
        UnitDefinition infantryBefore = units.get(UnitType.INFANTRY);
        Weapon machineGunBefore = weapons.get("INFANTRY_MG");

        service.seed();
        assertEquals(4, factions.size());
        assertEquals(4, commanders.size());
        assertEquals(16, units.size());
        assertEquals(20, weapons.size());
        assertSame(factionBefore, factions.get("RED"));
        assertSame(commanderBefore, commanders.get("CMD_RED"));
        assertSame(infantryBefore, units.get(UnitType.INFANTRY));
        assertSame(machineGunBefore, weapons.get("INFANTRY_MG"));
    }
}
