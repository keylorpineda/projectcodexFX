package com.municipal.dtos.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.OffsetDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;

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
        @JsonProperty("dt")
        @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        OffsetDateTime timestamp) {
}
