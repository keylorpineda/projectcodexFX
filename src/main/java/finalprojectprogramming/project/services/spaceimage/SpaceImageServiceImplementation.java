package finalprojectprogramming.project.services.spaceimage;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import finalprojectprogramming.project.exceptions.ResourceNotFoundException;
import finalprojectprogramming.project.models.Space;
import finalprojectprogramming.project.models.SpaceImage;
import finalprojectprogramming.project.repositories.SpaceImageRepository;
import finalprojectprogramming.project.repositories.SpaceRepository;
import finalprojectprogramming.project.services.storage.ImageStorageService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class SpaceImageServiceImplementation implements SpaceImageService {

    private final SpaceImageRepository spaceImageRepository;
    private final SpaceRepository spaceRepository;
    private final ModelMapper modelMapper;
    private final ImageStorageService imageStorageService;

    public SpaceImageServiceImplementation(SpaceImageRepository spaceImageRepository, SpaceRepository spaceRepository,
            ModelMapper modelMapper, ImageStorageService imageStorageService) {
        this.spaceImageRepository = spaceImageRepository;
        this.spaceRepository = spaceRepository;
        this.modelMapper = modelMapper;
        this.imageStorageService = imageStorageService;
    }

    @Override
    public SpaceImageDTO create(SpaceImageDTO spaceImageDTO) {
        Space space = getActiveSpace(spaceImageDTO.getSpaceId());

        SpaceImage spaceImage = new SpaceImage();
        spaceImage.setSpace(space);
        spaceImage.setImageUrl(spaceImageDTO.getImageUrl());
        spaceImage.setDescription(spaceImageDTO.getDescription());
        spaceImage.setActive(spaceImageDTO.getActive());
        spaceImage.setDisplayOrder(spaceImageDTO.getDisplayOrder() != null ? spaceImageDTO.getDisplayOrder() : 0);
        spaceImage.setUploadedAt(spaceImageDTO.getUploadedAt() != null ? spaceImageDTO.getUploadedAt()
                : LocalDateTime.now());

        SpaceImage saved = spaceImageRepository.save(spaceImage);
        return toDto(saved);
    }

    @Override
    public SpaceImageDTO update(Long id, SpaceImageDTO spaceImageDTO) {
        SpaceImage spaceImage = spaceImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space image with id " + id + " not found"));

        if (spaceImageDTO.getSpaceId() != null
                && !Objects.equals(spaceImageDTO.getSpaceId(), spaceImage.getSpace().getId())) {
            spaceImage.setSpace(getActiveSpace(spaceImageDTO.getSpaceId()));
        }
        if (spaceImageDTO.getImageUrl() != null) {
            spaceImage.setImageUrl(spaceImageDTO.getImageUrl());
        }
        if (spaceImageDTO.getDescription() != null) {
            spaceImage.setDescription(spaceImageDTO.getDescription());
        }
        if (spaceImageDTO.getActive() != null) {
            spaceImage.setActive(spaceImageDTO.getActive());
        }
        if (spaceImageDTO.getDisplayOrder() != null) {
            spaceImage.setDisplayOrder(spaceImageDTO.getDisplayOrder());
        }
        if (spaceImageDTO.getUploadedAt() != null) {
            spaceImage.setUploadedAt(spaceImageDTO.getUploadedAt());
        }

        SpaceImage saved = spaceImageRepository.save(spaceImage);
        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SpaceImageDTO findById(Long id) {
        SpaceImage spaceImage = spaceImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space image with id " + id + " not found"));
        return toDto(spaceImage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceImageDTO> findAll() {
        return spaceImageRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SpaceImageDTO> findBySpace(Long spaceId) {
        return spaceImageRepository.findBySpaceId(spaceId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SpaceImageDTO upload(Long spaceId, MultipartFile file, String description, Boolean active,
            Integer displayOrder) {
        Space space = getActiveSpace(spaceId);
        String storedUrl = imageStorageService.store(spaceId, file);

        SpaceImage spaceImage = new SpaceImage();
        spaceImage.setSpace(space);
        spaceImage.setImageUrl(storedUrl);
        spaceImage.setDescription(description);
        spaceImage.setActive(active != null ? active : Boolean.TRUE);
        spaceImage.setDisplayOrder(displayOrder != null ? displayOrder : 0);
        spaceImage.setUploadedAt(LocalDateTime.now());

        SpaceImage saved = spaceImageRepository.save(spaceImage);
        return toDto(saved);
    }

    @Override
    public void delete(Long id) {
        SpaceImage spaceImage = spaceImageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Space image with id " + id + " not found"));
        imageStorageService.delete(spaceImage.getImageUrl());
        spaceImageRepository.delete(spaceImage);
    }

    private Space getActiveSpace(Long spaceId) {
        Space space = spaceRepository.findById(spaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Space with id " + spaceId + " not found"));
        if (space.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Space with id " + spaceId + " not found");
        }
        return space;
    }

    private SpaceImageDTO toDto(SpaceImage spaceImage) {
        SpaceImageDTO dto = modelMapper.map(spaceImage, SpaceImageDTO.class);
        dto.setSpaceId(spaceImage.getSpace() != null ? spaceImage.getSpace().getId() : null);
        return dto;
    }
}