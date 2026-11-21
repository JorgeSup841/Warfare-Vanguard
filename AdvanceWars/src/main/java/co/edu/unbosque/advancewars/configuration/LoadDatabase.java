
package co.edu.unbosque.advancewars.configuration;

import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import co.edu.unbosque.advancewars.service.GameCatalogService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializa el catalogo del juego y configura
 * el primer administrador si corresponde.
 */
@Component
public class LoadDatabase implements CommandLineRunner {

    private final UserRepository users;
    private final GameCatalogService catalog;
    private final String username;

    public LoadDatabase(
            UserRepository users,
            GameCatalogService catalog,
            @Value("${app.bootstrap.admin-username:}") String username) {

        this.users = users;
        this.catalog = catalog;
        this.username = username;
    }

    @Override
    public void run(String... args) {

        System.out.println("=== LOAD DATABASE ===");

        // Cargar facciones, comandantes, unidades y armas.
        catalog.seed();

        // Comprobar si se configuro un administrador.
        if (username == null || username.isBlank()) {
            System.out.println("No se configuro un usuario ADMIN.");
            return;
        }

        // Evitar crear varios administradores.
        if (users.existsByRole(Role.ADMIN)) {
            System.out.println("Ya existe un ADMIN en la base de datos.");
            return;
        }

        // Buscar el usuario configurado.
        User user = users.findByUsername(username.trim()).orElse(null);

        if (user == null) {
            System.out.println("El usuario no existe.");
            return;
        }

        // Validar que pueda ser administrador.
        if (!user.isEmailVerified() || !user.isEnabled()) {
            System.out.println("El usuario no esta verificado o esta deshabilitado.");
            return;
        }

        // Asignar rol de administrador.
        user.setRole(Role.ADMIN);
        users.save(user);

        System.out.println("ADMIN creado correctamente: " + user.getUsername());
    }
}
