package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.services.analytics.AnalyticsService;
import finalprojectprogramming.project.services.analytics.AnalyticsService.SpaceStatistics;
import finalprojectprogramming.project.services.analytics.AnalyticsService.SystemStatistics;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics", description = "Endpoints for system metrics and analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/occupancy-by-space")
    @Operation(summary = "Get occupancy rate by space", 
               description = "Returns the occupancy rate (0-100%) for each space")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved occupancy rates"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Map<Long, Double>> getOccupancyRateBySpace() {
        return ResponseEntity.ok(analyticsService.getOccupancyRateBySpace());
    }

    @GetMapping("/top-spaces")
    @Operation(summary = "Get most reserved spaces", 
               description = "Returns the top N most reserved spaces with statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved top spaces"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<SpaceStatistics>> getMostReservedSpaces(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(analyticsService.getMostReservedSpaces(limit));
    }

    @GetMapping("/reservations-by-hour")
    @Operation(summary = "Get reservations distribution by hour", 
               description = "Returns the number of reservations for each hour of the day (0-23)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved hourly distribution"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Map<Integer, Long>> getReservationsByHour() {
        return ResponseEntity.ok(analyticsService.getReservationsByHour());
    }

    @GetMapping("/no-show-rate-by-user")
    @Operation(summary = "Get no-show rate by user", 
               description = "Returns the no-show rate (0-100%) for each user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved no-show rates"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<Long, Double>> getNoShowRateByUser() {
        return ResponseEntity.ok(analyticsService.getNoShowRateByUser());
    }

    @GetMapping("/system-statistics")
    @Operation(summary = "Get overall system statistics", 
               description = "Returns comprehensive system statistics including users, spaces, and reservations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved system statistics"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SystemStatistics> getSystemStatistics() {
        return ResponseEntity.ok(analyticsService.getSystemStatistics());
    }

    @GetMapping("/reservations-by-status")
    @Operation(summary = "Get reservations grouped by status", 
               description = "Returns the count of reservations for each status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved status distribution"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Map<String, Long>> getReservationsByStatus() {
        return ResponseEntity.ok(analyticsService.getReservationsByStatus());
    }
}
