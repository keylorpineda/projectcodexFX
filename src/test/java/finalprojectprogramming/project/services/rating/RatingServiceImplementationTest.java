package finalprojectprogramming.project.services.rating;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Rating;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.ReservationStatus;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.RatingRepository;
import finalprojectprogramming.project.repositories.ReservationRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RatingServiceImplementationTest {

    private RatingRepository ratingRepo;
    private ReservationRepository reservationRepo;
    private ModelMapper mapper;
    private RatingServiceImplementation service;

    @BeforeEach
    void setup() {
        ratingRepo = Mockito.mock(RatingRepository.class);
        reservationRepo = Mockito.mock(ReservationRepository.class);
        mapper = Mockito.mock(ModelMapper.class);
        service = new RatingServiceImplementation(ratingRepo, reservationRepo, mapper);
        when(mapper.map(any(Rating.class), eq(RatingDTO.class))).thenAnswer(i -> new RatingDTO());
    }

    private Reservation activeRes(Long id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setDeletedAt(null);
        r.setEndTime(LocalDateTime.now().minusMinutes(10));
        r.setStatus(ReservationStatus.CHECKED_IN);
        User user = new User();
        user.setId(1L);
        r.setUser(user);
        return r;
    }

    @Test
    void create_persists_when_reservation_has_no_rating() {
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(activeRes(1L)));
        when(ratingRepo.findByReservationId(1L)).thenReturn(Optional.empty());
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingDTO in = new RatingDTO();
        in.setReservationId(1L);
        in.setScore(5);
        in.setComment("Excelente");
        in.setCreatedAt(LocalDateTime.now());
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(1L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            RatingDTO out = service.create(in);
            assertThat(out).isNotNull();
            verify(ratingRepo).save(any(Rating.class));
            mocked.verify(() -> SecurityUtils.requireSelfOrAny(1L, UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void create_rejects_when_reservation_already_has_rating() {
        Reservation res = activeRes(1L);
        res.setRating(new Rating());
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(res));

        RatingDTO in = new RatingDTO();
        in.setReservationId(1L);
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(1L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(in)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    void create_rejects_when_repo_has_rating_for_reservation() {
        when(reservationRepo.findById(1L)).thenReturn(Optional.of(activeRes(1L)));
        when(ratingRepo.findByReservationId(1L)).thenReturn(Optional.of(new Rating()));

        RatingDTO in = new RatingDTO();
        in.setReservationId(1L);
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(1L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(in)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    void update_changes_mutable_fields() {
        Rating existing = new Rating();
        existing.setId(9L);
        when(ratingRepo.findById(9L)).thenReturn(Optional.of(existing));
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingDTO patch = new RatingDTO();
        patch.setScore(4);
        patch.setComment("Bien");
        patch.setCreatedAt(LocalDateTime.now());

        RatingDTO out = service.update(9L, patch);
        assertThat(out).isNotNull();
        assertThat(existing.getScore()).isEqualTo(4);
        assertThat(existing.getComment()).isEqualTo("Bien");
    }

    @Test
    void find_variants_and_delete() {
        Rating r = new Rating();
        r.setId(5L);
        Reservation res = activeRes(2L);
        r.setReservation(res);
        when(ratingRepo.findById(5L)).thenReturn(Optional.of(r));
        when(ratingRepo.findAll()).thenReturn(List.of(r));
        when(ratingRepo.findByReservationId(2L)).thenReturn(Optional.of(r));

        assertThat(service.findById(5L)).isNotNull();
        assertThat(service.findAll()).hasSize(1);
        assertThat(service.findByReservation(2L)).isNotNull();

        service.delete(5L);
        verify(ratingRepo).delete(r);
        assertThat(res.getRating()).isNull();
    }

    @Test
    void getActiveReservation_throws_when_missing_or_deleted() {
        // missing
        RatingDTO in = new RatingDTO();
        in.setReservationId(99L);
        assertThatThrownBy(() -> service.create(in)).isInstanceOf(ResourceNotFoundException.class);

        // deleted
        Reservation deleted = new Reservation();
        deleted.setId(3L);
        deleted.setDeletedAt(LocalDateTime.now());
        when(reservationRepo.findById(3L)).thenReturn(Optional.of(deleted));
        in.setReservationId(3L);
        assertThatThrownBy(() -> service.create(in)).isInstanceOf(ResourceNotFoundException.class);
    }
}
