package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/** Proporciona consultas de persistencia para armas. */
public interface WeaponRepository extends JpaRepository<Weapon, Long> {

    /** Busca un arma mediante su código único. */
    Optional<Weapon> findByCode(String code);

    /** Comprueba si existe un arma con el código indicado. */
    boolean existsByCode(String code);

    /** Comprueba si existe un arma con el nombre indicado, sin distinguir mayúsculas. */
    boolean existsByNameIgnoreCase(String name);

    /** Lista todas las armas ordenadas por nombre. */
    List<Weapon> findAllByOrderByNameAsc();
}
