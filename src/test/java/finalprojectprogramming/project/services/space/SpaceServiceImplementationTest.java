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
import finalprojectprogramming.project.repositories.RatingRepository;
import finalprojectprogramming.project.services.spaceimage.SpaceImageService;

class SpaceServiceImplementationTest {

    private SpaceRepository repo;
    private ModelMapper mapper;
    private SpaceAvailabilityValidator availability;
    private RatingRepository ratingRepository;
    private SpaceImageService spaceImageService;
    private SpaceServiceImplementation service;
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
    repo = Mockito.mock(SpaceRepository.class);
    mapper = Mockito.mock(ModelMapper.class);
    availability = Mockito.mock(SpaceAvailabilityValidator.class);
    ratingRepository = Mockito.mock(RatingRepository.class);
    spaceImageService = Mockito.mock(SpaceImageService.class);
    auditLogService = Mockito.mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new SpaceServiceImplementation(repo, mapper, availability, ratingRepository, spaceImageService, auditLogService, objectMapper);
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
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

            SpaceDTO out = service.create(in);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));
            verify(repo, times(1)).save(any(Space.class));
            assertThat(out).isNotNull();
        }
    }

    @Test
    void create_logs_audit_even_when_no_authenticated_user() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            // Make SecurityUtils.getCurrentUserId throw to exercise recordAudit catch path
            mocked.when(SecurityUtils::getCurrentUserId).thenThrow(new RuntimeException("no auth"));

            SpaceDTO in = new SpaceDTO();
            in.setName("Poli");
            Space saved = sampleSpace(5L, true, false);
            when(mapper.map(any(SpaceDTO.class), eq(Space.class))).thenReturn(new Space());
            when(repo.save(any(Space.class))).thenReturn(saved);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

            SpaceDTO out = service.create(in);

            assertThat(out).isNotNull();
            // At least one audit event should be logged
            verify(auditLogService, atLeastOnce()).logEvent(any(), any(), any(), any());
        }
    }

    @Test
    void update_maps_fields_and_handles_active_toggle() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space existing = sampleSpace(9L, true, false);
            when(repo.findById(9L)).thenReturn(Optional.of(existing));
            when(repo.save(any(Space.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

            SpaceDTO patch = new SpaceDTO();
            patch.setName("Nuevo");
            patch.setActive(false);
            patch.setCapacity(50);
            SpaceDTO out = service.update(9L, patch);

            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));
            // Se verifica que el update se efectúa correctamente sobre la entidad
            assertThat(out).isNotNull();
            assertThat(existing.getName()).isEqualTo("Nuevo");
            assertThat(existing.getActive()).isFalse();
            // Al desactivar no se marca como eliminado; deletedAt permanece igual (nulo)
            assertThat(existing.getDeletedAt()).isNull();
        }
    }

    @Test
    void update_throws_when_space_not_found() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            when(repo.findById(404L)).thenReturn(Optional.empty());
            SpaceDTO patch = new SpaceDTO();
            assertThatThrownBy(() -> service.update(404L, patch))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Space with id 404 not found");
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
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

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
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

            SpaceDTO out = service.changeStatus(11L, false);
            assertThat(out).isNotNull();
            assertThat(s.getActive()).isFalse();
            verify(repo).save(s);
        }
    }

    @Test
    void changeStatus_throws_when_not_found() {
        try (MockedStatic<SecurityUtils> ignored = Mockito.mockStatic(SecurityUtils.class)) {
            when(repo.findById(404L)).thenReturn(java.util.Optional.empty());

            assertThatThrownBy(() -> service.changeStatus(404L, true))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Space with id 404 not found");
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

    @Test
    void createWithImage_uploads_when_file_present_and_ignores_when_null_or_exception() throws Exception {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            // Arrange: creation returns a mapped DTO with id
            Space saved = sampleSpace(33L, true, false);
            when(mapper.map(any(SpaceDTO.class), eq(Space.class))).thenReturn(new Space());
            when(repo.save(any(Space.class))).thenReturn(saved);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenAnswer(inv -> {
                Space s = inv.getArgument(0);
                SpaceDTO dto = new SpaceDTO();
                dto.setId(s.getId());
                return dto;
            });
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);

            // Case 1: with image
            org.springframework.mock.web.MockMultipartFile image =
                    new org.springframework.mock.web.MockMultipartFile("image", "a.png", "image/png", new byte[]{1});
            SpaceDTO out1 = service.createWithImage(new SpaceDTO(), image);
            assertThat(out1.getId()).isEqualTo(33L);
            verify(spaceImageService, times(1)).upload(eq(33L), any(), isNull(), eq(true), eq(1));

            // Case 2: null image (no upload)
            SpaceDTO out2 = service.createWithImage(new SpaceDTO(), null);
            assertThat(out2.getId()).isEqualTo(33L);
            // upload count still 1 (no extra)
            verify(spaceImageService, times(1)).upload(anyLong(), any(), any(), anyBoolean(), anyInt());

            // Case 3: upload throws, method still returns created DTO
            org.springframework.mock.web.MockMultipartFile imageBad =
                    new org.springframework.mock.web.MockMultipartFile("image", "b.png", "image/png", new byte[]{2});
            doThrow(new RuntimeException("oops")).when(spaceImageService)
                    .upload(eq(33L), any(), any(), anyBoolean(), anyInt());
            SpaceDTO out3 = service.createWithImage(new SpaceDTO(), imageBad);
            assertThat(out3.getId()).isEqualTo(33L);
        }
    }

    @Test
    void changeStatus_true_clears_deletedAt_and_keeps_active_true_and_handles_null_average_rating() {
        try (MockedStatic<SecurityUtils> ignored = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(44L, false, true); // deleted
            when(repo.findById(44L)).thenReturn(Optional.of(s));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenAnswer(inv -> {
                Space space = inv.getArgument(0);
                SpaceDTO dto = new SpaceDTO();
                dto.setId(space.getId());
                dto.setActive(space.getActive());
                return dto;
            });
            // avgRating null branch
            when(ratingRepository.getAverageScoreBySpaceId(44L)).thenReturn(null);
            when(ratingRepository.getRatingCountBySpaceId(44L)).thenReturn(0L);

            SpaceDTO dto = service.changeStatus(44L, true);
            assertThat(dto.getId()).isEqualTo(44L);
            assertThat(s.getActive()).isTrue();
            assertThat(s.getDeletedAt()).isNull();
        }
    }

    @Test
    void findById_returns_dto_when_active_and_exists() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(50L, true, false);
            when(repo.findById(50L)).thenReturn(Optional.of(s));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(1.5);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(2L);

            SpaceDTO dto = service.findById(50L);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN));
            assertThat(dto).isNotNull();
        }
    }

    @Test
    void changeStatus_false_does_not_override_existing_deletedAt() {
        try (MockedStatic<SecurityUtils> ignored = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(60L, false, true); // ya eliminado
            when(repo.findById(60L)).thenReturn(Optional.of(s));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(0L);
            var previousDeletedAt = s.getDeletedAt();

            SpaceDTO dto = service.changeStatus(60L, false);
            assertThat(dto).isNotNull();
            assertThat(s.getActive()).isFalse();
            // permanece con el mismo deletedAt (no nulo)
            assertThat(s.getDeletedAt()).isEqualTo(previousDeletedAt);
            verify(repo).save(s);
        }
    }

    @Test
    void update_sets_optional_fields_when_present() {
        try (MockedStatic<SecurityUtils> ignored = Mockito.mockStatic(SecurityUtils.class)) {
            // Debe ser un espacio NO eliminado (deletedAt == null) para que update() no lance excepción
            Space existing = sampleSpace(70L, false, false);
            when(repo.findById(70L)).thenReturn(Optional.of(existing));
            when(repo.save(any(Space.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(ratingRepository.getAverageScoreBySpaceId(anyLong())).thenReturn(2.0);
            when(ratingRepository.getRatingCountBySpaceId(anyLong())).thenReturn(5L);

            SpaceDTO patch = new SpaceDTO();
            patch.setDescription("Desc");
            patch.setLocation("Loc");
            patch.setMaxReservationDuration(90);
            patch.setRequiresApproval(Boolean.TRUE);
            patch.setAverageRating(3.4);
            // activar nuevamente
            patch.setActive(true);

            SpaceDTO out = service.update(70L, patch);
            assertThat(out).isNotNull();
            assertThat(existing.getDescription()).isEqualTo("Desc");
            assertThat(existing.getLocation()).isEqualTo("Loc");
            assertThat(existing.getMaxReservationDuration()).isEqualTo(90);
            assertThat(existing.getRequiresApproval()).isTrue();
            assertThat(existing.getAverageRating()).isEqualTo(3.4);
            assertThat(existing.getActive()).isTrue();
            assertThat(existing.getDeletedAt()).isNull();
        }
    }

    @Test
    void toDto_handles_null_collections_and_sets_active_from_space_when_null_on_dto() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space s = sampleSpace(80L, true, false);
            s.setImages(null);
            s.setSchedules(null);
            s.setReservations(null);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            when(repo.findById(80L)).thenReturn(Optional.of(s));
            when(ratingRepository.getAverageScoreBySpaceId(80L)).thenReturn(4.0);
            when(ratingRepository.getRatingCountBySpaceId(80L)).thenReturn(7L);

            SpaceDTO dto = service.findById(80L);
            assertThat(dto.getActive()).isTrue();
            assertThat(dto.getImageIds()).isEmpty();
            assertThat(dto.getScheduleIds()).isEmpty();
            assertThat(dto.getReservationIds()).isEmpty();
        }
    }

    @Test
    void validateSpaceAvailability_invokes_assertion() {
        Space s = sampleSpace(90L, true, false);
        var start = LocalDateTime.now();
        var end = start.plusHours(1);
        service.validateSpaceAvailability(s, start, end, null);
        verify(availability).assertAvailability(s, start, end, null);
    }

    @Test
    void findAvailableSpaces_handles_null_filters_and_unavailable_spaces() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            Space a = sampleSpace(1L, true, false);
            Space b = sampleSpace(2L, true, false);
            when(repo.findAll()).thenReturn(List.of(a, b));
            // ninguno disponible segun validador
            when(availability.isAvailable(any(), any(), any(), isNull())).thenReturn(false);
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());

            var start = LocalDateTime.now().plusDays(1);
            var end = start.plusHours(2);
            var list = service.findAvailableSpaces(start, end, null, null);

            assertThat(list).isEmpty();
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.USER, UserRole.SUPERVISOR, UserRole.ADMIN));
        }
    }

    @Test
    void searchSpaces_applies_all_filters_and_excludes_deleted() {
        // Datos
        Space s1 = sampleSpace(101L, true, false); // debe pasar
        s1.setType(SpaceType.SALA);
        s1.setCapacity(80);
        s1.setLocation("Centro Cultural Central");

        Space s2 = sampleSpace(102L, true, false); // filtrado por tipo
        s2.setType(SpaceType.CANCHA);
        s2.setCapacity(80);
        s2.setLocation("Centro Cultural Central");

        Space s3 = sampleSpace(103L, true, false); // filtrado por minCapacity
        s3.setType(SpaceType.SALA);
        s3.setCapacity(30);
        s3.setLocation("Centro Cultural Central");

        Space s4 = sampleSpace(104L, true, false); // filtrado por maxCapacity
        s4.setType(SpaceType.SALA);
        s4.setCapacity(120);
        s4.setLocation("Centro Cultural Central");

        Space s5 = sampleSpace(105L, false, false); // filtrado por active
        s5.setType(SpaceType.SALA);
        s5.setCapacity(80);
        s5.setLocation("Centro Cultural Central");

        Space s6 = sampleSpace(106L, true, false); // filtrado por location nula
        s6.setType(SpaceType.SALA);
        s6.setCapacity(80);
        s6.setLocation(null);

        Space s7 = sampleSpace(107L, true, true); // eliminado -> siempre fuera
        s7.setType(SpaceType.SALA);
        s7.setCapacity(80);
        s7.setLocation("Central");

        when(repo.findAll()).thenReturn(List.of(s1, s2, s3, s4, s5, s6, s7));
        when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenAnswer(inv -> {
            Space space = inv.getArgument(0);
            SpaceDTO dto = new SpaceDTO();
            dto.setId(space.getId());
            return dto;
        });

        // Filtros a aplicar en conjunción
        var result = service.searchSpaces(
                SpaceType.SALA,
                50,
                100,
                "trO", // should match "CenTro" case-insensitive substring
                true);

        assertThat(result).extracting(SpaceDTO::getId).containsExactly(101L);
    }

    @Test
    void searchSpaces_ignores_location_when_blank_and_active_null_includes_both_states() {
        Space a = sampleSpace(201L, true, false);
        a.setType(SpaceType.SALA);
        a.setCapacity(10);
        a.setLocation("Barrio Norte");

        Space b = sampleSpace(202L, false, false);
        b.setType(SpaceType.SALA);
        b.setCapacity(15);
        b.setLocation("Barrio Sur");

        when(repo.findAll()).thenReturn(List.of(a, b));
        when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenAnswer(inv -> {
            Space space = inv.getArgument(0);
            SpaceDTO dto = new SpaceDTO();
            dto.setId(space.getId());
            return dto;
        });

        // location en blanco -> no filtra por ubicación
        var out = service.searchSpaces(SpaceType.SALA, null, null, "   ", null);
        assertThat(out).extracting(SpaceDTO::getId).containsExactlyInAnyOrder(201L, 202L);
    }

    @Test
    void create_initializes_collections_active_and_averageRating_when_null() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            // Arrange: mapper returns a brand new Space with all fields null to trigger defaults
            Space mapped = new Space();
            // Force nulls to cover initialization branches
            mapped.setImages(null);
            mapped.setSchedules(null);
            mapped.setReservations(null);
            mapped.setActive(null);
            mapped.setAverageRating(null);
            when(mapper.map(any(SpaceDTO.class), eq(Space.class))).thenReturn(mapped);
            // Capture the entity passed to save to assert default initialization
            when(repo.save(any(Space.class))).thenAnswer(inv -> {
                Space s = inv.getArgument(0);
                // simulate DB assigning ID but keep defaults set by service
                s.setId(999L);
                return s;
            });
            // ModelMapper for toDto: return plain DTO
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenReturn(new SpaceDTO());
            // Rating repository tolerates any id (including null just in case)
            when(ratingRepository.getAverageScoreBySpaceId(any())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(any())).thenReturn(0L);

            // Act
            SpaceDTO input = new SpaceDTO();
            SpaceDTO out = service.create(input);

            // Assert: defaults were initialized on the entity
            assertThat(out).isNotNull();
            assertThat(mapped.getImages()).isNotNull();
            assertThat(mapped.getSchedules()).isNotNull();
            assertThat(mapped.getReservations()).isNotNull();
            assertThat(mapped.getActive()).isTrue();
            assertThat(mapped.getAverageRating()).isEqualTo(0.0);
        }
    }

    @Test
    void toDto_sets_active_from_space_when_mapper_returns_dto_with_null_active() {
        try (MockedStatic<SecurityUtils> mocked = Mockito.mockStatic(SecurityUtils.class)) {
            // Arrange: active=false on space, mapper returns DTO with active=null so branch executes
            Space s = sampleSpace(305L, false, false);
            when(repo.findById(305L)).thenReturn(Optional.of(s));
            when(mapper.map(any(Space.class), eq(SpaceDTO.class))).thenAnswer(inv -> {
                SpaceDTO dto = new SpaceDTO();
                dto.setActive(null); // force null so service sets it from space
                return dto;
            });
            when(ratingRepository.getAverageScoreBySpaceId(any())).thenReturn(0.0);
            when(ratingRepository.getRatingCountBySpaceId(any())).thenReturn(0L);

            // Act
            SpaceDTO dto = service.findById(305L);

            // Assert: active copied from space
            assertThat(dto.getActive()).isFalse();
        }
    }

    @Test
    void recordAudit_handles_null_detailsCustomizer_via_reflection() throws Exception {
        // Preparar un espacio simple
        Space s = sampleSpace(901L, true, false);

        // Invocar método privado recordAudit(action, space, null) por reflexión para cubrir rama null
        var method = SpaceServiceImplementation.class.getDeclaredMethod(
                "recordAudit", String.class, Space.class, java.util.function.Consumer.class);
        method.setAccessible(true);

        // No debe lanzar y debe registrar un evento de auditoría
        method.invoke(service, "TEST_ACTION", s, null);
        verify(auditLogService, atLeastOnce()).logEvent(any(), eq("TEST_ACTION"), eq("901"), any());
    }
}
