package finalprojectprogramming.project.services.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.services.mail.EmailService;
import finalprojectprogramming.project.services.mail.EmailServiceImplementation.MailSendingException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ReservationNotificationServiceImplementationTest {

    private NotificationRepository repo;
    private EmailService emailService;
    private ReservationNotificationServiceImplementation service;

    @BeforeEach
    void setUp() {
        repo = mock(NotificationRepository.class);
        emailService = mock(EmailService.class);
        service = new ReservationNotificationServiceImplementation(repo, emailService);
    }

    private static Reservation reservationWithUser(String email) {
        User user = User.builder().email(email).build();
        Space space = new Space();
        space.setName("Sala A");
        return Reservation.builder()
                .id(3L)
                .user(user)
                .space(space)
                .startTime(LocalDateTime.of(2025, 1, 1, 9, 0))
                .endTime(LocalDateTime.of(2025, 1, 1, 10, 0))
                .status(ReservationStatus.CONFIRMED)
                .build();
    }

    @Test
    void dispatch_skips_when_no_email() {
        Reservation withoutUser = Reservation.builder().id(1L).build();
    service.notifyReservationCreated(withoutUser);
    verifyNoInteractions(emailService);
    verifyNoInteractions(repo);
    }

    @Test
    void notifyReservationCreated_sends_and_saves_success() {
        Reservation r = reservationWithUser("p@q.com");
        service.notifyReservationCreated(r);
        verify(emailService).sendReservationCreated(r);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.CONFIRMATION);
        assertThat(saved.getSentTo()).isEqualTo("p@q.com");
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(saved.getMessageContent()).contains("Sala A");
    }

    @Test
    void notifyReservationCanceled_marks_failed_when_email_throws() {
        Reservation r = reservationWithUser("p@q.com");
        doThrow(new MailSendingException("down", new RuntimeException("x")))
                .when(emailService).sendReservationCanceled(r);
        service.notifyReservationCanceled(r);
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(repo).save(captor.capture());
        Notification saved = captor.getValue();
        assertThat(saved.getType()).isEqualTo(NotificationType.CANCELLATION);
        assertThat(saved.getStatus()).isEqualTo(NotificationStatus.FAILED);
    }

    @Test
    void notifyReservationApproved_calls_email() {
        Reservation r = reservationWithUser("p@q.com");
        assertThatCode(() -> service.notifyReservationApproved(r)).doesNotThrowAnyException();
        verify(emailService).sendReservationApproved(r);
    }
}
