package finalprojectprogramming.project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
@SpringBootTest
@ActiveProfiles("test")
class ProjectApplicationTests {

	@Test
    void contextLoads() {
        // Mantener este smoke test garantiza que el contexto completo de Spring Boot
        // se levante correctamente antes de ejecutar suites más específicas.
    }

}
