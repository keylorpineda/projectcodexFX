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

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeatherController.class);

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/daily")
    public ResponseEntity<WeatherDailyResponse> getDailyForecast(@RequestParam("lat") double lat,
            @RequestParam("lon") double lon, HttpServletRequest request) {
        String userKey = resolveUserKey(request);
        WeatherDailyResponse response = weatherService.getDailyForecast(lat, lon, userKey);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<CurrentWeatherResponse> getCurrentWeather(@RequestParam("lat") double lat,
            @RequestParam("lon") double lon, HttpServletRequest request) {
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