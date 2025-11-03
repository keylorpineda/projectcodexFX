package finalprojectprogramming.project.security;

import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class AppUserDetailsServiceTest {

    @Test
    void loadUserByUsername_returns_details_when_found() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        AppUserDetailsService svc = new AppUserDetailsService(repo);
        User u = User.builder().id(1L).email("u@x").active(true).build();
        when(repo.findByEmail("u@x")).thenReturn(Optional.of(u));
        var details = svc.loadUserByUsername("u@x");
        assertThat(details).isInstanceOf(AppUserDetails.class);
        assertThat(((AppUserDetails) details).getDomainUser()).isSameAs(u);
    }

    @Test
    void loadUserByUsername_throws_when_not_found() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        AppUserDetailsService svc = new AppUserDetailsService(repo);
        when(repo.findByEmail("missing@x")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> svc.loadUserByUsername("missing@x"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
