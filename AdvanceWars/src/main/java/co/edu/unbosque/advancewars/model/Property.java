
package co.edu.unbosque.advancewars.model;

import jakarta.persistence.*;

@Entity
@Table(name = "game_property")
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Tipo de propiedad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_definition_id", nullable = false)
    private PropertyDefinition definition;

    // Casilla donde esta ubicada. Solo una propiedad por casilla.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tile_id", nullable = false, unique = true)
    private Tile tile;

    // Jugador dueño de la propiedad.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private Player owner;

    // Jugador que intenta capturarla.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capturing_player_id")
    private Player capturingPlayer;

    private int captureProgress;
    private boolean operational = true;

    // Constructor necesario para JPA.
    public Property() {
    }

    // Crea una propiedad sin dueño.
    public Property(PropertyDefinition definition, Tile tile) {
        this.definition = definition;
        this.tile = tile;
    }

    // Crea una propiedad con dueño.
    public Property(PropertyDefinition definition, Tile tile, Player owner) {
        this(definition, tile);
        this.owner = owner;
    }

    // Actualiza el progreso de captura.
    public void updateCaptureState(Player player, int progress) {
        this.capturingPlayer = player;
        this.captureProgress = progress;
    }

    // Completa la captura y cambia el dueño.
    public void completeCapture(Player newOwner) {
        this.owner = newOwner;
        resetCaptureProgress();
    }

    // Asigna un dueño y reinicia la captura.
    public void assignOwner(Player owner) {
        this.owner = owner;
        resetCaptureProgress();
    }

    // Reinicia el progreso de captura.
    public void resetCaptureProgress() {
        this.captureProgress = 0;
        this.capturingPlayer = null;
    }

    // Comprueba si se alcanzo el progreso necesario.
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

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }
}
