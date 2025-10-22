package finalprojectprogramming.project.repositories;

import finalprojectprogramming.project.models.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReservationId(Long reservationId);
}