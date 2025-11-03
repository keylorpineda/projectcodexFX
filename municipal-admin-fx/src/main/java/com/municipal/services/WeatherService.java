package com.municipal.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.weather.CurrentWeatherDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides access to weather endpoints exposed by the backend API.
 * ✅ Implementa caché simple para evitar exceder rate limits del API
 */
public class WeatherService {

    private static final TypeReference<CurrentWeatherDTO> CURRENT_WEATHER_TYPE = new TypeReference<>() {
    };

    private final ApiClient apiClient;
    
    // ✅ Caché simple: lat_lon -> (timestamp, data)
    private final Map<String, CachedWeather> weatherCache = new ConcurrentHashMap<>();
    private static final Duration CACHE_DURATION = Duration.ofMinutes(10); // 10 minutos de caché

    public WeatherService() {
        this(new ApiClient());
    }

    public WeatherService(ApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient, "apiClient");
    }

    public CurrentWeatherDTO getCurrentWeather(double latitude, double longitude, String bearerToken) {
        String cacheKey = String.format("%.4f_%.4f", latitude, longitude);
        
        // Verificar si hay dato en caché y aún es válido
        CachedWeather cached = weatherCache.get(cacheKey);
        if (cached != null && cached.isValid()) {
            System.out.println("✅ Clima obtenido desde caché (evita llamada API)");
            return cached.data;
        }
        
        // Si no hay caché o expiró, hacer llamada real
        try {
            String path = String.format("/api/weather/current?lat=%s&lon=%s", latitude, longitude);
            CurrentWeatherDTO data = apiClient.get(path, bearerToken, CURRENT_WEATHER_TYPE);
            
            // Guardar en caché
            weatherCache.put(cacheKey, new CachedWeather(data));
            System.out.println("✅ Clima obtenido desde API y guardado en caché");
            
            return data;
        } catch (Exception e) {
            // Si falla pero hay caché expirado, usar el caché viejo
            if (cached != null) {
                System.out.println("⚠️ Error en API, usando caché expirado");
                return cached.data;
            }
            throw e;
        }
    }
    
    /**
     * Limpia el caché manualmente (útil para testing)
     */
    public void clearCache() {
        weatherCache.clear();
    }
    
    /**
     * Clase interna para almacenar datos con timestamp
     */
    private static class CachedWeather {
        final LocalDateTime timestamp;
        final CurrentWeatherDTO data;
        
        CachedWeather(CurrentWeatherDTO data) {
            this.timestamp = LocalDateTime.now();
            this.data = data;
        }
        
        boolean isValid() {
            return Duration.between(timestamp, LocalDateTime.now()).compareTo(CACHE_DURATION) < 0;
        }
    }
}
