package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO utilizado para crear o actualizar usuarios desde la aplicación JavaFX.
 */
public record UserInputDTO(
        @JsonProperty("role") String role,
        @JsonProperty("name") String name,
        @JsonProperty("email") String email,
        @JsonProperty("active") Boolean active) {
}
