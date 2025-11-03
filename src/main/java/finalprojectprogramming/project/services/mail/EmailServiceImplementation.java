package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.services.qr.QRCodeService;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.util.ByteArrayDataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
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
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM yyyy 'a las' hh:mm a", LOCALE_ES_CR);

    private final JavaMailSender mailSender;
    private final MailConfig mailConfig;
    private final QRCodeService qrCodeService;

    public EmailServiceImplementation(JavaMailSender mailSender, MailConfig mailConfig, QRCodeService qrCodeService) {
        this.mailSender = mailSender;
        this.mailConfig = mailConfig;
        this.qrCodeService = qrCodeService;
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

    @Override
    public void sendCustomEmail(Reservation reservation, String subject, String customMessage) {
        String preheader = "Mensaje importante sobre tu reserva.";
        String title = "Notificación de reserva";
        String intro = customMessage;
        send(reservation, subject, preheader, title, intro, "#4C51BF");
    }

    private void send(Reservation reservation, String subject, String preheader, String title, String intro,
            String accentColor) {
        User user = reservation.getUser();
        if (user == null || !StringUtils.hasText(user.getEmail())) {
            LOGGER.warn("Skipping email for reservation {} because it has no recipient email configured", reservation.getId());
            return;
        }
        
        // Generar imagen QR si existe código
        String qrImageCid = null;
        byte[] qrImageBytes = null;
        if (StringUtils.hasText(reservation.getQrCode())) {
            try {
                qrImageBytes = qrCodeService.generateQRCodeImage(reservation.getQrCode(), 250, 250);
                qrImageCid = "qr-code-" + reservation.getId();
            } catch (IOException e) {
                LOGGER.warn("Failed to generate QR code image for reservation {}", reservation.getId(), e);
            }
        }
        
        String htmlBody = buildHtml(reservation, preheader, title, intro, accentColor, qrImageCid);
        
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
            
            // Adjuntar imagen QR como recurso embebido
            if (qrImageBytes != null && qrImageCid != null) {
                DataSource qrDataSource = new ByteArrayDataSource(qrImageBytes, "image/png");
                helper.addInline(qrImageCid, qrDataSource);
            }
            
            mailSender.send(message);
            LOGGER.info("Reservation email '{}' sent to {} (QR included: {})", subject, user.getEmail(), qrImageCid != null);
        } catch (MessagingException | MailException ex) {
            LOGGER.error("Error sending reservation email to {}", user.getEmail(), ex);
            throw new MailSendingException("Error sending email", ex);
        }
    }

    private String buildHtml(Reservation reservation, String preheader, String title, String intro, String accentColor, 
            String qrImageCid) {
        Space space = reservation.getSpace();
        String spaceName = sanitize(space != null ? space.getName() : "Espacio municipal");
        String location = sanitize(space != null ? nullSafe(space.getLocation()) : "Por definir");
        String spaceType = translateSpaceType(space != null ? space.getType() : null);
        String capacity = formatAttendees(space != null ? space.getCapacity() : null);
        String approval = space != null && Boolean.TRUE.equals(space.getRequiresApproval()) ?
                "Requiere aprobación del administrador" : "Aprobación automática";
        String maxDuration = formatMaxDuration(space != null ? space.getMaxReservationDuration() : null);
        String spaceDescription = sanitize(space != null ? nullSafe(space.getDescription()) : "");

        String attendees = formatAttendees(reservation.getAttendees());
        String notes = sanitize(nullSafe(reservation.getNotes()));
        String qrCode = sanitize(nullSafe(reservation.getQrCode()));
        String status = translateStatus(reservation.getStatus());

        String dateLine = formatDate(reservation.getStartTime());
        String scheduleLine = formatSchedule(reservation.getStartTime(), reservation.getEndTime());
        String durationLine = formatReservationDuration(reservation.getStartTime(), reservation.getEndTime());
        String createdLine = formatDateTime(reservation.getCreatedAt());
        String checkInLine = formatDateTime(reservation.getCheckinAt());

        String cancellationReason = sanitize(nullSafe(reservation.getCancellationReason()));
        boolean isCanceled = reservation.getStatus() == ReservationStatus.CANCELED;
        String statusSummary = buildStatusSummary(reservation);
        List<String> nextSteps = buildNextSteps(reservation);
        String normalizedIntro = sanitize(intro);

        StringBuilder builder = new StringBuilder();
        builder.append("<!DOCTYPE html>")
                .append("<html lang=\"es\">")
                .append("<head><meta charset=\"UTF-8\"><meta name=\"color-scheme\" content=\"light\"/>")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>").append(title).append("</title></head>")
                .append("<body style=\"margin:0;padding:32px;background:#f4f7fb;font-family:'Segoe UI',Arial,sans-serif;\">")
                .append("<div style=\"max-width:640px;margin:0 auto;background:#ffffff;border-radius:20px;padding:36px;box-shadow:0 24px 48px rgba(15,35,95,0.12);\">")
                .append("<div style=\"font-size:0;color:transparent;height:0;overflow:hidden\">").append(preheader)
                .append("</div>")
                .append("<div style=\"text-align:center\">")
                .append("<div style=\"display:inline-block;padding:12px 20px;border-radius:999px;background:")
                .append(accentColor)
                .append("14;color:").append(accentColor)
                .append(";font-size:12px;font-weight:700;letter-spacing:1.8px;text-transform:uppercase;\">")
                .append(escape(title))
                .append("</div>")
                .append("<h1 style=\"color:#0f235f;font-size:28px;margin:20px 0 14px;font-weight:700;\">")
                .append(escape(spaceName))
                .append("</h1>")
                .append("<p style=\"color:#4c5d73;font-size:16px;line-height:1.6;margin:0 auto 24px;max-width:460px;\">")
                .append(formatMultiline(normalizedIntro))
                .append("</p>")
                .append("</div>");
        
        // Sección del código QR (destacada) - SOLO si existe QR
        if (qrImageCid != null && StringUtils.hasText(qrCode)) {
            builder.append("<div style=\"background:linear-gradient(135deg, ").append(accentColor).append("0A 0%, ")
                    .append(accentColor).append("20 100%);border-radius:20px;padding:32px;margin-bottom:32px;text-align:center;border:3px solid ")
                    .append(accentColor).append("30;\">")
                    .append("<div style=\"display:inline-block;padding:8px 16px;border-radius:999px;background:")
                    .append(accentColor).append(";color:#ffffff;font-size:11px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;margin-bottom:20px;\">")
                    .append("✓ Tu código QR")
                    .append("</div>")
                    .append("<div style=\"background:#ffffff;border-radius:16px;padding:24px;display:inline-block;box-shadow:0 8px 24px rgba(0,0,0,0.08);\">")
                    .append("<img src=\"cid:").append(qrImageCid).append("\" alt=\"Código QR\" style=\"display:block;width:250px;height:250px;margin:0 auto;\"/>")
                    .append("</div>")
                    .append("<p style=\"margin:20px 0 8px;font-size:14px;font-weight:700;color:#0f235f;letter-spacing:0.5px;\">Código de reserva</p>")
                    .append("<p style=\"margin:0;font-size:20px;font-weight:800;color:")
                    .append(accentColor).append(";font-family:'Courier New',monospace;letter-spacing:2px;\">")
                    .append(escape(qrCode))
                    .append("</p>")
                    .append("<p style=\"margin:16px auto 0;font-size:13px;color:#4c5d73;max-width:400px;line-height:1.5;\">")
                    .append("📱 Presentá este código QR al ingresar al espacio o mostrá el código alfanumérico")
                    .append("</p>")
                    .append("</div>");
        }
        
        builder.append("<div style=\"background:").append(accentColor).append("0F;border-radius:16px;padding:22px;margin-bottom:28px;\">")
                .append("<h2 style=\"margin:0 0 8px;font-size:20px;color:").append(accentColor).append(";text-transform:uppercase;letter-spacing:1px;\">Estado actual</h2>")
                .append("<p style=\"margin:0;font-size:15px;line-height:1.6;color:#1f2a44;\">")
                .append(formatMultiline(statusSummary))
                .append("</p>")
                .append("</div>")
                .append("<div style=\"display:grid;gap:20px;margin-bottom:28px;grid-template-columns:repeat(auto-fit,minmax(240px,1fr));\">")
                .append("<div style=\"background:#f5f7ff;border-radius:16px;padding:24px;\">")
                .append("<h3 style=\"margin:0 0 16px;color:#0f235f;font-size:18px;\">Detalles de la reserva</h3>")
                .append(detailRow("Fecha", dateLine))
                .append(detailRow("Horario", scheduleLine))
                .append(detailRow("Duración", durationLine))
                .append(detailRow("Asistentes", attendees))
                .append(detailRow("Estado", status))
                .append(detailRow("Creada", createdLine));
        if (StringUtils.hasText(checkInLine)) {
            builder.append(detailRow("Ingreso registrado", checkInLine));
        }
        // No mostrar código QR aquí ya que está en la sección destacada
        builder.append("</div>")
                .append("<div style=\"background:#f5f7ff;border-radius:16px;padding:24px;\">")
                .append("<h3 style=\"margin:0 0 16px;color:#0f235f;font-size:18px;\">Información del espacio</h3>")
                .append(detailRow("Ubicación", location))
                .append(detailRow("Tipo", spaceType))
                .append(detailRow("Capacidad", capacity))
                .append(detailRow("Política", approval))
                .append(detailRow("Máx. por reserva", maxDuration));
        if (StringUtils.hasText(spaceDescription)) {
            builder.append(detailRow("Descripción", spaceDescription));
        }
        builder.append("</div>")
                .append("</div>");
        if (StringUtils.hasText(notes)) {
            builder.append("<div style=\"background:#fef7e5;border-radius:14px;padding:22px;margin-bottom:28px;border:1px solid #f6d78f;\">")
                    .append("<h3 style=\"margin:0 0 10px;font-size:17px;color:#8b6a11;\">📝 Notas adicionales</h3>")
                    .append("<p style=\"margin:0;font-size:15px;line-height:1.6;color:#5b4607;\">")
                    .append(formatMultiline(notes))
                    .append("</p>")
                    .append("</div>");
        }
        if (isCanceled && StringUtils.hasText(cancellationReason)) {
            builder.append("<div style=\"background:#fee9ec;border-radius:14px;padding:22px;margin-bottom:28px;border:1px solid #f8b7c1;\">")
                    .append("<h3 style=\"margin:0 0 10px;font-size:17px;color:#a30b2f;\">⚠️ Motivo de cancelación</h3>")
                    .append("<p style=\"margin:0;font-size:15px;line-height:1.6;color:#781125;\">")
                    .append(formatMultiline(cancellationReason))
                    .append("</p>")
                    .append("</div>");
        }
        if (!nextSteps.isEmpty()) {
            builder.append("<div style=\"background:#eef3ff;border-radius:16px;padding:24px;margin-bottom:28px;\">")
                    .append("<h3 style=\"margin:0 0 14px;font-size:18px;color:#1f2a44;\">📋 Próximos pasos recomendados</h3>")
                    .append("<ul style=\"margin:0;padding-left:20px;color:#1f2a44;font-size:15px;line-height:1.6;\">");
            for (String step : nextSteps) {
                builder.append("<li style=\"margin-bottom:8px;\">")
                        .append(formatMultiline(step))
                        .append("</li>");
            }
            builder.append("</ul></div>");
        }
        builder.append("<div style=\"border-radius:16px;padding:24px;background:#0f235f;color:#ffffff;\">")
                .append("<h2 style=\"margin:0;font-size:22px;\">💬 ¿Necesitas ayuda?</h2>")
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
                .append("<div style=\"display:flex;gap:16px;align-items:flex-start;margin-bottom:14px;\">")
                .append("<span style=\"min-width:120px;font-size:13px;color:#6b7a99;font-weight:700;letter-spacing:0.6px;text-transform:uppercase;\">")
                .append(escape(label))
                .append("</span>")
                .append("<span style=\"flex:1;font-size:15px;color:#1f2a44;font-weight:600;white-space:pre-wrap;\">")
                .append(formatMultiline(value))
                .append("</span></div>")
                .toString();
    }

    private String formatMultiline(String value) {
        String normalized = normalizeSpacing(value);
        String escaped = escape(normalized);
        return escaped.replace("\n", "<br/>");
    }

    private String formatAttendees(Integer attendees) {
        if (attendees == null || attendees <= 0) {
            return "--";
        }
        return attendees + (attendees == 1 ? " persona" : " personas");
    }

    private String formatReservationDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return "Por definir";
        }
        Duration duration = Duration.between(start, end);
        if (duration.isZero() || duration.isNegative()) {
            return "Menos de un minuto";
        }
        long totalMinutes = duration.toMinutes();
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        List<String> parts = new ArrayList<>();
        if (hours > 0) {
            parts.add(hours + (hours == 1 ? " hora" : " horas"));
        }
        if (minutes > 0) {
            parts.add(minutes + " min");
        }
        if (parts.isEmpty()) {
            return "Menos de un minuto";
        }
        return String.join(" ", parts);
    }

    private String formatMaxDuration(Integer minutes) {
        if (minutes == null || minutes <= 0) {
            return "Sin límite definido";
        }
        int absMinutes = Math.abs(minutes);
        int hours = absMinutes / 60;
        int remaining = absMinutes % 60;
        if (hours > 0 && remaining > 0) {
            return String.format("%d %s %d min", hours, hours == 1 ? "hora" : "horas", remaining);
        }
        if (hours > 0) {
            return hours + (hours == 1 ? " hora" : " horas");
        }
        return remaining + " min";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return capitalize(DATE_TIME_FORMATTER.format(dateTime));
    }

    private String buildStatusSummary(Reservation reservation) {
        ReservationStatus status = reservation.getStatus();
        if (status == null) {
            return "La reserva aún no tiene un estado definido. Nuestro equipo confirmará los detalles muy pronto.";
        }
        return switch (status) {
            case PENDING -> "Tu solicitud está en revisión. Te notificaremos apenas el administrador valide la disponibilidad del espacio.";
            case CONFIRMED -> "La reserva está confirmada. Presentá el código QR al ingresar y llegá con 10 minutos de anticipación para evitar contratiempos.";
            case CANCELED -> "La reserva fue cancelada. Si necesitás reagendar, podés solicitar un nuevo espacio desde la plataforma.";
            case CHECKED_IN -> "El ingreso ya fue registrado en el sistema. Disfrutá del espacio y recordá dejarlo en óptimas condiciones.";
            case NO_SHOW -> "La reserva se marcó como inasistencia. Si fue un error, contactá a la oficina de reservas para regularizar la situación.";
            case COMPLETED -> "La reserva fue completada exitosamente. Gracias por usar nuestro sistema de reservas.";
            default -> "Estado de reserva desconocido. Por favor, contactá al equipo de soporte.";
        };
    }

    private List<String> buildNextSteps(Reservation reservation) {
        List<String> steps = new ArrayList<>();
        ReservationStatus status = reservation.getStatus();
        if (status == null) {
            steps.add("Revisá tu bandeja de entrada: te enviaremos una confirmación tan pronto validemos la disponibilidad.");
            return steps;
        }
        switch (status) {
            case PENDING -> {
                steps.add("Verificá que tus datos de contacto estén actualizados para recibir la aprobación sin retrasos.");
                if (reservation.getSpace() != null && Boolean.TRUE.equals(reservation.getSpace().getRequiresApproval())) {
                    steps.add("Recordá que este espacio requiere aprobación explícita del administrador municipal.");
                }
            }
            case CONFIRMED -> {
                steps.add("Guardá este correo: contiene toda la información necesaria para tu ingreso.");
                steps.add("Mostrá el código QR y un documento de identificación al llegar al espacio.");
            }
            case CHECKED_IN -> {
                steps.add("Si detectás algún incidente durante tu uso del espacio, reportalo inmediatamente al personal municipal.");
            }
            case NO_SHOW -> {
                steps.add("Contactá al equipo de reservas si necesitás reactivar tu acceso o gestionar una nueva fecha.");
            }
            case CANCELED -> {
                steps.add("Evaluá reagendar en otro horario disponible desde la plataforma de reservas.");
            }
            default -> {
                // Handle any unexpected status values
            }
        }
        steps.add("Recordá que las cancelaciones están sujetas a las restricciones temporales vigentes de la municipalidad.");
        return steps;
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
            case COMPLETED -> "Completada";
            default -> "Estado desconocido";
        };
    }

    private String translateSpaceType(SpaceType type) {
        if (type == null) {
            return "Sin clasificar";
        }
        return switch (type) {
            case SALA -> "Sala";
            case CANCHA -> "Cancha";
            case AUDITORIO -> "Auditorio";
            case GIMNASIO -> "Gimnasio";
            case PISCINA -> "Piscina";
            case PARQUE -> "Parque";
            case LABORATORIO -> "Laboratorio";
            case BIBLIOTECA -> "Biblioteca";
            case TEATRO -> "Teatro";
            default -> "Tipo desconocido";
        };
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private String sanitize(String value) {
        return normalizeSpacing(value);
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

    private String normalizeSpacing(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.strip();
        if (trimmed.isEmpty()) {
            return "";
        }
        String[] lines = trimmed.split("\\R", -1);
        return Arrays.stream(lines)
                .map(line -> line.replaceAll("[ \t]{2,}", " ").strip())
                .collect(Collectors.joining("\n"));
    }

    public static class MailSendingException extends RuntimeException {
        public MailSendingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}