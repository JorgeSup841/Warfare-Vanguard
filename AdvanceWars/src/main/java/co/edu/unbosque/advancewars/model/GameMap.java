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

/** Representa el mapa de una partida y sus casillas. */
@Entity
@Table(name = "game_map")
public class GameMap {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	@Column(nullable = false)
	private int width;

	@Column(nullable = false)
	private int height;

	@OneToMany(mappedBy = "gameMap", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Tile> tiles = new ArrayList<>();

	@Transient
	private Map<Position, Tile> tileIndex;

	/** Constructor requerido por JPA para materializar la entidad. */
	protected GameMap() {
		this.tiles = new ArrayList<>();
		this.tileIndex = new HashMap<>();
	}

	/** Construye un mapa con las dimensiones indicadas. */
	public GameMap(int width, int height) {
		this.width = width;
		this.height = height;
		this.tiles = new ArrayList<>();
		this.tileIndex = new HashMap<>();
	}

	/** Añade una casilla al mapa y actualiza su índice de posiciones. */
	public void addTile(Tile tile) {
		if (!tiles.contains(tile)) {
			tile.assignGameMap(this);
			tiles.add(tile);
			getTileIndex().put(tile.getPosition(), tile);
		}
	}

	/** Retira una casilla del mapa y desvincula la relación con este mapa. */
	public void removeTile(Tile tile) {
		if (tiles.remove(tile)) {
			tile.assignGameMap(null);
			getTileIndex().remove(tile.getPosition());
		}
	}

	/** Busca la casilla ubicada en las coordenadas indicadas. */
	public Optional<Tile> findTileAt(int x, int y) {
		if (!isInsideMap(x, y)) {
			return Optional.empty();
		}

		return Optional.ofNullable(getTileIndex().get(new Position(x, y)));
	}

	/** Obtiene las casillas ortogonalmente adyacentes a una posición. */
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

	/** Indica si la posición o las coordenadas pertenecen a los límites del mapa. */
	public boolean isInsideMap(Position position) {
		return position != null && isInsideMap(position.getX(), position.getY());
	}

	/** Indica si las coordenadas pertenecen a los límites del mapa. */
	public boolean isInsideMap(int x, int y) {
		return x >= 0 && x < width && y >= 0 && y < height;
	}

	/** Indica si el mapa contiene todas las casillas esperadas. */
	public boolean isComplete() {
		return tiles.size() == (long) width * height;
	}

	/** Calcula la cantidad de casillas que debe contener el mapa. */
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

	/** Devuelve la cantidad actual de casillas del mapa. */
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
