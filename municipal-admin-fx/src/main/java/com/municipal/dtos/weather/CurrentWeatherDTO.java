package com.municipal.dtos.weather;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/**
 * Representation of the current weather payload returned by the backend.
 */
public record CurrentWeatherDTO(
        @JsonProperty("temp") Double temperature,
        @JsonProperty("feelsLike") Double feelsLike,
        @JsonProperty("humidity") Integer humidity,
        @JsonProperty("windSpeed") Double windSpeed,
        @JsonProperty("description") String description,
        @JsonProperty("icon") String icon,
        @JsonProperty("dt") OffsetDateTime timestamp) {
}
