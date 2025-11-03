package finalprojectprogramming.project.services.space;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

class SpaceServiceImplementationTest {

    private SpaceRepository repo;
    private ModelMapper mapper;
    private SpaceAvailabilityValidator availability;
    private SpaceServiceImplementation service;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(SpaceRepository.class);
        mapper = Mockito.mock(ModelMapper.class);
        availability = Mockito.mock(SpaceAvailabilityValidator.class);
        service = new SpaceServiceImplementation(repo, mapper, availability);
    }

    private Space sampleSpace(Long id, boolean active, boolean deleted) {
        Space s = new Space();
        s.setId(id);
        s.setName("Polideportivo");
    s.setType(SpaceType.SALA);
        s.setCapacity(100);
        s.setActive(active);
        s.setDeletedAt(deleted ? LocalDateTime.now() : null);
        List<SpaceImage> imgs = new ArrayList<>();
        SpaceImage img1 = new SpaceImage(); img1.setId(1L); imgs.add(img1);
        SpaceImage imgNull = new SpaceImage(); imgNull.setId(null); imgs.add(imgNull);
        s.setImages(imgs);
        List<SpaceSchedule> sch = new ArrayList<>();
        SpaceSchedule sc1 = new SpaceSchedule(); sc1.setId(10L); sch.add(sc1);
        SpaceSchedule scNull = new SpaceSchedule(); scNull.setId(null); sch.add(scNull);
        s.setSchedules(sch);
        List<Reservation> rs = new ArrayList<>();
        Reservation r1 = new Reservation(); r1.setId(100L); rs.add(r1);
        Reservation rNull = new Reservation(); rNull.setId(null); rs.add(rNull);
        s.setReservations(rs);
        return s;
    }

    @Test
    void create_sets_defaults_and_saves() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            SpaceDTO in = new SpaceDTO();
            in.setName("Poli");
            Space saved = sampleSpace(5L, true, false);
            when(mapper.map(any(SpaceDTO.class), eq(Space.class))).thenReturn(new Space());
            when(repo.save(any(Space.class))).thenReturn(saved);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            SpaceDTO out = service.create(in);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));
            verify(repo, times(1)).save(any(Space.class));
            assertThat(out).isNotNull();
        }
    }

    @Test
    void update_maps_fields_and_handles_active_toggle() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space existing = sampleSpace(9L, true, false);
            when(repo.findById(9L)).thenReturn(Optional.of(existing));
            when(repo.save(any(Space.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            SpaceDTO patch = new SpaceDTO();
            patch.setName("Nuevo");
            patch.setActive(false);
            patch.setCapacity(50);
            SpaceDTO out = service.update(9L, patch);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));
            assertThat(out).isNotNull();
            assertThat(existing.getName()).isEqualTo("Nuevo");
            assertThat(existing.getActive()).isFalse();
            assertThat(existing.getDeletedAt()).isNotNull();
        }
    }

    @Test
    void findById_requires_role_and_filters_deleted() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space deleted = sampleSpace(2L, true, true);
            when(repo.findById(2L)).thenReturn(Optional.of(deleted));

            assertThatThrownBy(() -> service.findById(2L))
                    .isInstanceOf(ResourceNotFoundException.class);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void findAll_filters_deleted_and_maps() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space a = sampleSpace(1L, true, false);
            Space b = sampleSpace(2L, true, true); // eliminado
            when(repo.findAll()).thenReturn(List.of(a, b));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            List<SpaceDTO> list = service.findAll();

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN));
            assertThat(list).hasSize(1);
        }
    }

    @Test
    void delete_marks_deleted_and_saves() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(7L, true, false);
            when(repo.findById(7L)).thenReturn(Optional.of(s));

            service.delete(7L);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));
            assertThat(s.getDeletedAt()).isNotNull();
            assertThat(s.getActive()).isFalse();
            verify(repo).save(s);
        }
    }

    @Test
    void changeStatus_switches_state_and_updates() {
        try (MockedStatic<SecurityUtils> ignored = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(11L, true, false);
            when(repo.findById(11L)).thenReturn(Optional.of(s));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            SpaceDTO out = service.changeStatus(11L, false);
            assertThat(out).isNotNull();
            assertThat(s.getActive()).isFalse();
            verify(repo).save(s);
        }
    }

    @Test
    void findAvailableSpaces_filters_by_criteria_and_uses_validator() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space a = sampleSpace(1L, true, false);
            a.setType(SpaceType.SALA);
            a.setCapacity(10);
            Space b = sampleSpace(2L, true, false);
            b.setType(SpaceType.CANCHA);
            b.setCapacity(100);
            Space c = sampleSpace(3L, true, true); // eliminado
            when(repo.findAll()).thenReturn(List.of(a, b, c));
            when(availability.isAvailable(eq(b), any(), any(), isNull())).thenReturn(true);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            var start = LocalDateTime.now().plusDays(1);
            var end = start.plusHours(2);
            var list = service.findAvailableSpaces(start, end, SpaceType.CANCHA, 50);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN));
            verify(availability).validateTimeRange(start, end);
            assertThat(list.size()).isEqualTo(1);
        }
    }
}
