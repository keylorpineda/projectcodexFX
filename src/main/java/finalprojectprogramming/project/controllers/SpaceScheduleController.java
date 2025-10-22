package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceScheduleDTO;
import finalprojectprogramming.project.services.spaceschedule.SpaceScheduleService;
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
@RequestMapping("/api/space-schedules")
@Validated
@Tag(name = "Space Schedules", description = "Operations related to space schedules")
public class SpaceScheduleController {

    private final SpaceScheduleService spaceScheduleService;

    public SpaceScheduleController(SpaceScheduleService spaceScheduleService) {
        this.spaceScheduleService = spaceScheduleService;
    }

    @PostMapping
    @Operation(summary = "Create a new space schedule")
    public ResponseEntity<SpaceScheduleDTO> createSpaceSchedule(@Valid @RequestBody SpaceScheduleDTO spaceScheduleDTO) {
        SpaceScheduleDTO created = spaceScheduleService.create(spaceScheduleDTO);
        return ResponseEntity.created(URI.create("/api/space-schedules/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing space schedule")
    public ResponseEntity<SpaceScheduleDTO> updateSpaceSchedule(@PathVariable Long id,
            @Valid @RequestBody SpaceScheduleDTO spaceScheduleDTO) {
        return ResponseEntity.ok(spaceScheduleService.update(id, spaceScheduleDTO));
    }

    @GetMapping
    @Operation(summary = "Retrieve all space schedules")
    public ResponseEntity<List<SpaceScheduleDTO>> getAllSpaceSchedules() {
        return ResponseEntity.ok(spaceScheduleService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a space schedule by id")
    public ResponseEntity<SpaceScheduleDTO> getSpaceScheduleById(@PathVariable Long id) {
        return ResponseEntity.ok(spaceScheduleService.findById(id));
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "Retrieve space schedules by space")
    public ResponseEntity<List<SpaceScheduleDTO>> getSpaceSchedulesBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(spaceScheduleService.findBySpace(spaceId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a space schedule")
    public ResponseEntity<Void> deleteSpaceSchedule(@PathVariable Long id) {
        spaceScheduleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}