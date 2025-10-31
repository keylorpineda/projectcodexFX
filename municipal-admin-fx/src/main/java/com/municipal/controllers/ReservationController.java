package com.municipal.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.ReservationCheckInRequest;
import com.municipal.exceptions.ApiClientException;
import com.municipal.utils.JsonUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ReservationController {
    private final ApiClient apiClient;

    public ReservationController() {
        this(new ApiClient());
    }

    public ReservationController(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<ReservationDTO> getAllReservations(String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), new TypeReference<List<ReservationDTO>>() {});
        }
          throw new ApiClientException(response.statusCode(), response.body());
    }

    public ReservationDTO getReservationById(Long id, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + id;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
        }
          throw new ApiClientException(response.statusCode(), response.body());
    }

    public ReservationDTO createReservation(ReservationDTO reservation, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations";
        String jsonBody = JsonUtils.toJson(reservation);

        HttpRequest httpRequest = HttpRequest.newBuilder()

            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
       }
        throw new ApiClientException(response.statusCode(), response.body());
    }

    public ReservationDTO updateReservation(Long id, ReservationDTO reservation, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + id;
        String jsonBody = JsonUtils.toJson(reservation);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
        } 
        throw new ApiClientException(response.statusCode(), response.body());
    }

    public void deleteReservation(Long id, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + id;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .DELETE()
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
        throw new ApiClientException(response.statusCode(), response.body());   
        }
    }

    // ✅ NUEVO MÉTODO - Cancelar reserva
    public void cancelReservation(Long reservationId, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + reservationId + "/cancel";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 204) {
            throw new ApiClientException(response.statusCode(), response.body());
         }
    }

    public List<ReservationDTO> getReservationsByUserId(Long userId, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/user/" + userId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), new TypeReference<List<ReservationDTO>>() {});
        }
         throw new ApiClientException(response.statusCode(), response.body());
    }

    public List<ReservationDTO> getReservationsBySpaceId(Long spaceId, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/space/" + spaceId;
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .GET()
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), new TypeReference<List<ReservationDTO>>() {});
         }
         throw new ApiClientException(response.statusCode(), response.body());
    }
    public List<ReservationDTO> loadReservations(String token) throws Exception {
        return getAllReservations(token);
    }

     public ReservationDTO markCheckIn(Long reservationId, String token, ReservationCheckInRequest request) throws Exception {   String url = apiClient.getBaseUrl() + "/api/reservations/" + reservationId + "/check-in";

        String jsonBody = JsonUtils.toJson(request);
      HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
        }
        if (response.statusCode() == 204) {
            return getReservationById(reservationId, token);
        }
        throw new ApiClientException(response.statusCode(), response.body());
    }
}