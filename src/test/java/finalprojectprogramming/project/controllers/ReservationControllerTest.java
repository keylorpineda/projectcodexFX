package finalprojectprogramming.project.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.controllers.ReservationController.ApprovalRequest;
import finalprojectprogramming.project.dtos.ReservationCheckInRequest;
import finalprojectprogramming.project.dtos.ReservationDTO;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.services.reservation.ReservationService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class ReservationControllerTest extends BaseControllerTest {

    @MockBean
    private ReservationService reservationService;

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
}
