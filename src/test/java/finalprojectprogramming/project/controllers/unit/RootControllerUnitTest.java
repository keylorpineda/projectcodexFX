package finalprojectprogramming.project.controllers.unit;

import finalprojectprogramming.project.controllers.RootController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RootControllerUnitTest {

    @Test
    void rootReturnsOkStatusJson() {
        RootController controller = new RootController();

        ResponseEntity<Map<String, String>> resp = controller.root();

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody()).containsEntry("status", "ok");
    }
}
