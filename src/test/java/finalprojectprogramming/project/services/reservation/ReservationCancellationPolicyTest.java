package finalprojectprogramming.project.services.reservation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.repositories.SettingRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ReservationCancellationPolicyTest {

    @Mock
    private SettingRepository settingRepository;

    private ReservationCancellationPolicy policy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        policy = new ReservationCancellationPolicy(settingRepository);
    }

    @Test
    void assertCancellationAllowed_respectsConfiguration() {
        Reservation reservation = Reservation.builder()
                .startTime(LocalDateTime.now().plusHours(5))
                .build();
        when(settingRepository.findByKey(ReservationCancellationPolicy.MIN_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("2").build()));
        when(settingRepository.findByKey(ReservationCancellationPolicy.MAX_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("48").build()));

        policy.assertCancellationAllowed(reservation);
    }

    @Test
    void assertCancellationAllowed_throwsWhenOutsideWindow() {
        Reservation reservation = Reservation.builder()
                .startTime(LocalDateTime.now().plusHours(1))
                .build();
        when(settingRepository.findByKey(ReservationCancellationPolicy.MIN_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("4").build()));

        assertThatThrownBy(() -> policy.assertCancellationAllowed(reservation))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class);
    }

    @Test
    void assertCancellationAllowed_throwsWhenAfterMax() {
        Reservation reservation = Reservation.builder()
                .startTime(LocalDateTime.now().plusHours(100))
                .build();
        when(settingRepository.findByKey(ReservationCancellationPolicy.MAX_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("24").build()));

        assertThatThrownBy(() -> policy.assertCancellationAllowed(reservation))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class);
    }

    @Test
    void assertCancellationAllowed_throwsWhenStartTimeMissingOrPast() {
        // Missing start time
        Reservation missing = Reservation.builder().startTime(null).build();
        assertThatThrownBy(() -> policy.assertCancellationAllowed(missing))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("start time is required");

        // Past start time
        Reservation past = Reservation.builder().startTime(LocalDateTime.now().minusHours(1)).build();
        assertThatThrownBy(() -> policy.assertCancellationAllowed(past))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("cannot be canceled");
    }

    @Test
    void assertCancellationAllowed_throwsWhenSettingsHaveInvalidNumbers() {
        Reservation reservation = Reservation.builder()
                .startTime(LocalDateTime.now().plusHours(5))
                .build();

        // Non-numeric value
        when(settingRepository.findByKey(ReservationCancellationPolicy.MIN_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("abc").build()));
        assertThatThrownBy(() -> policy.assertCancellationAllowed(reservation))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("valid number");

        // Negative value
        when(settingRepository.findByKey(ReservationCancellationPolicy.MIN_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("-5").build()));
        assertThatThrownBy(() -> policy.assertCancellationAllowed(reservation))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("zero or a positive");
    }

    @Test
    void assertCancellationAllowed_throwsWhenMinGreaterThanMax() {
        Reservation reservation = Reservation.builder()
                .startTime(LocalDateTime.now().plusHours(10))
                .build();
        when(settingRepository.findByKey(ReservationCancellationPolicy.MIN_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("12").build()));
        when(settingRepository.findByKey(ReservationCancellationPolicy.MAX_HOURS_KEY))
                .thenReturn(Optional.of(Setting.builder().value("5").build()));

        assertThatThrownBy(() -> policy.assertCancellationAllowed(reservation))
                .isInstanceOf(finalprojectprogramming.project.exceptions.BusinessRuleException.class)
                .hasMessageContaining("minimum is greater than maximum");
    }
}
