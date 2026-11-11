package co.edu.unbosque.advancewars.model;



import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Representa una propiedad del mapa, su dueño y su progreso de captura. */
@Entity
@Table(name = "game_property", uniqueConstraints = {
		@UniqueConstraint(name = "uk_property_tile", columnNames = { "tile_id" }) })
public class Property {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id; // skipcq: JAVA-E1065

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "property_definition_id", nullable = false)
	private PropertyDefinition definition;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tile_id", nullable = false)
	private Tile tile;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private Player owner;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "capturing_player_id")
	private Player capturingPlayer;

	@Column(name = "capture_progress", nullable = false)
	private int captureProgress;

	@Column(nullable = false)
	private boolean operational;

	protected Property() {
	}

	/** Construye una propiedad para una definición y una casilla, opcionalmente con dueño. */
	public Property(PropertyDefinition definition, Tile tile) {
		this.definition = definition;
		this.tile = tile;
		this.captureProgress = 0;
		this.operational = true;
	}

	/** Construye una propiedad para una definición y una casilla, opcionalmente con dueño. */
	public Property(PropertyDefinition definition, Tile tile, Player owner) {
		this(definition, tile);
		this.owner = owner;
	}

	/** Actualiza el jugador que captura la propiedad y su progreso. */
	public void updateCaptureState(Player capturingPlayer, int progress) {
		this.capturingPlayer = capturingPlayer;
		this.captureProgress = progress;
	}

	/** Completa la captura y asigna la propiedad al nuevo dueño. */
	public void completeCapture(Player newOwner) {
		this.owner = newOwner;
		resetCaptureProgress();
	}

	/** Asigna el dueño de la propiedad y reinicia el progreso de captura. */
	public void assignOwner(Player owner) {
		this.owner = owner;
		resetCaptureProgress();
	}

	/** Reinicia el progreso y elimina el jugador que estaba capturando. */
	public void resetCaptureProgress() {
		this.captureProgress = 0;
		this.capturingPlayer = null;
	}

	/** Indica si el progreso alcanzó el umbral necesario para capturar. */
	public boolean isCaptureComplete() {
		return captureProgress >= definition.getCaptureThreshold();
	}

	public Long getId() {
		return id;
	}

	public PropertyDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(PropertyDefinition definition) {
		this.definition = definition;
	}

	public Tile getTile() {
		return tile;
	}

	public void setTile(Tile tile) {
		this.tile = tile;
	}

	public Player getOwner() {
		return owner;
	}

	public void setOwner(Player owner) {
		this.owner = owner;
	}

	public Player getCapturingPlayer() {
		return capturingPlayer;
	}

	public void setCapturingPlayer(Player capturingPlayer) {
		this.capturingPlayer = capturingPlayer;
	}

	public int getCaptureProgress() {
		return captureProgress;
	}

	public void setCaptureProgress(int captureProgress) {
		this.captureProgress = captureProgress;
	}

	/** Indica si la propiedad se encuentra operativa. */
	public boolean isOperational() {
		return operational;
	}

	public void setOperational(boolean operational) {
		this.operational = operational;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}

		if (!(object instanceof Property other)) {
			return false;
		}

		return id != null && id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return Property.class.hashCode();
	}

	@Override
	public String toString() {
		return "Property{" + "id=" + id + ", captureProgress=" + captureProgress + ", operational=" + operational + '}';
	}
}
