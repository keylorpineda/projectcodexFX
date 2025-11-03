package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.municipal.utils.UtcToCostaRicaDeserializer;
import com.municipal.utils.CostaRicaToUtcSerializer;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the reservation payload returned by the backend API.
 * ✅ Las fechas se convierten automáticamente entre UTC (backend) y Costa Rica (frontend)
 */
public record ReservationDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("userId") Long userId,
        @JsonProperty("spaceId") Long spaceId,
        @JsonProperty("startTime")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime startTime,
        @JsonProperty("endTime")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime endTime,
        @JsonProperty("status") String status,
        @JsonProperty("qrCode") String qrCode,
        @JsonProperty("canceledAt")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime canceledAt,
        @JsonProperty("checkinAt")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime checkinAt,
        @JsonProperty("notes") String notes,
        @JsonProperty("attendees") Integer attendees,
        @JsonProperty("approvedByUserId") Long approvedByUserId,
        @JsonProperty("weatherCheck") JsonNode weatherCheck,
        @JsonProperty("cancellationReason") String cancellationReason,
        @JsonProperty("createdAt")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime createdAt,
        @JsonProperty("updatedAt")
        @JsonDeserialize(using = UtcToCostaRicaDeserializer.class)
        @JsonSerialize(using = CostaRicaToUtcSerializer.class)
        LocalDateTime updatedAt,
        @JsonProperty("ratingId") Long ratingId,
      @JsonProperty("notificationIds") List<Long> notificationIds,
        @JsonProperty("attendeeRecords") List<ReservationAttendeeDTO> attendeeRecords) {
}
