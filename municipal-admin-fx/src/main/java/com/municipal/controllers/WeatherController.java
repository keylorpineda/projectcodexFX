package com.municipal.controllers;

import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.services.WeatherService;

/**
 * Provides a lightweight API for JavaFX controllers that need weather data.
 */
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController() {
        this(new WeatherService());
    }

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public CurrentWeatherDTO loadCurrentWeather(double latitude, double longitude, String bearerToken) {
        return weatherService.getCurrentWeather(latitude, longitude, bearerToken);
    }
}
