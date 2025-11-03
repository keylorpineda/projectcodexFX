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
}
