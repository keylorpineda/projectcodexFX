package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.dtos.NotificationDTO;
import java.util.List;

public interface NotificationService {

    NotificationDTO create(NotificationDTO notificationDTO);

    NotificationDTO update(Long id, NotificationDTO notificationDTO);

    NotificationDTO findById(Long id);

    List<NotificationDTO> findAll();

    List<NotificationDTO> findByReservation(Long reservationId);

    void delete(Long id);
    
    void sendCustomEmailToReservation(Long reservationId, String subject, String message);
}