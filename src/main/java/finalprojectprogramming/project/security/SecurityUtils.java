package finalprojectprogramming.project.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.models.User;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static void requireAny(UserRole... roles) {
        Authentication authentication = getAuthentication();
        if (!hasAnyRole(authentication, roles)) {
            throw new AccessDeniedException("Access is denied.");
        }
    }

    public static void requireSelfOrAny(Long targetUserId, UserRole... roles) {
        Authentication authentication = getAuthentication();
        if (isSameUser(authentication, targetUserId)) {
            return;
        }
        if (!hasAnyRole(authentication, roles)) {
            throw new AccessDeniedException("Access is denied.");
        }
    }

    public static boolean hasAny(UserRole... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return hasAnyRole(authentication, roles);
    }

    public static Long getCurrentUserId() {
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails details) {
            User user = details.getDomainUser();
            return user != null ? user.getId() : null;
        }
        if (principal instanceof UserDetails) {
            return null;
        }
        if (principal instanceof User user) {
            return user.getId();
        }
        return null;
    }

    private static Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AuthenticationCredentialsNotFoundException("Authentication is required to access this resource.");
        }
        return authentication;
    }

    private static boolean hasAnyRole(Authentication authentication, UserRole... roles) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (authorities == null || authorities.isEmpty()) {
            return false;
        }
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> Arrays.stream(roles)
                        .anyMatch(role -> authority.equals(role.name()) || authority.equals("ROLE_" + role.name())));
    }

    private static boolean isSameUser(Authentication authentication, Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails details) {
            User domainUser = details.getDomainUser();
            return domainUser != null && Objects.equals(domainUser.getId(), targetUserId);
        }
        if (principal instanceof User user) {
            return Objects.equals(user.getId(), targetUserId);
        }
        return false;
    }
}