package co.edu.unbosque.advancewars.controller;

import co.edu.unbosque.advancewars.dto.EnabledUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.RoleUpdateRequestDTO;
import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Permite consultar el perfil y administrar cuentas mediante JWT.
 * La modificación y consulta de otros usuarios requiere rol ADMIN.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Consulta de sesión y administración de cuentas")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService users;

    /**
     * Inicializa las operaciones de gestión de usuarios.
     *
     * @param users servicio de consulta y administración de cuentas
     */
    public UserController(UserService users) {
        this.users = users;
    }

    /**
     * Devuelve los datos de la cuenta que realiza la solicitud.
     *
     * @param principal usuario autenticado
     * @return datos públicos de su perfil
     */
    @GetMapping("/me")
    @Operation(summary = "Consultar perfil actual", description = "Requiere autenticación JWT")
    public ResponseEntity<UserResponseDTO> me(Principal principal) {
        return ResponseEntity.ok(users.me(principal.getName()));
    }

    /**
     * Enumera las cuentas registradas para administradores.
     *
     * @return lista de perfiles sin contraseñas
     */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Solo ADMIN")
    public ResponseEntity<List<UserResponseDTO>> list() {
        return ResponseEntity.ok(users.list());
    }

    /**
     * Busca una cuenta por identificador para administración.
     *
     * @param id identificador de la cuenta
     * @return datos públicos del usuario
     */
    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuario por ID", description = "Solo ADMIN")
    public ResponseEntity<UserResponseDTO> find(@PathVariable Long id) {
        return ResponseEntity.ok(users.find(id));
    }

    /**
     * Cambia el rol de otra cuenta; impide cambios de rol propios.
     *
     * @param id identificador del usuario a modificar
     * @param request nuevo rol solicitado
     * @param principal administrador que realiza el cambio
     * @return perfil actualizado
     */
    @PatchMapping("/{id}/role")
    @Operation(summary = "Modificar rol", description = "Solo ADMIN; no permite modificar el rol propio")
    public ResponseEntity<UserResponseDTO> role(@PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setRole(id, request.getRole(), principal.getName()));
    }

    /**
     * Habilita o deshabilita una cuenta con las validaciones del servicio.
     *
     * @param id identificador del usuario a modificar
     * @param request nuevo estado habilitado
     * @param principal administrador que realiza el cambio
     * @return perfil actualizado
     */
    @PatchMapping("/{id}/enabled")
    @Operation(summary = "Habilitar o deshabilitar usuario", description = "Solo ADMIN, sujeto a verificación de correo")
    public ResponseEntity<UserResponseDTO> enabled(@PathVariable Long id,
            @Valid @RequestBody EnabledUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setEnabled(id, request.getEnabled(), principal.getName()));
    }
}
