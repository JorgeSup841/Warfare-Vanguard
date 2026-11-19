
package co.edu.unbosque.advancewars.dto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unbosque.advancewars.model.GameMap;
import co.edu.unbosque.advancewars.model.GameState;
import co.edu.unbosque.advancewars.model.Position;
import co.edu.unbosque.advancewars.model.Property;
import co.edu.unbosque.advancewars.model.Tile;

public class MapDTO {

    private Long gameId;
    private int width;
    private int height;
    private List<TileDTO> tiles = new ArrayList<>();

    public MapDTO(GameState game, List<Property> properties) {

        this.gameId = game.getId();

        GameMap map = game.getGameMap();

        this.width = map.getWidth();
        this.height = map.getHeight();

        Map<Position, Property> byPosition = new HashMap<>();

        for (Property property : properties) {
            byPosition.put(
                property.getTile().getPosition(),
                property
            );
        }

        List<Tile> sortedTiles = new ArrayList<>(map.getTiles());

        sortedTiles.sort(
            Comparator.comparingInt(
                (Tile tile) -> tile.getPosition().getY()
            ).thenComparingInt(
                tile -> tile.getPosition().getX()
            )
        );

        for (Tile tile : sortedTiles) {

            Property property = byPosition.get(tile.getPosition());

            tiles.add(new TileDTO(tile, property));
        }
    }

    public Long getGameId() {
        return gameId;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public List<TileDTO> getTiles() {
        return tiles;
    }
}
