package co.edu.unbosque.advancewars.model;



import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "game_map")
public class GameMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private int width;

	@Column(nullable = false)
	private int height;

	@OneToMany(mappedBy = "gameMap", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Tile> tiles = new ArrayList<>();

	@Transient
	private Map<Position, Tile> tileIndex;

	protected GameMap() {
		this.tiles = new ArrayList<>();
		this.tileIndex = new HashMap<>();
	}

	public GameMap(int width, int height) {
		this.width = width;
		this.height = height;
		this.tiles = new ArrayList<>();
		this.tileIndex = new HashMap<>();
	}

	public void addTile(Tile tile) {
		if (!tiles.contains(tile)) {
			tile.assignGameMap(this);
			tiles.add(tile);
			getTileIndex().put(tile.getPosition(), tile);
		}
	}

	public void removeTile(Tile tile) {
		if (tiles.remove(tile)) {
			tile.assignGameMap(null);
			getTileIndex().remove(tile.getPosition());
		}
	}

	public Optional<Tile> findTileAt(int x, int y) {
		if (!isInsideMap(x, y)) {
			return Optional.empty();
		}

		return Optional.ofNullable(getTileIndex().get(new Position(x, y)));
	}

	public List<Tile> findNeighborTiles(Position position) {
		if (position == null || !isInsideMap(position)) {
			return Collections.emptyList();
		}

		List<Tile> neighbors = new ArrayList<>();
		int[][] directions = { { 0, -1 }, { 0, 1 }, { -1, 0 }, { 1, 0 } };

		for (int[] direction : directions) {
			int x = position.getX() + direction[0];
			int y = position.getY() + direction[1];
			findTileAt(x, y).ifPresent(neighbors::add);
		}

		return Collections.unmodifiableList(neighbors);
	}

	public boolean isInsideMap(Position position) {
		return position != null && isInsideMap(position.getX(), position.getY());
	}

	public boolean isInsideMap(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}

	public boolean isComplete() {
		return tiles.size() == (long) width * height;
	}

	public long getExpectedTileCount() {
		return (long) width * height;
	}

	private Map<Position, Tile> getTileIndex() {
		if (tileIndex == null) {
			tileIndex = new HashMap<>();

			for (Tile tile : tiles) {
				tileIndex.put(tile.getPosition(), tile);
			}
		}

		return tileIndex;
	}

	public Long getId() {
		return id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Tile> getTiles() {
		return Collections.unmodifiableList(tiles);
	}

	public void setTiles(List<Tile> tiles) {
		for (Tile tile : this.tiles) {
			tile.assignGameMap(null);
		}

		this.tiles.clear();
		getTileIndex().clear();

		if (tiles != null) {
			for (Tile tile : tiles) {
				addTile(tile);
			}
		}
	}

	public int getTileCount() {
		return tiles.size();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof GameMap other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return GameMap.class.hashCode();
	}

	@Override
	public String toString() {
		return "GameMap{" + "id=" + id + ", width=" + width + ", height=" + height + '}';
	}
}
