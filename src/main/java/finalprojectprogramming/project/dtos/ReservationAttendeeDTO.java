package finalprojectprogramming.project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
public class ReservationAttendeeDTO {

    private Long id;

    @NotNull
    private Long reservationId;

    @NotBlank
    private String idNumber;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @PastOrPresent
    private LocalDateTime checkInAt;
}