package finalprojectprogramming.project.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import finalprojectprogramming.project.security.AppUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtAuthFilterTest {

    private JwtService jwtService;
    private AppUserDetailsService userDetailsService;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        jwtService = Mockito.mock(JwtService.class);
        userDetailsService = Mockito.mock(AppUserDetailsService.class);
        filter = new JwtAuthFilter(jwtService, userDetailsService);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private FilterChain chainSpy() throws IOException, ServletException {
        // Use a spy of a no-op chain to verify invocation
        return Mockito.spy(new FilterChain() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response)
                    throws IOException, ServletException {
                // no-op
            }
        });
    }

    @Test
    void continuesWhenNoAuthorizationHeader() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void continuesWhenHeaderWithoutBearerPrefix() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Token abc");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void continuesWhenExtractUsernameThrows() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        doThrow(new RuntimeException("parse error")).when(jwtService).extractUsername("token");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void continuesWhenExtractUsernameReturnsNull() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        when(jwtService.extractUsername("token")).thenReturn(null);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void skipsWhenAlreadyAuthenticated() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        when(jwtService.extractUsername("token")).thenReturn("user@example.com");

        // Put an existing Authentication in context
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null, java.util.List.of()));

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        // Should not attempt to load user or validate token further
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void continuesWhenUserDetailsServiceThrows() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        when(jwtService.extractUsername("token")).thenReturn("user@example.com");
        doThrow(new RuntimeException("load failed")).when(userDetailsService).loadUserByUsername("user@example.com");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void continuesWhenTokenInvalid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        when(jwtService.extractUsername("token")).thenReturn("user@example.com");
        UserDetails ud = User.withUsername("user@example.com").password("pwd").roles("USER").build();
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(ud);
        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(false);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void authenticatesWhenTokenValid() throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse res = new MockHttpServletResponse();
        FilterChain chain = chainSpy();

        when(jwtService.extractUsername("token")).thenReturn("user@example.com");
        UserDetails ud = User.withUsername("user@example.com").password("pwd").roles("USER").build();
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(ud);
        when(jwtService.isTokenValid("token", "user@example.com")).thenReturn(true);

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(ud);
    }
}
