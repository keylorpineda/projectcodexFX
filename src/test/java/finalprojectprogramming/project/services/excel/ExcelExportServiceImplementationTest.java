package finalprojectprogramming.project.services.excel;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class ExcelExportServiceImplementationTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    SpaceRepository spaceRepository;

    @InjectMocks
    ExcelExportServiceImplementation service;

    @Test
    void exportUserReservations_filters_by_user_and_writes_expected_columns() throws Exception {
        User user1 = userBuilder().withId(1L).withName("Ana").build();
        User user2 = userBuilder().withId(2L).withName("Bob").build();
        Space spaceA = spaceBuilder().withId(10L).withActive(true).build();

        Reservation r1 = reservationBuilder()
                .withId(101L)
                .withUser(user1)
                .withSpace(spaceA)
                .withStart(LocalDateTime.of(2025, Month.JANUARY, 1, 8, 0))
                .withEnd(LocalDateTime.of(2025, Month.JANUARY, 1, 9, 0))
                .withStatus(ReservationStatus.CONFIRMED)
                .withQrCode("QR-101")
                .build();
    // Necesario para columnas de fecha de creación en export
    r1.setCreatedAt(LocalDateTime.now());
        Reservation r2 = reservationBuilder()
                .withId(102L)
                .withUser(user2) // not included
                .withSpace(spaceA)
                .withStatus(ReservationStatus.PENDING)
                .build();

        when(reservationRepository.findAll()).thenReturn(List.of(r1, r2));

        var out = service.exportUserReservations(1L);
        byte[] bytes = out.toByteArray();
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheetAt(0);
            // headers
            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("ID");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Espacio");

            // only one row for user 1
            Row row1 = sheet.getRow(1);
            assertThat(row1.getCell(0).getNumericCellValue()).isEqualTo(101d);
            assertThat(row1.getCell(1).getStringCellValue()).isEqualTo(spaceA.getName());
            assertThat(row1.getCell(4).getStringCellValue()).isEqualTo("CONFIRMED");
            assertThat(row1.getCell(5).getStringCellValue()).isEqualTo("QR-101");
            assertThat(sheet.getRow(2)).isNull();
        }
    }

    @Test
    void exportAllReservations_includes_user_email_and_checkin_flag() throws Exception {
        User u = userBuilder().withId(7L).withName("Carla").withEmail("c@example.com").build();
        Space s = spaceBuilder().withId(77L).build();
        Reservation r = reservationBuilder()
                .withId(501L)
                .withUser(u)
                .withSpace(s)
                .withStatus(ReservationStatus.CONFIRMED)
                .withQrCode(null) // Will render N/A
                .withCheckInAt(LocalDateTime.now())
                .build();
    // Necesario para columna "Creado"
    r.setCreatedAt(LocalDateTime.now());
        when(reservationRepository.findAll()).thenReturn(List.of(r));

        var out = service.exportAllReservations();
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(1);
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Carla");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("c@example.com");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo(s.getName());
            assertThat(row.getCell(6).getStringCellValue()).isEqualTo("CONFIRMED");
            // QR N/A
            assertThat(row.getCell(7).getStringCellValue()).isEqualTo("N/A");
            // Check-in flag "Sí"
            assertThat(row.getCell(8).getStringCellValue()).isEqualTo("Sí");
        }
    }

    @Test
    void exportSpaceStatistics_calculates_counts_and_occupancy() throws Exception {
        Space a = spaceBuilder().withId(1L).build();
        Space b = spaceBuilder().withId(2L).build();
        when(spaceRepository.findAll()).thenReturn(List.of(a, b));

        Reservation a1 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.CONFIRMED).build();
        Reservation a2 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.PENDING).build();
        Reservation a3 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.CANCELED).build();
    // En este export, "Aprobadas" cuenta solo CONFIRMED, así que usamos CONFIRMED para espacio B
    Reservation b1 = reservationBuilder().withSpace(b).withStatus(ReservationStatus.CONFIRMED).build();
        when(reservationRepository.findAll()).thenReturn(List.of(a1, a2, a3, b1));

        var out = service.exportSpaceStatistics();
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            // Space A row
            Row rowA = sheet.getRow(1);
            assertThat(rowA.getCell(0).getStringCellValue()).isEqualTo(a.getName());
            // total reservations for space A: 3
            assertThat(rowA.getCell(3).getNumericCellValue()).isEqualTo(3d);
            // confirmed (CONFIRMED only): 1
            assertThat(rowA.getCell(4).getNumericCellValue()).isEqualTo(1d);
            // pending: 1, canceled: 1
            assertThat(rowA.getCell(5).getNumericCellValue()).isEqualTo(1d);
            assertThat(rowA.getCell(6).getNumericCellValue()).isEqualTo(1d);
            // occupancy rate = confirmed / total * 100 = 33.33% (tolerar coma/punto)
            assertThat(rowA.getCell(7).getStringCellValue().replace(',', '.')).contains("33.33%");

            // Space B row
            Row rowB = sheet.getRow(2);
            assertThat(rowB.getCell(3).getNumericCellValue()).isEqualTo(1d);
            assertThat(rowB.getCell(4).getNumericCellValue()).isEqualTo(1d);
            assertThat(rowB.getCell(5).getNumericCellValue()).isEqualTo(0d);
            assertThat(rowB.getCell(6).getNumericCellValue()).isEqualTo(0d);
            // Tolerar formato local (coma o punto decimal)
            assertThat(rowB.getCell(7).getStringCellValue().replace(',', '.')).isEqualTo("100.00%");
        }
    }

    @Test
    void exportSpaceStatistics_handles_no_reservations() throws Exception {
        Space a = spaceBuilder().withId(1L).build();
        when(spaceRepository.findAll()).thenReturn(List.of(a));
        when(reservationRepository.findAll()).thenReturn(List.of());

        var out = service.exportSpaceStatistics();
        try (var wb = new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()))) {
            Sheet sheet = wb.getSheetAt(0);
            // one data row should still exist with zeros
            Row row = sheet.getRow(1);
            assertThat(row.getCell(3).getNumericCellValue()).isEqualTo(0d);
            assertThat(row.getCell(4).getNumericCellValue()).isEqualTo(0d);
            assertThat(row.getCell(5).getNumericCellValue()).isEqualTo(0d);
            assertThat(row.getCell(6).getNumericCellValue()).isEqualTo(0d);
            // Tolerar formato local (coma o punto decimal)
            assertThat(row.getCell(7).getStringCellValue().replace(',', '.')).isEqualTo("0.00%");
        }
    }
}
