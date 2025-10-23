package com.municipal.reservationsfx.backend.controllers;

import com.municipal.reservationsfx.backend.responses.AuthResponse;
import com.municipal.reservationsfx.backend.services.AuthService;

/**
 * Bridges UI login flows with the backend authentication endpoints.
 */
public class AuthController {

    private final AuthService authService;

    public AuthController() {
        this(new AuthService());
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public AuthResponse authenticateWithAzure(String accessToken) {
        return authService.loginWithAzureToken(accessToken);
    }
}
