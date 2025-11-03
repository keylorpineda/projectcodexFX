package finalprojectprogramming.project.security;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityHandlersConfigTest {

    @Test
    void authenticationEntryPoint_writes_401_json() throws Exception {
        SecurityHandlersConfig cfg = new SecurityHandlersConfig();
        AuthenticationEntryPoint ep = cfg.authenticationEntryPoint();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        ep.commence(null, resp, new org.springframework.security.authentication.BadCredentialsException("x"));
        assertThat(resp.getStatus()).isEqualTo(401);
        assertThat(resp.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(resp.getContentAsString()).contains("unauthorized");
    }

    @Test
    void accessDeniedHandler_writes_403_json() throws Exception {
        SecurityHandlersConfig cfg = new SecurityHandlersConfig();
        AccessDeniedHandler h = cfg.accessDeniedHandler();
        MockHttpServletResponse resp = new MockHttpServletResponse();
        h.handle(null, resp, new org.springframework.security.access.AccessDeniedException("nope"));
        assertThat(resp.getStatus()).isEqualTo(403);
        assertThat(resp.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(resp.getContentAsString()).contains("forbidden");
    }
}
