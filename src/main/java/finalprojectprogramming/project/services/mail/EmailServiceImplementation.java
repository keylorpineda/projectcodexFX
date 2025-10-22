package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.io.UnsupportedEncodingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

@Service
public class EmailServiceImplementation implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImplementation.class);
    private static final Locale LOCALE_ES_CR = Locale.forLanguageTag("es-CR");
    private static final DateTimeFormatter DAY_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy", LOCALE_ES_CR);
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("hh:mm a", LOCALE_ES_CR);

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;

    public EmailServiceImplementation(JavaMailSender mailSender, MailConfig mailConfig) {
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
    }

    @Override
    public void sendReservationCreated(Reservation reservation) {
        String subject = "¡Tu solicitud de reserva fue recibida!";
        String preheader = "Estamos revisando la disponibilidad de tu espacio.";
        String title = "Reserva en proceso";
        String intro = "Hemos recibido tu solicitud y la municipalidad la revisará en breve.";
        send(reservation, subject, preheader, title, intro, "#6C63FF");
    }

    @Override
    public void sendReservationApproved(Reservation reservation) {
        String subject = "¡Tu reserva está confirmada!";
        String preheader = "Todo listo para disfrutar del espacio.";
        String title = "Reserva confirmada";
        String intro = "Tu solicitud fue aprobada y el espacio ya está reservado a tu nombre.";
        send(reservation, subject, preheader, title, intro, "#38B2AC");
    }

    @Override
    public void sendReservationCanceled(Reservation reservation) {
        String subject = "Tu reserva fue cancelada";
        String preheader = "Te contamos los detalles de la cancelación.";
        String title = "Reserva cancelada";
        String intro = "La reserva ha sido cancelada. Aquí tienes un resumen con la información clave.";
        send(reservation, subject, preheader, title, intro, "#F56565");
    }

    private void send(Reservation reservation, String subject, String preheader, String title, String intro,
            String accentColor) {
        User user = reservation.getUser();
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            LOGGER.warn("Skipping email for reservation {} because it has no recipient email configured", reservation.getId());
            return;
        }
        String htmlBody = buildHtml(reservation, preheader, title, intro, accentColor);
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            String fromAddress = StringUtils.hasText(mailConfig.getAddress()) ? mailConfig.getAddress()
                    : user.getEmail();
            String fromName = StringUtils.hasText(mailConfig.getName()) ? mailConfig.getName()
                    : "Municipalidad de Pérez Zeledón";
            try {
                helper.setFrom(fromAddress, fromName);
            } catch (UnsupportedEncodingException ex) {
                LOGGER.warn("Invalid sender encoding, using raw address instead", ex);
                helper.setFrom(fromAddress);
            }
            helper.setText(htmlBody, true);
            mailSender.send(message);
            LOGGER.info("Reservation email '{}' sent to {}", subject, user.getEmail());
        } catch (MessagingException | MailException ex) {
            LOGGER.error("Error sending reservation email to {}", user.getEmail(), ex);
            throw new MailSendingException("Error sending email", ex);
        }
    }

    private String buildHtml(Reservation reservation, String preheader, String title, String intro, String accentColor) {
        Space space = reservation.getSpace();
        String spaceName = sanitize(space != null ? space.getName() : "Espacio municipal");
        String location = sanitize(space != null ? nullSafe(space.getLocation()) : "Por definir");
        String attendees = reservation.getAttendees() != null ? reservation.getAttendees().toString() : "--";
        String notes = sanitize(nullSafe(reservation.getNotes()));
        String qrCode = sanitize(nullSafe(reservation.getQrCode()));
        String status = translateStatus(reservation.getStatus());

        String dateLine = formatDate(reservation.getStartTime());
        String scheduleLine = formatSchedule(reservation.getStartTime(), reservation.getEndTime());

        String cancellationReason = sanitize(nullSafe(reservation.getCancellationReason()));
        boolean isCanceled = reservation.getStatus() == ReservationStatus.CANCELED;

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>")
                .append("<html lang=\"es\">")
                .append("<head><meta charset=\"UTF-8\"><meta name=\"color-scheme\" content=\"light\"/>")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>").append(title).append("</title></head>")
                .append("<body style=\"margin:0;padding:32px;background:#f4f7fb;font-family:'Segoe UI',Arial,sans-serif;\">")
                .append("<div style=\"max-width:600px;margin:0 auto;background:#ffffff;border-radius:18px;padding:32px;box-shadow:0 24px 48px rgba(15,35,95,0.12);\">")
                .append("<div style=\"font-size:0;color:transparent;height:0;overflow:hidden\">").append(preheader)
                .append("</div>")
                .append("<div style=\"text-align:center\">")
                .append("<div style=\"display:inline-block;padding:10px 18px;border-radius:999px;background:")
                .append(accentColor)
                .append("10;color:").append(accentColor)
                .append(";font-size:13px;font-weight:600;letter-spacing:1.6px;text-transform:uppercase;\">")
                .append(escape(title))
                .append("</div>")
                .append("<h1 style=\"color:#0f235f;font-size:28px;margin:18px 0 12px;font-weight:700;\">")
                .append(escape(spaceName))
                .append("</h1>")
                .append("<p style=\"color:#4c5d73;font-size:16px;line-height:1.6;margin:0 auto 24px;max-width:460px;\">")
                .append(escape(intro))
                .append("</p>")
                .append("</div>")
                .append("<div style=\"background:#f5f7ff;border-radius:14px;padding:24px;margin-bottom:24px;\">")
                .append(detailRow("Fecha", dateLine))
                .append(detailRow("Horario", scheduleLine))
                .append(detailRow("Ubicación", location))
                .append(detailRow("Asistentes", attendees))
                .append(detailRow("Estado", status));
        if (StringUtils.hasText(qrCode)) {
            builder.append(detailRow("Código QR", qrCode));
        }
        if (StringUtils.hasText(notes)) {
            builder.append(detailRow("Notas", notes));
        }
        if (isCanceled && StringUtils.hasText(cancellationReason)) {
            builder.append(detailRow("Motivo de cancelación", cancellationReason));
        }
        builder.append("</div>")
                .append("<div style=\"border-radius:14px;padding:24px;background:#0f235f;color:#ffffff;\">")
                .append("<h2 style=\"margin:0;font-size:22px;\">¿Necesitas ayuda?</h2>")
                .append("<p style=\"margin:12px 0 0;font-size:15px;line-height:1.6;color:#d9e2ff;\">")
                .append("Respondé este correo o contactá a la Oficina de Reservas de la Municipalidad de Pérez Zeledón.")
                .append("</p>")
                .append("</div>")
                .append("<p style=\"color:#6b7a99;font-size:13px;text-align:center;margin:28px 0 0;\">")
                .append("© ").append(LocalDateTime.now().getYear())
                .append(" Municipalidad de Pérez Zeledón. Todos los derechos reservados.")
                .append("</p>")
                .append("</div></body></html>");
        return builder.toString();
    }

    private String detailRow(String label, String value) {
        return new StringBuilder()
                .append("<div style=\"display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:14px;\">")
                .append("<span style=\"font-size:14px;color:#6b7a99;font-weight:600;letter-spacing:0.5px;text-transform:uppercase;\">")
                .append(escape(label))
                .append("</span>")
                .append("<span style=\"font-size:15px;color:#1f2a44;font-weight:600;text-align:right;max-width:60%;\">")
                .append(escape(value))
                .append("</span></div>")
                .toString();
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Por definir";
        }
        String formatted = DAY_FORMATTER.format(dateTime);
        return capitalize(formatted);
    }

    private String formatSchedule(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "Horario pendiente";
        }
        return TIME_FORMATTER.format(start) + " - " + TIME_FORMATTER.format(end);
    }

    private String translateStatus(ReservationStatus status) {
        if (status == null) {
            return "Sin estado";
        }
        return switch (status) {
            case PENDING -> "Pendiente de aprobación";
            case CONFIRMED -> "Confirmada";
            case CANCELED -> "Cancelada";
            case CHECKED_IN -> "Con registro de ingreso";
            case NO_SHOW -> "Marcada como inasistencia";
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String sanitize(String value) {
        return value != null ? value.trim() : "";
    }

    private String escape(String value) {
        return HtmlUtils.htmlEscape(value != null ? value : "", StandardCharsets.UTF_8.name());
    }

    private String capitalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        String lower = value.toLowerCase(LOCALE_ES_CR);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static class MailSendingException extends RuntimeException {
        public MailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}