package finalprojectprogramming.project.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import finalprojectprogramming.project.models.enums.UserRole;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;

@JsonTest
@ActiveProfiles("test")
class UserOutputDtoJsonTest {

    @Autowired
    private JacksonTester<UserOutputDTO> json;

    @Test
    void serializeMatchesSnapshot() throws Exception {
        UserOutputDTO dto = sampleUser();
        assertThat(json.write(dto)).isEqualToJson("/__snapshots__/user_output_dto.json");
    }

    @Test
    void deserializeRestoresListsAndMetadata() throws Exception {
        UserOutputDTO dto = json.readObject("/__snapshots__/user_output_dto.json");

        assertThat(dto.getId()).isEqualTo(7L);
        assertThat(dto.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(dto.getReservationIds()).containsExactly(10L, 11L);
        assertThat(dto.getApprovedReservationIds()).containsExactly(20L);
        assertThat(dto.getAuditLogIds()).containsExactly(30L, 31L, 32L);
        assertThat(dto.getDeletedAt()).isNull();
    }

    private UserOutputDTO sampleUser() {
        return UserOutputDTO.builder()
                .id(7L)
                .role(UserRole.ADMIN)
                .name("Carolina Rojas")
                .email("carolina@example.com")
                .active(true)
                .lastLoginAt(LocalDateTime.of(2024, 3, 10, 8, 45))
                .createdAt(LocalDateTime.of(2023, 12, 1, 9, 30))
                .updatedAt(LocalDateTime.of(2024, 2, 5, 14, 15))
                .reservationIds(List.of(10L, 11L))
                .approvedReservationIds(List.of(20L))
                .auditLogIds(List.of(30L, 31L, 32L))
                .build();
    }
}
