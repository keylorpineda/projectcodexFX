package com.municipal.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.municipal.ApiClient;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.ReservationCheckInRequest;
import com.municipal.exceptions.ApiClientException;
import com.municipal.responses.BinaryFileResponse;
import com.municipal.utils.JsonUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReservationController {
    private final ApiClient apiClient;
    private static final Pattern FILENAME_PATTERN = Pattern.compile("filename=\"?([^\";]+)\"?");

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

        // 🔍 DEBUG: Ver el JSON que se envía al backend
        System.out.println("🌐 JSON enviado al backend:");
        System.out.println(jsonBody);
        System.out.println("───────────────────────────────────────────");

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
        
        // 🔍 DEBUG: Ver el error exacto del backend
        System.err.println("❌ Error del backend (status " + response.statusCode() + "):");
        System.err.println(response.body());
        System.err.println("───────────────────────────────────────────");
        
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

    // ✅ MÉTODO PARA ELIMINACIÓN PERMANENTE - Elimina físicamente de la base de datos
    public void permanentlyDeleteReservation(Long id, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + id + "/permanent";

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

    // ✅ NUEVO MÉTODO - Cancelar reserva con motivo
    public ReservationDTO cancelReservation(Long reservationId, String reason, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + reservationId + "/cancel";
        
        // Crear el payload con el motivo de cancelación
        String jsonBody = reason != null && !reason.trim().isEmpty() 
            ? String.format("{\"reason\": \"%s\"}", reason.replace("\"", "\\\""))
            : "{}";
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
        }
        throw new ApiClientException(response.statusCode(), response.body());
    }

    // ✅ NUEVO MÉTODO - Aprobar reserva (cambia de PENDING a CONFIRMED)
    public ReservationDTO approveReservation(Long reservationId, Long approverUserId, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/" + reservationId + "/approve";
        
        // Crear el payload con el ID del aprobador
        String jsonBody = String.format("{\"approverUserId\": %d}", approverUserId);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + token)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

        HttpResponse<String> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return JsonUtils.fromJson(response.body(), ReservationDTO.class);
        }
        throw new ApiClientException(response.statusCode(), response.body());
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

    public BinaryFileResponse exportAllReservationsExcel(String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/export";
        return downloadExcel(url, token);
    }

    public BinaryFileResponse exportUserReservationsExcel(Long userId, String token) throws Exception {
        String url = apiClient.getBaseUrl() + "/api/reservations/user/" + userId + "/export";
        return downloadExcel(url, token);
    }

    private BinaryFileResponse downloadExcel(String url, String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .GET()
                .build();

        HttpResponse<byte[]> response = apiClient.getHttpClient().send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            String fileName = response.headers()
                    .firstValue("Content-Disposition")
                    .flatMap(this::extractFileName)
                    .orElse("reservas.xlsx");
            return new BinaryFileResponse(response.body(), fileName);
        }
        throw new ApiClientException(response.statusCode(), new String(response.body()));
    }

    private Optional<String> extractFileName(String header) {
        if (header == null || header.isBlank()) {
            return Optional.empty();
        }
        Matcher matcher = FILENAME_PATTERN.matcher(header);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(1));
        }
        return Optional.empty();
    }
}
