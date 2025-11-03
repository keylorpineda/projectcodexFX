package finalprojectprogramming.project.services.storage;

import finalprojectprogramming.project.exceptions.StorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalImageStorageService implements ImageStorageService {

    private final Path rootLocation;
    private final String urlPrefix;
    private final String urlPrefixWithSlash;
    private final Set<String> allowedContentTypes;
    private final long maxFileSize;

    public LocalImageStorageService(
            @Value("${storage.images.base-path:storage/uploads}") String basePath,
            @Value("${storage.images.base-url:/uploads}") String publicUrl,
            @Value("${storage.images.allowed-content-types:image/jpeg,image/png,image/webp}") String allowedTypes,
            @Value("${storage.images.max-file-size:5242880}") long maxFileSize) {
        this.rootLocation = Paths.get(basePath).toAbsolutePath().normalize();
        this.urlPrefix = normalizeUrlPrefix(publicUrl);
        this.urlPrefixWithSlash = ensureTrailingSlash(this.urlPrefix);
        this.allowedContentTypes = parseAllowedContentTypes(allowedTypes);
        this.maxFileSize = maxFileSize;
        createRootDirectory();
    }

    @Override
    public String store(Long spaceId, MultipartFile file) {
        if (spaceId == null || spaceId <= 0) {
            throw new StorageException("Space id must be provided to store an image");
        }
        if (file == null || file.isEmpty()) {
            throw new StorageException("File must not be empty");
        }
        if (file.getSize() > maxFileSize) {
            throw new StorageException("File exceeds maximum allowed size of " + maxFileSize + " bytes");
        }

        String contentType = file.getContentType();
        if (!allowedContentTypes.isEmpty()) {
            if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase(Locale.ROOT))) {
                throw new StorageException("Unsupported content type: " + contentType);
            }
        }

        String extension = resolveExtension(file, contentType);
        String filename = UUID.randomUUID().toString();
        if (extension != null && !extension.isBlank()) {
            filename += "." + extension;
        }

        Path spaceDirectory = rootLocation.resolve(String.valueOf(spaceId));
        try {
            Files.createDirectories(spaceDirectory);
        } catch (IOException exception) {
            throw new StorageException("Failed to create directory for space " + spaceId, exception);
        }

        Path destination = spaceDirectory.resolve(filename);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new StorageException("Failed to store image " + filename, exception);
        }

        return urlPrefix + "/" + spaceId + "/" + filename;
    }

    @Override
    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }
        String relativePath = extractRelativePath(imageUrl);
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path file = rootLocation.resolve(relativePath).normalize();
        if (!file.startsWith(rootLocation)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new StorageException("Failed to delete image: " + imageUrl, exception);
        }
    }

    private void createRootDirectory() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException exception) {
            throw new StorageException("Unable to initialize storage directory", exception);
        }
    }

    private Set<String> parseAllowedContentTypes(String allowedTypes) {
        if (allowedTypes == null || allowedTypes.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    private String normalizeUrlPrefix(String url) {
        String normalized = (url == null || url.isBlank()) ? "/uploads" : url.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private String ensureTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value;
        }
        return value + "/";
    }

    private String resolveExtension(MultipartFile file, String contentType) {
        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (extension != null && !extension.isBlank()) {
            return extension.toLowerCase(Locale.ROOT);
        }
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            default -> null;
        };
    }

    private String extractRelativePath(String imageUrl) {
        String normalized = imageUrl.replace("\\", "/");
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }
        int prefixIndex = normalized.indexOf(urlPrefixWithSlash);
        if (prefixIndex >= 0) {
            return normalized.substring(prefixIndex + urlPrefixWithSlash.length());
        }
        if (normalized.startsWith(urlPrefix)) {
            String remaining = normalized.substring(urlPrefix.length());
            if (remaining.startsWith("/")) {
                remaining = remaining.substring(1);
            }
            return remaining;
        }
        return null;
    }
}
