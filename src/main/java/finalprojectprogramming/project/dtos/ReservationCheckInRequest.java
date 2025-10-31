package finalprojectprogramming.project.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCheckInRequest {

    @NotBlank
    private String qrCode;

    @NotBlank
    private String attendeeIdNumber;

    @NotBlank
    private String attendeeFirstName;

    @NotBlank
    private String attendeeLastName;
}