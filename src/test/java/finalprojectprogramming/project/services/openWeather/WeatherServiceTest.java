package finalprojectprogramming.project.services.openWeather;

import finalprojectprogramming.project.APIs.openWeather.ForecastAggregator;
import finalprojectprogramming.project.APIs.openWeather.WeatherClient;
import finalprojectprogramming.project.APIs.openWeather.WeatherDailyResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WeatherServiceTest {

    private WeatherClient client;
    private RateLimiterService limiter;
    private WeatherCacheService cache;
    private ForecastAggregator aggregator;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        client = mock(WeatherClient.class);
        limiter = mock(RateLimiterService.class);
        cache = mock(WeatherCacheService.class);
        aggregator = mock(ForecastAggregator.class);
        service = new WeatherService(client, limiter, cache, aggregator);
    }

    @Test
    void getDailyForecast_uses_cache_and_falls_back_to_remote() {
        double lat = 9.93, lon = -84.08; String key = "u1";
        WeatherDailyResponse.Location loc = new WeatherDailyResponse.Location(lat, lon);
        WeatherDailyResponse cached = new WeatherDailyResponse(loc, List.of());
        when(cache.get(anyString(), eq(WeatherDailyResponse.class))).thenReturn(Optional.of(cached));

        WeatherDailyResponse fromCache = service.getDailyForecast(lat, lon, key);
        assertThat(fromCache).isSameAs(cached);
        verifyNoInteractions(client, aggregator);

        // cache miss -> call client & aggregator, then cache put
        when(cache.get(anyString(), eq(WeatherDailyResponse.class))).thenReturn(Optional.empty());
        Forecast5Dto five = new Forecast5Dto();
        when(client.getFiveDayForecast(lat, lon)).thenReturn(five);
        when(aggregator.aggregate(five)).thenReturn(List.of(new DailyForecastDto()));

    WeatherDailyResponse fresh = service.getDailyForecast(lat, lon, key);
    assertThat(fresh.getLocation().getLat()).isEqualTo(lat);
        verify(cache).put(anyString(), any(WeatherDailyResponse.class));
        verify(limiter, times(2)).checkAndConsume(key);
    }

    @Test
    void getCurrentWeather_uses_cache_then_client() {
        double lat = 10.0, lon = -84.0; String key = "u2";
        // Primero: cache miss -> va al cliente y guarda en cache
        when(cache.get(anyString(), eq(CurrentWeatherResponse.class))).thenReturn(Optional.empty());
        CurrentWeatherResponse current = new CurrentWeatherResponse();
        when(client.getCurrentWeather(lat, lon)).thenReturn(current);

        CurrentWeatherResponse out = service.getCurrentWeather(lat, lon, key);
        assertThat(out).isSameAs(current);
        verify(cache).put(anyString(), eq(current));

        // Segundo: cache hit -> no llama al cliente
        reset(cache, client);
        when(cache.get(anyString(), eq(CurrentWeatherResponse.class))).thenReturn(Optional.of(current));
        CurrentWeatherResponse fromCache = service.getCurrentWeather(lat, lon, key);
        assertThat(fromCache).isSameAs(current);
        verifyNoInteractions(client);
    }

    @Test
    void invalid_coordinates_throw() {
        assertThatThrownBy(() -> service.getDailyForecast(200, 0, "k")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.getCurrentWeather(0, 200, "k")).isInstanceOf(IllegalArgumentException.class);
    }
}
