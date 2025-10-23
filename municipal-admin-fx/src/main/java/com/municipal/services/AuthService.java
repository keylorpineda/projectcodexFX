package com.municipal.services;

import com.municipal.ApiClient;
import com.municipal.requests.AzureLoginRequest;
import com.municipal.responses.AuthResponse;

/**
 * Service responsible for delegating authentication calls to the backend API.
 */
public class AuthService {

    private final ApiClient apiClient;

    public AuthService() {
        this(new ApiClient());
    }

    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AuthResponse loginWithAzureToken(String accessToken) {
        AzureLoginRequest request = new AzureLoginRequest(accessToken);
        return apiClient.post("/api/auth/azure-login", request, AuthResponse.class);
    }
}
