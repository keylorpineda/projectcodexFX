package finalprojectprogramming.project.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import finalprojectprogramming.project.models.AuditLog;
import finalprojectprogramming.project.models.Notification;
import finalprojectprogramming.project.models.Rating;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.ReservationAttendee;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.NotificationStatus;
import finalprojectprogramming.project.models.enums.NotificationType;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.models.enums.UserRole;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@Transactional
class RepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceScheduleRepository spaceScheduleRepository;

    @Autowired
    private SpaceImageRepository spaceImageRepository;

    @Autowired
    private SettingRepository settingRepository;

    @Test
    void userRepository_findByEmail_returnsMatchingUser() {
        User user = userRepository.save(User.builder()
                .role(UserRole.ADMIN)
                .email("alice@example.com")
                .name("Alice")
                .build());

        Optional<User> result = userRepository.findByEmail("alice@example.com");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(user.getId());
    }

    @Test
    void userRepository_findByEmail_whenMissing_returnsEmptyOptional() {
        assertThat(userRepository.findByEmail("missing@example.com")).isEmpty();
    }

    @Test
    void notificationRepository_returnsNotificationsByReservationId() {
        Reservation reservation = persistReservationGraph("notif");

        List<Notification> notifications = notificationRepository.findByReservationId(reservation.getId());

        assertThat(notifications)
                .hasSize(1)
                .first()
                .satisfies(notification -> {
                    assertThat(notification.getType()).isEqualTo(NotificationType.CONFIRMATION);
                    assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
                    assertThat(notification.getSentTo()).isEqualTo("notif@example.com");
                });

        assertThat(notificationRepository.findByReservationId(-1L)).isEmpty();
    }

    @Test
    void ratingRepository_returnsRatingByReservationId() {
        Reservation reservation = persistReservationGraph("rating");

        Optional<Rating> rating = ratingRepository.findByReservationId(reservation.getId());

        assertThat(rating).isPresent();
        assertThat(rating.orElseThrow().getScore()).isEqualTo(5);
        assertThat(rating.orElseThrow().getReservation().getId()).isEqualTo(reservation.getId());
    }

    @Test
    void auditLogRepository_returnsLogsByUserId() {
        User user = userRepository.save(User.builder()
                .role(UserRole.SUPERVISOR)
                .email("audit@example.com")
                .name("Auditor")
                .build());

        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action("RESERVATION_CREATED")
                .build());
        auditLogRepository.save(AuditLog.builder()
                .user(user)
                .action("RESERVATION_UPDATED")
                .build());

        List<AuditLog> logs = auditLogRepository.findByUserId(user.getId());

        assertThat(logs)
                .extracting(AuditLog::getAction)
                .containsExactlyInAnyOrder("RESERVATION_CREATED", "RESERVATION_UPDATED");
    }

    @Test
    void spaceScheduleRepository_supportsDerivedQueries() {
        Space space = spaceRepository.save(Space.builder()
                .name("Sala Principal")
                .type(SpaceType.SALA)
                .capacity(20)
                .build());

        spaceScheduleRepository.save(SpaceSchedule.builder()
                .space(space)
                .dayOfWeek(DayOfWeek.MONDAY)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(18, 0))
                .build());
        spaceScheduleRepository.save(SpaceSchedule.builder()
                .space(space)
                .dayOfWeek(DayOfWeek.TUESDAY)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(17, 0))
                .build());

        List<SpaceSchedule> schedules = spaceScheduleRepository.findBySpaceId(space.getId());
        Optional<SpaceSchedule> monday = spaceScheduleRepository.findBySpaceIdAndDayOfWeek(space.getId(), DayOfWeek.MONDAY);
        Optional<SpaceSchedule> friday = spaceScheduleRepository.findBySpaceIdAndDayOfWeek(space.getId(), DayOfWeek.FRIDAY);

        assertThat(schedules).hasSize(2);
        assertThat(monday).isPresent();
        assertThat(friday).isEmpty();
    }

    @Test
    void spaceImageRepository_returnsImagesForSpace() {
        Space space = spaceRepository.save(Space.builder()
                .name("Patio Central")
                .type(SpaceType.CANCHA)
                .capacity(30)
                .build());

        spaceImageRepository.save(SpaceImage.builder()
                .space(space)
                .imageUrl("https://example.com/image-1.jpg")
                .description("Principal")
                .build());
        spaceImageRepository.save(SpaceImage.builder()
                .space(space)
                .imageUrl("https://example.com/image-2.jpg")
                .description("Alterna")
                .build());

        List<SpaceImage> images = spaceImageRepository.findBySpaceId(space.getId());

        assertThat(images)
                .hasSize(2)
                .extracting(SpaceImage::getImageUrl)
                .contains("https://example.com/image-1.jpg", "https://example.com/image-2.jpg");
    }

    @Test
    void settingRepository_findByKey_returnsConfigurationValue() {
        settingRepository.save(Setting.builder()
                .key("reservation.buffer.hours")
                .value("2")
                .description("Horas de amortiguamiento")
                .build());

        Optional<Setting> setting = settingRepository.findByKey("reservation.buffer.hours");

        assertThat(setting).isPresent();
        assertThat(setting.orElseThrow().getValue()).isEqualTo("2");
        assertThat(settingRepository.findByKey("reservation.unknown")).isEmpty();
    }

    private Reservation persistReservationGraph(String suffix) {
        User user = userRepository.save(User.builder()
                .role(UserRole.USER)
                .email(suffix + "@example.com")
                .name("User " + suffix)
                .build());
        Space space = spaceRepository.save(Space.builder()
                .name("Space " + suffix)
                .type(SpaceType.AUDITORIO)
                .capacity(40)
                .build());

        Reservation reservation = Reservation.builder()
                .user(user)
                .space(space)
                .startTime(LocalDateTime.of(2024, 7, 1, 10, 0))
                .endTime(LocalDateTime.of(2024, 7, 1, 12, 0))
                .status(ReservationStatus.CONFIRMED)
                .qrCode("QR-" + suffix)
                .build();

        Notification notification = Notification.builder()
                .reservation(reservation)
                .type(NotificationType.CONFIRMATION)
                .status(NotificationStatus.SENT)
                .sentTo(suffix + "@example.com")
                .sentAt(LocalDateTime.of(2024, 6, 1, 9, 0))
                .build();
        reservation.getNotifications().add(notification);

        ReservationAttendee attendee = ReservationAttendee.builder()
                .reservation(reservation)
                .idNumber("ID-" + suffix)
                .firstName("Invitado")
                .lastName(suffix.toUpperCase())
                .checkInAt(LocalDateTime.of(2024, 7, 1, 10, 15))
                .build();
        reservation.getAttendeeRecords().add(attendee);

        Rating rating = Rating.builder()
                .reservation(reservation)
                .user(user)
                .space(space)
                .score(5)
                .comment("Excelente")
                .build();
        reservation.setRating(rating);

        Reservation saved = reservationRepository.save(reservation);
        reservationRepository.flush();
        return saved;
    }
}
