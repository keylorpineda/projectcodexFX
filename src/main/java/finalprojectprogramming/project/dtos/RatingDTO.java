package finalprojectprogramming.project.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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

    @NotNull
    @Positive
    private Long reservationId;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer score;

    private String comment;

    @PastOrPresent
    private LocalDateTime createdAt;
}