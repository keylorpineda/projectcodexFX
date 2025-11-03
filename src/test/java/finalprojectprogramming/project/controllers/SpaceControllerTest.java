package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.services.space.SpaceService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = SpaceController.class)
@Import({BaseControllerTest.TestMethodSecurityConfig.class})
class SpaceControllerTest extends BaseControllerTest {

    @MockBean
    private SpaceService spaceService;

    private SpaceDTO buildSpaceDto() {
        return SpaceDTO.builder()
                .id(12L)
                .name("Main Hall")
                .type(SpaceType.AUDITORIO)
                .capacity(100)
                .active(true)
                .requiresApproval(true)
                .averageRating(4.5)
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createSpaceAsAdminReturnsCreated() throws Exception {
        SpaceDTO dto = buildSpaceDto();
        when(spaceService.create(any(SpaceDTO.class))).thenReturn(dto);

        performPost("/api/spaces", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/spaces/12"));

        verify(spaceService).create(any(SpaceDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createSpaceForbiddenForUser() throws Exception {
        SpaceDTO dto = buildSpaceDto();

        performPost("/api/spaces", dto)
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createSpaceFailsValidationForMissingName() throws Exception {
        SpaceDTO invalid = SpaceDTO.builder()
                .type(SpaceType.CANCHA)
                .capacity(20)
                .build();

        performPost("/api/spaces", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getSpaceByIdHandlesServiceException() throws Exception {
        when(spaceService.findById(50L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/spaces/50")
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void changeStatusDelegatesToService() throws Exception {
        SpaceDTO dto = buildSpaceDto();
        when(spaceService.changeStatus(12L, false)).thenReturn(dto);

        performPatch("/api/spaces/12/status?active=false", dto)
                .andExpect(status().isOk());

        verify(spaceService).changeStatus(12L, false);
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void findAvailableSpacesReturnsOk() throws Exception {
        when(spaceService.findAvailableSpaces(any(LocalDateTime.class), any(LocalDateTime.class), any(), any()))
                .thenReturn(List.of(buildSpaceDto()));

        performGet("/api/spaces/available?startTime=2024-01-01T10:00:00&endTime=2024-01-01T12:00:00&type=SALA&minimumCapacity=10")
                .andExpect(status().isOk());
    }
}
