package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.model.PropertyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PropertyDefinitionRepository extends JpaRepository<PropertyDefinition, Long> {

    Optional<PropertyDefinition> findByType(PropertyType type);

    List<PropertyDefinition> findAllByTypeOrderByNameAsc(PropertyType type);

    List<PropertyDefinition> findAllByOrderByNameAsc();

    boolean existsByType(PropertyType type);

    boolean existsByNameIgnoreCase(String name);
}
