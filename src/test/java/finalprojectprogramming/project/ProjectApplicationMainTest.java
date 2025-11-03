package finalprojectprogramming.project;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

class ProjectApplicationMainTest {

    @Test
    void main_starts_application_context() {
        // Evita arrancar el contexto real: mockeamos la llamada estática a SpringApplication.run
        try (MockedStatic<SpringApplication> spring = Mockito.mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext ctx = Mockito.mock(ConfigurableApplicationContext.class);
            spring.when(() -> SpringApplication.run(
                    Mockito.eq(ProjectApplication.class),
                    Mockito.any(String[].class))
            ).thenReturn(ctx);

            ProjectApplication.main(new String[]{"--spring.main.web-application-type=none"});

            spring.verify(() -> SpringApplication.run(
                    Mockito.eq(ProjectApplication.class),
                    Mockito.any(String[].class))
            );
        }
    }
}
