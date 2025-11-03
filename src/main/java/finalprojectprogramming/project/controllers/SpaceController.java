package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.services.space.SpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    @Operation(summary = "Create a new space", description = "Creates a new reservable space in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Space created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> createSpace(@Valid @RequestBody SpaceDTO spaceDTO) {
        SpaceDTO created = spaceService.create(spaceDTO);
        return ResponseEntity.created(URI.create("/api/spaces/" + created.getId())).body(created);
    }

    @GetMapping
    @Operation(summary = "Retrieve all spaces", description = "Returns a list of all reservable spaces")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved spaces"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<List<SpaceDTO>> getAllSpaces() {
        return ResponseEntity.ok(spaceService.findAll());
    }

    @GetMapping("/search")
    @Operation(summary = "Advanced search for spaces with multiple filters", 
               description = "Search spaces by type, capacity range, location, and availability")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','USER')")
    public ResponseEntity<List<SpaceDTO>> searchSpaces(
            @RequestParam(required = false) SpaceType type,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(spaceService.searchSpaces(type, minCapacity, maxCapacity, location, active));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a space by id", description = "Returns detailed information about a specific space")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Space found"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> getSpaceById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceService.findById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space", description = "Updates space information including capacity, location, and features")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Space updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> updateSpace(@PathVariable Long id, @Valid @RequestBody SpaceDTO spaceDTO) {
        return ResponseEntity.ok(spaceService.update(id, spaceDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a space", description = "Marks a space as deleted without removing from database")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Space deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Space not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<Void> deleteSpace(@PathVariable Long id) {
        spaceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Change the active status of a space", description = "Activates or deactivates a space for reservations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status changed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Space not found")
    })
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

    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create a new space with an image")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ResponseEntity<SpaceDTO> createSpaceWithImage(
            @RequestPart("space") @Valid SpaceDTO spaceDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        SpaceDTO created = spaceService.createWithImage(spaceDTO, image);
        return ResponseEntity.created(URI.create("/api/spaces/" + created.getId())).body(created);
    }
}