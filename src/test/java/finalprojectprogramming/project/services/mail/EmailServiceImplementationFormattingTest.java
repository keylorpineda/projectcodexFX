package finalprojectprogramming.project.services.mail;

import finalprojectprogramming.project.configs.MailConfig;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EmailServiceImplementationFormattingTest {

    private EmailServiceImplementation service;

    @BeforeEach
    void setup() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MailConfig mailConfig = new MailConfig();
        service = new EmailServiceImplementation(mailSender, mailConfig);
    }

    @Test
    void formatDate_handles_null_and_formats_properly() {
        String nullFormatted = ReflectionTestUtils.invokeMethod(service, "formatDate", new Object[]{null});
        assertThat(nullFormatted).isEqualTo("Por definir");

        LocalDateTime dt = LocalDateTime.of(2025, 1, 5, 10, 30);
        String formatted = ReflectionTestUtils.invokeMethod(service, "formatDate", dt);
        assertThat(formatted).contains("de");
    }

    @Test
    void formatSchedule_handles_nulls_and_range() {
        String missing = ReflectionTestUtils.invokeMethod(service, "formatSchedule", new Object[]{null, null});
        assertThat(missing).isEqualTo("Horario pendiente");

        LocalDateTime start = LocalDateTime.of(2025, 1, 5, 8, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 5, 9, 30);
        String range = ReflectionTestUtils.invokeMethod(service, "formatSchedule", start, end);
        assertThat(range).contains(" - ");
    }

    @Test
    void translateStatus_covers_all_statuses_and_null() {
        String none = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", new Object[]{null});
        assertThat(none).isEqualTo("Sin estado");

        String s1 = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", ReservationStatus.PENDING);
        assertThat(s1).isEqualTo("Pendiente de aprobación");
        String s2 = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", ReservationStatus.CONFIRMED);
        assertThat(s2).isEqualTo("Confirmada");
        String s3 = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", ReservationStatus.CANCELED);
        assertThat(s3).isEqualTo("Cancelada");
        String s4 = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", ReservationStatus.CHECKED_IN);
        assertThat(s4).isEqualTo("Con registro de ingreso");
        String s5 = (String) ReflectionTestUtils.invokeMethod(service, "translateStatus", ReservationStatus.NO_SHOW);
        assertThat(s5).isEqualTo("Marcada como inasistencia");
    }

    @Test
    void sanitize_and_escape_and_capitalize_branches() {
        String ns = (String) ReflectionTestUtils.invokeMethod(service, "nullSafe", new Object[]{null});
        assertThat(ns).isEqualTo("");
        String san = (String) ReflectionTestUtils.invokeMethod(service, "sanitize", new Object[]{null});
        assertThat(san).isEqualTo("");

        String html = "<b>Hola</b> & adiós";
        String escaped = ReflectionTestUtils.invokeMethod(service, "escape", html);
        assertThat(escaped).contains("&lt;b&gt;");

        String c1 = (String) ReflectionTestUtils.invokeMethod(service, "capitalize", new Object[]{""});
        assertThat(c1).isEqualTo("");
        String c2 = (String) ReflectionTestUtils.invokeMethod(service, "capitalize", "lunes");
        assertThat(c2).isEqualTo("Lunes");
    }

    @Test
    void formatReservationDuration_and_formatMaxDuration_cover_branches() {
        // formatReservationDuration
        String undef = ReflectionTestUtils.invokeMethod(service, "formatReservationDuration", new Object[]{null, null});
        assertThat(undef).isEqualTo("Por definir");

        LocalDateTime t = LocalDateTime.of(2025, 1, 5, 10, 0);
        String lessThanMinute = ReflectionTestUtils.invokeMethod(service, "formatReservationDuration", t, t);
        assertThat(lessThanMinute).isEqualTo("Menos de un minuto");

        String minutesOnly = ReflectionTestUtils.invokeMethod(service, "formatReservationDuration", t, t.plusMinutes(15));
        assertThat(minutesOnly).isEqualTo("15 min");

        String hoursOnly = ReflectionTestUtils.invokeMethod(service, "formatReservationDuration", t, t.plusHours(2));
        assertThat(hoursOnly).contains("2 horas");

        String hoursAndMinutes = ReflectionTestUtils.invokeMethod(service, "formatReservationDuration", t, t.plusHours(1).plusMinutes(30));
        assertThat(hoursAndMinutes).isEqualTo("1 hora 30 min");

        // formatMaxDuration
        String noLimit = ReflectionTestUtils.invokeMethod(service, "formatMaxDuration", new Object[]{null});
        assertThat(noLimit).isEqualTo("Sin límite definido");

        String minutes = ReflectionTestUtils.invokeMethod(service, "formatMaxDuration", 45);
        assertThat(minutes).isEqualTo("45 min");

        String hours = ReflectionTestUtils.invokeMethod(service, "formatMaxDuration", 120);
        assertThat(hours).isEqualTo("2 horas");

    String hoursMinutes = ReflectionTestUtils.invokeMethod(service, "formatMaxDuration", 90);
    assertThat(hoursMinutes).isEqualTo("1 hora 30 min");
    }

    @Test
    void translateSpaceType_and_buildNextSteps_cover_all() {
        // translateSpaceType
        String none = ReflectionTestUtils.invokeMethod(service, "translateSpaceType", new Object[]{null});
        assertThat(none).isEqualTo("Sin clasificar");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "translateSpaceType", finalprojectprogramming.project.models.enums.SpaceType.SALA))
        .isEqualTo("Sala");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "translateSpaceType", finalprojectprogramming.project.models.enums.SpaceType.CANCHA))
        .isEqualTo("Cancha");
    assertThat((String) ReflectionTestUtils.invokeMethod(service, "translateSpaceType", finalprojectprogramming.project.models.enums.SpaceType.AUDITORIO))
        .isEqualTo("Auditorio");

        // buildNextSteps for different statuses
        finalprojectprogramming.project.models.Reservation r = new finalprojectprogramming.project.models.Reservation();
        // null status
        java.util.List<String> stepsNull = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsNull).isNotEmpty();

        r.setStatus(ReservationStatus.PENDING);
        finalprojectprogramming.project.models.Space s = new finalprojectprogramming.project.models.Space();
        s.setRequiresApproval(true);
        r.setSpace(s);
        java.util.List<String> stepsPending = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsPending).anyMatch(str -> str.contains("requiere aprobación"));

        r.setStatus(ReservationStatus.CONFIRMED);
        java.util.List<String> stepsConfirmed = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsConfirmed).anyMatch(str -> str.contains("código QR"));

        r.setStatus(ReservationStatus.CHECKED_IN);
        java.util.List<String> stepsChecked = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsChecked).isNotEmpty();

        r.setStatus(ReservationStatus.NO_SHOW);
        java.util.List<String> stepsNoShow = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsNoShow).isNotEmpty();

        r.setStatus(ReservationStatus.CANCELED);
        java.util.List<String> stepsCanceled = ReflectionTestUtils.invokeMethod(service, "buildNextSteps", r);
        assertThat(stepsCanceled).isNotEmpty();
    }
}
