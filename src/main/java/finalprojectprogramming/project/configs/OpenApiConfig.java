package finalprojectprogramming.project.configs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Municipality Facilities Reservation API",
                description = "REST API for managing spaces, users and reservations for the final project",
                version = "1.0.0",
                contact = @Contact(name = "Proyecto Final", email = "support@example.com"),
                license = @License(name = "Apache 2.0")))
public class OpenApiConfig {
}