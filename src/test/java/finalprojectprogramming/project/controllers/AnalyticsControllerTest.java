package finalprojectprogramming.project.controllers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.services.analytics.AnalyticsService;
import finalprojectprogramming.project.services.analytics.AnalyticsService.SpaceStatistics;
import finalprojectprogramming.project.services.analytics.AnalyticsService.SystemStatistics;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.test.mock.mockito.MockBean;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({BaseControllerTest.TestMethodSecurityConfig.class})
class AnalyticsControllerTest extends BaseControllerTest {

    @Autowired
    ObjectMapper mapper;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getOccupancyRateBySpace_returns_map() throws Exception {
        when(analyticsService.getOccupancyRateBySpace()).thenReturn(Map.of(1L, 75.5, 2L, 0.0));

        performGet("/api/analytics/occupancy-by-space")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"1\":75.5,\"2\":0.0}"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getMostReservedSpaces_returns_list() throws Exception {
        SpaceStatistics s1 = new SpaceStatistics(1L, "Gimnasio", "GIMNASIO", 12L, 8L, 0.85);
        SpaceStatistics s2 = new SpaceStatistics(2L, "Auditorio", "AUDITORIO", 5L, 3L, 0.40);
        when(analyticsService.getMostReservedSpaces(anyInt())).thenReturn(List.of(s1, s2));

        performGet("/api/analytics/top-spaces?limit=2")
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(List.of(s1, s2))));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getReservationsByHour_returns_map() throws Exception {
        when(analyticsService.getReservationsByHour()).thenReturn(Map.of(9, 3L, 18, 7L));

        performGet("/api/analytics/reservations-by-hour")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"9\":3,\"18\":7}"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void getNoShowRateByUser_returns_map() throws Exception {
        when(analyticsService.getNoShowRateByUser()).thenReturn(Map.of(10L, 12.5));

        performGet("/api/analytics/no-show-rate-by-user")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"10\":12.5}"));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getSystemStatistics_returns_object() throws Exception {
        SystemStatistics stats = new SystemStatistics(
                100L, 80L, 15L, 300L, 210L, 30L, 60L, 0.72, 0.10);
        when(analyticsService.getSystemStatistics()).thenReturn(stats);

        performGet("/api/analytics/system-statistics")
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(stats)));
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getReservationsByStatus_returns_map() throws Exception {
        when(analyticsService.getReservationsByStatus()).thenReturn(Map.of("CONFIRMED", 3L, "PENDING", 2L));

        performGet("/api/analytics/reservations-by-status")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"CONFIRMED\":3,\"PENDING\":2}"));
    }
}
