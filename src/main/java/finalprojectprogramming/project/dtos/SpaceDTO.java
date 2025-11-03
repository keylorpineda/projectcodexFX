package finalprojectprogramming.project.dtos;

import finalprojectprogramming.project.models.enums.SpaceType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class SpaceDTO {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private SpaceType type;

    @NotNull
    @Positive
    private Integer capacity;

    private String description;

    @Size(max = 255)
    private String location;

   @Builder.Default
    private Boolean active = Boolean.TRUE;

    @Positive
    private Integer maxReservationDuration;

    @Builder.Default
    private Boolean requiresApproval = Boolean.FALSE;

    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private Double averageRating;

    private Long reviewCount;

    @PastOrPresent
    private LocalDateTime createdAt;

    @PastOrPresent
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<Long> imageIds = new ArrayList<>();

    @Builder.Default
    private List<Long> scheduleIds = new ArrayList<>();

    @Builder.Default
    private List<Long> reservationIds = new ArrayList<>();
}