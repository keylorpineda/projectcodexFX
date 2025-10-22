package finalprojectprogramming.project.dtos;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
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
public class AuditLogDTO {

    private Long id;

    @Positive
    private Long userId;

    @NotBlank
    @Size(max = 100)
    private String action;

    @Size(max = 255)
    private String entityId;

    private JsonNode details;

    @PastOrPresent
    private LocalDateTime timestamp;
}
