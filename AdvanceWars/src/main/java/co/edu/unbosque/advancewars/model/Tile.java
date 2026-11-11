package co.edu.unbosque.advancewars.model;

import co.edu.unbosque.advancewars.enums.TerrainType;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "map_tile", uniqueConstraints = {
		@UniqueConstraint(name = "uk_tile_map_position", columnNames = { "game_map_id", "position_x", "position_y" }) })
public class Tile {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Embedded
	@AttributeOverride(name = "x", column = @Column(name = "position_x", nullable = false))
	@AttributeOverride(name = "y", column = @Column(name = "position_y", nullable = false))
	private Position position;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private TerrainType terrainType;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "game_map_id", nullable = false)
	private GameMap gameMap;

	protected Tile() {
	}

	public Tile(Position position, TerrainType terrainType) {
		this.position = position;
		this.terrainType = terrainType;
	}

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

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Tile other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Tile.class.hashCode();
	}

	@Override
	public String toString() {
		return "Tile{" + "id=" + id + ", position=" + position + ", terrainType=" + terrainType + '}';
	}
}
