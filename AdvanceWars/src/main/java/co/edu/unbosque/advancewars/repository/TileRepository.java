package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para casillas de los mapas. */
public interface TileRepository extends JpaRepository<Tile, Long> {

    /** Lista las casillas asociadas a un mapa. */
    List<Tile> findAllByGameMap_Id(Long gameMapId);

    /** Busca una casilla de un mapa mediante sus coordenadas. */
    Optional<Tile> findByGameMap_IdAndPosition_XAndPosition_Y(
            Long gameMapId, int x, int y);

    /** Comprueba si un mapa contiene una casilla en las coordenadas indicadas. */
    boolean existsByGameMap_IdAndPosition_XAndPosition_Y(
            Long gameMapId, int x, int y);

    /** Cuenta las casillas asociadas a un mapa. */
    long countByGameMap_Id(Long gameMapId);
}
