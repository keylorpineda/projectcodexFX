package finalprojectprogramming.project.dtos;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.test.context.ActiveProfiles;

@JsonTest
@ActiveProfiles("test")
class ReservationDtoJsonTest {

    @Autowired
    private JacksonTester<ReservationDTO> json;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializeMatchesSnapshot() throws Exception {
        ReservationDTO dto = sampleDto();
        assertThat(json.write(dto)).isEqualToJson("/__snapshots__/reservation_dto.json");
    }

    @Test
    void deserializePopulatesComplexCollections() throws Exception {
        ReservationDTO dto = json.readObject("/__snapshots__/reservation_dto.json");

        assertThat(dto.getId()).isEqualTo(42L);
        assertThat(dto.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(dto.getNotificationIds()).containsExactly(5L, 6L);
        assertThat(dto.getAttendeeRecords()).hasSize(1);
        ReservationAttendeeDTO attendee = dto.getAttendeeRecords().getFirst();
        assertThat(attendee.getFirstName()).isEqualTo("Ana");
        assertThat(attendee.getIdNumber()).isEqualTo("12345678");
    }

    private ReservationDTO sampleDto() throws Exception {
        JsonNode weather = objectMapper.readTree("{\"forecast\":\"sunny\",\"temperature\":27}");
        return ReservationDTO.builder()
                .id(42L)
                .userId(7L)
                .spaceId(3L)
                .startTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .endTime(LocalDateTime.of(2024, 1, 15, 11, 30))
                .status(ReservationStatus.CONFIRMED)
                .qrCode("QR-XYZ-42")
                .canceledAt(LocalDateTime.of(2024, 1, 10, 9, 0))
                .checkinAt(LocalDateTime.of(2024, 1, 15, 9, 45))
                .notes("Presentar identificación oficial")
                .attendees(4)
                .approvedByUserId(2L)
                .weatherCheck(weather)
                .cancellationReason("Reprogramada")
                .createdAt(LocalDateTime.of(2024, 1, 1, 8, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 8, 0))
                .ratingId(99L)
                .notificationIds(List.of(5L, 6L))
                .attendeeRecords(List.of(ReservationAttendeeDTO.builder()
                        .id(1L)
                        .reservationId(42L)
                        .idNumber("12345678")
                        .firstName("Ana")
                        .lastName("Pérez")
                        .checkInAt(LocalDateTime.of(2024, 1, 15, 9, 50))
                        .build()))
                .build();
    }
}
