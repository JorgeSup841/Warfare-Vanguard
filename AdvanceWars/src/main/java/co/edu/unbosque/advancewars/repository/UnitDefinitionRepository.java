package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.enums.UnitCategory;
import co.edu.unbosque.advancewars.enums.UnitType;
import co.edu.unbosque.advancewars.model.UnitDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para definiciones de unidades. */
public interface UnitDefinitionRepository extends JpaRepository<UnitDefinition, Long> {

    /** Busca la definición de una unidad mediante su tipo. */
    Optional<UnitDefinition> findByUnitType(UnitType unitType);

    /** Lista las definiciones de una categoría ordenadas por nombre. */
    List<UnitDefinition> findAllByCategoryOrderByNameAsc(UnitCategory category);

    /** Lista todas las definiciones de unidades ordenadas por nombre. */
    List<UnitDefinition> findAllByOrderByNameAsc();

    /** Comprueba si ya existe una definición para el tipo de unidad indicado. */
    boolean existsByUnitType(UnitType unitType);

    /** Comprueba si existe una definición con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByNameIgnoreCase(String name);
}
