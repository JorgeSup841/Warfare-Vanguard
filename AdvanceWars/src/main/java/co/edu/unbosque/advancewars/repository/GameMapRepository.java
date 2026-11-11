package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GameMapRepository extends JpaRepository<GameMap, Long> {

    List<GameMap> findAllByWidthAndHeight(int width, int height);
}
