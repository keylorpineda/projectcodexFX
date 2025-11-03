package finalprojectprogramming.project.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import finalprojectprogramming.project.APIs.openWeather.WeatherDailyResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.services.openWeather.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather", description = "🌤️ Weather information from OpenWeather API")
public class WeatherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/daily")
    @Operation(
        summary = "Get 5-day weather forecast",
        description = """
            Retrieves daily weather forecast for the next 5 days from OpenWeather API.
            
            **Use case:** Display weather forecast in user dashboard to help plan reservations.
            
            **Coordinates:** Use latitude and longitude of the location (e.g., Costa Rica: lat=9.7489, lon=-83.7534)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Weather data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "❌ Invalid coordinates"),
        @ApiResponse(responseCode = "401", description = "❌ OpenWeather API key invalid or missing"),
        @ApiResponse(responseCode = "500", description = "❌ Error communicating with OpenWeather API")
    })
    public ResponseEntity<WeatherDailyResponse> getDailyForecast(
        @Parameter(description = "Latitude coordinate", example = "9.7489", required = true)
        @RequestParam("lat") double lat,
        @Parameter(description = "Longitude coordinate", example = "-83.7534", required = true)
        @RequestParam("lon") double lon, 
        HttpServletRequest request
    ) {
        String userKey = resolveUserKey(request);
        WeatherDailyResponse response = weatherService.getDailyForecast(lat, lon, userKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    @Operation(
        summary = "Get current weather conditions",
        description = """
            Retrieves current weather conditions from OpenWeather API.
            
            **Use case:** Display real-time weather in dashboard widgets.
            
            **Data includes:**
            - Temperature (current, feels like, min, max)
            - Weather condition (clear, cloudy, rain, etc.)
            - Humidity percentage
            - Wind speed and direction
            - Atmospheric pressure
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Current weather retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "❌ Invalid coordinates"),
        @ApiResponse(responseCode = "401", description = "❌ OpenWeather API key invalid or missing"),
        @ApiResponse(responseCode = "500", description = "❌ Error communicating with OpenWeather API")
    })
    public ResponseEntity<CurrentWeatherResponse> getCurrentWeather(
        @Parameter(description = "Latitude coordinate", example = "9.7489", required = true)
        @RequestParam("lat") double lat,
        @Parameter(description = "Longitude coordinate", example = "-83.7534", required = true)
        @RequestParam("lon") double lon, 
        HttpServletRequest request
    ) {
        String userKey = resolveUserKey(request);
        CurrentWeatherResponse response = weatherService.getCurrentWeather(lat, lon, userKey);
        return ResponseEntity.ok(response);
    }

    private String resolveUserKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        if (!StringUtils.hasText(remoteAddr)) {
            LOGGER.debug("No se pudo resolver la IP remota, usando 'desconocido'");
            return "desconocido";
        }
        return remoteAddr;
    }
}