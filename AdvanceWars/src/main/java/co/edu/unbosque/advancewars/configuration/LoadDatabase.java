
package co.edu.unbosque.advancewars.configuration;

import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import co.edu.unbosque.advancewars.service.GameCatalogService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


/**
 * Inicializa los datos del catálogo del videojuego al arrancar
 * la aplicación y configura el primer administrador cuando
 * existe un usuario válido y todavía no hay otro administrador.
 */
@Component
public class LoadDatabase implements CommandLineRunner {


    private final UserRepository users;
    private final GameCatalogService catalog;
    private final String username;


/**
 * Inicializa las dependencias necesarias para cargar los datos.
 *
 * @param users repositorio de usuarios registrados
 * @param catalog servicio encargado de inicializar el catálogo del juego
 * @param username nombre del usuario configurado como administrador
 */
public LoadDatabase(UserRepository users, GameCatalogService catalog,
        @Value("${app.bootstrap.admin-username:}") String username) {
    this.users = users;
    this.catalog = catalog;
    this.username = username;
}


    @Override
    public void run(String... args) {

        System.out.println("=== LOAD DATABASE ===");
        // Inicializa facciones, comandantes, unidades y armas aunque no haya ADMIN configurado.
        catalog.seed();
        System.out.println("Usuario configurado: " + username);

        if (username == null || username.isBlank()) {
            System.out.println("No se configuro un usuario ADMIN.");
            return;
        }

        if (users.existsByRole(Role.ADMIN)) {
            System.out.println("Ya existe un ADMIN en la base de datos.");
            return;
        }

        User user = users.findByUsername(username.trim()).orElse(null);

        if (user == null) {
            System.out.println("El usuario no existe.");
            return;
        }

        if (!user.isEmailVerified()) {
            System.out.println("El correo no esta verificado.");
            return;
        }

        if (!user.isEnabled()) {
            System.out.println("El usuario esta deshabilitado.");
            return;
        }

        user.setRole(Role.ADMIN);
        users.save(user);

        System.out.println("ADMIN creado correctamente: " + user.getUsername());
    }
}
