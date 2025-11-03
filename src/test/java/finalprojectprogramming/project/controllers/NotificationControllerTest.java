package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.NotificationDTO;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.services.notification.NotificationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class NotificationControllerTest extends BaseControllerTest {

    @MockBean
    private NotificationService notificationService;

    private NotificationDTO buildNotificationDto() {
        return NotificationDTO.builder()
                .id(7L)
                .reservationId(3L)
                .type(NotificationType.CONFIRMATION)
                .sentTo("user@example.com")
                .messageContent("hello")
                .sentAt(LocalDateTime.now())
                .status(NotificationStatus.SENT)
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createNotificationAsAdminReturnsCreated() throws Exception {
        NotificationDTO input = buildNotificationDto();
        when(notificationService.create(any(NotificationDTO.class))).thenReturn(input);

        performPost("/api/notifications", input)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/notifications/7"));

        verify(notificationService).create(any(NotificationDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createNotificationDeniedForRegularUser() throws Exception {
        NotificationDTO input = buildNotificationDto();

        performPost("/api/notifications", input)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createNotificationFailsValidationForInvalidEmail() throws Exception {
        NotificationDTO invalid = NotificationDTO.builder()
                .reservationId(3L)
                .type(NotificationType.CONFIRMATION)
                .sentTo("not-an-email")
                .status(NotificationStatus.SENT)
                .build();

        performPost("/api/notifications", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getNotificationByIdHandlesServiceException() throws Exception {
        when(notificationService.findById(99L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/notifications/99")
                .andExpect(status().isInternalServerError());
    }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        void getNotificationByIdReturnsOk() throws Exception {
                when(notificationService.findById(7L)).thenReturn(buildNotificationDto());
                performGet("/api/notifications/7")
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        void updateNotificationReturnsOk() throws Exception {
                NotificationDTO dto = buildNotificationDto();
                when(notificationService.update(eq(7L), any(NotificationDTO.class))).thenReturn(dto);
                performPut("/api/notifications/7", dto)
                                .andExpect(status().isOk());
                verify(notificationService).update(eq(7L), any(NotificationDTO.class));
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        void getAllNotificationsReturnsOk() throws Exception {
                when(notificationService.findAll()).thenReturn(java.util.List.of(buildNotificationDto()));
                performGet("/api/notifications")
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        void getNotificationsByReservationReturnsOk() throws Exception {
                when(notificationService.findByReservation(3L)).thenReturn(java.util.List.of(buildNotificationDto()));
                performGet("/api/notifications/reservation/3")
                                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = {"ADMIN"})
        void deleteNotificationReturnsNoContent() throws Exception {
                performDelete("/api/notifications/7")
                                .andExpect(status().isNoContent());
                verify(notificationService).delete(7L);
        }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void sendCustomEmailDelegatesToService() throws Exception {
        NotificationController.CustomEmailRequest request = new NotificationController.CustomEmailRequest(
                2L, "Subject", "Body");

        performPost("/api/notifications/send-custom-email", request)
                .andExpect(status().isOk());

        verify(notificationService).sendCustomEmailToReservation(eq(2L), eq("Subject"), eq("Body"));
    }
}
