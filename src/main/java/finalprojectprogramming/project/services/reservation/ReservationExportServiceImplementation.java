package finalprojectprogramming.project.services.reservation;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReservationExportServiceImplementation implements ReservationExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final ReservationRepository reservationRepository;

    public ReservationExportServiceImplementation(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public byte[] exportAllReservations() {
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .sorted(Comparator.comparing(Reservation::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return buildWorkbook(reservations);
    }

    @Override
    public byte[] exportReservationsForUser(Long userId) {
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getDeletedAt() == null)
                .filter(reservation -> reservation.getUser() != null
                        && Objects.equals(reservation.getUser().getId(), userId))
                .sorted(Comparator.comparing(Reservation::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return buildWorkbook(reservations);
    }

    private byte[] buildWorkbook(List<Reservation> reservations) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Reservas");
            sheet.setDefaultColumnWidth(20);

            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setFont(headerFont);

            String[] headers = new String[] {
                    "ID",
                    "Usuario",
                    "Correo",
                    "Espacio",
                    "Estado",
                    "Fecha",
                    "Hora inicio",
                    "Hora fin",
                    "Aprobado por",
                    "Cancelada",
                    "Motivo cancelación",
                    "Asistentes registrados",
                    "Creada",
                    "Actualizada"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            int rowIndex = 1;
            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowIndex++);
                fillRow(row, reservation, wrapStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate Excel report", exception);
        }
    }

    private void fillRow(Row row, Reservation reservation, CellStyle wrapStyle) {
        User user = reservation.getUser();
        Space space = reservation.getSpace();
        ReservationStatus status = reservation.getStatus();

        int columnIndex = 0;
        row.createCell(columnIndex++).setCellValue(reservation.getId() != null ? reservation.getId() : 0);
        row.createCell(columnIndex++).setCellValue(user != null ? defaultString(user.getName()) : "");
        row.createCell(columnIndex++).setCellValue(user != null ? defaultString(user.getEmail()) : "");
        row.createCell(columnIndex++).setCellValue(space != null ? defaultString(space.getName()) : "");
        row.createCell(columnIndex++).setCellValue(status != null ? translateStatus(status) : "");

        if (reservation.getStartTime() != null) {
            row.createCell(columnIndex).setCellValue(DATE_FORMAT.format(reservation.getStartTime()));
        } else {
            row.createCell(columnIndex).setBlank();
        }
        columnIndex++;

        if (reservation.getStartTime() != null) {
            row.createCell(columnIndex).setCellValue(TIME_FORMAT.format(reservation.getStartTime()));
        } else {
            row.createCell(columnIndex).setBlank();
        }
        columnIndex++;

        if (reservation.getEndTime() != null) {
            row.createCell(columnIndex).setCellValue(TIME_FORMAT.format(reservation.getEndTime()));
        } else {
            row.createCell(columnIndex).setBlank();
        }
        columnIndex++;

        row.createCell(columnIndex++).setCellValue(reservation.getApprovedBy() != null
                ? defaultString(reservation.getApprovedBy().getName())
                : "");

        row.createCell(columnIndex++).setCellValue(reservation.getCanceledAt() != null ? "Sí" : "No");

        Cell reasonCell = row.createCell(columnIndex++);
        reasonCell.setCellValue(defaultString(reservation.getCancellationReason()));
        reasonCell.setCellStyle(wrapStyle);

        int attendeesCount = reservation.getAttendeeRecords() != null ? reservation.getAttendeeRecords().size() : 0;
        row.createCell(columnIndex++).setCellValue(attendeesCount);

        if (reservation.getCreatedAt() != null) {
            row.createCell(columnIndex).setCellValue(reservation.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            row.createCell(columnIndex).setBlank();
        }
        columnIndex++;

        if (reservation.getUpdatedAt() != null) {
            row.createCell(columnIndex).setCellValue(reservation.getUpdatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        } else {
            row.createCell(columnIndex).setBlank();
        }
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private String translateStatus(ReservationStatus status) {
        return switch (status) {
            case PENDING -> "Pendiente";
            case CONFIRMED -> "Confirmada";
            case CANCELED -> "Cancelada";
            case CHECKED_IN -> "En sitio";
            case NO_SHOW -> "Inasistencia";
            default -> "Desconocido";
        };
    }
}
