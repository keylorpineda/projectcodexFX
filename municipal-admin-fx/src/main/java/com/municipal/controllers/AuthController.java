package com.municipal.controllers;

import com.municipal.responses.AuthResponse;
import com.municipal.services.AuthService;

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
