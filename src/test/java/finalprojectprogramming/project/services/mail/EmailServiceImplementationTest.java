package finalprojectprogramming.project.services.mail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;

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
        service = new EmailServiceImplementation(mailSender, mailConfig);

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
    void sendCustomEmail_sends_message_when_user_has_email() {
        Reservation r = buildReservation("p@q.com");

        service.sendCustomEmail(r, "Asunto", "Hola mundo");

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomEmail_skips_when_user_missing_email() {
        Reservation r = buildReservation(null);

        service.sendCustomEmail(r, "Asunto", "Hola mundo");

        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendCustomEmail_wraps_and_throws_when_mail_sender_fails() {
        Reservation r = buildReservation("p@q.com");
        doThrow(new MailException("down") {})
                .when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> service.sendCustomEmail(r, "Asunto", "Hola"))
                .isInstanceOf(EmailServiceImplementation.MailSendingException.class);
    }
}
