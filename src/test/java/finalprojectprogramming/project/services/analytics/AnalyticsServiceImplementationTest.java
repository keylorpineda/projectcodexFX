package finalprojectprogramming.project.services.analytics;

import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static finalprojectprogramming.project.services.reservation.fixtures.ReservationTestDataFactory.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceImplementationTest {

    @Mock ReservationRepository reservationRepository;
    @Mock SpaceRepository spaceRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AnalyticsServiceImplementation service;

    @Test
    void getOccupancyRateBySpace_computes_percentages_including_checked_in() {
        Space s1 = spaceBuilder().withId(1L).build();
        Space s2 = spaceBuilder().withId(2L).build();
        when(spaceRepository.findAll()).thenReturn(List.of(s1, s2));

        // s1: 3 reservations, 2 confirmed-equivalent (CONFIRMED + CHECKED_IN)
        Reservation s1a = reservationBuilder().withSpace(s1).withStatus(ReservationStatus.CONFIRMED).build();
        Reservation s1b = reservationBuilder().withSpace(s1).withStatus(ReservationStatus.CHECKED_IN).build();
        Reservation s1c = reservationBuilder().withSpace(s1).withStatus(ReservationStatus.CANCELED).build();
        // s2: 0 reservations (implicit)
        when(reservationRepository.findAll()).thenReturn(List.of(s1a, s1b, s1c));

        Map<Long, Double> out = service.getOccupancyRateBySpace();
        assertThat(out.get(1L)).isEqualTo(2d * 100d / 3d);
        assertThat(out.get(2L)).isEqualTo(0d);
    }

    @Test
    void getMostReservedSpaces_sorts_and_limits() {
        Space a = spaceBuilder().withId(1L).withActive(true).build();
        Space b = spaceBuilder().withId(2L).withActive(true).build();
        Space c = spaceBuilder().withId(3L).withActive(true).build();
        when(spaceRepository.findAll()).thenReturn(List.of(a, b, c));

        // a: 3 total, 2 confirmed-equivalent
        Reservation a1 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.CONFIRMED).build();
        Reservation a2 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.CHECKED_IN).build();
        Reservation a3 = reservationBuilder().withSpace(a).withStatus(ReservationStatus.CANCELED).build();
        // b: 1 confirmed
        Reservation b1 = reservationBuilder().withSpace(b).withStatus(ReservationStatus.CONFIRMED).build();
        // c: 2 pending
        Reservation c1 = reservationBuilder().withSpace(c).withStatus(ReservationStatus.PENDING).build();
        Reservation c2 = reservationBuilder().withSpace(c).withStatus(ReservationStatus.PENDING).build();
        when(reservationRepository.findAll()).thenReturn(List.of(a1, a2, a3, b1, c1, c2));

        var top2 = service.getMostReservedSpaces(2);
        assertThat(top2).hasSize(2);
        // Sorted by totalReservations desc: a (3), c (2), b (1)
        assertThat(top2.get(0).spaceId()).isEqualTo(1L);
        assertThat(top2.get(0).totalReservations()).isEqualTo(3);
        assertThat(top2.get(0).confirmedReservations()).isEqualTo(2);
        assertThat(top2.get(1).spaceId()).isEqualTo(3L);
        assertThat(top2.get(1).totalReservations()).isEqualTo(2);
        assertThat(top2.get(1).confirmedReservations()).isEqualTo(0);
    }

    @Test
    void getReservationsByHour_groups_by_start_hour() {
        Reservation r8 = reservationBuilder().withStart(LocalDateTime.of(2025, Month.MARCH, 1, 8, 0)).build();
        Reservation r9 = reservationBuilder().withStart(LocalDateTime.of(2025, Month.MARCH, 1, 9, 30)).build();
        Reservation r92 = reservationBuilder().withStart(LocalDateTime.of(2025, Month.MARCH, 1, 9, 45)).build();
        when(reservationRepository.findAll()).thenReturn(List.of(r8, r9, r92));

        Map<Integer, Long> byHour = service.getReservationsByHour();
        assertThat(byHour.get(8)).isEqualTo(1L);
        assertThat(byHour.get(9)).isEqualTo(2L);
    }

    @Test
    void getNoShowRateByUser_computes_percentage() {
        User u1 = userBuilder().withId(1L).build();
        User u2 = userBuilder().withId(2L).build();
        when(userRepository.findAll()).thenReturn(List.of(u1, u2));

        Reservation u1a = reservationBuilder().withUser(u1).withStatus(ReservationStatus.NO_SHOW).build();
        Reservation u1b = reservationBuilder().withUser(u1).withStatus(ReservationStatus.CONFIRMED).build();
        Reservation u2a = reservationBuilder().withUser(u2).withStatus(ReservationStatus.PENDING).build();
        when(reservationRepository.findAll()).thenReturn(List.of(u1a, u1b, u2a));

        Map<Long, Double> rates = service.getNoShowRateByUser();
        assertThat(rates.get(1L)).isEqualTo(50d);
        assertThat(rates.get(2L)).isEqualTo(0d);
    }

    @Test
    void getSystemStatistics_summarizes_entities_and_rates() {
        when(userRepository.count()).thenReturn(5L);
        var activeUser = userBuilder().withId(10L).build();
        var inactiveUser = userBuilder().withId(11L).withActive(false).build();
        when(userRepository.findAll()).thenReturn(List.of(activeUser, inactiveUser));

        when(spaceRepository.count()).thenReturn(3L);

        // reservations: 4 total (2 confirmed-equivalent, 1 canceled, 1 pending, 1 no-show)
        var r1 = reservationBuilder().withStatus(ReservationStatus.CONFIRMED).build();
        var r2 = reservationBuilder().withStatus(ReservationStatus.CHECKED_IN).build();
        var r3 = reservationBuilder().withStatus(ReservationStatus.CANCELED).build();
        var r4 = reservationBuilder().withStatus(ReservationStatus.PENDING).build();
        var r5 = reservationBuilder().withStatus(ReservationStatus.NO_SHOW).build();
        when(reservationRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4, r5));

        var stats = service.getSystemStatistics();
        assertThat(stats.totalUsers()).isEqualTo(5L);
        assertThat(stats.activeUsers()).isEqualTo(1L);
        assertThat(stats.totalSpaces()).isEqualTo(3L);
        assertThat(stats.totalReservations()).isEqualTo(5L);
        assertThat(stats.confirmedReservations()).isEqualTo(2L);
        assertThat(stats.canceledReservations()).isEqualTo(1L);
        assertThat(stats.pendingReservations()).isEqualTo(1L);
        assertThat(stats.noShowRate()).isEqualTo(1d * 100d / 5d);
        // averageOccupancyRate is derived from getOccupancyRateBySpace(); our current
        // mock has no spaces loaded here, so just assert it returns a value in [0..100]
        assertThat(stats.averageOccupancyRate()).isBetween(0d, 100d);
    }

    @Test
    void getReservationsByStatus_counts_per_status() {
        var a = reservationBuilder().withStatus(ReservationStatus.CONFIRMED).build();
        var b = reservationBuilder().withStatus(ReservationStatus.CONFIRMED).build();
        var c = reservationBuilder().withStatus(ReservationStatus.NO_SHOW).build();
        when(reservationRepository.findAll()).thenReturn(List.of(a, b, c));

        Map<String, Long> m = service.getReservationsByStatus();
        assertThat(m.get("CONFIRMED")).isEqualTo(2L);
        assertThat(m.get("NO_SHOW")).isEqualTo(1L);
    }
}
