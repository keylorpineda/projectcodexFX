package finalprojectprogramming.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@AutoConfigureMockMvc(addFilters = false)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

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

    private MockHttpServletRequestBuilder withJsonBody(MockHttpServletRequestBuilder builder, Object body) throws Exception {
        return builder
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body));
    }

    @Configuration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }
}
