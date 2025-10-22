package finalprojectprogramming.project.dtos;

import finalprojectprogramming.project.models.enums.UserRole;
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
public class UserOutputDTO {

    private Long id;
    private UserRole role;
    private String name;
    private String email;
    private Boolean active;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @Builder.Default
    private List<Long> reservationIds = new ArrayList<>();

    @Builder.Default
    private List<Long> approvedReservationIds = new ArrayList<>();

    @Builder.Default
    private List<Long> auditLogIds = new ArrayList<>();
}