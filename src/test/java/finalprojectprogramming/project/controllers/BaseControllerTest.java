package finalprojectprogramming.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import finalprojectprogramming.project.security.AppUserDetailsService;
import finalprojectprogramming.project.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(withJsonBody(post(url), body));
    }

    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(withJsonBody(put(url), body));
    }

    protected ResultActions performPatch(String url, Object body) throws Exception {
        return mockMvc.perform(withJsonBody(patch(url), body));
    }

    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url).contentType(MediaType.APPLICATION_JSON));
    }

    protected ResultActions performMultipart(String url, MockMultipartFile file, MultiValueMap<String, String> parameters)
            throws Exception {
        MockMultipartHttpServletRequestBuilder builder = multipart(url).file(file);
        if (parameters != null) {
            parameters.forEach((key, values) -> {
                for (String value : values) {
                    builder.param(key, value);
                }
            });
        }
        builder.contentType(MediaType.MULTIPART_FORM_DATA);
        return mockMvc.perform(builder);
    }

    private MockHttpServletRequestBuilder withJsonBody(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return builder
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(registry -> registry.anyRequest().permitAll());
            return http.build();
        }
    }
}
