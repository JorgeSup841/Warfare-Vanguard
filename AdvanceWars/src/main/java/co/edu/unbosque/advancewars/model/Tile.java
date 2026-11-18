
package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.TerrainType;
import jakarta.persistence.*;

/** Representa una casilla del mapa y su tipo de terreno. */
@Entity
@Table(name = "map_tile", uniqueConstraints = {
    @UniqueConstraint(columnNames = {
        "game_map_id", "position_x", "position_y"
    })
})
public class Tile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Coordenadas de la casilla.
    @Embedded
    @AttributeOverride(name = "x", column = @Column(name = "position_x"))
    @AttributeOverride(name = "y", column = @Column(name = "position_y"))
    private Position position;

    // Tipo de terreno de la casilla.
    @Enumerated(EnumType.STRING)
    private TerrainType terrainType;

    // Mapa al que pertenece la casilla.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_map_id", nullable = false)
    private GameMap gameMap;

    // Constructor necesario para JPA.
    public Tile() {
    }

    // Crea una casilla con posicion y terreno.
    public Tile(Position position, TerrainType terrainType) {
        this.position = position;
        this.terrainType = terrainType;
    }

    // Asigna la casilla a un mapa.
    void assignGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }

    public Long getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType terrainType) {
        this.terrainType = terrainType;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void setGameMap(GameMap gameMap) {
        this.gameMap = gameMap;
    }
}
