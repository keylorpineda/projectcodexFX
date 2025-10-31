package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReservationCheckInRequest(
        @JsonProperty("qrCode") String qrCode,
        @JsonProperty("attendeeIdNumber") String attendeeIdNumber,
        @JsonProperty("attendeeFirstName") String attendeeFirstName,
        @JsonProperty("attendeeLastName") String attendeeLastName) {
}