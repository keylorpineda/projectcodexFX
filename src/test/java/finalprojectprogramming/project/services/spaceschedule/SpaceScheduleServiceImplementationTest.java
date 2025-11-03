package finalprojectprogramming.project.services.spaceschedule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import finalprojectprogramming.project.dtos.SpaceScheduleDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceSchedule;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.repositories.SpaceScheduleRepository;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;

class SpaceScheduleServiceImplementationTest {

    private SpaceScheduleRepository repo;
    private SpaceRepository spaceRepo;
    private ModelMapper mapper;
    private SpaceScheduleServiceImplementation service;
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(SpaceScheduleRepository.class);
        spaceRepo = Mockito.mock(SpaceRepository.class);
    mapper = Mockito.mock(ModelMapper.class);
    auditLogService = Mockito.mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new SpaceScheduleServiceImplementation(repo, spaceRepo, mapper, auditLogService, objectMapper);
        when(mapper.map(any(SpaceSchedule.class), eq(SpaceScheduleDTO.class))).thenAnswer(inv -> new SpaceScheduleDTO());
    }

    private Space activeSpace(Long id) {
        Space s = new Space();
        s.setId(id);
        s.setDeletedAt(null);
        return s;
    }

    @Test
    void create_validates_time_and_unique_day_and_persists() {
        when(spaceRepo.findById(1L)).thenReturn(Optional.of(activeSpace(1L)));
        when(repo.findBySpaceIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(Optional.empty());
        when(repo.save(any(SpaceSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceScheduleDTO in = new SpaceScheduleDTO();
        in.setSpaceId(1L);
        in.setDayOfWeek(DayOfWeek.MONDAY);
        in.setOpenTime(LocalTime.of(8, 0));
        in.setCloseTime(LocalTime.of(17, 0));
        SpaceScheduleDTO out = service.create(in);

        assertThat(out).isNotNull();
        verify(repo).save(any(SpaceSchedule.class));
    }

    @Test
    void create_rejects_duplicate_day() {
        when(spaceRepo.findById(1L)).thenReturn(Optional.of(activeSpace(1L)));
    SpaceSchedule existing = new SpaceSchedule();
    existing.setId(10L); // asegurar que no coincida con currentId=null
    when(repo.findBySpaceIdAndDayOfWeek(1L, DayOfWeek.MONDAY)).thenReturn(Optional.of(existing));

        SpaceScheduleDTO in = new SpaceScheduleDTO();
        in.setSpaceId(1L);
        in.setDayOfWeek(DayOfWeek.MONDAY);
        in.setOpenTime(LocalTime.of(8, 0));
        in.setCloseTime(LocalTime.of(17, 0));

        assertThatThrownBy(() -> service.create(in)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void update_changes_fields_and_revalidates() {
        Space sp = activeSpace(1L);
        SpaceSchedule existing = new SpaceSchedule();
        existing.setId(5L);
        existing.setSpace(sp);
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setOpenTime(LocalTime.of(8, 0));
        existing.setCloseTime(LocalTime.of(12, 0));
        when(repo.findById(5L)).thenReturn(Optional.of(existing));
        when(repo.findBySpaceIdAndDayOfWeek(1L, DayOfWeek.TUESDAY)).thenReturn(Optional.empty());
        when(spaceRepo.findById(2L)).thenReturn(Optional.of(activeSpace(2L)));
        when(repo.save(any(SpaceSchedule.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceScheduleDTO patch = new SpaceScheduleDTO();
        patch.setSpaceId(2L); // cambia espacio
        patch.setDayOfWeek(DayOfWeek.TUESDAY); // valida unique
        patch.setOpenTime(LocalTime.of(9, 0)); // valida rango con close actual
        patch.setCloseTime(LocalTime.of(18, 0)); // valida rango con open actual si procede

        SpaceScheduleDTO out = service.update(5L, patch);
        assertThat(out).isNotNull();
        assertThat(existing.getSpace().getId()).isEqualTo(2L);
        assertThat(existing.getDayOfWeek()).isEqualTo(DayOfWeek.TUESDAY);
        assertThat(existing.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(existing.getCloseTime()).isEqualTo(LocalTime.of(18, 0));
    }

    @Test
    void update_rejects_invalid_time_range() {
        SpaceSchedule existing = new SpaceSchedule();
        existing.setId(5L);
        existing.setSpace(activeSpace(1L));
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setOpenTime(LocalTime.of(8, 0));
        existing.setCloseTime(LocalTime.of(12, 0));
        when(repo.findById(5L)).thenReturn(Optional.of(existing));

        SpaceScheduleDTO patch = new SpaceScheduleDTO();
        patch.setOpenTime(LocalTime.of(13, 0));
        patch.setCloseTime(LocalTime.of(12, 0));

        assertThatThrownBy(() -> service.update(5L, patch)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findById_findAll_findBySpace_delete() {
        SpaceSchedule s = new SpaceSchedule();
        s.setId(7L);
        s.setDayOfWeek(DayOfWeek.MONDAY);
        when(repo.findById(7L)).thenReturn(Optional.of(s));
        when(repo.findAll()).thenReturn(List.of(s));
        when(repo.findBySpaceId(3L)).thenReturn(List.of(s));

        assertThat(service.findById(7L)).isNotNull();
        assertThat(service.findAll()).hasSize(1);
        assertThat(service.findBySpace(3L)).hasSize(1);

        service.delete(7L);
        verify(repo).delete(s);
    }

    @Test
    void getActiveSpace_throws_when_deleted_or_missing() {
        // missing
        assertThatThrownBy(() -> service.findById(999L)).isInstanceOf(ResourceNotFoundException.class);

        // deleted
        Space deleted = new Space();
        deleted.setId(1L);
        deleted.setDeletedAt(LocalDateTime.now());
        when(spaceRepo.findById(1L)).thenReturn(Optional.of(deleted));
        when(repo.findBySpaceIdAndDayOfWeek(anyLong(), any())).thenReturn(Optional.empty());

        SpaceScheduleDTO in = new SpaceScheduleDTO();
        in.setSpaceId(1L);
        in.setDayOfWeek(DayOfWeek.MONDAY);
        in.setOpenTime(LocalTime.of(8, 0));
        in.setCloseTime(LocalTime.of(9, 0));

        assertThatThrownBy(() -> service.create(in)).isInstanceOf(ResourceNotFoundException.class);
    }
}
