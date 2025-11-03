package finalprojectprogramming.project.services.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.dtos.UserInputDTO;
import finalprojectprogramming.project.dtos.UserOutputDTO;
import finalprojectprogramming.project.exceptions.BusinessRuleException;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.AuditLog;
import finalprojectprogramming.project.models.Reservation;
import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;
import finalprojectprogramming.project.security.SecurityUtils;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
import finalprojectprogramming.project.transformers.GenericMapperFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplementationTest {

    private UserRepository repository;
    private GenericMapperFactory mapperFactory;
    private ModelMapper modelMapper;
    private AuditLogService auditLogService;
    private ObjectMapper objectMapper;
    private UserServiceImplementation service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        modelMapper = new ModelMapper();
        mapperFactory = new GenericMapperFactory(modelMapper);
        auditLogService = mock(AuditLogService.class);
        objectMapper = new ObjectMapper();
        service = new UserServiceImplementation(repository, mapperFactory, modelMapper, auditLogService, objectMapper);
    }

    @Test
    void create_requires_admin_and_initializes_defaults() {
        UserInputDTO input = UserInputDTO.builder()
                .role(UserRole.USER).name("Ana").email("ana@example.com").active(true).build();

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            when(repository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(1L);
                return u;
            });

            UserOutputDTO out = service.create(input);
            assertThat(out.getId()).isEqualTo(1L);
            verify(repository).save(any(User.class));
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN));
        }
    }

    @Test
    void create_rejects_duplicate_email() {
        UserInputDTO input = UserInputDTO.builder()
                .role(UserRole.USER).name("Ana").email("dup@example.com").active(true).build();
        when(repository.findByEmail("dup@example.com")).thenReturn(Optional.of(User.builder().id(2L).build()));

        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> service.create(input)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    void update_maps_fields_and_checks_email_uniqueness() {
        User existing = User.builder().id(5L).email("old@example.com").name("Old").role(UserRole.USER).build();
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            UserOutputDTO out = service.update(5L, UserInputDTO.builder()
                    .role(UserRole.ADMIN).name("New").email("new@example.com").active(true).build());
            assertThat(out.getName()).isEqualTo("New");
            assertThat(out.getRole()).isEqualTo(UserRole.ADMIN);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN));
        }
    }

    @Test
    void update_throws_when_not_found_or_duplicate_email() {
        when(repository.findById(9L)).thenReturn(Optional.empty());
        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> service.update(9L, UserInputDTO.builder().build()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        User existing = User.builder().id(6L).email("a@a").build();
        when(repository.findById(6L)).thenReturn(Optional.of(existing));
        when(repository.findByEmail("dup@example.com")).thenReturn(Optional.of(User.builder().id(7L).build()));
        try (MockedStatic<SecurityUtils> ignored = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> service.update(6L, UserInputDTO.builder().email("dup@example.com").build()))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }

    @Test
    void findById_findAll_delete_enforce_security_and_map_ids() {
        Reservation r1 = Reservation.builder().id(1L).build();
        Reservation r2 = Reservation.builder().id(null).build();
        Reservation a1 = Reservation.builder().id(2L).build();
        AuditLog log1 = AuditLog.builder().id(10L).build();
        User user = User.builder()
                .id(3L).email("x@y").name("X").role(UserRole.USER)
                .reservations(List.of(r1, r2))
                .approvedReservations(List.of(a1))
                .auditLogs(List.of(log1))
                .build();

        when(repository.findById(3L)).thenReturn(Optional.of(user));
        when(repository.findAll()).thenReturn(List.of(user));

        try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
            UserOutputDTO byId = service.findById(3L);
            assertThat(byId.getReservationIds()).containsExactly(1L);
            assertThat(byId.getApprovedReservationIds()).containsExactly(2L);
            assertThat(byId.getAuditLogIds()).containsExactly(10L);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR));

            List<UserOutputDTO> all = service.findAll();
            assertThat(all).hasSize(1);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN, UserRole.SUPERVISOR), times(2));

            service.delete(3L);
            mocked.verify(() -> SecurityUtils.requireAny(UserRole.ADMIN));
        }

        verify(repository, atLeastOnce()).save(any(User.class));
    }
}
