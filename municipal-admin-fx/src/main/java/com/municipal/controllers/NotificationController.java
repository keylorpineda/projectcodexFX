package com.municipal.controllers;

import com.municipal.ApiClient;
import com.municipal.exceptions.ApiClientException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NotificationController {
    
    private final ApiClient apiClient;
    
    public NotificationController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }
    
    /**
     * Envía un email personalizado relacionado con una reserva
     * @param reservationId ID de la reserva
     * @param subject Asunto del email
     * @param message Mensaje del email
     * @param token Token de autenticación
     * @throws Exception si ocurre un error
     */
    public void sendCustomEmail(Long reservationId, String subject, String message, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/notifications/send-custom-email";
        
        // Escapar caracteres especiales en JSON
        String jsonSubject = subject.replace("\"", "\\\"").replace("\n", "\\n");
        String jsonMessage = message.replace("\"", "\\\"").replace("\n", "\\n");
        
        String jsonBody = String.format(
            "{\"reservationId\": %d, \"subject\": \"%s\", \"message\": \"%s\"}",
            reservationId, jsonSubject, jsonMessage
        );
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new ApiClientException(response.statusCode(), response.body());
        }
    }
}
