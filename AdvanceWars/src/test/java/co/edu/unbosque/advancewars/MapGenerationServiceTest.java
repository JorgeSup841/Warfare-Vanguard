package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.service.MapGenerationService;

public class MapGenerationServiceTest {

    private final MapGenerationService service = new MapGenerationService();

    @Test
    void createsWholeMap() {
        GameMap map = service.generateMap(15, 12, 12345L);
        assertEquals(15, map.getWidth());
        assertEquals(12, map.getHeight());
        assertEquals(180, map.getTileCount());
        assertTrue(map.isComplete());
    }

    @Test
    void sameSeedProducesSameTerrain() {
        GameMap first = service.generateMap(15, 12, 12345L);
        GameMap second = service.generateMap(15, 12, 12345L);

        for (int y = 0; y < 12; y++) {
            for (int x = 0; x < 15; x++) {
                TerrainType a = first.findTileAt(x, y).orElseThrow().getTerrainType();
                TerrainType b = second.findTileAt(x, y).orElseThrow().getTerrainType();
                assertEquals(a, b);
            }
        }
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> service.generateMap(4, 15, 1L));
        assertThrows(IllegalArgumentException.class, () -> service.generateMap(51, 15, 1L));
    }

    @Test
    void basePositionsAreNeverWater() {
        GameMap map = service.generateMap(15, 12, 12345L);
        assertNotEquals(TerrainType.WATER, map.findTileAt(1, 1).orElseThrow().getTerrainType());
        assertNotEquals(TerrainType.WATER, map.findTileAt(13, 1).orElseThrow().getTerrainType());
        assertNotEquals(TerrainType.WATER, map.findTileAt(1, 10).orElseThrow().getTerrainType());
        assertNotEquals(TerrainType.WATER, map.findTileAt(13, 10).orElseThrow().getTerrainType());
    }
}
