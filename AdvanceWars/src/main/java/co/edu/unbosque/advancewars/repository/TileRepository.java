package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TileRepository extends JpaRepository<Tile, Long> {

    List<Tile> findAllByGameMap_Id(Long gameMapId);

    Optional<Tile> findByGameMap_IdAndPosition_XAndPosition_Y(
            Long gameMapId, int x, int y);

    boolean existsByGameMap_IdAndPosition_XAndPosition_Y(
            Long gameMapId, int x, int y);

    long countByGameMap_Id(Long gameMapId);
}
