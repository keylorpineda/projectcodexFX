package finalprojectprogramming.project.security;

import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppUserDetailsTest {

    @Test
    void authorities_empty_when_role_null_and_populated_when_present() {
        User u1 = User.builder().email("a@b.com").active(true).role(null).build();
        AppUserDetails d1 = new AppUserDetails(u1);
        assertThat(d1.getAuthorities()).isEmpty();

        User u2 = User.builder().email("a@b.com").active(true).role(UserRole.ADMIN).build();
        AppUserDetails d2 = new AppUserDetails(u2);
        assertThat(d2.getAuthorities())
                .extracting(Object::toString)
                .anyMatch(s -> s.contains("ROLE_ADMIN"));
    }

    @Test
    void enabled_and_account_flags_reflect_active_field() {
        User active = User.builder().email("x@y.com").active(true).build();
        AppUserDetails ad = new AppUserDetails(active);
        assertThat(ad.isEnabled()).isTrue();
        assertThat(ad.isAccountNonExpired()).isTrue();
        assertThat(ad.isAccountNonLocked()).isTrue();
        assertThat(ad.isCredentialsNonExpired()).isTrue();
        assertThat(ad.getUsername()).isEqualTo("x@y.com");
        assertThat(ad.getPassword()).isNull();

        User inactive = User.builder().email("x@y.com").active(false).build();
        AppUserDetails id = new AppUserDetails(inactive);
        assertThat(id.isEnabled()).isFalse();
        assertThat(id.isAccountNonExpired()).isFalse();
        assertThat(id.isAccountNonLocked()).isFalse();
    }
}
