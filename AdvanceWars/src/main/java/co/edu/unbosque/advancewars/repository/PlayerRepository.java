package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.PlayerStatus;
import co.edu.unbosque.advancewars.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para jugadores. */
public interface PlayerRepository extends JpaRepository<Player, Long> {

    /** Lista los jugadores de una partida según su orden de turno. */
    List<Player> findAllByGameState_IdOrderByTurnOrderAsc(Long gameStateId);

    /** Lista los jugadores de una partida con el estado indicado, según su orden de turno. */
    List<Player> findAllByGameState_IdAndStatusOrderByTurnOrderAsc(
            Long gameStateId, PlayerStatus status);

    /** Busca un jugador por identificador dentro de una partida. */
    Optional<Player> findByIdAndGameState_Id(Long playerId, Long gameStateId);

    /** Busca un jugador por su orden de turno dentro de una partida. */
    Optional<Player> findByGameState_IdAndTurnOrder(Long gameStateId, int turnOrder);

    /** Comprueba si una partida ya tiene un jugador con el orden de turno indicado. */
    boolean existsByGameState_IdAndTurnOrder(Long gameStateId, int turnOrder);

    /** Comprueba si una partida tiene un jugador con el nombre indicado. */
    boolean existsByGameState_IdAndName(Long gameStateId, String name);

    /** Comprueba si una partida tiene un jugador con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByGameState_IdAndNameIgnoreCase(Long gameStateId, String name);

    /** Cuenta los jugadores asociados a una partida. */
    long countByGameState_Id(Long gameStateId);

    /** Cuenta los jugadores de una partida que tienen el estado indicado. */
    long countByGameState_IdAndStatus(Long gameStateId, PlayerStatus status);
}
