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
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        ratingRepo = Mockito.mock(RatingRepository.class);
        reservationRepo = Mockito.mock(ReservationRepository.class);
    mapper = Mockito.mock(ModelMapper.class);
    auditLogService = Mockito.mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new RatingServiceImplementation(ratingRepo, reservationRepo, mapper, auditLogService, objectMapper);
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

    @Test
    void create_rejects_when_reservation_has_no_user() {
        Reservation res = activeRes(10L);
        res.setUser(null);
        when(reservationRepo.findById(10L)).thenReturn(Optional.of(res));

        RatingDTO in = new RatingDTO();
        in.setReservationId(10L);

        assertThatThrownBy(() -> service.create(in))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("no associated user");
    }

    @Test
    void create_rejects_when_status_not_completed_nor_checked_in() {
        Reservation res = activeRes(12L);
        res.setStatus(ReservationStatus.PENDING);
        when(reservationRepo.findById(12L)).thenReturn(Optional.of(res));
        when(ratingRepo.findByReservationId(12L)).thenReturn(Optional.empty());

        RatingDTO in = new RatingDTO();
        in.setReservationId(12L);
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            mocked.when(() -> SecurityUtils.requireSelfOrAny(eq(1L), eq(UserRole.SUPERVISOR), eq(UserRole.ADMIN)))
                    .thenAnswer(invocation -> null);

            assertThatThrownBy(() -> service.create(in))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("Only completed or checked-in");
        }
    }

    @Test
    void findBySpace_maps_list_and_toDto_populates_fields() {
        Rating rating = new Rating();
        rating.setId(1L);
        Reservation res = activeRes(20L);
        finalprojectprogramming.project.models.Space space = new finalprojectprogramming.project.models.Space();
        space.setId(2L);
        space.setName("Sala");
        res.setSpace(space);
        rating.setReservation(res);
        rating.setUser(res.getUser());
        rating.setVisible(true);
        when(ratingRepo.findBySpaceIdAndVisibleTrueOrderByCreatedAtDesc(2L)).thenReturn(List.of(rating));
        when(mapper.map(any(Rating.class), eq(RatingDTO.class))).thenAnswer(i -> new RatingDTO());

        List<RatingDTO> out = service.findBySpace(2L);
        assertThat(out).hasSize(1);
    }

    @Test
    void average_and_count_by_space_cover_branches() {
        when(ratingRepo.getAverageScoreBySpaceId(3L)).thenReturn(4.26);
        when(ratingRepo.getAverageScoreBySpaceId(4L)).thenReturn(null);
        when(ratingRepo.getRatingCountBySpaceId(3L)).thenReturn(7L);

        assertThat(service.getAverageBySpace(3L)).isEqualTo(4.3);
        assertThat(service.getAverageBySpace(4L)).isEqualTo(0.0);
        assertThat(service.getCountBySpace(3L)).isEqualTo(7L);
    }

    @Test
    void toggle_and_increment_update_entity_and_save() {
        Rating rating = new Rating();
        rating.setId(9L);
        rating.setVisible(true);
        rating.setHelpfulCount(0);
        when(ratingRepo.findById(9L)).thenReturn(Optional.of(rating));
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingDTO t = service.toggleVisibility(9L);
        assertThat(rating.getVisible()).isFalse();
        assertThat(t).isNotNull();

        RatingDTO h = service.incrementHelpful(9L);
        assertThat(rating.getHelpfulCount()).isEqualTo(1);
        assertThat(h).isNotNull();
    }

    @Test
    void toggle_visibility_with_space_executes_space_fields_in_audit() {
        // Configurar un rating con espacio asociado para cubrir las líneas que incorporan spaceId/spaceName en auditoría
        Rating rating = new Rating();
        rating.setId(12L);
        rating.setVisible(true);
        finalprojectprogramming.project.models.Space space = new finalprojectprogramming.project.models.Space();
        space.setId(77L);
        space.setName("Sala Magna");
        Reservation res = activeRes(55L);
        res.setSpace(space);
        rating.setReservation(res);
        // Importante: también seteamos el espacio directamente en el rating, pues recordAudit lo toma de rating.getSpace()
        rating.setSpace(space);

        when(ratingRepo.findById(12L)).thenReturn(Optional.of(rating));
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingDTO out = service.toggleVisibility(12L);
        assertThat(out).isNotNull();
        // No validamos el contenido del log de auditoría, solo ejecutamos el flujo para cubrir las líneas
        verify(ratingRepo, atLeastOnce()).save(any(Rating.class));
    }
}
