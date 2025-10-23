package com.municipal.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Payload used to authenticate a user against the backend API using an Azure
 * AD access token.
 */
public record AzureLoginRequest(@JsonProperty("accessToken") String accessToken) {

    public AzureLoginRequest {
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalArgumentException("Access token must not be blank");
        }
    }
}
