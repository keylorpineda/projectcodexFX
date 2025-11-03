package finalprojectprogramming.project.services.setting;

import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Setting;
import finalprojectprogramming.project.repositories.SettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SettingServiceImplementationTest {

    private SettingRepository repository;
    private ModelMapper modelMapper;
    private SettingServiceImplementation service;
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
    repository = mock(SettingRepository.class);
    modelMapper = new ModelMapper();
    auditLogService = mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new SettingServiceImplementation(repository, modelMapper, auditLogService, objectMapper);
    }

    @Test
    void create_saves_and_returns_dto() {
        var input = SettingDTO.builder().key("theme").value("dark").description("ui").build();
        when(repository.findByKey("theme")).thenReturn(Optional.empty());

        Setting saved = Setting.builder()
                .id(10L).key("theme").value("dark").description("ui").updatedAt(LocalDateTime.now())
                .build();
        when(repository.save(any(Setting.class))).thenReturn(saved);

        SettingDTO out = service.create(input);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getKey()).isEqualTo("theme");
        assertThat(out.getValue()).isEqualTo("dark");
        assertThat(out.getDescription()).isEqualTo("ui");

        ArgumentCaptor<Setting> captor = ArgumentCaptor.forClass(Setting.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    void create_rejects_blank_key_and_duplicate() {
        var input = SettingDTO.builder().key("") .value("v").build();
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("key cannot be blank");

        input.setKey("exists");
        when(repository.findByKey("exists")).thenReturn(Optional.of(Setting.builder().id(1L).key("exists").build()));
        assertThatThrownBy(() -> service.create(input))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void update_changes_mutable_fields_and_validates_unique_key() {
        Setting existing = Setting.builder().id(5L).key("k1").value("v1").description("d1").build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByKey("k2")).thenReturn(Optional.empty());

        when(repository.save(any(Setting.class))).thenAnswer(inv -> inv.getArgument(0));

        SettingDTO updated = service.update(5L, SettingDTO.builder()
                .key("k2").value("v2").description("d2").build());

        assertThat(updated.getKey()).isEqualTo("k2");
        assertThat(updated.getValue()).isEqualTo("v2");
        assertThat(updated.getDescription()).isEqualTo("d2");
    }

    @Test
    void update_throws_when_not_found_or_duplicate_key() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(99L, SettingDTO.builder().build()))
                .isInstanceOf(ResourceNotFoundException.class);

    Setting existing = Setting.builder().id(6L).key("k1").build();
        when(repository.findById(6L)).thenReturn(Optional.of(existing));
    when(repository.findByKey("dup")).thenReturn(Optional.of(Setting.builder().id(7L).key("dup").build()));
    // ensure save isn't called to avoid null mapping
    assertThatThrownBy(() -> service.update(6L, SettingDTO.builder().key("dup").build()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void findById_and_findByKey_and_findAll_and_delete() {
        Setting setting = Setting.builder().id(1L).key("k").value("v").build();
        when(repository.findById(1L)).thenReturn(Optional.of(setting));
        when(repository.findByKey("k")).thenReturn(Optional.of(setting));
        when(repository.findAll()).thenReturn(List.of(setting));

        assertThat(service.findById(1L).getKey()).isEqualTo("k");
        assertThat(service.findByKey("k").getValue()).isEqualTo("v");
        assertThat(service.findAll()).hasSize(1);

        when(repository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(2L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.findByKey("missing")).isInstanceOf(ResourceNotFoundException.class);

        when(repository.findById(1L)).thenReturn(Optional.of(setting));
        service.delete(1L);
        verify(repository).delete(setting);

        when(repository.findById(3L)).thenReturn(Optional.of(Setting.builder().id(3L).build()));
        service.delete(3L);

        when(repository.findById(4L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(4L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
