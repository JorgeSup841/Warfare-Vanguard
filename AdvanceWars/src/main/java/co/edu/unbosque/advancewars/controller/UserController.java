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

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) { this.users = users; }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> me(Principal principal) {
        return ResponseEntity.ok(users.me(principal.getName()));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> list() {
        return ResponseEntity.ok(users.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> find(@PathVariable Long id) {
        return ResponseEntity.ok(users.find(id));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> role(@PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setRole(id, request.role(), principal.getName()));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<UserResponseDTO> enabled(@PathVariable Long id,
            @Valid @RequestBody EnabledUpdateRequestDTO request, Principal principal) {
        return ResponseEntity.ok(users.setEnabled(id, request.enabled(), principal.getName()));
    }
}
