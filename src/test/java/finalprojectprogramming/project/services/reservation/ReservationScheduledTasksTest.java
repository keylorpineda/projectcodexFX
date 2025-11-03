package finalprojectprogramming.project.services.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationScheduledTasksTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Test
    void markExpiredReservationsUpdatesStatusAndPersistsChanges() {
        Reservation expired = new Reservation();
        expired.setId(1L);
        expired.setStatus(ReservationStatus.CONFIRMED);
        expired.setStartTime(LocalDateTime.now().minusHours(2));
        Reservation pending = new Reservation();
        pending.setId(2L);
        pending.setStatus(ReservationStatus.PENDING);
        pending.setStartTime(LocalDateTime.now().minusHours(1));
        Reservation fresh = new Reservation();
        fresh.setId(3L);
        fresh.setStatus(ReservationStatus.CONFIRMED);
        fresh.setStartTime(LocalDateTime.now().plusHours(1));

    when(reservationRepository.findAll()).thenReturn(List.of(expired, pending, fresh));

        ReservationScheduledTasks tasks = new ReservationScheduledTasks(reservationRepository);
        tasks.markExpiredReservationsAsNoShow();

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        // Solo CONFIRMED con ventana de check-in expirada debe marcarse como NO_SHOW
        verify(reservationRepository, times(1)).save(captor.capture());
        Reservation saved = captor.getValue();
        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void markExpiredReservationsDoesNothingWhenNoCandidates() {
        Reservation upcoming = new Reservation();
        upcoming.setId(5L);
        upcoming.setStatus(ReservationStatus.CONFIRMED);
        upcoming.setStartTime(LocalDateTime.now().plusHours(5));
        when(reservationRepository.findAll()).thenReturn(List.of(upcoming));

        ReservationScheduledTasks tasks = new ReservationScheduledTasks(reservationRepository);
        tasks.markExpiredReservationsAsNoShow();

        verify(reservationRepository).findAll();
        verify(reservationRepository, times(0)).save(any(Reservation.class));
    }

    @Test
    void markExpiredReservationsSwallowsRepositoryErrors() {
        when(reservationRepository.findAll()).thenThrow(new IllegalStateException("boom"));
        ReservationScheduledTasks tasks = new ReservationScheduledTasks(reservationRepository);

        assertThatNoException().isThrownBy(tasks::markExpiredReservationsAsNoShow);
    }
}
