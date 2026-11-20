package co.edu.unbosque.advancewars.repository;

import java.util.List;
import co.edu.unbosque.advancewars.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<UserSession, String> {
    List<UserSession> findAllByOrderByLastSeenDesc();
    List<UserSession> findAllByUser_Id(Long userId);
}
