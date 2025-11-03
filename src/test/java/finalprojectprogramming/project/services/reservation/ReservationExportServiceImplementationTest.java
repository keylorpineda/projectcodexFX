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
