package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para unidades en partida. */
public interface UnitRepository extends JpaRepository<Unit, Long> {

    /** Lista las unidades que pertenecen al jugador indicado. */
    List<Unit> findAllByOwner_Id(Long ownerId);

    /** Lista las unidades pertenecientes a jugadores de una partida. */
    List<Unit> findAllByOwner_GameState_Id(Long gameStateId);

    /** Busca una unidad por identificador y dueño. */
    Optional<Unit> findByIdAndOwner_Id(Long unitId, Long ownerId);

    /** Comprueba si una casilla ya está ocupada por una unidad. */
    boolean existsByTile_Id(Long tileId);

    /** Cuenta las unidades de un jugador. */
    long countByOwner_Id(Long ownerId);

    /** Cuenta las unidades de todos los jugadores de una partida. */
    long countByOwner_GameState_Id(Long gameStateId);
}
