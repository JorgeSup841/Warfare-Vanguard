package co.edu.unbosque.advancewars.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import co.edu.unbosque.advancewars.enums.GameStatus;
import co.edu.unbosque.advancewars.model.GameState;

import jakarta.persistence.LockModeType;

public interface GameStateRepository extends JpaRepository<GameState, Long> {

    List<GameState> findAllByOrderByCreatedAtDesc();
    long countByStatus(GameStatus status);

    // Buscar partidas por estado
    List<GameState> findAllByStatusOrderByCreatedAtDesc(GameStatus status);

    // Buscar partidas de un usuario
    List<GameState> findAllByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    // Buscar una partida de un usuario
    Optional<GameState> findByIdAndCreatedBy_Id(Long gameId, Long userId);

    // Buscar partida bloqueada para turnos y movimientos
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<GameState> findWithLockByIdAndCreatedBy_Id(Long gameId, Long userId);

    // Buscar partida por ID y estado
    Optional<GameState> findByIdAndStatus(Long id, GameStatus status);
}
