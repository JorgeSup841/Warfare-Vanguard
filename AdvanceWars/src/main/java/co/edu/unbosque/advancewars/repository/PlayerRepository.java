package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.PlayerStatus;
import co.edu.unbosque.advancewars.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    List<Player> findAllByGameState_IdOrderByTurnOrderAsc(Long gameStateId);

    List<Player> findAllByGameState_IdAndStatusOrderByTurnOrderAsc(
            Long gameStateId, PlayerStatus status);

    Optional<Player> findByIdAndGameState_Id(Long playerId, Long gameStateId);

    Optional<Player> findByGameState_IdAndTurnOrder(Long gameStateId, int turnOrder);

    boolean existsByGameState_IdAndTurnOrder(Long gameStateId, int turnOrder);

    boolean existsByGameState_IdAndName(Long gameStateId, String name);

    boolean existsByGameState_IdAndNameIgnoreCase(Long gameStateId, String name);

    long countByGameState_Id(Long gameStateId);

    long countByGameState_IdAndStatus(Long gameStateId, PlayerStatus status);
}
