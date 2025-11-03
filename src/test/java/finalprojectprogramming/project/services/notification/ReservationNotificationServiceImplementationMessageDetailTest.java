package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.services.mail.EmailService;
import finalprojectprogramming.project.services.mail.EmailServiceImplementation.MailSendingException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReservationNotificationServiceImplementationMessageDetailTest {

    @Test
    void canceled_notification_includes_end_time_and_reason_and_handles_mail_failure() {
        // Arrange
        NotificationRepository notificationRepository = mock(NotificationRepository.class);
        EmailService emailService = mock(EmailService.class);
        ReservationNotificationServiceImplementation service =
                new ReservationNotificationServiceImplementation(notificationRepository, emailService);

        Reservation reservation = new Reservation();
        reservation.setId(3L);
        User user = new User();
        user.setEmail("citizen@example.com");
        reservation.setUser(user);
        Space space = new Space();
        space.setName("Auditorio");
        reservation.setSpace(space);
        reservation.setStartTime(LocalDateTime.now().minusHours(1));
        reservation.setEndTime(LocalDateTime.now());
        reservation.setCancellationReason("mantenimiento");
        reservation.setNotifications(new ArrayList<>());

        doThrow(new MailSendingException("down", null)).when(emailService).sendReservationCanceled(any());
        when(notificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        service.notifyReservationCanceled(reservation);

        // Assert: se añadió a la lista y se guardó con estado FAILED por el fallo de email
        assertThat(reservation.getNotifications()).hasSize(1);
        Notification notif = reservation.getNotifications().get(0);
        assertThat(notif.getType()).isEqualTo(NotificationType.CANCELLATION);
        assertThat(notif.getStatus()).isEqualTo(NotificationStatus.FAILED);
        verify(notificationRepository, times(1)).save(any());
    }
}
