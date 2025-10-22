package finalprojectprogramming.project.security.jwt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import finalprojectprogramming.project.security.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtService jwtService;
    private final AppUserDetailsService userDetailsService;

    public JwtAuthFilter(JwtService jwtService, AppUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String prefix = "Bearer ";

        if (authHeader == null || !authHeader.startsWith(prefix)) {
            continueWithoutAuthentication(filterChain, request, response);
            return;
        }

        final String jwt = authHeader.substring(prefix.length());

        String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
           LOGGER.warn("Failed to extract username from JWT", e);
            continueWithoutAuthentication(filterChain, request, response);
            return;
        }

        if (username == null) {
            continueWithoutAuthentication(filterChain, request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(username);
        } catch (Exception ex) {
            LOGGER.warn("Failed to load user details for JWT", ex);
            continueWithoutAuthentication(filterChain, request, response);
            return;
        }

        if (!jwtService.isTokenValid(jwt, userDetails.getUsername())) {
            LOGGER.warn("Invalid JWT token");
            continueWithoutAuthentication(filterChain, request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }

    private void continueWithoutAuthentication(
            FilterChain filterChain,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        
        filterChain.doFilter(request, response);
    }
}