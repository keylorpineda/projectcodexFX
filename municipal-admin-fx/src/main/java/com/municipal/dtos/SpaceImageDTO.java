package com.municipal.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;

public record SpaceImageDTO(
        @JsonProperty("id") Long id,
        @JsonProperty("spaceId") Long spaceId,
        @JsonProperty("imageUrl") String imageUrl,
        @JsonProperty("description") String description,
        @JsonProperty("active") Boolean active,
        @JsonProperty("displayOrder") Integer displayOrder,
        @JsonProperty("uploadedAt")
        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        LocalDateTime uploadedAt) {
}
