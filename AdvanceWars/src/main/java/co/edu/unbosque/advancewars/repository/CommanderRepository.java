package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Commander;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para comandantes. */
public interface CommanderRepository extends JpaRepository<Commander, Long> {

    /** Busca un comandante mediante su código único. */
    Optional<Commander> findByCode(String code);

    /** Busca un comandante por su identificador y el identificador de su facción. */
    Optional<Commander> findByIdAndFaction_Id(Long commanderId, Long factionId);

    /** Lista los comandantes de una facción ordenados por nombre. */
    List<Commander> findAllByFaction_IdOrderByNameAsc(Long factionId);

    /** Lista todos los comandantes ordenados por nombre. */
    List<Commander> findAllByOrderByNameAsc();

    /** Comprueba si existe un comandante con el código indicado. */
    boolean existsByCode(String code);

    /** Comprueba si existe un comandante con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByNameIgnoreCase(String name);
}
