package com.municipal.reservationsfx.session;

import com.municipal.reservationsfx.backend.responses.AuthResponse;

import java.util.Optional;

/**
 * Stores session details retrieved from the backend so different JavaFX
 * controllers can access authentication context.
 */
public final class SessionManager {

    private AuthResponse authResponse;

    public void clear() {
        authResponse = null;
    }

    public void storeAuthResponse(AuthResponse response) {
        this.authResponse = response;
    }

    public Optional<AuthResponse> getAuthResponse() {
        return Optional.ofNullable(authResponse);
    }

    public String getAccessToken() {
        return authResponse != null ? authResponse.token() : null;
    }

    public String getUserDisplayName() {
        return authResponse != null ? authResponse.name() : null;
    }

    public String getUserEmail() {
        return authResponse != null ? authResponse.email() : null;
    }
}
