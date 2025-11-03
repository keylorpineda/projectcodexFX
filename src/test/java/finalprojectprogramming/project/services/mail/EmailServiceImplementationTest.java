package finalprojectprogramming.project.services.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

class EmailServiceImplementationTest {

    private JavaMailSender mailSender;
    private MailConfig mailConfig;
    private EmailServiceImplementation service;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        mailConfig = new MailConfig();
        mailConfig.setAddress("noreply@muni.test");
        mailConfig.setName("Municipalidad");
        finalprojectprogramming.project.services.qr.QRCodeService qr = Mockito.mock(finalprojectprogramming.project.services.qr.QRCodeService.class);
        try {
            Mockito.when(qr.generateQRCodeImage(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(new byte[]{1,2});
        } catch (Exception ignored) {}
        service = new EmailServiceImplementation(mailSender, mailConfig, qr);

        // Return a real MimeMessage so MimeMessageHelper can work normally
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);
    }

    private Reservation buildReservation(String email) {
        User user = new User();
        user.setEmail(email);
        Space space = new Space();
        space.setName("Gimnasio Municipal");
        Reservation r = new Reservation();
        r.setId(3L);
        r.setUser(user);
        r.setSpace(space);
        r.setStatus(ReservationStatus.CONFIRMED);
        return r;
    }

    @Test
    void sendWelcomeEmail_skips_when_user_null_or_no_email_and_sends_when_valid() {
        // No envía cuando user es null
        service.sendWelcomeEmail(null);
        verify(mailSender, never()).send(any(MimeMessage.class));

        // No envía cuando no hay email
        var u = new User();
        u.setName("Test");
        u.setEmail(null);
        service.sendWelcomeEmail(u);
        verify(mailSender, never()).send(any(MimeMessage.class));

        // Envía cuando el usuario es válido
        u.setEmail("citizen@example.com");
        service.sendWelcomeEmail(u);
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_validations_and_success() {
        // No envía cuando user null
        service.sendPasswordResetEmail(null, "token");
        verify(mailSender, never()).send(any(MimeMessage.class));

        // No envía cuando falta token
        var u = new User();
        u.setEmail("citizen@example.com");
        service.sendPasswordResetEmail(u, " ");
        verify(mailSender, never()).send(any(MimeMessage.class));

        // Envía correctamente cuando user y token son válidos
        service.sendPasswordResetEmail(u, "ABC123");
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void sendReservationConfirmed_sends_message_when_user_has_email() {
        Reservation r = buildReservation("p@q.com");

        service.sendReservationConfirmed(r);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendReservationConfirmed_skips_when_user_missing_email() {
        Reservation r = buildReservation(null);

        service.sendReservationConfirmed(r);

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendReservationConfirmed_wraps_and_throws_when_mail_sender_fails() {
        Reservation r = buildReservation("p@q.com");
        doThrow(new MailException("down") {})
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendReservationConfirmed(r))
                .isInstanceOf(EmailServiceImplementation.MailSendingException.class);
    }

    @Test
    void send_uses_defaults_when_mail_config_blank() {
        // Config vacío: debe usar email del usuario como fromAddress
    MailConfig blank = new MailConfig();
    finalprojectprogramming.project.services.qr.QRCodeService qr = Mockito.mock(finalprojectprogramming.project.services.qr.QRCodeService.class);
    EmailServiceImplementation svc = new EmailServiceImplementation(mailSender, blank, qr);
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        Reservation r = buildReservation("citizen@example.com");
        // No lanzará excepción; simplemente ejecuta ramas de fromAddress/name por defecto
    svc.sendReservationConfirmed(r);

        // Verifica que se intentó enviar un correo
        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void render_full_email_body_canceled_with_extras() {
        // Build a rich reservation to exercise optional sections and formatting branches
        Reservation r = buildReservation("citizen@example.com");
        r.setStatus(ReservationStatus.CANCELED);
        r.setAttendees(1);
        r.setNotes("  Nota   con  espacios   y\n\nlineas   ");
        r.setQrCode("QR-123");
        r.setCancellationReason("  Motivo <p>HTML</p>  ");
        r.setStartTime(java.time.LocalDateTime.now());
        r.setEndTime(r.getStartTime()); // zero duration path
        r.setCreatedAt(java.time.LocalDateTime.now());
        r.setCheckinAt(java.time.LocalDateTime.now());

        Space space = r.getSpace();
        space.setType(finalprojectprogramming.project.models.enums.SpaceType.AUDITORIO);
        space.setCapacity(1);
        space.setRequiresApproval(true);
        space.setMaxReservationDuration(185); // 3 horas 5 min
        space.setDescription("  Descripción con   <b>HTML</b>  ");
        space.setLocation("  Ubicación   con   espacios  ");

        // Exercise the send path for canceled emails
        service.sendReservationCanceled(r);

        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }

    @Test
    void send_falls_back_to_raw_from_when_personal_encoding_unsupported() throws Exception {
        // Mock construction of MimeMessageHelper to throw UnsupportedEncodingException on setFrom(address, name)
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, context) -> {
                    // setFrom(address, personal) throws -> triggers fallback
                    doThrow(new UnsupportedEncodingException("bad-personal")).when(mock)
                            .setFrom(anyString(), anyString());
                    // single-arg setFrom should succeed
                    doNothing().when(mock).setFrom(anyString());
                    // Allow other interactions
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doNothing().when(mock).setText(anyString(), eq(true));
                })) {

            Reservation r = buildReservation("citizen@example.com");

            // Execute any send path that uses helper.setFrom(address, name)
            service.sendReservationConfirmed(r);

            // Verify the two-arg setFrom was attempted then fallback to single-arg was used
            MimeMessageHelper helper = mocked.constructed().get(0);
            verify(helper, times(1)).setFrom(eq(mailConfig.getAddress()), eq(mailConfig.getName()));
            verify(helper, times(1)).setFrom(eq(mailConfig.getAddress()));
            verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
        }
    }

    @Test
    void send_skips_qr_embedding_when_qr_generation_fails() throws Exception {
        // Arrange a service with a QR service that throws, so QR section is skipped
        finalprojectprogramming.project.services.qr.QRCodeService badQr = Mockito.mock(finalprojectprogramming.project.services.qr.QRCodeService.class);
        Mockito.when(badQr.generateQRCodeImage(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                .thenThrow(new java.io.IOException("qr-fail"));
        EmailServiceImplementation svc = new EmailServiceImplementation(mailSender, mailConfig, badQr);
        MimeMessage mime = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mime);

        // Capture helper interactions to ensure addInline (QR embed) is never called
        try (MockedConstruction<MimeMessageHelper> mocked = Mockito.mockConstruction(
                MimeMessageHelper.class,
                (mock, context) -> {
                    // Allow all the usual interactions
                    doNothing().when(mock).setTo(anyString());
                    doNothing().when(mock).setSubject(anyString());
                    doNothing().when(mock).setFrom(anyString(), anyString());
                    doNothing().when(mock).setText(anyString(), eq(true));
                    // addInline should not be called because QR generation fails
                    doNothing().when(mock).addInline(anyString(), any(jakarta.activation.DataSource.class));
                })) {

            Reservation r = buildReservation("citizen@example.com");
            r.setQrCode("QR-FAIL");

            // Act
            svc.sendReservationConfirmed(r);

            // Assert: mail was sent but no inline resource was added
            MimeMessageHelper helper = mocked.constructed().get(0);
            verify(helper, never()).addInline(anyString(), any(jakarta.activation.DataSource.class));
            verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
        }
    }

    @Test
    void sendReservationPending_handles_null_space_and_no_qr() {
        // Exercise buildHtml branch when reservation has no space and no QR code
        Reservation r = buildReservation("citizen@example.com");
        r.setSpace(null);
        r.setQrCode(null);

        service.sendReservationPending(r);

        verify(mailSender, atLeastOnce()).send(any(MimeMessage.class));
    }
}
