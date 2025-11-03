package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailServiceImplementationBuildHtmlTest {

    private EmailServiceImplementation service;

    @BeforeEach
    void setup() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailConfig mailConfig = new MailConfig();
        finalprojectprogramming.project.services.qr.QRCodeService qr = mock(finalprojectprogramming.project.services.qr.QRCodeService.class);
        service = new EmailServiceImplementation(mailSender, mailConfig, qr);
    }

    @Test
    void buildHtml_covers_optional_sections_and_content() {
        // Build fully populated reservation with canceled status to cover conditional blocks
        User user = new User();
        user.setEmail("person@example.com");
        Space space = new Space();
        space.setName("Centro Cívico");
        space.setLocation("Av. Principal 123");
        space.setDescription("Espacio multiuso\nCon buena iluminación.");
        space.setCapacity(120);
        space.setType(SpaceType.AUDITORIO);
        space.setRequiresApproval(true);
        space.setMaxReservationDuration(150);

        Reservation r = new Reservation();
        r.setId(77L);
        r.setUser(user);
        r.setSpace(space);
        r.setStatus(ReservationStatus.CANCELED);
        r.setStartTime(LocalDateTime.now().plusDays(1));
        r.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        r.setCreatedAt(LocalDateTime.now().minusDays(1));
        r.setCheckinAt(LocalDateTime.now().minusHours(1));
        r.setQrCode("QR-XYZ");
        r.setNotes("Traer documento de identidad.\nLlegar 10 min antes.");
        r.setCancellationReason("Por mantenimiento");

    String html = ReflectionTestUtils.invokeMethod(
                service,
                "buildHtml",
                r,
                "Preheader",
                "Título",
                "Introducción",
        "#123456",
        "cid-77"
        );

    assertThat(html)
                .contains("Centro Cívico")
                .contains("Estado actual")
                .contains("Motivo de cancelación")
                .contains("Código QR")
                .contains("Próximos pasos")
        // La sección de política muestra "Requiere aprobación del administrador"
        .contains("Requiere aprobación")
        .contains("Política")
                .contains("Ubicación")
                .contains("Capacidad")
                .contains("Máx. por reserva")
                .contains("Notas adicionales")
                .contains("QR-XYZ")
                .contains("Por mantenimiento");
    }
}
