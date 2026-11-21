package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.edu.unbosque.advancewars.dto.CommanderCatalogDTO;
import co.edu.unbosque.advancewars.dto.FactionCatalogDTO;
import co.edu.unbosque.advancewars.dto.UnitCatalogDTO;
import co.edu.unbosque.advancewars.dto.WeaponCatalogDTO;
import co.edu.unbosque.advancewars.enums.UnitType;

/** Verifica la compatibilidad JSON de los DTO del catalogo con Angular. */
class GameCatalogDTOContractTest {

    private final ObjectMapper json = new ObjectMapper();

    /** Comprueba que los nombres de los campos de faccion y comandante no cambiaron. */
    @Test
    void factionAndCommanderKeepJsonFieldNames() {
        FactionCatalogDTO faction = new FactionCatalogDTO(1L, "RED", "Red", "Red faction", "#d63031");
        JsonNode factionJson = json.valueToTree(faction);
        assertEquals("RED", factionJson.path("code").asText());
        assertEquals("#d63031", factionJson.path("color").asText());

        CommanderCatalogDTO commander = new CommanderCatalogDTO(1L, "CMD_RED", "Leader", "Description",
                "RED", "Charge", 3, "More movement", "MOVE_PLUS_1", "PASSIVE", "Skill", "Skill description");
        JsonNode commanderJson = json.valueToTree(commander);
        assertEquals("RED", commanderJson.path("factionCode").asText());
        assertEquals(3, commanderJson.path("powerCost").asInt());
        assertEquals("Charge", commanderJson.path("powerName").asText());
    }

    /** Garantiza que el catalogo de unidad conserve sus armas y sus datos de combate. */
    @Test
    void unitKeepsJsonWeaponStructure() {
        WeaponCatalogDTO weapon = new WeaponCatalogDTO("MAIN", "Cannon", 1, 2, true, 6,
                Map.of(UnitType.TANK, 45));
        UnitCatalogDTO unit = new UnitCatalogDTO("TANK", "Tank", "LAND", "TREAD", 7000, 6,
                100, 70, false, true, 0, 1, List.of(weapon));
        JsonNode result = json.valueToTree(unit);
        assertEquals("TANK", result.path("unitType").asText());
        assertEquals("LAND", result.path("category").asText());
        assertTrue(result.path("weapons").isArray());
        assertFalse(result.path("weapons").isEmpty());
        assertEquals(45, result.path("weapons").get(0).path("damageByTarget").path("TANK").asInt());
    }
}
