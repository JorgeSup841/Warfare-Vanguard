package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.dto.EnabledUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.RoleUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.service.UserService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expone las consultas y operaciones de administración de usuarios.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    /**
     * Inyecta el servicio de gestión de usuarios.
     */
    public UserController(UserService users) { this.users = users; }

    /**
     * Consulta los datos públicos de la cuenta autenticada.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Principal principal) {
        return ResponseEntity.ok(users.me(principal.getName()));
    }

    /**
     * Consulta los usuarios mediante autorización administrativa.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list() {
        return ResponseEntity.ok(users.list());
    }

    /**
     * Consulta un usuario por su identificador.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> find(@PathVariable Long id) {
        return ResponseEntity.ok(users.find(id));
    }

    /**
     * Actualiza el rol de una cuenta con las restricciones del servicio.
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> role(@PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setRole(id, request.getRole(), principal.getName()));
    }

    /**
     * Habilita o deshabilita una cuenta según las reglas del servicio.
     */
    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDTO> enabled(@PathVariable Long id,
            @Valid @RequestBody EnabledUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setEnabled(id, request.getEnabled(), principal.getName()));
    }
}
