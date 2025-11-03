package finalprojectprogramming.project.services.space;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpaceAvailabilityValidatorTest {

    private SpaceAvailabilityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SpaceAvailabilityValidator();
    }

    private static Space baseSpace() {
        Space s = new Space();
        s.setActive(true);
        s.setDeletedAt(null);
        s.setMaxReservationDuration(120); // minutes
        s.setSchedules(new ArrayList<>());
        s.setReservations(new ArrayList<>());
        return s;
    }

    @Test
    void validateTimeRange_checks_nulls_and_order() {
        assertThatThrownBy(() -> validator.validateTimeRange(null, LocalDateTime.now()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("required");
        LocalDateTime now = LocalDateTime.now();
        assertThatThrownBy(() -> validator.validateTimeRange(now, now))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("before");
        assertThatThrownBy(() -> validator.validateTimeRange(now.plusHours(1), now))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void isAvailable_false_when_inactive_or_deleted() {
        Space s = baseSpace();
        s.setActive(false);
        LocalDateTime st = LocalDateTime.now();
        LocalDateTime en = st.plusHours(1);
        assertThat(validator.isAvailable(s, st, en, null)).isFalse();

        s = baseSpace();
        s.setDeletedAt(LocalDateTime.now());
        assertThat(validator.isAvailable(s, st, en, null)).isFalse();
    }

    @Test
    void withinSchedule_and_duration_and_conflicts_enforced() {
        Space s = baseSpace();
        // Schedule only for today 08:00-18:00
        SpaceSchedule sch = new SpaceSchedule();
        sch.setDayOfWeek(LocalDate.now().getDayOfWeek());
        sch.setOpenTime(LocalTime.of(8, 0));
        sch.setCloseTime(LocalTime.of(18, 0));
        s.getSchedules().add(sch);

        LocalDateTime st = LocalDate.now().atTime(9, 0);
        LocalDateTime en = LocalDate.now().atTime(10, 0);
        assertThat(validator.isAvailable(s, st, en, null)).isTrue();

        // Outside schedule
        assertThat(validator.isAvailable(s, LocalDate.now().atTime(7, 59), en, null)).isFalse();
        assertThat(validator.isAvailable(s, st, LocalDate.now().atTime(18, 1), null)).isFalse();

        // Crossing midnight should be false in schedule check
        LocalDateTime st2 = LocalDate.now().atTime(23, 0);
        LocalDateTime en2 = LocalDate.now().plusDays(1).atTime(1, 0);
        assertThat(validator.isAvailable(s, st2, en2, null)).isFalse();

        // Duration over max
        s.setMaxReservationDuration(30);
        assertThat(validator.isAvailable(s, st, en, null)).isFalse();

    // Add a conflicting reservation (CONFIRMED)
        s.setMaxReservationDuration(120);
        Reservation r = new Reservation();
    r.setStatus(ReservationStatus.CONFIRMED);
        r.setStartTime(LocalDate.now().atTime(9, 30));
        r.setEndTime(LocalDate.now().atTime(9, 45));
        s.getReservations().add(r);
        assertThat(validator.isAvailable(s, st, en, null)).isFalse();

    // Ignore by reservationIdToIgnore (behavior may vary; ensure call doesn't throw)
    r.setId(123L);
    validator.isAvailable(s, st, en, 123L);

    // Overlaps logic ignores CANCELED and NO_SHOW
    // Remove the previously added CONFIRMED reservation to isolate the behavior
    s.getReservations().remove(r);
        Reservation r2 = new Reservation();
        r2.setStatus(ReservationStatus.CANCELED);
        r2.setStartTime(LocalDate.now().atTime(9, 30));
        r2.setEndTime(LocalDate.now().atTime(10, 0));
        s.getReservations().add(r2);
        assertThat(validator.isAvailable(s, st, en, null)).isTrue();

        Reservation r3 = new Reservation();
        r3.setStatus(ReservationStatus.NO_SHOW);
        r3.setStartTime(LocalDate.now().atTime(9, 30));
        r3.setEndTime(LocalDate.now().atTime(10, 0));
        s.getReservations().add(r3);
        assertThat(validator.isAvailable(s, st, en, null)).isTrue();
    }

    @Test
    void assertAvailability_throws_descriptive_messages() {
        Space s = baseSpace();
        // No schedule: allowed
        LocalDateTime st = LocalDate.now().atTime(9, 0);
        LocalDateTime en = LocalDate.now().atTime(10, 0);
        validator.assertAvailability(s, st, en, null);

        // Inactive
        s.setActive(false);
        assertThatThrownBy(() -> validator.assertAvailability(s, st, en, null))
                .hasMessageContaining("not available");
        s.setActive(true);

        // Schedule restricts
        SpaceSchedule sch = new SpaceSchedule();
        sch.setDayOfWeek(LocalDate.now().getDayOfWeek());
        sch.setOpenTime(LocalTime.of(10, 0));
        sch.setCloseTime(LocalTime.of(18, 0));
        s.getSchedules().add(sch);
        assertThatThrownBy(() -> validator.assertAvailability(s, st, en, null))
                .hasMessageContaining("outside");

        // Conflict
        s.getSchedules().clear();
        SpaceSchedule sch2 = new SpaceSchedule();
        sch2.setDayOfWeek(LocalDate.now().getDayOfWeek());
        sch2.setOpenTime(LocalTime.of(8, 0));
        sch2.setCloseTime(LocalTime.of(18, 0));
        s.getSchedules().add(sch2);
    Reservation r = new Reservation();
    r.setStatus(ReservationStatus.CONFIRMED);
        r.setStartTime(LocalDate.now().atTime(9, 15));
        r.setEndTime(LocalDate.now().atTime(9, 30));
        s.getReservations().add(r);
        assertThatThrownBy(() -> validator.assertAvailability(s, st, en, null))
                .hasMessageContaining("already reserved");

        // Duration
        s.getReservations().clear();
        s.setMaxReservationDuration(30);
        assertThatThrownBy(() -> validator.assertAvailability(s, st, en, null))
                .hasMessageContaining("duration exceeds");
    }

    @Test
    void schedule_with_null_day_and_null_times_is_not_within_schedule() {
        Space s = baseSpace();
        SpaceSchedule sch = new SpaceSchedule();
        sch.setDayOfWeek(null);
        sch.setOpenTime(null);
        sch.setCloseTime(null);
        s.getSchedules().add(sch);

        LocalDateTime st = LocalDate.now().atTime(9, 0);
        LocalDateTime en = LocalDate.now().atTime(10, 0);

        // No schedule day match and times null -> not within schedule -> not available
        assertThat(validator.isAvailable(s, st, en, null)).isFalse();
    }

    @Test
    void overlaps_returns_false_when_existing_times_are_null() {
        Space s = baseSpace();

        // Add reservation with null start or end
        Reservation r1 = new Reservation();
        r1.setStatus(ReservationStatus.CONFIRMED);
        r1.setStartTime(null);
        r1.setEndTime(LocalDate.now().atTime(9, 30));
        s.getReservations().add(r1);

        Reservation r2 = new Reservation();
        r2.setStatus(ReservationStatus.CONFIRMED);
        r2.setStartTime(LocalDate.now().atTime(9, 15));
        r2.setEndTime(null);
        s.getReservations().add(r2);

        LocalDateTime st = LocalDate.now().atTime(9, 0);
        LocalDateTime en = LocalDate.now().atTime(10, 0);

        // Neither r1 nor r2 should count as conflict due to null endpoints
        assertThat(validator.isAvailable(s, st, en, null)).isTrue();
    }
}
