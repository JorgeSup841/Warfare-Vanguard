package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.exception.BadRequestException;
import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona consultas y cambios autorizados de cuentas de usuario.
 */
@Service
public class UserService {
    private final UserRepository repository;

    /**
     * Inyecta el repositorio de usuarios.
     */
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /**
     * Consulta los datos públicos de la cuenta autenticada.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO me(String username) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        return toDto(user);
    }

    /**
     * Devuelve los usuarios disponibles con datos de salida seguros.
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    /**
     * Consulta los datos públicos del usuario solicitado.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO find(Long id) {
        return toDto(load(id));
    }

    /**
     * Actualiza un rol, impidiendo la automodificación administrativa.
     */
    @Transactional
    public UserResponseDTO setRole(Long id, Role role, String actingUsername) {
        User user = load(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new BadRequestException("No puedes cambiar tu propio rol");
        }
        user.setRole(role);
        return toDto(user);
    }

    /**
     * Modifica la habilitación de una cuenta según su verificación.
     */
    @Transactional
    public UserResponseDTO setEnabled(Long id, boolean enabled, String actingUsername) {
        User user = load(id);
        if (user.getUsername().equals(actingUsername) && !enabled) {
            throw new BadRequestException("No puedes deshabilitar tu propia cuenta");
        }
        if (enabled && !user.isEmailVerified()) {
            throw new BadRequestException("Primero se debe verificar el correo");
        }
        user.setEnabled(enabled);
        return toDto(user);
    }

    /**
     * Convierte una entidad User en su representación pública.
     */
    public UserResponseDTO toDto(User user) {
        return new UserResponseDTO(user.getId(), user.getUsername(), user.getEmail(),
                user.getRole(), user.isEmailVerified(), user.isEnabled());
    }

    /**
     * Busca un usuario por id o señala que no existe.
     */
    private User load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }
}
