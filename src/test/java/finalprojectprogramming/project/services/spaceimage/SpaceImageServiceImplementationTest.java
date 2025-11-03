package finalprojectprogramming.project.services.spaceimage;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.repositories.SpaceImageRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.services.storage.ImageStorageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.Mockito;

class SpaceImageServiceImplementationTest {

    private SpaceImageRepository repo;
    private SpaceRepository spaceRepo;
    private ModelMapper mapper;
    private ImageStorageService storage;
    private SpaceImageServiceImplementation service;
    private finalprojectprogramming.project.services.auditlog.AuditLogService auditLogService;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        repo = Mockito.mock(SpaceImageRepository.class);
        spaceRepo = Mockito.mock(SpaceRepository.class);
    mapper = Mockito.mock(ModelMapper.class);
    storage = Mockito.mock(ImageStorageService.class);
    auditLogService = Mockito.mock(finalprojectprogramming.project.services.auditlog.AuditLogService.class);
    objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
    service = new SpaceImageServiceImplementation(repo, spaceRepo, mapper, storage, auditLogService, objectMapper);
        when(mapper.map(any(SpaceImage.class), eq(SpaceImageDTO.class))).thenAnswer(i -> new SpaceImageDTO());
    }

    private Space active(Long id) {
        Space s = new Space();
        s.setId(id);
        return s;
    }

    @Test
    void create_persists_with_defaults() {
        when(spaceRepo.findById(1L)).thenReturn(Optional.of(active(1L)));
        when(repo.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceImageDTO in = new SpaceImageDTO();
        in.setSpaceId(1L);
        in.setImageUrl("https://x");
        in.setDescription("foto");
        in.setActive(true);
        in.setDisplayOrder(null);
        in.setUploadedAt(null);

        SpaceImageDTO out = service.create(in);
        assertThat(out).isNotNull();
    }

    @Test
    void update_changes_mutable_fields() {
        SpaceImage existing = new SpaceImage();
        existing.setId(7L);
        existing.setSpace(active(1L));
        when(repo.findById(7L)).thenReturn(Optional.of(existing));
        when(spaceRepo.findById(2L)).thenReturn(Optional.of(active(2L)));
        when(repo.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceImageDTO patch = new SpaceImageDTO();
        patch.setSpaceId(2L);
        patch.setImageUrl("https://y");
        patch.setDescription("otra");
        patch.setActive(false);
        patch.setDisplayOrder(5);
        patch.setUploadedAt(LocalDateTime.now());

        SpaceImageDTO out = service.update(7L, patch);
        assertThat(out).isNotNull();
        assertThat(existing.getSpace().getId()).isEqualTo(2L);
        assertThat(existing.getImageUrl()).isEqualTo("https://y");
        assertThat(existing.getActive()).isFalse();
        assertThat(existing.getDisplayOrder()).isEqualTo(5);
    }

    @Test
    void findAndDelete_variants() {
        SpaceImage s = new SpaceImage();
        s.setId(10L);
        when(repo.findById(10L)).thenReturn(Optional.of(s));
        when(repo.findAll()).thenReturn(List.of(s));
        when(repo.findBySpaceId(3L)).thenReturn(List.of(s));

        assertThat(service.findById(10L)).isNotNull();
        assertThat(service.findAll()).hasSize(1);
        assertThat(service.findBySpace(3L)).hasSize(1);

        service.delete(10L);
        verify(repo).delete(s);
    }

    @Test
    void create_throws_when_space_missing_or_deleted() {
        // missing
        SpaceImageDTO in = new SpaceImageDTO();
        in.setSpaceId(99L);
        assertThatThrownBy(() -> service.create(in)).isInstanceOf(ResourceNotFoundException.class);

        // deleted
        Space deleted = new Space();
        deleted.setId(1L);
        deleted.setDeletedAt(LocalDateTime.now());
        when(spaceRepo.findById(1L)).thenReturn(Optional.of(deleted));

        in.setSpaceId(1L);
        assertThatThrownBy(() -> service.create(in)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void upload_stores_file_and_applies_defaults() {
        when(spaceRepo.findById(5L)).thenReturn(Optional.of(active(5L)));
        when(storage.store(eq(5L), any())).thenReturn("/stored/url.png");
        when(repo.save(any(SpaceImage.class))).thenAnswer(inv -> inv.getArgument(0));

        SpaceImageDTO out = service.upload(5L, Mockito.mock(org.springframework.web.multipart.MultipartFile.class),
                null, null, null);

        assertThat(out).isNotNull();
        verify(storage).store(eq(5L), any());
        verify(repo).save(any(SpaceImage.class));
    }

    @Test
    void delete_throws_when_not_found_and_does_not_delete_storage() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
        verify(storage, never()).delete(any());
    }

    @Test
    void update_throws_when_not_found() {
        when(repo.findById(77L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update(77L, new SpaceImageDTO())).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_throws_when_not_found() {
        when(repo.findById(88L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(88L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
