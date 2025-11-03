package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.APIs.openWeather.WeatherDailyResponse;
import finalprojectprogramming.project.APIs.openWeather.WeatherDailyResponse.Location;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;
import finalprojectprogramming.project.services.openWeather.WeatherService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WeatherController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class WeatherControllerTest extends BaseControllerTest {

    @MockBean
    private WeatherService weatherService;

    @Test
    @WithMockUser(username = "alice")
    void getDailyForecastUsesAuthenticatedUserAsKey() throws Exception {
        WeatherDailyResponse response = new WeatherDailyResponse();
        response.setLocation(new Location(10, 20));
        DailyForecastDto forecast = new DailyForecastDto();
        forecast.setDate(LocalDate.now());
        forecast.setDescription("Sunny");
        response.setDaily(List.of(forecast));
        when(weatherService.getDailyForecast(eq(10.0), eq(20.0), eq("alice"))).thenReturn(response);

        mockMvc.perform(get("/api/weather/daily")
                        .param("lat", "10")
                        .param("lon", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentWeatherFallsBackToForwardedHeader() throws Exception {
        CurrentWeatherResponse response = new CurrentWeatherResponse();
        response.setDescription("Cloudy");
        response.setDt(OffsetDateTime.now());
        when(weatherService.getCurrentWeather(eq(1.0), eq(2.0), eq("3.3.3.3"))).thenReturn(response);

        mockMvc.perform(get("/api/weather/current")
                        .param("lat", "1")
                        .param("lon", "2")
                        .header("X-Forwarded-For", "3.3.3.3"))
                .andExpect(status().isOk());
    }

    @Test
    void getCurrentWeatherUsesRemoteAddressWhenNoHeader() throws Exception {
        CurrentWeatherResponse response = new CurrentWeatherResponse();
        response.setDescription("Rain");
        when(weatherService.getCurrentWeather(eq(5.0), eq(6.0), eq("9.9.9.9"))).thenReturn(response);

        mockMvc.perform(get("/api/weather/current")
                        .param("lat", "5")
                        .param("lon", "6")
                        .with(request -> {
                            request.setRemoteAddr("9.9.9.9");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "bob")
    void getDailyForecastPropagatesServiceError() throws Exception {
        when(weatherService.getDailyForecast(eq(7.0), eq(8.0), eq("bob"))).thenThrow(new RuntimeException("boom"));

        mockMvc.perform(get("/api/weather/daily")
                        .param("lat", "7")
                        .param("lon", "8"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getCurrentWeatherParsesFirstForwardedIp() throws Exception {
        CurrentWeatherResponse response = new CurrentWeatherResponse();
        response.setDescription("Windy");
        when(weatherService.getCurrentWeather(eq(12.0), eq(34.0), eq("1.1.1.1"))).thenReturn(response);

        mockMvc.perform(get("/api/weather/current")
                        .param("lat", "12")
                        .param("lon", "34")
                        .header("X-Forwarded-For", "1.1.1.1, 2.2.2.2"))
                .andExpect(status().isOk());
    }

    @Test
    void getDailyForecastFallsBackToUnknownWhenNoIp() throws Exception {
        WeatherDailyResponse response = new WeatherDailyResponse();
        response.setLocation(new Location(0, 0));
        when(weatherService.getDailyForecast(eq(0.0), eq(0.0), eq("desconocido"))).thenReturn(response);

        mockMvc.perform(get("/api/weather/daily")
                        .param("lat", "0")
                        .param("lon", "0")
                        .with(request -> {
                            request.setRemoteAddr("");
                            return request;
                        }))
                .andExpect(status().isOk());
    }
}
