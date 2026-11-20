package co.edu.unbosque.advancewars.repository;

import java.util.List;
import co.edu.unbosque.advancewars.model.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {
    List<UserActivityLog> findTop200ByOrderByCreatedAtDesc();
    List<UserActivityLog> findTop200ByUser_IdOrderByCreatedAtDesc(Long userId);
}
