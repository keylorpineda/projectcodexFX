package finalprojectprogramming.project.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import jakarta.validation.constraints.FutureOrPresent;
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
public class ReservationDTO {

    private Long id;

    @NotNull
    @Positive
    private Long userId;

    @NotNull
    @Positive
    private Long spaceId;

    @FutureOrPresent
    @NotNull
    private LocalDateTime startTime;

    @FutureOrPresent
    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private ReservationStatus status;

    @NotBlank
    @Size(max = 255)
    private String qrCode;

    @PastOrPresent
    private LocalDateTime canceledAt;

    @PastOrPresent
    private LocalDateTime checkinAt;

    private String notes;

    @NotNull
    @Positive
    private Integer attendees;

    @Positive
    private Long approvedByUserId;

    private JsonNode weatherCheck;

    private String cancellationReason;

    @PastOrPresent
    private LocalDateTime createdAt;

    @PastOrPresent
    private LocalDateTime updatedAt;

    @Positive
    private Long ratingId;

    @NotNull
    @Builder.Default
    private List<Long> notificationIds = new ArrayList<>();
}