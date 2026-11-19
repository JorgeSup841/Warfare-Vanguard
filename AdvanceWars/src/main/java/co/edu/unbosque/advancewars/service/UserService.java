package co.edu.unbosque.advancewars.service;

import co.edu.unbosque.advancewars.dto.UserResponseDTO;
import co.edu.unbosque.advancewars.enums.Role;
import co.edu.unbosque.advancewars.exception.BadRequestException;
import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;
import java.util.List;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository repository;
    private final ModelMapper modelMapper;

    public UserService(UserRepository repository, ModelMapper modelMapper) {
        this.repository = repository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO me(String username) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDTO find(Long id) {
        return toDto(load(id));
    }

    @Transactional
    public UserResponseDTO setRole(Long id, Role role, String actingUsername) {
        User user = load(id);
        if (user.getUsername().equals(actingUsername)) {
            throw new BadRequestException("No puedes cambiar tu propio rol");
        }
        user.setRole(role);
        return toDto(user);
    }

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

    public UserResponseDTO toDto(User user) {
        return modelMapper.map(user, UserResponseDTO.class);
    }

    private User load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
    }
}
