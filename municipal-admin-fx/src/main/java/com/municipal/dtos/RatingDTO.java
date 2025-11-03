package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Rating/Review entity.
 * Consolidated DTO that handles both ratings (1-5 score) and reviews (comments).
 */
public record RatingDTO(
        Long id,
        Long reservationId,
        Long userId,
        String userName,
        Long spaceId,
        String spaceName,
        Integer score,
        String comment,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt,
        Integer helpfulCount,
        Boolean visible
) {}
