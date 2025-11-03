package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.excel.ExcelExportService;
import finalprojectprogramming.project.services.reservation.ReservationExportService;
import finalprojectprogramming.project.services.reservation.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@Validated
@Tag(name = "Reservations", description = "Operations related to reservations management")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationExportService reservationExportService;
    private final ExcelExportService excelExportService;

    public ReservationController(ReservationService reservationService,
            ReservationExportService reservationExportService,
            ExcelExportService excelExportService) {
        this.reservationService = reservationService;
        this.reservationExportService = reservationExportService;
        this.excelExportService = excelExportService;
    }

    @PostMapping
    @Operation(summary = "Create a new reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<ReservationDTO> createReservation(@Valid @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO created = reservationService.create(reservationDTO);
        return ResponseEntity.created(URI.create("/api/reservations/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<ReservationDTO> updateReservation(@PathVariable Long id,
            @Valid @RequestBody ReservationDTO reservationDTO) {
        return ResponseEntity.ok(reservationService.update(id, reservationDTO));
    }

    @GetMapping
    @Operation(summary = "Retrieve all reservations")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a reservation by id")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<ReservationDTO> getReservationById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.findById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Retrieve reservations by user")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<List<ReservationDTO>> getReservationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationService.findByUser(userId));
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "Retrieve reservations by space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<ReservationDTO>> getReservationsBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(reservationService.findBySpace(spaceId));
    }

    @GetMapping(value = "/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export all reservations to Excel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<byte[]> exportAllReservations() {
        byte[] document = reservationExportService.exportAllReservations();
        HttpHeaders headers = buildExcelHeaders("reporte_reservas.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }

    @GetMapping(value = "/user/{userId}/export",
            produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Export reservations for a user to Excel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<byte[]> exportReservationsForUser(@PathVariable Long userId) {
        SecurityUtils.requireSelfOrAny(userId, UserRole.SUPERVISOR, UserRole.ADMIN);
        byte[] document = reservationExportService.exportReservationsForUser(userId);
        HttpHeaders headers = buildExcelHeaders("historial_reservas_usuario_" + userId + ".xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(document);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long id,
            @RequestBody(required = false) CancellationRequest request) {
        String reason = request != null ? request.reason() : null;
        return ResponseEntity.ok(reservationService.cancel(id, reason));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve a reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ReservationDTO> approveReservation(@PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        return ResponseEntity.ok(reservationService.approve(id, request.approverUserId()));
    }

    @PostMapping("/{id}/check-in")
    @Operation(summary = "Register reservation check-in")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ReservationDTO> markCheckIn(@PathVariable Long id,
            @Valid @RequestBody ReservationCheckInRequest request) {
        return ResponseEntity.ok(reservationService.markCheckIn(id, request));
    }

    @PostMapping("/{id}/no-show")
    @Operation(summary = "Mark reservation as no-show")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<ReservationDTO> markNoShow(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.markNoShow(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/permanent")
    @Operation(summary = "Permanently delete a reservation from database (only for CHECKED_IN or NO_SHOW)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> permanentlyDeleteReservation(@PathVariable Long id) {
        reservationService.hardDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/space-statistics")
    @Operation(summary = "Export space statistics to Excel (Admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> exportSpaceStatistics() {
        try {
            ByteArrayOutputStream outputStream = excelExportService.exportSpaceStatistics();
            
            HttpHeaders headers = buildExcelHeaders("estadisticas-espacios.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public record CancellationRequest(String reason) {
    }

    public record ApprovalRequest(@NotNull Long approverUserId) {
    }

    private HttpHeaders buildExcelHeaders(String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return headers;
    }
}
