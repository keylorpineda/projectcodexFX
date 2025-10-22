package finalprojectprogramming.project.services.openWeather;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import finalprojectprogramming.project.APIs.openWeather.ForecastAggregator;
import finalprojectprogramming.project.APIs.openWeather.ValidationUtils;
import finalprojectprogramming.project.APIs.openWeather.WeatherClient;
import finalprojectprogramming.project.APIs.openWeather.WeatherDailyResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.CurrentWeatherResponse;
import finalprojectprogramming.project.dtos.openWeatherDTOs.Forecast5Dto;
import finalprojectprogramming.project.dtos.openWeatherDTOs.DailyForecastDto;


public class WeatherService {

    private static final String DAILY_KEY_SUFFIX = "FORECAST5_DAILY";
    private static final String CURRENT_KEY_SUFFIX = "CURRENT";

    private final WeatherClient weatherClient;
    private final RateLimiterService rateLimiterService;
    private final WeatherCacheService cacheService;
    private final ForecastAggregator forecastAggregator;

    public WeatherService(WeatherClient weatherClient, RateLimiterService rateLimiterService,
            WeatherCacheService cacheService, ForecastAggregator forecastAggregator) {
        this.weatherClient = weatherClient;
        this.rateLimiterService = rateLimiterService;
        this.cacheService = cacheService;
        this.forecastAggregator = forecastAggregator;
    }

    public WeatherDailyResponse getDailyForecast(double lat, double lon, String userKey) {
        ValidationUtils.validateLatLon(lat, lon);
        rateLimiterService.checkAndConsume(userKey);
        String cacheKey = buildCacheKey(userKey, lat, lon, DAILY_KEY_SUFFIX);
        Optional<WeatherDailyResponse> cached = cacheService.get(cacheKey, WeatherDailyResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        Forecast5Dto forecast5Dto = weatherClient.getFiveDayForecast(lat, lon);
        List<DailyForecastDto> aggregated = forecastAggregator.aggregate(forecast5Dto);
        WeatherDailyResponse response = new WeatherDailyResponse(
                new WeatherDailyResponse.Location(lat, lon),
                List.copyOf(aggregated));
        cacheService.put(cacheKey, response);
        return response;
    }

    public CurrentWeatherResponse getCurrentWeather(double lat, double lon, String userKey) {
        ValidationUtils.validateLatLon(lat, lon);
        rateLimiterService.checkAndConsume(userKey);
        String cacheKey = buildCacheKey(userKey, lat, lon, CURRENT_KEY_SUFFIX);
        Optional<CurrentWeatherResponse> cached = cacheService.get(cacheKey, CurrentWeatherResponse.class);
        if (cached.isPresent()) {
            return cached.get();
        }
        CurrentWeatherResponse response = weatherClient.getCurrentWeather(lat, lon);
        cacheService.put(cacheKey, response);
        return response;
    }

    private String buildCacheKey(String userKey, double lat, double lon, String suffix) {
        return String.format(Locale.ROOT, "%s:%.4f:%.4f:%s", userKey, lat, lon, suffix);
    }
}