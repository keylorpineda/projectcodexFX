package com.municipal.controllers;

import com.municipal.dtos.SpaceImageDTO;
import com.municipal.services.SpaceImageService;
import java.nio.file.Path;
import java.util.List;

public class SpaceImageController {

    private final SpaceImageService spaceImageService;

    public SpaceImageController() {
        this(new SpaceImageService());
    }

    public SpaceImageController(SpaceImageService spaceImageService) {
        this.spaceImageService = spaceImageService;
    }

    public List<SpaceImageDTO> loadImages(Long spaceId, String bearerToken) {
        return spaceImageService.findBySpace(spaceId, bearerToken);
    }

    public SpaceImageDTO uploadImage(Long spaceId, Path file, String description, Integer displayOrder, Boolean active,
            String bearerToken) {
        return spaceImageService.upload(spaceId, file, description, displayOrder, active, bearerToken);
    }

    public void deleteImage(Long imageId, String bearerToken) {
        spaceImageService.delete(imageId, bearerToken);
    }

    public String resolveImageUrl(SpaceImageDTO image) {
        return image != null ? spaceImageService.resolveImageUrl(image.imageUrl()) : null;
    }
}
