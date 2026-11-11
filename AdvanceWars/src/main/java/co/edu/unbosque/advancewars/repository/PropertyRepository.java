package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

    List<Property> findAllByOwner_Id(Long ownerId);

    List<Property> findAllByOwner_GameState_Id(Long gameStateId);

    Optional<Property> findByTile_Id(Long tileId);

    boolean existsByTile_Id(Long tileId);

    long countByOwner_Id(Long ownerId);

    long countByOwner_GameState_Id(Long gameStateId);
}
