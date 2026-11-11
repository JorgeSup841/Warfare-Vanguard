package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Faction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FactionRepository extends JpaRepository<Faction, Long> {

    Optional<Faction> findByCode(String code);

    Optional<Faction> findByNameIgnoreCase(String name);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    List<Faction> findAllByOrderByNameAsc();
}
