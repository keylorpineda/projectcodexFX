package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.weather.CurrentWeatherDTO;

import java.util.Objects;

/**
 * Provides access to weather endpoints exposed by the backend API.
 */
public class WeatherService {

    private static final TypeReference<CurrentWeatherDTO> CURRENT_WEATHER_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;

    public WeatherService() {
        this(new ApiClient());
    }

    public WeatherService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    public CurrentWeatherDTO getCurrentWeather(double latitude, double longitude, String bearerToken) {
        String path = String.format("/api/weather/current?lat=%s&lon=%s", latitude, longitude);
        return apiClient.get(path, bearerToken, CURRENT_WEATHER_TYPE);
    }
}
