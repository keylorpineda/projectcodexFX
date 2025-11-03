package finalprojectprogramming.project.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final Path storagePath;
    private final String resourceHandlerPattern;
    private final String resourceLocation;

    public WebConfig(@Value("${storage.images.base-path:storage/uploads}") String basePath,
            @Value("${storage.images.base-url:/uploads}") String publicUrl) {
        this.storagePath = Paths.get(basePath).toAbsolutePath().normalize();
        String normalizedUrl = normalizeUrl(publicUrl);
        this.resourceHandlerPattern = normalizedUrl + "/**";
        String location = storagePath.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        this.resourceLocation = location;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(resourceHandlerPattern)
                .addResourceLocations(resourceLocation);
    }

    private String normalizeUrl(String url) {
        String normalized = (url == null || url.isBlank()) ? "/uploads" : url.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
