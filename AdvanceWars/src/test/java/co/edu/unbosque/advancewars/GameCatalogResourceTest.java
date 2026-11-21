package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.unbosque.advancewars.enums.UnitType;

/** Comprueba que los recursos de catálogo coinciden con los tipos del juego. */
class GameCatalogResourceTest {
    private final ObjectMapper parser = new ObjectMapper();

    /** Lee un recurso de catálogo sin utilizar la base de datos. */
    private JsonNode read(String filename) throws IOException {
        try (var input = new ClassPathResource("game-catalog/" + filename).getInputStream()) {
            return parser.readTree(input);
        }
    }

    /** Verifica cuatro facciones, cuatro comandantes y los 16 tipos de unidad. */
    @Test
    void coversAllUnitsAndFourFactions() throws IOException {
        var factions = read("Facciones.json");
        var commanders = read("Comandantes.json");
        var units = read("Unidades.json");
        assertEquals(4, factions.size());
        assertEquals(4, commanders.size());
        assertEquals(16, units.size());
        Set<String> types = new HashSet<>();
        for (JsonNode unit : units) {
            assertTrue(unit.path("productionCost").asInt() > 0);
            assertTrue(unit.path("movementPoints").asInt() > 0);
            assertTrue(types.add(unit.path("unitType").asText()));
        }
        for (UnitType type : UnitType.values()) assertTrue(types.contains(type.name()));
    }
}
