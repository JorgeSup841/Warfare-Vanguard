package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Proporciona consultas de persistencia para el historial de partidas. */
public interface GameLogRepository extends JpaRepository<GameLog, Long> {

    /** Lista los registros de una partida en orden cronológico y por identificador. */
    List<GameLog> findAllByGameState_IdOrderByCreatedAtAscIdAsc(Long gameStateId);

    /** Lista los registros de una ronda de una partida en orden cronológico. */
    List<GameLog> findAllByGameState_IdAndRoundNumberOrderByCreatedAtAscIdAsc(
            Long gameStateId, int roundNumber);

    /** Lista los registros de un jugador dentro de una partida en orden cronológico. */
    List<GameLog> findAllByGameState_IdAndPlayer_IdOrderByCreatedAtAscIdAsc(
            Long gameStateId, Long playerId);

    /** Cuenta los registros asociados a una partida. */
    long countByGameState_Id(Long gameStateId);
}
