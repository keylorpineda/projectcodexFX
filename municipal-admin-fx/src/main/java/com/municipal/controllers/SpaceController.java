package com.municipal.controllers;

import com.municipal.dtos.SpaceDTO;
import com.municipal.services.SpaceService;

import java.util.List;

/**
 * Simple facade to request data from {@link SpaceService} while keeping UI
 * controllers unaware of HTTP details.
 */
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController() {
        this(new SpaceService());
    }

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    public List<SpaceDTO> loadSpaces(String bearerToken) {
        return spaceService.findAll(bearerToken);
    }
}
