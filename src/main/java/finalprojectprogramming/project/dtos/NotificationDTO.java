package finalprojectprogramming.project.dtos;

import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
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
public class NotificationDTO {

    private Long id;

    @Positive
    private Long reservationId;

    @NotNull
    private NotificationType type;

    @NotBlank
    @Email
    @Size(max = 255)
    private String sentTo;

    private String messageContent;

    @PastOrPresent
    private LocalDateTime sentAt;

    @NotNull
    private NotificationStatus status;
}