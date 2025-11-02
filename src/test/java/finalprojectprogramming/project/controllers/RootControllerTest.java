package finalprojectprogramming.project.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RootController.class)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class RootControllerTest extends BaseControllerTest {

    @Test
    void rootEndpointReturnsOkPayload() throws Exception {
        performGet("/")
                .andExpect(status().isOk())
                .andExpect(content().json("{\"status\":\"ok\"}"));
    }
}
