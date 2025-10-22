package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import finalprojectprogramming.project.services.spaceimage.SpaceImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/space-images")
@Validated
@Tag(name = "Space Images", description = "Operations related to space images")
public class SpaceImageController {

    private final SpaceImageService spaceImageService;

    public SpaceImageController(SpaceImageService spaceImageService) {
        this.spaceImageService = spaceImageService;
    }

    @PostMapping
    @Operation(summary = "Create a new space image")
    public ResponseEntity<SpaceImageDTO> createSpaceImage(@Valid @RequestBody SpaceImageDTO spaceImageDTO) {
        SpaceImageDTO created = spaceImageService.create(spaceImageDTO);
        return ResponseEntity.created(URI.create("/api/space-images/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space image")
    public ResponseEntity<SpaceImageDTO> updateSpaceImage(@PathVariable Long id,
            @Valid @RequestBody SpaceImageDTO spaceImageDTO) {
        return ResponseEntity.ok(spaceImageService.update(id, spaceImageDTO));
    }

    @GetMapping
    @Operation(summary = "Retrieve all space images")
    public ResponseEntity<List<SpaceImageDTO>> getAllSpaceImages() {
        return ResponseEntity.ok(spaceImageService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a space image by id")
    public ResponseEntity<SpaceImageDTO> getSpaceImageById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceImageService.findById(id));
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "Retrieve space images by space")
    public ResponseEntity<List<SpaceImageDTO>> getSpaceImagesBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(spaceImageService.findBySpace(spaceId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a space image")
    public ResponseEntity<Void> deleteSpaceImage(@PathVariable Long id) {
        spaceImageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}