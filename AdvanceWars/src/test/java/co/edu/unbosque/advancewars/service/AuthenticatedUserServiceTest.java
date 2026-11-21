package co.edu.unbosque.advancewars.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.repository.UserRepository;

/** Verifica que la identidad de las peticiones no se toma de un DTO manipulable. */
class AuthenticatedUserServiceTest {

    /** Resuelve la entidad solo a partir del nombre autenticado de Spring Security. */
    @Test
    void resolvesAuthenticatedUser() {
        UserRepository users = mock(UserRepository.class);
        Principal principal = () -> "jugador";
        User user = mock(User.class);
        when(users.findByUsername("jugador")).thenReturn(Optional.of(user));
        assertSame(user, new AuthenticatedUserService(users).requireUser(principal));
    }

    /** Rechaza una petición sin identidad autenticada. */
    @Test
    void rejectsMissingPrincipal() {
        UserRepository users = mock(UserRepository.class);
        assertThrows(ResponseStatusException.class,
                () -> new AuthenticatedUserService(users).requireUser(null));
    }
}
