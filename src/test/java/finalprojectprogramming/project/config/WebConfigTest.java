package finalprojectprogramming.project.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class WebConfigTest {

    @Test
    void adds_resource_handler_with_normalized_url_and_location() throws Exception {
        Path temp = Files.createTempDirectory("static-root");
        String basePath = temp.toString();
        String publicUrl = "pub///";

        WebConfig config = new WebConfig(basePath, publicUrl);

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(any(String.class))).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.addResourceHandlers(registry);

        // verify pattern
        ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
        verify(registry).addResourceHandler(patternCaptor.capture());
        assertThat(patternCaptor.getValue()).isEqualTo("/pub/**");

        // verify location ends with slash and equals the directory URI
        ArgumentCaptor<String> locCaptor = ArgumentCaptor.forClass(String.class);
        verify(registration).addResourceLocations(locCaptor.capture());
        String expectedLocation = temp.toAbsolutePath().normalize().toUri().toString();
        if (!expectedLocation.endsWith("/")) {
            expectedLocation = expectedLocation + "/";
        }
        assertThat(locCaptor.getValue()).isEqualTo(expectedLocation);
    }

    @Test
    void defaults_when_blank_public_url() throws Exception {
        Path temp = Files.createTempDirectory("static-root2");
        WebConfig config = new WebConfig(temp.toString(), "   ");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(any(String.class))).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.addResourceHandlers(registry);
        ArgumentCaptor<String> patternCaptor = ArgumentCaptor.forClass(String.class);
        verify(registry).addResourceHandler(patternCaptor.capture());
        assertThat(patternCaptor.getValue()).isEqualTo("/uploads/**");
    }

    @Test
    void appends_trailing_slash_when_missing_in_location_uri() throws Exception {
        // Use a temp file path (URI typically without trailing slash) to hit the branch that appends '/'
        Path tempFile = Files.createTempFile("static-file-base", ".tmp");
        WebConfig config = new WebConfig(tempFile.toString(), "/assets");

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler(any(String.class))).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        config.addResourceHandlers(registry);

        ArgumentCaptor<String> locCaptor = ArgumentCaptor.forClass(String.class);
        verify(registration).addResourceLocations(locCaptor.capture());
        String expected = tempFile.toAbsolutePath().normalize().toUri().toString();
        if (!expected.endsWith("/")) {
            expected = expected + "/";
        }
        // Ensure the code appended the trailing slash when missing
        assertThat(locCaptor.getValue()).isEqualTo(expected);
    }
}
