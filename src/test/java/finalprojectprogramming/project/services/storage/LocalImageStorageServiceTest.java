package finalprojectprogramming.project.services.storage;

import finalprojectprogramming.project.exceptions.StorageException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LocalImageStorageServiceTest {

    private Path tempDir;

    private LocalImageStorageService newService(String baseUrl) throws IOException {
        tempDir = Files.createTempDirectory("uploads-test");
        // base path per-test, public url configurable
        return new LocalImageStorageService(tempDir.toString(), baseUrl, "image/jpeg,image/png,image/webp", 5_000_000L);
    }

    @AfterEach
    void cleanup() throws IOException {
        if (tempDir != null && Files.exists(tempDir)) {
            // best-effort cleanup recursive
            Files.walk(tempDir)
                    .sorted((a,b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void store_success_with_filename_extension() throws Exception {
        LocalImageStorageService service = newService("/uploads");
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "data".getBytes());

        String url = service.store(5L, file);

        assertThat(url).startsWith("/uploads/5/");
        assertThat(url).endsWith(".png");

        // file exists on disk
        String filename = url.substring(url.lastIndexOf('/') + 1);
        Path stored = tempDir.resolve("5").resolve(filename);
        assertThat(Files.exists(stored)).isTrue();
    }

    @Test
    void store_adds_extension_from_contentType_when_missing() throws Exception {
        LocalImageStorageService service = newService("/uploads");
        MockMultipartFile file = new MockMultipartFile("file", "photo", "image/jpeg", "data".getBytes());

        String url = service.store(7L, file);
        assertThat(url).startsWith("/uploads/7/");
        assertThat(url).endsWith(".jpg");
    }

    @Test
    void store_adds_extension_from_webp_and_rejects_gif_contentType_when_missing() throws Exception {
        LocalImageStorageService service = newService("/uploads");
        MockMultipartFile webp = new MockMultipartFile("file", "img", "image/webp", new byte[]{1});
        String urlWebp = service.store(8L, webp);
        assertThat(urlWebp).startsWith("/uploads/8/");
        assertThat(urlWebp).endsWith(".webp");

        MockMultipartFile gif = new MockMultipartFile("file", "img", "image/gif", new byte[]{1});
        assertThatThrownBy(() -> service.store(9L, gif))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Unsupported content type");
    }

    @Test
    void constructor_normalizes_public_url_and_works_with_trailing_slashes() throws Exception {
        LocalImageStorageService service = newService("uploads///");
        MockMultipartFile file = new MockMultipartFile("file", "photo.PNG", "image/png", new byte[]{1});

        String url = service.store(5L, file);
        // normalized must start with /uploads
        assertThat(url).startsWith("/uploads/5/");
        // extension should be lowercased from original filename
        assertThat(url).endsWith(".png");
    }

    @Test
    void allowedTypes_blank_disables_validation_and_unknown_types_are_allowed() throws Exception {
        tempDir = Files.createTempDirectory("uploads-test-blank");
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/pub", "", 5_000_000L);
        MockMultipartFile file = new MockMultipartFile("file", "scan.bmp", "image/tiff", new byte[]{1});

        String url = service.store(2L, file);
        assertThat(url).startsWith("/pub/2/");
        assertThat(url).endsWith(".bmp");
    }

    @Test
    void allowedTypes_blank_and_no_extension_or_content_type_results_in_filename_without_extension() throws Exception {
        tempDir = Files.createTempDirectory("uploads-test-blank2");
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/pub", "", 5_000_000L);

        MockMultipartFile file = new MockMultipartFile("file", (String) null, null, new byte[]{1,2,3});
        String url = service.store(11L, file);
        assertThat(url).startsWith("/pub/11/");
        assertThat(url).doesNotEndWith(".");
    }

    @Test
    void allowedTypes_blank_allows_gif_and_extension_is_inferred_from_contentType() throws Exception {
        tempDir = Files.createTempDirectory("uploads-test-gif");
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/pub", "", 5_000_000L);

        // No filename extension, but contentType is image/gif -> resolveExtension should map to .gif
        MockMultipartFile file = new MockMultipartFile("file", "image", "image/gif", new byte[]{1,2});

        String url = service.store(21L, file);
        assertThat(url).startsWith("/pub/21/");
        assertThat(url).endsWith(".gif");
    }

    @Test
    void allowedTypes_enforced_rejects_missing_contentType_and_extension() throws Exception {
        // allowedTypes NO vacío -> si no hay contentType ni extensión, debe rechazar
        tempDir = java.nio.file.Files.createTempDirectory("uploads-test-enforced");
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/pub", "image/png,image/jpeg", 5_000_000L);

        // Archivo sin extensión y sin contentType
        org.springframework.web.multipart.MultipartFile file = org.mockito.Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        org.mockito.Mockito.when(file.isEmpty()).thenReturn(false);
        org.mockito.Mockito.when(file.getSize()).thenReturn(10L);
        org.mockito.Mockito.when(file.getOriginalFilename()).thenReturn(null);
        org.mockito.Mockito.when(file.getContentType()).thenReturn(null);
        org.mockito.Mockito.when(file.getInputStream()).thenReturn(new java.io.ByteArrayInputStream(new byte[]{1}));

        assertThatThrownBy(() -> service.store(1L, file))
                .isInstanceOf(finalprojectprogramming.project.exceptions.StorageException.class)
                .hasMessageContaining("Unsupported content type");
    }

    @Test
    void store_rejects_invalid_spaceId_or_empty_file_or_size_too_large() throws Exception {
        LocalImageStorageService service = newService("/uploads");

        MockMultipartFile empty = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);
        assertThatThrownBy(() -> service.store(null, empty))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Space id must be provided");
        assertThatThrownBy(() -> service.store(0L, empty))
                .isInstanceOf(StorageException.class);
        assertThatThrownBy(() -> service.store(1L, empty))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("must not be empty");

        // oversize
        LocalImageStorageService smallMax = new LocalImageStorageService(tempDir.toString(), "/uploads", "image/png", 1);
        MockMultipartFile big = new MockMultipartFile("file", "a.png", "image/png", new byte[]{1,2});
        assertThatThrownBy(() -> smallMax.store(1L, big))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    void store_rejects_unsupported_content_type() throws Exception {
        LocalImageStorageService service = new LocalImageStorageService(
                Files.createTempDirectory("uploads-test2").toString(), "/uploads", "image/png", 5_000_000L);
        MockMultipartFile file = new MockMultipartFile("file", "a.gif", "image/gif", new byte[]{1});
        assertThatThrownBy(() -> service.store(2L, file))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Unsupported content type");
    }

    @Test
    void store_rejects_null_file_reference() throws Exception {
    LocalImageStorageService service = newService("/uploads");
    assertThatThrownBy(() -> service.store(1L, null))
        .isInstanceOf(StorageException.class)
        .hasMessageContaining("must not be empty");
    }

    @Test
    void store_fails_when_directory_creation_fails() throws Exception {
        LocalImageStorageService service = newService("/uploads");
        // Build the space directory path we expect
        Path spaceDir = tempDir.resolve("9");

        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            // Allow root creation in constructor and path resolution inside the test
            files.when(() -> Files.createDirectories(tempDir)).thenAnswer(inv -> tempDir);
            // For any other createDirectories call, throw when it's the spaceDir
            files.when(() -> Files.createDirectories(spaceDir)).thenThrow(new IOException("nope"));
            // Allow copy and exists calls to hit real methods by default
            files.when(() -> Files.copy(any(InputStream.class), any(Path.class), any(StandardCopyOption.class)))
                    .thenAnswer(inv -> 1L);

            MultipartFile mf = mock(MultipartFile.class);
            when(mf.isEmpty()).thenReturn(false);
            when(mf.getSize()).thenReturn(10L);
            when(mf.getContentType()).thenReturn("image/png");
            when(mf.getOriginalFilename()).thenReturn("x.png");
            when(mf.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));

            assertThatThrownBy(() -> service.store(9L, mf))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Failed to create directory");
        }
    }

    @Test
    void store_fails_when_copy_throws() throws Exception {
        LocalImageStorageService service = newService("/uploads");

        MultipartFile mf = mock(MultipartFile.class);
        when(mf.isEmpty()).thenReturn(false);
        when(mf.getSize()).thenReturn(10L);
        when(mf.getContentType()).thenReturn("image/png");
        when(mf.getOriginalFilename()).thenReturn("x.png");
        when(mf.getInputStream()).thenThrow(new IOException("boom"));

        assertThatThrownBy(() -> service.store(3L, mf))
                .isInstanceOf(StorageException.class)
                .hasMessageContaining("Failed to store image");
    }

    @Test
    void delete_ignores_blank_or_unmatched_urls_and_prevents_outside_root() throws Exception {
        LocalImageStorageService service = newService("/uploads");

        // no-ops
        service.delete(null);
        service.delete("   ");
        service.delete("/otherprefix/1/a.png");

        // outside root should be ignored (path traversal attempt)
        service.delete("/uploads/../secret.txt");
    }

    @Test
    void delete_happy_path_and_failure_propagation() throws Exception {
        LocalImageStorageService service = newService("/uploads");

        // create a file to delete
        Path dir = tempDir.resolve("5");
        Files.createDirectories(dir);
        Path file = dir.resolve("a.png");
        Files.writeString(file, "x");

        // Happy path
        service.delete("/uploads/5/a.png?cache=bust");
        assertThat(Files.exists(file)).isFalse();

        // Recreate and cause delete failure
        Files.writeString(file, "x");
        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.deleteIfExists(file)).thenThrow(new IOException("nope"));
            assertThatThrownBy(() -> service.delete("/uploads/5/a.png"))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Failed to delete image");
        }
    }

    @Test
    void delete_handles_url_starting_with_prefix_without_slash_after() throws Exception {
        // This exercises extractRelativePath branch where normalized.startsWith(urlPrefix) but not urlPrefixWithSlash
        LocalImageStorageService service = newService("/uploads");

        // create the file named "foo" directly under the root (no space folder), so removing without slash resolves correctly
        Path file = tempDir.resolve("foo");
        Files.writeString(file, "x");

        service.delete("/uploadsfoo");
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void delete_supports_windows_style_backslashes_in_url() throws Exception {
        LocalImageStorageService service = newService("/uploads");

        // Create a file to delete
        Path dir = tempDir.resolve("6");
        Files.createDirectories(dir);
        Path file = dir.resolve("a.png");
        Files.writeString(file, "x");

        // Use backslashes in the URL (Windows-style); service should normalize and delete
        service.delete("\\uploads\\6\\a.png");
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void ensureTrailingSlash_keeps_root_slash_and_extractRelativePath_removes_leading_slash() throws Exception {
        // Asegurar tempDir inicializado para este test independiente
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("uploads-test-root");
        }
        // publicUrl "/" ensures ensureTrailingSlash returns value as-is (covers the true branch)
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/", "image/png", 1024);

        // Create a file directly under root to delete via path starting with prefix then slash
        Path file = tempDir.resolve("bar");
        Files.writeString(file, "x");

        // This hits extractRelativePath branch where normalized.startsWith(urlPrefix) and remaining starts with '/'
        service.delete("/" + "bar"); // effectively "/bar"
        assertThat(Files.exists(file)).isFalse();
    }

    @Test
    void store_infers_png_extension_when_missing_filename_and_unknown_contentType_yields_no_extension() throws Exception {
        // PNG inference when no filename extension
        LocalImageStorageService svc = newService("/uploads");
        MockMultipartFile pngNoExt = new MockMultipartFile("file", "image", "image/png", new byte[]{1});
        String urlPng = svc.store(31L, pngNoExt);
        assertThat(urlPng).endsWith(".png");

        // Unknown contentType with allowedTypes blank -> no extension (covers default branch in switch)
        LocalImageStorageService svc2 = new LocalImageStorageService(tempDir.toString(), "/pub", "", 5_000_000L);
        MockMultipartFile unknown = new MockMultipartFile("file", "pic", "application/octet-stream", new byte[]{1});
        String urlUnknown = svc2.store(32L, unknown);
        assertThat(urlUnknown).startsWith("/pub/32/");
        assertThat(urlUnknown).doesNotEndWith(".");
    }

    @Test
    void store_with_allowedTypes_blank_hits_png_switch_case() throws Exception {
        // Fuerza el uso del switch de contentType y el case image/png, sin bloqueo por allowedTypes
        tempDir = Files.createTempDirectory("uploads-test-png-case");
        LocalImageStorageService service = new LocalImageStorageService(tempDir.toString(), "/pub", "", 5_000_000L);
        MockMultipartFile file = new MockMultipartFile("file", "noext", "image/png", new byte[]{1});

        String url = service.store(41L, file);
        assertThat(url).startsWith("/pub/41/");
        assertThat(url).endsWith(".png");
    }

    @Test
    void constructor_fails_when_root_directory_creation_fails() throws Exception {
        Path bogus = Path.of("/definitely/nonexistent/dir-" + System.nanoTime());
        try (MockedStatic<Files> files = Mockito.mockStatic(Files.class)) {
            files.when(() -> Files.createDirectories(any(Path.class))).thenThrow(new IOException("nope"));

            assertThatThrownBy(() -> new LocalImageStorageService(bogus.toString(), "/uploads", "image/png", 1024))
                    .isInstanceOf(StorageException.class)
                    .hasMessageContaining("Unable to initialize storage directory");
        }
    }
}
