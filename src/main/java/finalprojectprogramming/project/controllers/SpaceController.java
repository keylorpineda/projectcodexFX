package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.services.space.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/spaces")
@Validated
@Tag(name = "Spaces", description = "Operations related to spaces management")
public class SpaceController {

    private final SpaceService spaceService;

    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @PostMapping
    @Operation(summary = "Create a new space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> createSpace(@Valid @RequestBody SpaceDTO spaceDTO) {
        SpaceDTO created = spaceService.create(spaceDTO);
        return ResponseEntity.created(URI.create("/api/spaces/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "Retrieve all spaces")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<List<SpaceDTO>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a space by id")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> getSpaceById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable Long id, @Valid @RequestBody SpaceDTO spaceDTO) {
        return ResponseEntity.ok(spaceService.update(id, spaceDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change the active status of a space")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> changeStatus(@PathVariable Long id, @RequestParam boolean active) {
        return ResponseEntity.ok(spaceService.changeStatus(id, active));
    }

    @GetMapping("/available")
    @Operation(summary = "Find available spaces in a time range")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<List<SpaceDTO>> findAvailableSpaces(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) SpaceType type,
            @RequestParam(required = false) Integer minimumCapacity) {
        return ResponseEntity.ok(spaceService.findAvailableSpaces(startTime, endTime, type, minimumCapacity));
    }
}