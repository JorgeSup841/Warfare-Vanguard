package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Commander;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CommanderRepository extends JpaRepository<Commander, Long> {

    Optional<Commander> findByCode(String code);

    Optional<Commander> findByIdAndFaction_Id(Long commanderId, Long factionId);

    List<Commander> findAllByFaction_IdOrderByNameAsc(Long factionId);

    List<Commander> findAllByOrderByNameAsc();

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);
}
