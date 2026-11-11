package co.edu.unbosque.advancewars.repository;

import co.edu.unbosque.advancewars.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Proporciona consultas de persistencia para cuentas de usuario. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Busca un usuario mediante su nombre de usuario. */
    Optional<User> findByUsername(String username);

    /** Busca un usuario mediante su correo electrónico. */
    Optional<User> findByEmail(String email);

    /** Comprueba si el nombre de usuario ya está registrado. */
    boolean existsByUsername(String username);

    /** Comprueba si el correo electrónico ya está registrado. */
    boolean existsByEmail(String email);

    /** Comprueba si el correo electrónico ya está registrado, sin distinguir mayúsculas. */
    boolean existsByEmailIgnoreCase(String email);
}
