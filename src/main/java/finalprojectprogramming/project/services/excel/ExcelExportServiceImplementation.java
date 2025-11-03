package finalprojectprogramming.project.services.excel;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExcelExportServiceImplementation implements ExcelExportService {

    private final ReservationRepository reservationRepository;
    private final SpaceRepository spaceRepository;

    @Autowired
    public ExcelExportServiceImplementation(
            ReservationRepository reservationRepository,
            SpaceRepository spaceRepository) {
        this.reservationRepository = reservationRepository;
        this.spaceRepository = spaceRepository;
    }

    @Override
    public ByteArrayOutputStream exportUserReservations(Long userId) throws IOException {
        List<Reservation> reservations = reservationRepository.findAll().stream()
                .filter(r -> r.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Mis Reservaciones");
            
            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Espacio", "Fecha Inicio", "Fecha Fin", "Estado", "QR Code", "Check-In", "Creado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;
            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(reservation.getId());
                row.createCell(1).setCellValue(reservation.getSpace().getName());
                
                Cell startCell = row.createCell(2);
                startCell.setCellValue(reservation.getStartTime().format(formatter));
                startCell.setCellStyle(dateStyle);
                
                Cell endCell = row.createCell(3);
                endCell.setCellValue(reservation.getEndTime().format(formatter));
                endCell.setCellStyle(dateStyle);
                
                row.createCell(4).setCellValue(reservation.getStatus().name());
                row.createCell(5).setCellValue(reservation.getQrCode() != null ? reservation.getQrCode() : "N/A");
                row.createCell(6).setCellValue(reservation.getCheckinAt() != null ? "Sí" : "No");
                
                Cell createdCell = row.createCell(7);
                createdCell.setCellValue(reservation.getCreatedAt().format(formatter));
                createdCell.setCellStyle(dateStyle);
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        }
    }

    @Override
    public ByteArrayOutputStream exportAllReservations() throws IOException {
        List<Reservation> reservations = reservationRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Todas las Reservaciones");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            
            // Cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Usuario", "Email", "Espacio", "Fecha Inicio", "Fecha Fin", "Estado", "QR Code", "Check-In", "Creado"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;
            for (Reservation reservation : reservations) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(reservation.getId());
                row.createCell(1).setCellValue(reservation.getUser().getName());
                row.createCell(2).setCellValue(reservation.getUser().getEmail());
                row.createCell(3).setCellValue(reservation.getSpace().getName());
                
                Cell startCell = row.createCell(4);
                startCell.setCellValue(reservation.getStartTime().format(formatter));
                startCell.setCellStyle(dateStyle);
                
                Cell endCell = row.createCell(5);
                endCell.setCellValue(reservation.getEndTime().format(formatter));
                endCell.setCellStyle(dateStyle);
                
                row.createCell(6).setCellValue(reservation.getStatus().name());
                row.createCell(7).setCellValue(reservation.getQrCode() != null ? reservation.getQrCode() : "N/A");
                row.createCell(8).setCellValue(reservation.getCheckinAt() != null ? "Sí" : "No");
                
                Cell createdCell = row.createCell(9);
                createdCell.setCellValue(reservation.getCreatedAt().format(formatter));
                createdCell.setCellStyle(dateStyle);
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        }
    }

    @Override
    public ByteArrayOutputStream exportSpaceStatistics() throws IOException {
        List<Space> spaces = spaceRepository.findAll();
        List<Reservation> allReservations = reservationRepository.findAll();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Estadísticas de Espacios");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            
            // Cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Espacio", "Tipo", "Capacidad", "Total Reservaciones", "Aprobadas", "Pendientes", "Canceladas", "Tasa de Ocupación (%)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Calcular estadísticas por espacio
            Map<Long, List<Reservation>> reservationsBySpace = allReservations.stream()
                    .collect(Collectors.groupingBy(r -> r.getSpace().getId()));
            
            int rowNum = 1;
            for (Space space : spaces) {
                Row row = sheet.createRow(rowNum++);
                List<Reservation> spaceReservations = reservationsBySpace.getOrDefault(space.getId(), List.of());
                
                long totalReservations = spaceReservations.size();
                long confirmed = spaceReservations.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
                long pending = spaceReservations.stream().filter(r -> r.getStatus() == ReservationStatus.PENDING).count();
                long canceled = spaceReservations.stream().filter(r -> r.getStatus() == ReservationStatus.CANCELED).count();
                
                // Tasa de ocupación simplificada (confirmadas / total posible en último mes)
                double occupancyRate = totalReservations > 0 ? (confirmed * 100.0 / totalReservations) : 0.0;
                
                row.createCell(0).setCellValue(space.getName());
                row.createCell(1).setCellValue(space.getType().toString());
                
                Cell capacityCell = row.createCell(2);
                capacityCell.setCellValue(space.getCapacity());
                capacityCell.setCellStyle(numberStyle);
                
                Cell totalCell = row.createCell(3);
                totalCell.setCellValue(totalReservations);
                totalCell.setCellStyle(numberStyle);
                
                Cell confirmedCell = row.createCell(4);
                confirmedCell.setCellValue(confirmed);
                confirmedCell.setCellStyle(numberStyle);
                
                Cell pendingCell = row.createCell(5);
                pendingCell.setCellValue(pending);
                pendingCell.setCellStyle(numberStyle);
                
                Cell canceledCell = row.createCell(6);
                canceledCell.setCellValue(canceled);
                canceledCell.setCellStyle(numberStyle);
                
                Cell occupancyCell = row.createCell(7);
                occupancyCell.setCellValue(String.format("%.2f%%", occupancyRate));
            }
            
            // Auto-ajustar columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
}
