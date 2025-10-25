package com.municipal.session;

import com.municipal.responses.AuthResponse;

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

    public String getUserRole() {
        if (authResponse == null || authResponse.role() == null || authResponse.role().isBlank()) {
            return "USER";
        }
        return authResponse.role();
    }

    public Long getUserId() {
        return authResponse != null ? authResponse.userId() : null;
    }

    public void updateProfileInfo(String name, String email) {
        if (authResponse == null) {
            return;
        }
        authResponse = new AuthResponse(
                authResponse.token(),
                authResponse.tokenType(),
                authResponse.expiresAt(),
                authResponse.userId(),
                authResponse.role(),
                email != null && !email.isBlank() ? email : authResponse.email(),
                name != null && !name.isBlank() ? name : authResponse.name(),
                authResponse.profileComplete(),
                authResponse.newUser());
    }
}
