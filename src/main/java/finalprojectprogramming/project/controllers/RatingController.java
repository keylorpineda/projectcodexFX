package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.services.rating.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@Validated
@Tag(name = "Ratings", description = "Gestión de calificaciones y reseñas de espacios")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Create a new rating")
    public ResponseEntity<RatingDTO> createRating(@Valid @RequestBody RatingDTO ratingDTO) {
        RatingDTO created = ratingService.create(ratingDTO);
        return ResponseEntity.created(URI.create("/api/ratings/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Update an existing rating")
    public ResponseEntity<RatingDTO> updateRating(@PathVariable Long id, @Valid @RequestBody RatingDTO ratingDTO) {
        return ResponseEntity.ok(ratingService.update(id, ratingDTO));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Retrieve all ratings")
    public ResponseEntity<List<RatingDTO>> getAllRatings() {
        return ResponseEntity.ok(ratingService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a rating by id")
    public ResponseEntity<RatingDTO> getRatingById(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.findById(id));
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "Retrieve a rating by reservation")
    public ResponseEntity<RatingDTO> getRatingByReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(ratingService.findByReservation(reservationId));
    }

    @GetMapping("/space/{spaceId}")
    @Operation(summary = "Obtener calificaciones de un espacio")
    public ResponseEntity<List<RatingDTO>> getRatingsBySpace(@PathVariable Long spaceId) {
        return ResponseEntity.ok(ratingService.findBySpace(spaceId));
    }

    @GetMapping("/space/{spaceId}/average")
    @Operation(summary = "Obtener calificación promedio de un espacio")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long spaceId) {
        return ResponseEntity.ok(ratingService.getAverageBySpace(spaceId));
    }

    @GetMapping("/space/{spaceId}/count")
    @Operation(summary = "Obtener cantidad de calificaciones de un espacio")
    public ResponseEntity<Long> getRatingCount(@PathVariable Long spaceId) {
        return ResponseEntity.ok(ratingService.getCountBySpace(spaceId));
    }

    @PutMapping("/{id}/toggle-visibility")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Cambiar visibilidad de una calificación")
    public ResponseEntity<RatingDTO> toggleVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.toggleVisibility(id));
    }

    @PutMapping("/{id}/helpful")
    @Operation(summary = "Marcar calificación como útil")
    public ResponseEntity<RatingDTO> incrementHelpful(@PathVariable Long id) {
        return ResponseEntity.ok(ratingService.incrementHelpful(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERVISOR')")
    @Operation(summary = "Delete a rating")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}