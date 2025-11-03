package finalprojectprogramming.project.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.controllers.ReservationController.ApprovalRequest;
import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.security.AppUserDetails;
import finalprojectprogramming.project.services.reservation.ReservationExportService;
import finalprojectprogramming.project.services.reservation.ReservationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
@org.junit.jupiter.api.Disabled("Duplicate legacy copy; kept to avoid accidental deletion conflicts")
class ReservationControllerTestDuplicate extends BaseControllerTest {

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservationExportService reservationExportService;

    private ReservationDTO buildReservationDto() {
        ObjectNode weather = objectMapper.createObjectNode();
        weather.put("condition", "sunny");
        return ReservationDTO.builder()
                .id(15L)
                .userId(1L)
                .spaceId(2L)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .status(ReservationStatus.PENDING)
                .qrCode("QR-123")
                .attendees(3)
                .weatherCheck(weather)
                .build();
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getAllReservationsAsSupervisorReturnsOk() throws Exception {
        when(reservationService.findAll()).thenReturn(java.util.List.of(buildReservationDto()));

        performGet("/api/reservations")
                .andExpect(status().isOk());

        verify(reservationService).findAll();
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getReservationByIdReturnsOk() throws Exception {
        when(reservationService.findById(10L)).thenReturn(buildReservationDto());

        performGet("/api/reservations/10")
                .andExpect(status().isOk());

        verify(reservationService).findById(10L);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getReservationsByUserReturnsOk() throws Exception {
        when(reservationService.findByUser(7L)).thenReturn(java.util.List.of(buildReservationDto()));

        performGet("/api/reservations/user/7")
                .andExpect(status().isOk());

        verify(reservationService).findByUser(7L);
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getReservationsBySpaceAsSupervisorReturnsOk() throws Exception {
        when(reservationService.findBySpace(3L)).thenReturn(java.util.List.of(buildReservationDto()));

        performGet("/api/reservations/space/3")
                .andExpect(status().isOk());

        verify(reservationService).findBySpace(3L);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void cancelReservationWithoutBodyPassesNullReason() throws Exception {
        when(reservationService.cancel(11L, null)).thenReturn(buildReservationDto());

        mockMvc.perform(post("/api/reservations/11/cancel")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reservationService).cancel(11L, null);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void cancelReservationWithBodyPassesReason() throws Exception {
        when(reservationService.cancel(12L, "because")).thenReturn(buildReservationDto());

        var payload = objectMapper.createObjectNode();
        payload.put("reason", "because");

        mockMvc.perform(post("/api/reservations/12/cancel")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(payload)))
                .andExpect(status().isOk());

        verify(reservationService).cancel(12L, "because");
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void markNoShowDelegatesToService() throws Exception {
        when(reservationService.markNoShow(13L)).thenReturn(buildReservationDto());

        mockMvc.perform(post("/api/reservations/13/no-show")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(reservationService).markNoShow(13L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void permanentlyDeleteReservationAsAdminReturnsNoContent() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/reservations/99/permanent")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(reservationService).hardDelete(99L);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createReservationReturnsCreatedWithLocation() throws Exception {
        ReservationDTO dto = buildReservationDto();
        when(reservationService.create(any(ReservationDTO.class))).thenReturn(dto);

        performPost("/api/reservations", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/reservations/15"));

        verify(reservationService).create(any(ReservationDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createReservationFailsValidationWhenMissingTimes() throws Exception {
        ReservationDTO invalid = ReservationDTO.builder()
                .userId(1L)
                .spaceId(2L)
                .status(ReservationStatus.PENDING)
                .qrCode("QR-123")
                .attendees(2)
                .build();

        performPost("/api/reservations", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createReservationPropagatesServiceErrors() throws Exception {
        ReservationDTO dto = buildReservationDto();
        when(reservationService.create(any(ReservationDTO.class))).thenThrow(new RuntimeException("boom"));

        performPost("/api/reservations", dto)
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllReservationsForbiddenForUserRole() throws Exception {
        performGet("/api/reservations")
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void approveReservationRequiresApproverId() throws Exception {
        ApprovalRequest request = new ApprovalRequest(null);

        performPost("/api/reservations/4/approve", request)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void approveReservationReturnsOk() throws Exception {
        ReservationDTO dto = buildReservationDto();
        when(reservationService.approve(4L, 9L)).thenReturn(dto);

        ApprovalRequest request = new ApprovalRequest(9L);

        performPost("/api/reservations/4/approve", request)
                .andExpect(status().isOk());

        verify(reservationService).approve(4L, 9L);
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void markCheckInValidatesPayload() throws Exception {
        ReservationCheckInRequest invalid = new ReservationCheckInRequest("", "123", "John", "Doe");

        performPost("/api/reservations/5/check-in", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void markCheckInDelegatesToService() throws Exception {
        ReservationDTO dto = buildReservationDto();
        ReservationCheckInRequest request = new ReservationCheckInRequest("QR-123", "123", "John", "Doe");
    when(reservationService.markCheckIn(eq(5L), any(ReservationCheckInRequest.class))).thenReturn(dto);

        performPost("/api/reservations/5/check-in", request)
                .andExpect(status().isOk());

    verify(reservationService).markCheckIn(eq(5L), any(ReservationCheckInRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteReservationReturnsNoContent() throws Exception {
        performDelete("/api/reservations/8")
                .andExpect(status().isNoContent());

        verify(reservationService).delete(8L);
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void exportAllReservationsReturnsExcel() throws Exception {
        when(reservationExportService.exportAllReservations()).thenReturn(new byte[] {1, 2, 3});

        performGet("/api/reservations/export")
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", containsString("attachment")));

        verify(reservationExportService).exportAllReservations();
    }

    @Test
    void exportUserReservationsAsOwnerReturnsExcel() throws Exception {
        when(reservationExportService.exportReservationsForUser(5L)).thenReturn(new byte[] {4, 5, 6});

        User authenticatedUser = User.builder()
                .id(5L)
                .email("user@example.com")
                .role(UserRole.USER)
                .active(true)
                .build();
        AppUserDetails principal = new AppUserDetails(authenticatedUser);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        try {
            mockMvc.perform(get("/api/reservations/user/5/export")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));

            verify(reservationExportService).exportReservationsForUser(5L);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
