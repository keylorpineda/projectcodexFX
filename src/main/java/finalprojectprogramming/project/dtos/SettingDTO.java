package finalprojectprogramming.project.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
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
public class SettingDTO {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String key; // unique

    @NotBlank
    @Size(max = 255)
    private String value;

    private String description;

    @PastOrPresent
    private LocalDateTime updatedAt;
}