package finalprojectprogramming.project.controllers;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Simple controller to expose a root endpoint that can be used by load balancers
 * and smoke tests to verify that the service is up.
 */
@RestController
@Tag(name = "Health Check", description = "🏥 Service health and status endpoints")
public class RootController {

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Health check endpoint",
        description = """
            Simple health check endpoint that returns service status.
            
            **Use cases:**
            - Load balancer health checks
            - Smoke tests
            - Verify service is running
            - Monitoring systems
            
            **No authentication required** - Public endpoint
            """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Service is running and healthy")
    })
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}