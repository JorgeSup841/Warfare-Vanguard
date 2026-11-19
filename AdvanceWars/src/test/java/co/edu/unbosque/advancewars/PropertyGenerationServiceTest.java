package co.edu.unbosque.advancewars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import co.edu.unbosque.advancewars.dto.MapDTO;
import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.enums.TerrainType;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Player;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.PropertyDefinition;
import co.edu.unbosque.advancewars.repository.PropertyDefinitionRepository;
import co.edu.unbosque.advancewars.service.MapGenerationService;
import co.edu.unbosque.advancewars.service.PropertyGenerationService;

public class PropertyGenerationServiceTest {

    // Prepara los tipos de edificios sin necesidad de MySQL ni seguridad.
    private PropertyGenerationService createService() {
        PropertyDefinitionRepository repository = mock(PropertyDefinitionRepository.class);

        for (PropertyType type : PropertyType.values()) {
            PropertyDefinition definition = new PropertyDefinition(
                    type, type.name(), 1000, 3, 20, Set.of());
            when(repository.findByType(type)).thenReturn(Optional.of(definition));
        }
        return new PropertyGenerationService(repository);
    }

    private GameState createGame(int width, int height, int players) {
        GameState game = new GameState(12345L);
        game.setGameMap(new MapGenerationService().generateMap(width, height, 12345L));
        for (int i = 1; i <= players; i++) {
            game.addPlayer(new Player("Jugador " + i, i, 0));
        }
        return game;
    }

    @Test
    void createsOneOwnedHeadquartersPerPlayer() {
        GameState game = createGame(15, 12, 4);
        List<Property> properties = createService().generateProperties(game);

        long headquarters = properties.stream()
                .filter(p -> p.getDefinition().getType() == PropertyType.HEADQUARTERS)
                .count();
        long owned = properties.stream().filter(p -> p.getOwner() != null).count();
        long factories = properties.stream()
                .filter(p -> p.getDefinition().getType() == PropertyType.FACTORY)
                .count();

        assertEquals(4, headquarters);
        assertEquals(4, owned);
        assertEquals(4, factories);
    }

    @Test
    void noTwoBuildingsShareOneTile() {
        GameState game = createGame(15, 12, 4);
        List<Property> properties = createService().generateProperties(game);
        Set<Position> positions = new HashSet<>();

        for (Property property : properties) {
            assertTrue(positions.add(property.getTile().getPosition()));
            assertEquals(TerrainType.PLAIN, property.getTile().getTerrainType());
        }
    }

    @Test
    void portsAlwaysTouchWater() {
        GameState game = createGame(15, 12, 2);
        List<Property> properties = createService().generateProperties(game);

        for (Property property : properties) {
            if (property.getDefinition().getType() == PropertyType.PORT) {
                boolean coastal = game.getGameMap()
                        .findNeighborTiles(property.getTile().getPosition())
                        .stream().anyMatch(t -> t.getTerrainType() == TerrainType.WATER);
                assertTrue(coastal);
            }
        }
    }

    @Test
    void smallMapSupportsFourPlayersAndMapDto() {
        GameState game = createGame(5, 5, 4);
        List<Property> properties = createService().generateProperties(game);
        MapDTO dto = new MapDTO(game, properties);

        assertEquals(25, dto.getTiles().size());
        assertEquals(4, properties.stream()
                .filter(p -> p.getDefinition().getType() == PropertyType.HEADQUARTERS)
                .count());
    }
}
