package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO utilizado para crear o actualizar espacios desde la aplicación JavaFX.
 */
public record SpaceInputDTO(
        @JsonProperty("name") String name,
        @JsonProperty("type") String type,
        @JsonProperty("capacity") Integer capacity,
        @JsonProperty("description") String description,
        @JsonProperty("location") String location,
        @JsonProperty("active") Boolean active,
        @JsonProperty("maxReservationDuration") Integer maxReservationDuration,
        @JsonProperty("requiresApproval") Boolean requiresApproval) {
}
