package finalprojectprogramming.project.configs;

import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AdminUserInitializerTest {

    private AdminUserProperties props(boolean enabled, String email, String name) {
        AdminUserProperties p = new AdminUserProperties();
        p.setEnabled(enabled);
        p.setEmail(email);
        p.setName(name);
        return p;
    }

    @Test
    void does_nothing_when_disabled_or_missing_email() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        AdminUserInitializer cfg = new AdminUserInitializer();

        cfg.adminProvisioningRunner(props(false, "admin@example.com", "Admin"), repo).run(null);
        verifyNoInteractions(repo);

        cfg.adminProvisioningRunner(props(true, "", "Admin"), repo).run(null);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void updates_existing_user_to_admin_and_active_and_name() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        AdminUserInitializer cfg = new AdminUserInitializer();

        User existing = User.builder()
                .id(1L)
                .email("admin@example.com")
                .name("Someone")
                .role(UserRole.USER)
                .active(false)
                .deletedAt(LocalDateTime.now())
                .build();

        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        cfg.adminProvisioningRunner(props(true, "admin@example.com", "Administrator"), repo).run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getDeletedAt()).isNull();
        assertThat(saved.getName()).isEqualTo("Administrator");
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void leaves_existing_admin_unchanged_if_up_to_date() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        AdminUserInitializer cfg = new AdminUserInitializer();

        User existing = User.builder()
                .id(2L)
                .email("admin@example.com")
                .name("Administrator")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));

        cfg.adminProvisioningRunner(props(true, "admin@example.com", "Administrator"), repo).run(null);

        verify(repo, never()).save(any());
    }

    @Test
    void creates_admin_when_missing() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        AdminUserInitializer cfg = new AdminUserInitializer();

        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        cfg.adminProvisioningRunner(props(true, "admin@example.com", "Administrator"), repo).run(null);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getEmail()).isEqualTo("admin@example.com");
        assertThat(saved.getName()).isEqualTo("Administrator");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updates_only_email_when_different_without_saving_other_fields() throws Exception {
        UserRepository repo = mock(UserRepository.class);
        AdminUserInitializer cfg = new AdminUserInitializer();

        // Existing admin is already up-to-date except for email value
        User existing = User.builder()
                .id(3L)
                .email("old@example.com")
                .name("Administrator")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        when(repo.findByEmail("admin@example.com")).thenReturn(Optional.of(existing));

        // Run: only the email should be set on the entity, but no save() should happen
        cfg.adminProvisioningRunner(props(true, "admin@example.com", "Administrator"), repo).run(null);

        // verify that the in-memory entity got its email changed
        assertThat(existing.getEmail()).isEqualTo("admin@example.com");
        // and no persistence update since nothing else required changes
        verify(repo, never()).save(any());
    }
}
