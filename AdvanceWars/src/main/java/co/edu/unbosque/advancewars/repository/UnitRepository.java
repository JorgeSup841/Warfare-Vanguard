package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findAllByOwner_Id(Long ownerId);

    List<Unit> findAllByOwner_GameState_Id(Long gameStateId);

    Optional<Unit> findByIdAndOwner_Id(Long unitId, Long ownerId);

    boolean existsByTile_Id(Long tileId);

    long countByOwner_Id(Long ownerId);

    long countByOwner_GameState_Id(Long gameStateId);
}
