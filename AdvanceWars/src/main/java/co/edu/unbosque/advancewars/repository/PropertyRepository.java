package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para propiedades de las partidas. */
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /** Lista las propiedades cuyo dueño coincide con el identificador indicado. */
    List<Property> findAllByOwner_Id(Long ownerId);

    /** Lista las propiedades pertenecientes a jugadores de una partida. */
    List<Property> findAllByOwner_GameState_Id(Long gameStateId);

    /** Busca la propiedad situada en la casilla indicada. */
    Optional<Property> findByTile_Id(Long tileId);

    /** Comprueba si una casilla ya tiene una propiedad asociada. */
    boolean existsByTile_Id(Long tileId);

    /** Cuenta las propiedades de un jugador. */
    long countByOwner_Id(Long ownerId);

    /** Cuenta las propiedades de los jugadores de una partida. */
    long countByOwner_GameState_Id(Long gameStateId);
}
