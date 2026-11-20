package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.exception.BadRequestException;
import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona la consulta de perfiles y operaciones administrativas de usuarios.
 * Nunca expone contraseñas al construir los DTO de respuesta.
 */
@Service
public class UserService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;
    private final UserSessionService sessions;

    /**
     * Inicializa la persistencia y conversión entre entidades y DTO.
     *
     * @param repository repositorio de cuentas
     * @param modelMapper conversor de usuarios a DTO de respuesta
     */
    public UserService(UserRepository repository, ModelMapper modelMapper,
            UserSessionService sessions) {
        this.repository = repository;
        this.modelMapper = modelMapper;
        this.sessions = sessions;
    }

    /**
     * Consulta el perfil de la cuenta autenticada.
     *
     * @param username nombre del usuario autenticado
     * @return DTO público del usuario
     */
    @Transactional(readOnly = true)
    public UserResponseDTO me(String username) {
        Optional<User> found = repository.findByUsername(username);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Cuenta no encontrada");
        }
        return toDto(found.get());
    }

    /**
     * Recupera todos los perfiles para uso administrativo.
     *
     * @return lista de usuarios convertidos a DTO
     */
    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() {
        List<UserResponseDTO> result = new ArrayList<>();
        for (User user : repository.findAll()) {
            result.add(toDto(user));
        }
        return result;
    }

    /**
     * Obtiene una cuenta por identificador.
     *
     * @param id identificador de la cuenta
     * @return DTO público de la cuenta solicitada
     */
    @Transactional(readOnly = true)
    public UserResponseDTO find(Long id) {
        return toDto(load(id));
    }

    /**
     * Actualiza el rol de una cuenta distinta a la del administrador.
     *
     * @param id identificador de la cuenta objetivo
     * @param role nuevo rol
     * @param actingUsername nombre del administrador que efectúa el cambio
     * @return perfil con rol actualizado
     */
    @Transactional
    public UserResponseDTO setRole(Long id, Role role, String actingUsername) {
        User user = load(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new BadRequestException("No puedes cambiar tu propio rol");
        }
        if (role == null) {
            throw new BadRequestException("Rol obligatorio");
        }
        if (user.getRole() != role) {
            user.setRole(role);
            sessions.revokeAllForUser(user.getId());
        }
        return toDto(user);
    }

    /**
     * Modifica el estado de una cuenta respetando la verificación por correo.
     *
     * @param id identificador de la cuenta objetivo
     * @param enabled nuevo estado de habilitación
     * @param actingUsername nombre del administrador que efectúa el cambio
     * @return perfil con estado actualizado
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
        if (!enabled) {
            sessions.revokeAllForUser(user.getId());
        }
        return toDto(user);
    }

    /**
     * Convierte la entidad persistida en una respuesta sin credenciales.
     *
     * @param user entidad de usuario
     * @return DTO para el cliente
     */
    public UserResponseDTO toDto(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    /**
     * Busca una cuenta y rechaza identificadores inexistentes.
     *
     * @param id identificador solicitado
     * @return entidad recuperada
     */
    private User load(Long id) {
        Optional<User> found = repository.findById(id);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Cuenta no encontrada");
        }
        return found.get();
    }
}
