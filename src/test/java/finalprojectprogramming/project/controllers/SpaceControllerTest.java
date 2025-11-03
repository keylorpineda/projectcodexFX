package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.services.space.SpaceService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@WebMvcTest(value = SpaceController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @Test
    @WithMockUser(roles = {"USER"})
    void getAllSpacesAsUserReturnsOk() throws Exception {
        when(spaceService.findAll()).thenReturn(List.of(buildSpaceDto()));

        performGet("/api/spaces")
                .andExpect(status().isOk());

        verify(spaceService).findAll();
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getSpaceByIdReturnsOk() throws Exception {
        when(spaceService.findById(12L)).thenReturn(buildSpaceDto());

        performGet("/api/spaces/12")
                .andExpect(status().isOk());

        verify(spaceService).findById(12L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateSpaceAsAdminReturnsOk() throws Exception {
        SpaceDTO dto = buildSpaceDto();
        when(spaceService.update(eq(12L), any(SpaceDTO.class))).thenReturn(dto);

        performPut("/api/spaces/12", dto)
                .andExpect(status().isOk());

        verify(spaceService).update(eq(12L), any(SpaceDTO.class));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void deleteSpaceAsSupervisorReturnsNoContent() throws Exception {
        performDelete("/api/spaces/12")
                .andExpect(status().isNoContent());

        verify(spaceService).delete(12L);
    }

        @Test
        @WithMockUser(roles = {"USER"})
        void searchSpaces_withAllFilters_callsServiceAndReturnsOk() throws Exception {
                when(spaceService.searchSpaces(any(), any(), any(), any(), any())).thenReturn(List.of(buildSpaceDto()));

                performGet("/api/spaces/search?type=AUDITORIO&minCapacity=10&maxCapacity=100&location=centro&active=true")
                                .andExpect(status().isOk());

                verify(spaceService).searchSpaces(eq(SpaceType.AUDITORIO), eq(10), eq(100), eq("centro"), eq(true));
        }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void createSpaceWithImageReturnsCreated() throws Exception {
        SpaceDTO dto = buildSpaceDto();
        when(spaceService.createWithImage(any(SpaceDTO.class), any())).thenReturn(dto);

        MockMultipartFile spacePart = new MockMultipartFile(
                "space",
                "space.json",
                "application/json",
                objectMapper.writeValueAsBytes(dto)
        );
        MockMultipartFile imagePart = new MockMultipartFile(
                "image",
                "image.png",
                "image/png",
                "data".getBytes()
        );

        mockMvc.perform(multipart("/api/spaces/with-image").file(spacePart).file(imagePart))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/spaces/12"));

        verify(spaceService).createWithImage(any(SpaceDTO.class), any());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void createSpaceWithImageWithoutFileStillWorks() throws Exception {
        SpaceDTO dto = buildSpaceDto();
        when(spaceService.createWithImage(any(SpaceDTO.class), any())).thenReturn(dto);

        MockMultipartFile spacePart = new MockMultipartFile(
                "space",
                "space.json",
                "application/json",
                objectMapper.writeValueAsBytes(dto)
        );

        mockMvc.perform(multipart("/api/spaces/with-image").file(spacePart))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/spaces/12"));

        verify(spaceService).createWithImage(any(SpaceDTO.class), any());
    }
}
