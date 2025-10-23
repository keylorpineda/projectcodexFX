package com.municipal.controllers;

import com.municipal.dtos.ReservationDTO;
import com.municipal.services.ReservationService;

import java.util.List;

/**
 * Facade used by UI controllers to interact with reservation endpoints.
 */
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController() {
        this(new ReservationService());
    }

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public List<ReservationDTO> loadReservations(String bearerToken) {
        return reservationService.findAll(bearerToken);
    }
}
