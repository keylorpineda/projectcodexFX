package finalprojectprogramming.project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
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
public class SpaceImageDTO {

    private Long id;

    @NotNull
    @Positive
    private Long spaceId;

    @NotBlank
    @Size(max = 255)
    private String imageUrl;

    @Size(max = 255)
    private String description;

    @NotNull
    private Boolean active;

    @PositiveOrZero
    private Integer displayOrder;

    @PastOrPresent
    private LocalDateTime uploadedAt;
}