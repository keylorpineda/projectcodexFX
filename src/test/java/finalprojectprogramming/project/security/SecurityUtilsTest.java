package finalprojectprogramming.project.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import finalprojectprogramming.project.models.User;
import finalprojectprogramming.project.models.enums.UserRole;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityUtilsTest {

    @BeforeEach
    void clearContextBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContextAfter() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void requireAny_allowsWhenRoleMatches() {
        setAuthentication(new AppUserDetails(userWithRole(UserRole.ADMIN)), "ROLE_ADMIN");

        assertThatCode(() -> SecurityUtils.requireAny(UserRole.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void requireAny_deniesWhenRoleMissing() {
        setAuthentication(new AppUserDetails(userWithRole(UserRole.USER)), "ROLE_USER");

        assertThatThrownBy(() -> SecurityUtils.requireAny(UserRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access is denied.");
    }

    @Test
    void requireAny_deniesWhenAuthoritiesAreAbsent() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(new AppUserDetails(userWithRole(UserRole.USER)), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThatThrownBy(() -> SecurityUtils.requireAny(UserRole.USER))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access is denied.");
    }

    @Test
    void requireAny_withoutAuthenticationThrows() {
        assertThatThrownBy(() -> SecurityUtils.requireAny(UserRole.ADMIN))
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authentication is required to access this resource.");
    }

    @Test
    void requireSelfOrAny_allowsSameUserWithoutRoleCheck() {
        User current = userWithIdAndRole(42L, UserRole.USER);
        setAuthentication(new AppUserDetails(current), "ROLE_USER");

        assertThatCode(() -> SecurityUtils.requireSelfOrAny(42L, UserRole.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void requireSelfOrAny_allowsDifferentUserWithAllowedRole() {
        User current = userWithIdAndRole(7L, UserRole.ADMIN);
        setAuthentication(new AppUserDetails(current), "ROLE_ADMIN");

        assertThatCode(() -> SecurityUtils.requireSelfOrAny(42L, UserRole.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void requireSelfOrAny_deniesDifferentUserWithoutAllowedRole() {
        User current = userWithIdAndRole(7L, UserRole.USER);
        setAuthentication(new AppUserDetails(current), "ROLE_USER");

        assertThatThrownBy(() -> SecurityUtils.requireSelfOrAny(42L, UserRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access is denied.");
    }

    @Test
    void requireSelfOrAny_allowsSameUserWhenPrincipalIsDomainUser() {
        User current = userWithIdAndRole(10L, UserRole.USER);
        // Use domain User as principal (not AppUserDetails) to cover alternate isSameUser branch
        setAuthentication(current, "ROLE_USER");

        assertThatCode(() -> SecurityUtils.requireSelfOrAny(10L, UserRole.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void requireSelfOrAny_withNullTargetUserId_requiresRoleAndDenies() {
        User current = userWithIdAndRole(7L, UserRole.USER);
        setAuthentication(current, "ROLE_USER");

        assertThatThrownBy(() -> SecurityUtils.requireSelfOrAny(null, UserRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access is denied.");
    }

    @Test
    void hasAny_returnsFalseWhenNoAuthenticationPresent() {
        assertThat(SecurityUtils.hasAny(UserRole.ADMIN)).isFalse();
    }

    @Test
    void hasAny_returnsFalseWhenAuthoritiesNullOrEmpty() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(new AppUserDetails(userWithRole(UserRole.USER)), "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(SecurityUtils.hasAny(UserRole.USER)).isFalse();
    }

    @Test
    void hasAny_supportsRolesWithAndWithoutPrefix() {
        setAuthentication(new AppUserDetails(userWithRole(UserRole.ADMIN)), "ROLE_ADMIN", "USER");

        assertThat(SecurityUtils.hasAny(UserRole.ADMIN)).isTrue();
        assertThat(SecurityUtils.hasAny(UserRole.USER)).isTrue();
    }

    @Test
    void getCurrentUserId_returnsIdForAppUserDetails() {
        User current = userWithIdAndRole(5L, UserRole.SUPERVISOR);
        setAuthentication(new AppUserDetails(current), "ROLE_SUPERVISOR");

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(5L);
    }

    @Test
    void getCurrentUserId_returnsNullForSpringUserDetails() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("someone@example.com")
                .password("password")
                .roles("USER")
                .build();

        setAuthentication(principal, "ROLE_USER");

        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }

    @Test
    void getCurrentUserId_returnsIdForDomainUserPrincipal() {
        User domainUser = userWithIdAndRole(99L, UserRole.USER);
        setAuthentication(domainUser, "ROLE_USER");

        assertThat(SecurityUtils.getCurrentUserId()).isEqualTo(99L);
    }

    @Test
    void getCurrentUserId_withoutAuthenticationThrows() {
        assertThatThrownBy(SecurityUtils::getCurrentUserId)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class)
                .hasMessage("Authentication is required to access this resource.");
    }

    @Test
    void getCurrentUserId_returnsNullForUnknownPrincipal() {
        // Principal no reconocido (ni AppUserDetails, ni Spring UserDetails, ni dominio User)
        setAuthentication("anon", "ROLE_USER");

        assertThat(SecurityUtils.getCurrentUserId()).isNull();
    }

    @Test
    void requireSelfOrAny_denies_with_unknown_principal() {
        // Principal desconocido no puede ser el mismo usuario; requiere rol permitido
        setAuthentication("anon", "ROLE_USER"); // no ADMIN

        assertThatThrownBy(() -> SecurityUtils.requireSelfOrAny(1L, UserRole.ADMIN))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Access is denied.");
    }

    @Test
    void private_constructor_is_invokable_via_reflection() throws Exception {
        var ctor = SecurityUtils.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        // should not throw
        ctor.newInstance();
    }

    private void setAuthentication(Object principal, String... authorities) {
        List<SimpleGrantedAuthority> grantedAuthorities = authorities == null
                ? List.of()
                : Arrays.stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, "password", grantedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private User userWithRole(UserRole role) {
        return User.builder()
                .id(1L)
                .role(role)
                .email("user@domain.test")
                .active(true)
                .build();
    }

    private User userWithIdAndRole(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .role(role)
                .email("user" + id + "@domain.test")
                .active(true)
                .build();
    }
}
