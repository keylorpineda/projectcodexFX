package finalprojectprogramming.project.services.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationExportServiceImplementationTest {

    @Mock
    private ReservationRepository reservationRepository;

    private ReservationExportServiceImplementation service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new ReservationExportServiceImplementation(reservationRepository);
    }

    @Test
    void exportAllReservations_generatesWorkbookWithData() throws IOException {
        Reservation reservation = buildReservation(1L, 10L, 100L, ReservationStatus.CONFIRMED);
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        byte[] bytes = service.exportAllReservations();

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(1d);
            assertThat(sheet.getRow(1).getCell(3).getStringCellValue()).isEqualTo("Space 100");
        }
    }

    @Test
    void exportReservationsForUser_filtersByUser() throws IOException {
        Reservation reservationUser = buildReservation(2L, 20L, 101L, ReservationStatus.CHECKED_IN);
        Reservation reservationOther = buildReservation(3L, 21L, 102L, ReservationStatus.CANCELED);
        when(reservationRepository.findAll()).thenReturn(List.of(reservationUser, reservationOther));

        byte[] bytes = service.exportReservationsForUser(20L);
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getNumericCellValue()).isEqualTo(2d);
        }
    }

    @Test
    void export_handles_nulls_and_unknown_status() throws IOException {
        // Reserva con muchos nulos para cubrir celdas en blanco y defaultString, y estado desconocido (COMPLETED)
        Reservation r = Reservation.builder()
                .id(5L)
                .user(null)
                .space(null)
                .status(finalprojectprogramming.project.models.enums.ReservationStatus.COMPLETED)
                .startTime(null)
                .endTime(null)
                .canceledAt(java.time.LocalDateTime.now())
                .cancellationReason("Texto largo\ncon salto")
                .attendeeRecords(java.util.Collections.emptyList())
                .createdAt(null)
                .updatedAt(null)
                .build();
        when(reservationRepository.findAll()).thenReturn(List.of(r));

        byte[] bytes = service.exportAllReservations();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            // fila de datos
            var row = sheet.getRow(1);
            // Usuario/Correo/Espacio vacíos
            org.assertj.core.api.Assertions.assertThat(row.getCell(1).getStringCellValue()).isEqualTo("");
            org.assertj.core.api.Assertions.assertThat(row.getCell(2).getStringCellValue()).isEqualTo("");
            org.assertj.core.api.Assertions.assertThat(row.getCell(3).getStringCellValue()).isEqualTo("");
            // Estado desconocido por default (COMPLETED no está en switch)
            org.assertj.core.api.Assertions.assertThat(row.getCell(4).getStringCellValue()).contains("Desconocido");
            // Fecha/Hora inicio/Hora fin en blanco
            org.assertj.core.api.Assertions.assertThat(row.getCell(5)).isNotNull();
            org.assertj.core.api.Assertions.assertThat(row.getCell(6)).isNotNull();
            org.assertj.core.api.Assertions.assertThat(row.getCell(7)).isNotNull();
            // Cancelada = "Sí"
            org.assertj.core.api.Assertions.assertThat(row.getCell(9).getStringCellValue()).isEqualTo("Sí");
        }
    }

    @Test
    void exportAllReservations_covers_status_mapping_and_extra_fields() throws IOException {
        // Cubre traducciones de estado PENDIENTE/CANCELADA/INASISTENCIA y campos approvedBy, attendees y reason nulo
        User approver = User.builder().id(999L).name("Admin Aprobador").build();

        Reservation pending = buildReservation(10L, 110L, 210L, ReservationStatus.PENDING);
        pending.setApprovedBy(approver); // cubrir columna "Aprobado por"
        pending.setCancellationReason(null); // defaultString -> ""
    pending.setAttendeeRecords(java.util.List.of(
        finalprojectprogramming.project.models.ReservationAttendee.builder()
            .idNumber("1").firstName("A").lastName("B").checkInAt(java.time.LocalDateTime.now()).build(),
        finalprojectprogramming.project.models.ReservationAttendee.builder()
            .idNumber("2").firstName("C").lastName("D").checkInAt(java.time.LocalDateTime.now()).build()
    )); // size = 2

        Reservation canceled = buildReservation(11L, 111L, 211L, ReservationStatus.CANCELED);
        canceled.setCanceledAt(java.time.LocalDateTime.now()); // columna Cancelada = "Sí"

        Reservation noShow = buildReservation(12L, 112L, 212L, ReservationStatus.NO_SHOW);
        // dejar start/end no nulos para cubrir ramas con formato de hora/fecha

        when(reservationRepository.findAll()).thenReturn(List.of(pending, canceled, noShow));

        byte[] bytes = service.exportAllReservations();
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            // 3 filas de datos + cabecera
            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(4);

            // Fila 1: pending
            var row1 = sheet.getRow(1);
            assertThat(row1.getCell(4).getStringCellValue()).isEqualTo("Pendiente");
            assertThat(row1.getCell(8).getStringCellValue()).isEqualTo("Admin Aprobador");
            assertThat(row1.getCell(10).getStringCellValue()).isEqualTo(""); // motivo nulo -> ""
            assertThat(row1.getCell(11).getNumericCellValue()).isEqualTo(2d); // attendees size

            // Fila 2: canceled
            var row2 = sheet.getRow(2);
            assertThat(row2.getCell(4).getStringCellValue()).isEqualTo("Cancelada");
            assertThat(row2.getCell(9).getStringCellValue()).isEqualTo("Sí");

            // Fila 3: no show
            var row3 = sheet.getRow(3);
            assertThat(row3.getCell(4).getStringCellValue()).isEqualTo("Inasistencia");
            // fecha/hora de inicio y fin no vacías (formateadas)
            assertThat(row3.getCell(5)).isNotNull();
            assertThat(row3.getCell(6)).isNotNull();
            assertThat(row3.getCell(7)).isNotNull();
        }
    }

    private Reservation buildReservation(Long reservationId, Long userId, Long spaceId, ReservationStatus status) {
        User user = User.builder().id(userId).name("User " + userId).email("user" + userId + "@mail").build();
        Space space = Space.builder().id(spaceId).name("Space " + spaceId).build();
        return Reservation.builder()
                .id(reservationId)
                .user(user)
                .space(space)
                .status(status)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .qrCode("QR" + reservationId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
