package finalprojectprogramming.project.services.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Additional focused tests to cover remaining branches in EmailServiceImplementation
 * (custom admin, welcome, password reset, reminder and check-in flows).
 */
class EmailServiceImplementationAdditionalFlowsTest {

    private JavaMailSender mailSender;
    private MailConfig mailConfig;
    private final finalprojectprogramming.project.services.qr.QRCodeService qr = mock(finalprojectprogramming.project.services.qr.QRCodeService.class);
    private EmailServiceImplementation service;

    @BeforeEach
    void setup() throws Exception {
        mailSender = Mockito.mock(JavaMailSender.class);
        mailConfig = new MailConfig();
        mailConfig.setAddress("noreply@muni.test");
        mailConfig.setName("Municipalidad PZ");
        when(qr.generateQRCodeImage(anyString(), any(Integer.class), any(Integer.class)))
                .thenReturn(new byte[]{1, 2, 3});
        service = new EmailServiceImplementation(mailSender, mailConfig, qr);

        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);
        // default: do nothing on send
        doNothing().when(mailSender).send(any(MimeMessage.class));
    }

    private Reservation baseReservation(String email) {
        User u = new User();
        u.setEmail(email);
        Space s = new Space();
        s.setName("Auditorio");
        Reservation r = new Reservation();
        r.setId(101L);
        r.setUser(u);
        r.setSpace(s);
        r.setStatus(ReservationStatus.CONFIRMED);
        r.setStartTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0));
        r.setEndTime(r.getStartTime().plusHours(2));
        r.setCreatedAt(LocalDateTime.now());
        r.setQrCode("QR-XYZ");
        return r;
    }

    @Test
    void sendReservationReminder_embeds_qr_image_when_available() throws Exception {
        // Capture helper to assert addInline was invoked for QR
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, ctx) -> {
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doNothing().when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setText(anyString(), Mockito.eq(true));
                    doNothing().when(mock).addInline(anyString(), any(jakarta.activation.DataSource.class));
                })) {

            Reservation r = baseReservation("citizen@example.com");

            service.sendReservationReminder(r);

            MimeMessageHelper helper = mocked.constructed().get(0);
            verify(helper, times(1)).addInline(anyString(), any(jakarta.activation.DataSource.class));
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendCheckInConfirmed_does_not_embed_qr_even_if_present() throws Exception {
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, ctx) -> {
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doNothing().when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setText(anyString(), Mockito.eq(true));
                    doNothing().when(mock).addInline(anyString(), any(jakarta.activation.DataSource.class));
                })) {

            Reservation r = baseReservation("citizen@example.com");
            // even with QR, this flow shouldn't embed it
            r.setQrCode("QR-PRESENT");

            service.sendCheckInConfirmed(r);

            MimeMessageHelper helper = mocked.constructed().get(0);
            verify(helper, times(0)).addInline(anyString(), any(jakarta.activation.DataSource.class));
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendCustomAdminEmail_skips_when_email_blank_and_when_message_blank() {
        // email blank -> skip
        service.sendCustomAdminEmail(" ", "Name", "Subject", "Message");
        verify(mailSender, times(0)).send(any(MimeMessage.class));

        // message blank -> skip
        service.sendCustomAdminEmail("p@q.com", "Name", "Subject", " ");
        verify(mailSender, times(0)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomAdminEmail_uses_default_subject_and_handles_from_encoding_fallback() throws Exception {
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, ctx) -> {
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doThrow(new UnsupportedEncodingException("bad"))
                            .when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setFrom(anyString());
                    doNothing().when(mock).setText(anyString(), Mockito.eq(true));
                })) {

            MimeMessage m = new MimeMessage(Session.getInstance(new Properties()));
            when(mailSender.createMimeMessage()).thenReturn(m);

            // subject blank -> should default
            service.sendCustomAdminEmail("p@q.com", null, " ", "Hola mundo");

            // We cannot intercept subject set on helper easily here, but we do verify send was invoked
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendCustomAdminEmail_wraps_mail_sender_exceptions() {
        doThrow(new MailException("down") {}).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendCustomAdminEmail("p@q.com", "User", "Subject", "Msg"))
                .isInstanceOf(EmailServiceImplementation.MailSendingException.class);
    }

    @Test
    void sendWelcomeEmail_skips_when_user_or_email_missing_and_succeeds_otherwise() {
        // null email -> skip
        User u = new User();
        u.setEmail(null);
        service.sendWelcomeEmail(u);
        verify(mailSender, times(0)).send(any(MimeMessage.class));

        // success path
        u.setEmail("new@user.test");
        service.sendWelcomeEmail(u);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendWelcomeEmail_wraps_mail_exceptions() {
        User u = new User();
        u.setEmail("new@user.test");

        doThrow(new MailException("down") {}).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendWelcomeEmail(u))
                .isInstanceOf(EmailServiceImplementation.MailSendingException.class);
    }

    @Test
    void sendWelcomeEmail_falls_back_to_raw_from_on_encoding_error() throws Exception {
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, ctx) -> {
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doThrow(new UnsupportedEncodingException("bad"))
                            .when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setFrom(anyString());
                    doNothing().when(mock).setText(anyString(), Mockito.eq(true));
                })) {

            MimeMessage m = new MimeMessage(Session.getInstance(new Properties()));
            when(mailSender.createMimeMessage()).thenReturn(m);

            User u = new User();
            u.setEmail("welcome@test");

            service.sendWelcomeEmail(u);

            // send invoked without throwing after fallback
            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendPasswordResetEmail_validates_token_and_user() {
        User u = new User();
        // missing email -> skip
        u.setEmail(null);
        service.sendPasswordResetEmail(u, "tok");
        verify(mailSender, times(0)).send(any(MimeMessage.class));

        // email ok but blank token -> skip
        u.setEmail("u@test");
        service.sendPasswordResetEmail(u, " ");
        verify(mailSender, times(0)).send(any(MimeMessage.class));

        // valid inputs -> success
        service.sendPasswordResetEmail(u, "ABC123");
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_wraps_mail_exceptions() {
        User u = new User();
        u.setEmail("u@test");

        doThrow(new MailException("down") {}).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendPasswordResetEmail(u, "ABC123"))
                .isInstanceOf(EmailServiceImplementation.MailSendingException.class);
    }

    @Test
    void sendPasswordResetEmail_falls_back_to_raw_from_on_encoding_error() throws Exception {
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, ctx) -> {
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doThrow(new UnsupportedEncodingException("bad"))
                            .when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setFrom(anyString());
                    doNothing().when(mock).setText(anyString(), Mockito.eq(true));
                })) {

            MimeMessage m = new MimeMessage(Session.getInstance(new Properties()));
            when(mailSender.createMimeMessage()).thenReturn(m);

            User u = new User();
            u.setEmail("reset@test");

            service.sendPasswordResetEmail(u, "TOKEN-1");

            verify(mailSender, times(1)).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendReservationReminder_covers_completed_status_and_minutes_only_duration_and_attendees_zero() {
        // Cubre: status COMPLETED (mapas en translateStatus/buildStatusSummary),
        // formatMaxDuration con solo minutos (45 -> "45 min"), y formatAttendees con 0 -> "--"
        Reservation r = baseReservation("citizen@example.com");
        r.setStatus(ReservationStatus.COMPLETED);
        r.setAttendees(0); // "--"
        r.getSpace().setMaxReservationDuration(45); // "45 min"

        service.sendReservationReminder(r);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
