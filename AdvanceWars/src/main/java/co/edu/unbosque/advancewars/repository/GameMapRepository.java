package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/** Proporciona consultas de persistencia para mapas. */
public interface GameMapRepository extends JpaRepository<GameMap, Long> {

    /** Busca mapas que coincidan con el ancho y el alto indicados. */
    List<GameMap> findAllByWidthAndHeight(int width, int height);
}
