package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Faction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para facciones. */
public interface FactionRepository extends JpaRepository<Faction, Long> {

    /** Busca una facción mediante su código único. */
    Optional<Faction> findByCode(String code);

    /** Busca una facción por nombre sin distinguir mayúsculas. */
    Optional<Faction> findByNameIgnoreCase(String name);

    /** Comprueba si existe una facción con el código indicado. */
    boolean existsByCode(String code);

    /** Comprueba si existe una facción con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByNameIgnoreCase(String name);

    /** Lista todas las facciones ordenadas por nombre. */
    List<Faction> findAllByOrderByNameAsc();
}
