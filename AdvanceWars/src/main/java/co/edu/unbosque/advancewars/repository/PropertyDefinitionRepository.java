package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.PropertyType;
import co.edu.unbosque.advancewars.model.PropertyDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para definiciones de propiedades. */
public interface PropertyDefinitionRepository extends JpaRepository<PropertyDefinition, Long> {

    /** Busca la definición correspondiente al tipo de propiedad. */
    Optional<PropertyDefinition> findByType(PropertyType type);

    /** Lista las definiciones de un tipo de propiedad ordenadas por nombre. */
    List<PropertyDefinition> findAllByTypeOrderByNameAsc(PropertyType type);

    /** Lista todas las definiciones de propiedades ordenadas por nombre. */
    List<PropertyDefinition> findAllByOrderByNameAsc();

    /** Comprueba si existe una definición para el tipo de propiedad indicado. */
    boolean existsByType(PropertyType type);

    /** Comprueba si existe una definición con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByNameIgnoreCase(String name);
}
