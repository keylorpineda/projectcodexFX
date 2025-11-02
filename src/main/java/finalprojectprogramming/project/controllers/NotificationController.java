package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.NotificationDTO;
import finalprojectprogramming.project.services.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/notifications")
@Validated
@Tag(name = "Notifications", description = "Operations related to notifications management")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a new notification")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<NotificationDTO> createNotification(@Valid @RequestBody NotificationDTO notificationDTO) {
        NotificationDTO created = notificationService.create(notificationDTO);
        return ResponseEntity.created(URI.create("/api/notifications/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing notification")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<NotificationDTO> updateNotification(@PathVariable Long id,
            @Valid @RequestBody NotificationDTO notificationDTO) {
        return ResponseEntity.ok(notificationService.update(id, notificationDTO));
    }

    @GetMapping
    @Operation(summary = "Retrieve all notifications")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<NotificationDTO>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a notification by id")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<NotificationDTO> getNotificationById(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.findById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "Retrieve notifications by reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<NotificationDTO>> getNotificationsByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(notificationService.findByReservation(reservationId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/send-custom-email")
    @Operation(summary = "Send custom email notification for a reservation")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> sendCustomEmail(@Valid @RequestBody CustomEmailRequest request) {
        notificationService.sendCustomEmailToReservation(
            request.reservationId(), 
            request.subject(), 
            request.message()
        );
        return ResponseEntity.ok().build();
    }
    
    public record CustomEmailRequest(Long reservationId, String subject, String message) {}
}