package finalprojectprogramming.project.configs.openWeather;

import com.fasterxml.jackson.databind.ObjectMapper;

import finalprojectprogramming.project.APIs.openWeather.ForecastAggregator;
import finalprojectprogramming.project.APIs.openWeather.WeatherClient;
import finalprojectprogramming.project.services.openWeather.RateLimiterService;
import finalprojectprogramming.project.services.openWeather.WeatherCacheService;
import finalprojectprogramming.project.services.openWeather.WeatherService;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WeatherProperties.class)
public class WeatherConfiguration {

    @Bean
    public RateLimiterService rateLimiterService(WeatherProperties properties) {
        return new RateLimiterService(properties.getPerUserRateLimitPerMinute());
    }

    @Bean
    public WeatherCacheService weatherCacheService(WeatherProperties properties) {
        return new WeatherCacheService(properties.getCacheTtlSeconds());
    }

    @Bean
    public ForecastAggregator forecastAggregator(WeatherProperties properties) {
        return new ForecastAggregator(properties.getZoneId());
    }

    @Bean
    public WeatherClient weatherClient(WeatherProperties properties, ObjectMapper objectMapper) {
        return new WeatherClient(properties, objectMapper);
    }

    @Bean
    public WeatherService weatherService(WeatherClient weatherClient, RateLimiterService rateLimiterService,
            WeatherCacheService weatherCacheService, ForecastAggregator forecastAggregator) {
        return new WeatherService(weatherClient, rateLimiterService, weatherCacheService, forecastAggregator);
    }

    @Bean
    public WeatherStartupLogger weatherStartupLogger(WeatherProperties properties) {
        return new WeatherStartupLogger(properties);
    }
}
