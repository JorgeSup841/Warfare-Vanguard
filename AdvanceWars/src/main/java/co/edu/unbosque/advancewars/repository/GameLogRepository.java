package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.GameLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameLogRepository extends JpaRepository<GameLog, Long> {

    List<GameLog> findAllByGameState_IdOrderByCreatedAtAscIdAsc(Long gameStateId);

    List<GameLog> findAllByGameState_IdAndRoundNumberOrderByCreatedAtAscIdAsc(
            Long gameStateId, int roundNumber);

    List<GameLog> findAllByGameState_IdAndPlayer_IdOrderByCreatedAtAscIdAsc(
            Long gameStateId, Long playerId);

    long countByGameState_Id(Long gameStateId);
}
