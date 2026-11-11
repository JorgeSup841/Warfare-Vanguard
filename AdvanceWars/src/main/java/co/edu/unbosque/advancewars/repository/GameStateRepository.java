package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameState;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GameStateRepository extends JpaRepository<GameState, Long> {

    List<GameState> findAllByStatusOrderByCreatedAtDesc(GameStatus status);

    Optional<GameState> findByIdAndStatus(Long id, GameStatus status);
}
