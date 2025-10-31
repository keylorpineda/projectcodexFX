package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the reservation payload returned by the backend API.
 */
public record ReservationDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("userId") Long userId,
        @JsonProperty("spaceId") Long spaceId,
        @JsonProperty("startTime")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime startTime,
        @JsonProperty("endTime")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime endTime,
        @JsonProperty("status") String status,
        @JsonProperty("qrCode") String qrCode,
        @JsonProperty("canceledAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime canceledAt,
        @JsonProperty("checkinAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime checkinAt,
        @JsonProperty("notes") String notes,
        @JsonProperty("attendees") Integer attendees,
        @JsonProperty("approvedByUserId") Long approvedByUserId,
        @JsonProperty("weatherCheck") JsonNode weatherCheck,
        @JsonProperty("cancellationReason") String cancellationReason,
        @JsonProperty("createdAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime createdAt,
        @JsonProperty("updatedAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime updatedAt,
        @JsonProperty("ratingId") Long ratingId,
      @JsonProperty("notificationIds") List<Long> notificationIds,
        @JsonProperty("attendeeRecords") List<ReservationAttendeeDTO> attendeeRecords) {
}
