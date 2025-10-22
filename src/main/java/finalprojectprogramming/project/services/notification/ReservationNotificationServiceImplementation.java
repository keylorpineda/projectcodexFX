package finalprojectprogramming.project.services.notification;

import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.repositories.NotificationRepository;
import finalprojectprogramming.project.services.mail.EmailService;
import finalprojectprogramming.project.services.mail.EmailServiceImplementation.MailSendingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ReservationNotificationServiceImplementation implements ReservationNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReservationNotificationServiceImplementation.class);
    private static final Locale LOCALE_ES_CR = Locale.forLanguageTag("es-CR");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy, hh:mm a", LOCALE_ES_CR);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public ReservationNotificationServiceImplementation(NotificationRepository notificationRepository,
            EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Override
    public void notifyReservationCreated(Reservation reservation) {
        dispatch(reservation, NotificationType.CONFIRMATION, () -> emailService.sendReservationCreated(reservation),
                "Se registró tu solicitud de reserva.");
    }

    @Override
    public void notifyReservationApproved(Reservation reservation) {
        dispatch(reservation, NotificationType.CONFIRMATION, () -> emailService.sendReservationApproved(reservation),
                "Tu reserva fue aprobada.");
    }

    @Override
    public void notifyReservationCanceled(Reservation reservation) {
        dispatch(reservation, NotificationType.CANCELLATION, () -> emailService.sendReservationCanceled(reservation),
                "Tu reserva fue cancelada.");
    }

    private void dispatch(Reservation reservation, NotificationType type, Runnable emailTask, String summary) {
        if (reservation == null || reservation.getUser() == null
                || !StringUtils.hasText(reservation.getUser().getEmail())) {
            LOGGER.warn("Notification of type {} not sent because reservation {} has no associated email", type,
                    reservation != null ? reservation.getId() : null);
            return;
        }
        Notification notification = Notification.builder()
                .reservation(reservation)
                .type(type)
                .sentTo(reservation.getUser().getEmail())
                .messageContent(buildMessage(reservation, summary))
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.SENT)
                .build();
        try {
            emailTask.run();
        } catch (MailSendingException ex) {
            notification.setStatus(NotificationStatus.FAILED);
            LOGGER.error("Email delivery failed for reservation {}", reservation.getId(), ex);
        }
        if (reservation.getNotifications() != null) {
            reservation.getNotifications().add(notification);
        }
        notificationRepository.save(notification);
    }

    private String buildMessage(Reservation reservation, String summary) {
        StringBuilder builder = new StringBuilder();
        builder.append(summary).append(' ');
        builder.append("Espacio: ")
                .append(reservation.getSpace() != null ? reservation.getSpace().getName() : "(sin nombre)")
                .append(". Fecha: ")
                .append(reservation.getStartTime() != null ? DATE_TIME_FORMATTER.format(reservation.getStartTime())
                        : "pendiente")
                .append(". Estado: ")
                .append(reservation.getStatus() != null ? reservation.getStatus().name() : "DESCONOCIDO");
        if (reservation.getEndTime() != null) {
            builder.append(". Finaliza: ").append(DATE_TIME_FORMATTER.format(reservation.getEndTime()));
        }
        if (StringUtils.hasText(reservation.getCancellationReason())) {
            builder.append(". Motivo de cancelación: ").append(reservation.getCancellationReason());
        }
        return builder.toString();
    }
}