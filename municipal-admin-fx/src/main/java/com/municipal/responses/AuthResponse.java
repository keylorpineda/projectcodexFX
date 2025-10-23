package com.municipal.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Representation of the authentication response returned by the backend API.
 */
public record AuthResponse(
        @JsonProperty("token") String token,
        @JsonProperty("tokenType") String tokenType,
        @JsonProperty("expiresAt") Instant expiresAt,
        @JsonProperty("userId") Long userId,
        @JsonProperty("role") String role,
        @JsonProperty("email") String email,
        @JsonProperty("name") String name,
        @JsonProperty("profileComplete") boolean profileComplete,
        @JsonProperty("newUser") boolean newUser) {
}
