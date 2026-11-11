package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UnitDefinitionRepository extends JpaRepository<UnitDefinition, Long> {

    Optional<UnitDefinition> findByUnitType(UnitType unitType);

    List<UnitDefinition> findAllByCategoryOrderByNameAsc(UnitCategory category);

    List<UnitDefinition> findAllByOrderByNameAsc();

    boolean existsByUnitType(UnitType unitType);

    boolean existsByNameIgnoreCase(String name);
}
