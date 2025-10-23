package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO mirroring the SpaceDTO from the backend service so the JavaFX
 * application can display the data returned by the API.
 */
public record SpaceDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("capacity") Integer capacity,
        @JsonProperty("description") String description,
        @JsonProperty("location") String location,
        @JsonProperty("active") Boolean active,
        @JsonProperty("maxReservationDuration") Integer maxReservationDuration,
        @JsonProperty("requiresApproval") Boolean requiresApproval,
        @JsonProperty("averageRating") Float averageRating,
        @JsonProperty("createdAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createdAt,
        @JsonProperty("updatedAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime updatedAt,
        @JsonProperty("imageIds") List<Long> imageIds,
        @JsonProperty("scheduleIds") List<Long> scheduleIds,
        @JsonProperty("reservationIds") List<Long> reservationIds) {
}
