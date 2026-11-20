package co.edu.unbosque.advancewars.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.unbosque.advancewars.dto.UserActivityDTO;
import co.edu.unbosque.advancewars.dto.UserSessionDTO;
import co.edu.unbosque.advancewars.exception.ResourceNotFoundException;
import co.edu.unbosque.advancewars.model.User;
import co.edu.unbosque.advancewars.model.UserActivityLog;
import co.edu.unbosque.advancewars.model.UserSession;
import co.edu.unbosque.advancewars.repository.UserActivityLogRepository;
import co.edu.unbosque.advancewars.repository.UserSessionRepository;

/** Presencia, cierre de sesiones e historial sencillo de acciones autenticadas. */
@Service
public class UserSessionService {
    private final UserSessionRepository sessions;
    private final UserActivityLogRepository activities;

    public UserSessionService(UserSessionRepository sessions, UserActivityLogRepository activities) {
        this.sessions = sessions;
        this.activities = activities;
    }

    @Transactional
    public void open(User user, String tokenId, Instant expiresAt) {
        sessions.save(new UserSession(tokenId, user, expiresAt));
        activities.save(new UserActivityLog(user, "LOGIN"));
    }

    /** Verificacion obligatoria de revocacion; el JWT por si solo no basta. */
    @Transactional(readOnly = true)
    public boolean isAllowed(User user, String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        UserSession session = sessions.findById(tokenId).orElse(null);
        return session != null && session.getUser().getId().equals(user.getId())
                && !session.isRevoked() && session.getExpiresAt().isAfter(Instant.now());
    }

    /** Reduce escrituras: actualizar la actividad como maximo cada 15 segundos. */
    @Transactional
    public void touch(User user, String tokenId) {
        UserSession session = sessions.findById(tokenId).orElse(null);
        if (session == null || !session.getUser().getId().equals(user.getId()) || session.isRevoked()) {
            return;
        }
        Instant now = Instant.now();
        if (session.getLastSeen().isBefore(now.minusSeconds(15))) {
            session.touch(now);
        }
    }

    /** Registra solo operaciones HTTP exitosas que modifican datos. */
    @Transactional
    public void recordAction(User user, String tokenId, String action) {
        UserSession session = sessions.findById(tokenId).orElse(null);
        if (session == null || !session.getUser().getId().equals(user.getId())) {
            return;
        }
        session.recordAction(action, Instant.now());
        activities.save(new UserActivityLog(user, action));
    }

    @Transactional
    public void logout(User user, String tokenId) {
        UserSession session = sessions.findById(tokenId).orElse(null);
        if (session == null || !session.getUser().getId().equals(user.getId()) || session.isRevoked()) {
            return;
        }
        session.revoke();
        activities.save(new UserActivityLog(user, "LOGOUT"));
    }

    @Transactional
    public void revoke(User admin, String tokenId) {
        Optional<UserSession> found = sessions.findById(tokenId);
        if (found.isEmpty()) {
            throw new ResourceNotFoundException("Sesion no encontrada");
        }
        UserSession session = found.get();
        session.revoke();
        activities.save(new UserActivityLog(admin, "REVOKE_SESSION " + session.getUser().getUsername()));
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        for (UserSession session : sessions.findAllByUser_Id(userId)) {
            if (!session.isRevoked()) {
                session.revoke();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<UserSessionDTO> getSessions(boolean onlyOnline) {
        List<UserSessionDTO> result = new ArrayList<>();
        Instant now = Instant.now();
        for (UserSession session : sessions.findAllByOrderByLastSeenDesc()) {
            UserSessionDTO dto = new UserSessionDTO(session, now);
            if (!onlyOnline || dto.isOnline()) {
                result.add(dto);
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public int countOnlineUsers() {
        Set<Long> userIds = new HashSet<>();
        Instant now = Instant.now();
        for (UserSession session : sessions.findAllByOrderByLastSeenDesc()) {
            if (session.isOnline(now)) {
                userIds.add(session.getUser().getId());
            }
        }
        return userIds.size();
    }

    @Transactional(readOnly = true)
    public List<UserActivityDTO> getActivity(Long userId) {
        List<UserActivityDTO> result = new ArrayList<>();
        List<UserActivityLog> logs;
        if (userId == null) {
            logs = activities.findTop200ByOrderByCreatedAtDesc();
        } else {
            logs = activities.findTop200ByUser_IdOrderByCreatedAtDesc(userId);
        }
        for (UserActivityLog log : logs) {
            result.add(new UserActivityDTO(log));
        }
        return result;
    }
}
