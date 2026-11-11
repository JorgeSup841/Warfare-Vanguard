package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WeaponRepository extends JpaRepository<Weapon, Long> {

    Optional<Weapon> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByNameIgnoreCase(String name);

    List<Weapon> findAllByOrderByNameAsc();
}
