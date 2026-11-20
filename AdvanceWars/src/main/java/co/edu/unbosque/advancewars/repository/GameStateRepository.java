package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameState;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para partidas. */
public interface GameStateRepository extends JpaRepository<GameState, Long> {

    /** Lista las partidas con el estado indicado, de la más reciente a la más antigua. */
    List<GameState> findAllByStatusOrderByCreatedAtDesc(GameStatus status);

    /** Consulta las partidas creadas por el usuario, de la mas reciente a la mas antigua. */
    List<GameState> findAllByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    /** Busca una partida solo cuando pertenece al usuario indicado. */
    Optional<GameState> findByIdAndCreatedBy_Id(Long gameId, Long userId);

    /**
     * Busca la partida de su creador y bloquea su fila hasta terminar la transaccion.
     * Evita que dos solicitudes simultaneas cambien el turno a la vez.
     */

    /** Busca una partida por identificador y estado. */
    Optional<GameState> findByIdAndStatus(Long id, GameStatus status);
}
