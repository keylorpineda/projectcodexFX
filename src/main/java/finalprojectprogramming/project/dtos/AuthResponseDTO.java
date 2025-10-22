package finalprojectprogramming.project.dtos;

import java.time.Instant;

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

public class AuthResponseDTO {

    private String token;
    private String tokenType;
    private Instant expiresAt;
    private Long userId;
    private String role;
    private String email;
    private String name;
    private boolean profileComplete;
    private boolean newUser;
}