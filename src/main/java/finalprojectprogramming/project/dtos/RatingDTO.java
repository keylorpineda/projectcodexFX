package finalprojectprogramming.project.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingDTO {

    private Long id;

    @NotNull(message = "La reservación es requerida")
    @Positive
    private Long reservationId;

    @Positive
    private Long userId;

    private String userName;

    private String spaceName;

    @NotNull(message = "La calificación es requerida")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer score;

    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String comment;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @PastOrPresent
    private LocalDateTime createdAt;

    private Integer helpfulCount;

    private Boolean visible;
}