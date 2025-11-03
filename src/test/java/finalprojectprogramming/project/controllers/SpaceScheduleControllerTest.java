package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceScheduleDTO;
import finalprojectprogramming.project.services.spaceschedule.SpaceScheduleService;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaceScheduleController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class SpaceScheduleControllerTest extends BaseControllerTest {

    @MockBean
    private SpaceScheduleService spaceScheduleService;

    private SpaceScheduleDTO buildScheduleDto() {
        return SpaceScheduleDTO.builder()
                .id(30L)
                .spaceId(5L)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(17, 0))
                .holidayOverride(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createScheduleReturnsCreated() throws Exception {
        SpaceScheduleDTO dto = buildScheduleDto();
        when(spaceScheduleService.create(any(SpaceScheduleDTO.class))).thenReturn(dto);

        performPost("/api/space-schedules", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/space-schedules/30"));

        verify(spaceScheduleService).create(any(SpaceScheduleDTO.class));
    }

    @Test
    void createScheduleFailsValidationWhenMissingTimes() throws Exception {
        SpaceScheduleDTO invalid = SpaceScheduleDTO.builder()
                .spaceId(5L)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .holidayOverride(true)
                .build();

        performPost("/api/space-schedules", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getScheduleByIdHandlesServiceException() throws Exception {
        when(spaceScheduleService.findById(11L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/space-schedules/11")
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getScheduleByIdReturnsOk() throws Exception {
        when(spaceScheduleService.findById(30L)).thenReturn(buildScheduleDto());

        performGet("/api/space-schedules/30")
                .andExpect(status().isOk());

        verify(spaceScheduleService).findById(30L);
    }

    @Test
    void getAllSchedulesReturnsOk() throws Exception {
        when(spaceScheduleService.findAll()).thenReturn(java.util.List.of(buildScheduleDto()));

        performGet("/api/space-schedules")
                .andExpect(status().isOk());

        verify(spaceScheduleService).findAll();
    }

    @Test
    void getSchedulesBySpaceReturnsOk() throws Exception {
        when(spaceScheduleService.findBySpace(5L)).thenReturn(java.util.List.of(buildScheduleDto()));

        performGet("/api/space-schedules/space/5")
                .andExpect(status().isOk());

        verify(spaceScheduleService).findBySpace(5L);
    }

    @Test
    void updateScheduleReturnsOk() throws Exception {
        SpaceScheduleDTO updated = buildScheduleDto();
        when(spaceScheduleService.update(eq(30L), any(SpaceScheduleDTO.class))).thenReturn(updated);

        performPut("/api/space-schedules/30", buildScheduleDto())
                .andExpect(status().isOk());

        verify(spaceScheduleService).update(eq(30L), any(SpaceScheduleDTO.class));
    }

    @Test
    void deleteScheduleReturnsNoContent() throws Exception {
        performDelete("/api/space-schedules/6")
                .andExpect(status().isNoContent());

        verify(spaceScheduleService).delete(eq(6L));
    }
}
