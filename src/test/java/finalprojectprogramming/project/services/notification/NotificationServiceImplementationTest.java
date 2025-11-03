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
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        notificationRepository = mock(NotificationRepository.class);
        reservationRepository = mock(ReservationRepository.class);
    modelMapper = new ModelMapper();
        emailService = mock(EmailService.class);
    auditLogService = mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new NotificationServiceImplementation(notificationRepository, reservationRepository, modelMapper, emailService, auditLogService, objectMapper);
    }

    @Test
    void delete_records_audit_with_null_reservation_and_actor_fallback() {
        // Notification sin reserva asociada para cubrir rama que omite reservationId en detalles
        Notification n = Notification.builder()
                .id(100L)
                .type(NotificationType.REMINDER)
                .status(NotificationStatus.SENT)
                .reservation(null)
                .sentTo("a@b")
                .build();
        when(notificationRepository.findById(100L)).thenReturn(Optional.of(n));

        // Mockear SecurityUtils.getCurrentUserId para que lance excepción y cubra el catch
        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireAny(eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(inv -> null);
            mocked.when(SecurityUtils::getCurrentUserId).thenThrow(new RuntimeException("whoops"));

            // Capturar detalles enviados al audit log
            var detailsCaptor = org.mockito.ArgumentCaptor.forClass(com.fasterxml.jackson.databind.JsonNode.class);
            doNothing().when(auditLogService).logEvent(isNull(), eq("NOTIFICATION_DELETED"), eq("100"), detailsCaptor.capture());

            service.delete(100L);

            verify(notificationRepository).delete(any(Notification.class));

            com.fasterxml.jackson.databind.JsonNode details = detailsCaptor.getValue();
            // Debe existir notificationId y status, pero NO reservationId
            assertThat(details.get("notificationId")).isNotNull();
            assertThat(details.get("status")).isNotNull();
            assertThat(details.get("reservationId")).isNull();
        }
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
            verify(emailService).sendCustomAdminEmail(eq("p@q.com"), isNull(), eq("Sub"), eq("Hola"));
            verify(notificationRepository).save(any(Notification.class));

            // failure path -> MailSendingException propagates and status updated to FAILED
        doThrow(new MailSendingException("down", new RuntimeException("x")))
            .when(emailService).sendCustomAdminEmail(anyString(), nullable(String.class), anyString(), anyString());
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

    @Test
    void sendCustomEmail_does_not_add_to_null_notifications_but_saves_notification() {
        // Reserva con notifications == null para cubrir la rama false del finally
        Reservation res = Reservation.builder()
                .id(13L)
                .user(User.builder().id(99L).email("z@y.com").build())
                .notifications(null)
                .build();
        when(reservationRepository.findById(13L)).thenReturn(Optional.of(res));

        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            // Envío exitoso
            service.sendCustomEmailToReservation(13L, "Asunto", "Mensaje");

            // No se debe intentar añadir a una lista nula, pero la notificación debe guardarse
            verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
        }
    }

    @Test
    void sendCustomEmail_throws_when_reservation_not_found_and_findByReservation_throws_when_deleted() {
        // not found path (covers getActiveReservation not-found throw line)
        when(reservationRepository.findById(404L)).thenReturn(Optional.empty());
        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> service.sendCustomEmailToReservation(404L, "s", "m"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation with id 404 not found");
        }

        // deleted path (covers second throw in getActiveReservation)
        Reservation deleted = Reservation.builder().id(7L).deletedAt(java.time.LocalDateTime.now()).build();
        when(reservationRepository.findById(7L)).thenReturn(Optional.of(deleted));
        try (MockedStatic<SecurityUtils> ignored2 = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> service.findByReservation(7L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Reservation with id 7 not found");
        }
    }
}
