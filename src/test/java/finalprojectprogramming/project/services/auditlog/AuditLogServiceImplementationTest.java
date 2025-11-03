package finalprojectprogramming.project.services.auditlog;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.AuditLogDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.AuditLog;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.repositories.AuditLogRepository;
import finalprojectprogramming.project.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplementationTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AuditLogServiceImplementation service;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void setupModelMapperStub() {
        // Stub de ModelMapper.map(AuditLog, AuditLogDTO.class)
        lenient().when(modelMapper.map(any(AuditLog.class), eq(AuditLogDTO.class)))
            .thenAnswer(inv -> {
                AuditLog src = inv.getArgument(0);
                return AuditLogDTO.builder()
                        .id(src.getId())
                        .userId(src.getUser() != null ? src.getUser().getId() : null)
                        .action(src.getAction())
                        .entityId(src.getEntityId())
                        .details(src.getDetails())
                        .timestamp(src.getTimestamp())
                        .build();
            });
    }

    @Test
    void create_withUser_success() {
        ObjectNode details = json.createObjectNode().put("k", "v");
        AuditLogDTO input = AuditLogDTO.builder()
                .userId(1L)
                .action("CREATE")
                .entityId("42")
                .details(details)
                .timestamp(null) // debe setearse a now() dentro del servicio
                .build();

        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog a = inv.getArgument(0);
            a.setId(10L);
            // timestamp ya viene seteado por el servicio si era null; lo respetamos
            return a;
        });

        AuditLogDTO out = service.create(input);

        assertThat(out.getId()).isEqualTo(10L);
        assertThat(out.getUserId()).isEqualTo(1L);
        assertThat(out.getAction()).isEqualTo("CREATE");
        assertThat(out.getEntityId()).isEqualTo("42");
        assertThat(out.getDetails()).isEqualTo(details);
        assertThat(out.getTimestamp()).isNotNull();

        verify(userRepository).findById(1L);
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void create_userNotFound_continues_without_user() {
        AuditLogDTO input = AuditLogDTO.builder()
                .userId(99L)
                .action("X")
                .entityId("E-1")
                .build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog a = inv.getArgument(0);
            a.setId(99L);
            if (a.getTimestamp() == null) a.setTimestamp(LocalDateTime.now());
            return a;
        });

        AuditLogDTO out = service.create(input);
        assertThat(out.getId()).isEqualTo(99L);
        assertThat(out.getUserId()).isNull();
        assertThat(out.getAction()).isEqualTo("X");
        assertThat(out.getEntityId()).isEqualTo("E-1");
    }

    @Test
    void create_withoutUser_success_setsNullUserId() {
        AuditLogDTO input = AuditLogDTO.builder()
                .userId(null)
                .action("UPDATE")
                .entityId("ABC")
                .build();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> {
            AuditLog a = inv.getArgument(0);
            a.setId(11L);
            if (a.getTimestamp() == null) {
                a.setTimestamp(LocalDateTime.now());
            }
            return a;
        });

        AuditLogDTO out = service.create(input);
        assertThat(out.getId()).isEqualTo(11L);
        assertThat(out.getUserId()).isNull();
        assertThat(out.getAction()).isEqualTo("UPDATE");
        assertThat(out.getTimestamp()).isNotNull();
    }

    @Test
    void logEvent_buildsDtoAndPersists() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findById(5L)).thenReturn(Optional.of(User.builder().id(5L).build()));

        service.logEvent(5L, "RESERVATION_CREATED", "77", json.createObjectNode().put("k", "v"));

        verify(auditLogRepository).save(any(AuditLog.class));
        verify(userRepository).findById(5L);
    }

    @Test
    void logEvent_handles_repository_exception_without_throwing() {
        // Simula que durante la persistencia ocurre un error inesperado
        when(auditLogRepository.save(any(AuditLog.class))).thenThrow(new RuntimeException("db down"));

        // No debe propagarse la excepción (se registra el error y continúa)
        service.logEvent(1L, "ANY", "E-1", json.createObjectNode().put("x", 1));

        // Se intentó guardar
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void findById_success() {
        AuditLog entity = AuditLog.builder()
                .id(5L)
                .user(User.builder().id(3L).build())
                .action("READ")
                .entityId("E1")
                .timestamp(LocalDateTime.now())
                .build();
        when(auditLogRepository.findById(5L)).thenReturn(Optional.of(entity));

        AuditLogDTO dto = service.findById(5L);
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getUserId()).isEqualTo(3L);
        assertThat(dto.getAction()).isEqualTo("READ");
    }

    @Test
    void findById_notFound_throws() {
        when(auditLogRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(404L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Audit log with id 404 not found");
    }

    @Test
    void findAll_mapsList() {
        AuditLog a1 = AuditLog.builder().id(1L).action("A").timestamp(LocalDateTime.now()).build();
        AuditLog a2 = AuditLog.builder().id(2L).action("B").timestamp(LocalDateTime.now()).build();
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(a1, a2));

        List<AuditLogDTO> list = service.findAll();
        assertThat(list).hasSize(2);
        assertThat(list).extracting(AuditLogDTO::getId).containsExactly(1L, 2L);
    }

    @Test
    void findByUser_mapsList() {
        AuditLog a1 = AuditLog.builder().id(7L).user(User.builder().id(9L).build()).action("X").timestamp(LocalDateTime.now()).build();
        when(auditLogRepository.findByUserId(9L)).thenReturn(List.of(a1));

        List<AuditLogDTO> list = service.findByUser(9L);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo(7L);
        assertThat(list.get(0).getUserId()).isEqualTo(9L);
    }

    @Test
    void delete_success() {
        AuditLog a = AuditLog.builder().id(12L).build();
        when(auditLogRepository.findById(12L)).thenReturn(Optional.of(a));
        doNothing().when(auditLogRepository).delete(a);

        service.delete(12L);
        verify(auditLogRepository).delete(a);
    }

    @Test
    void delete_notFound_throws() {
        when(auditLogRepository.findById(13L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(13L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Audit log with id 13 not found");
    }
}
