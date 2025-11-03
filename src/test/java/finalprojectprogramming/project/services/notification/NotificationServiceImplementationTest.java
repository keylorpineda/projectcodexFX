package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.dtos.NotificationDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.mail.EmailService;
import finalprojectprogramming.project.services.mail.EmailServiceImplementation.MailSendingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.MockedStatic;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NotificationServiceImplementationTest {

    private NotificationRepository notificationRepository;
    private ReservationRepository reservationRepository;
    private ModelMapper modelMapper;
    private EmailService emailService;
    private NotificationServiceImplementation service;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        reservationRepository = mock(ReservationRepository.class);
        modelMapper = new ModelMapper();
        emailService = mock(EmailService.class);
        service = new NotificationServiceImplementation(notificationRepository, reservationRepository, modelMapper, emailService);
    }

    private Reservation activeReservation(Long id, Long userId, String email) {
        User user = User.builder().id(userId).email(email).build();
        return Reservation.builder().id(id).user(user).notifications(new ArrayList<>()).build();
    }

    @Test
    void create_saves_notification_for_active_reservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(activeReservation(1L, 10L, "a@b.com")));
        Notification saved = Notification.builder().id(5L).type(NotificationType.REMINDER).build();
        when(notificationRepository.save(any(Notification.class))).thenReturn(saved);

        NotificationDTO out = service.create(NotificationDTO.builder()
                .reservationId(1L).type(NotificationType.REMINDER).sentTo("a@b.com")
                .messageContent("hi").status(NotificationStatus.SENT).build());

        assertThat(out.getId()).isEqualTo(5L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void update_changes_fields_and_throws_when_not_found() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, NotificationDTO.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);

    Notification existing = Notification.builder().id(7L)
                .reservation(activeReservation(1L, 2L, "u@x"))
        .type(NotificationType.REMINDER).sentTo("u@x").messageContent("m")
        .status(NotificationStatus.SENT).build();
        when(notificationRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(reservationRepository.findById(2L)).thenReturn(Optional.of(activeReservation(2L, 3L, "c@d")));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

    NotificationDTO out = service.update(7L, NotificationDTO.builder()
        .reservationId(2L).type(NotificationType.CANCELLATION).sentTo("x@y")
                .messageContent("n").sentAt(LocalDateTime.now()).status(NotificationStatus.SENT).build());

    assertThat(out.getType()).isEqualTo(NotificationType.CANCELLATION);
        assertThat(out.getSentTo()).isEqualTo("x@y");
        assertThat(out.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void findById_and_findAll_and_findByReservation_and_delete_enforce_security() {
        Notification n = Notification.builder().id(1L).reservation(activeReservation(5L, 9L, "e@f"))
                .type(NotificationType.REMINDER).status(NotificationStatus.SENT).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(n));
        when(notificationRepository.findAll()).thenReturn(List.of(n));
        when(notificationRepository.findByReservationId(5L)).thenReturn(List.of(n));
        when(reservationRepository.findById(5L)).thenReturn(Optional.of(activeReservation(5L, 9L, "e@f")));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            service.findById(1L);
            service.findAll();
            service.findByReservation(5L);

            mocked.verify(() -> SecurityUtils.requireSelfOrAny(eq(9L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)), times(2));
            mocked.verify(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)), times(1));
        }

        when(notificationRepository.findById(2L)).thenReturn(Optional.of(n));
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            service.delete(2L);
            mocked.verify(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)));
        }
        verify(notificationRepository).delete(any(Notification.class));
    }

    @Test
    void sendCustomEmail_records_success_and_failure() {
        Reservation res = activeReservation(3L, 11L, "p@q.com");
        when(reservationRepository.findById(3L)).thenReturn(Optional.of(res));

        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            // success
            service.sendCustomEmailToReservation(3L, "Sub", "Hola");
            verify(emailService).sendCustomEmail(eq(res), eq("Sub"), eq("Hola"));
            verify(notificationRepository).save(any(Notification.class));

            // failure path -> MailSendingException propagates and status updated to FAILED
            doThrow(new MailSendingException("down", new RuntimeException("x")))
                    .when(emailService).sendCustomEmail(any(Reservation.class), anyString(), anyString());
            assertThatThrownBy(() -> service.sendCustomEmailToReservation(3L, "Sub", "Hola"))
                    .isInstanceOf(MailSendingException.class);
            verify(notificationRepository, atLeast(2)).save(any(Notification.class));
        }

    // missing user email
    try (MockedStatic<SecurityUtils> ignored2 = mockStatic(SecurityUtils.class)) {
        Reservation noUser = Reservation.builder().id(4L).user(new User()).build();
        when(reservationRepository.findById(4L)).thenReturn(Optional.of(noUser));
        assertThatThrownBy(() -> service.sendCustomEmailToReservation(4L, "a", "b"))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    }
}
