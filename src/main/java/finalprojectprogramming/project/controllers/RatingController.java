package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.services.rating.RatingService;
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
@RequestMapping("/api/ratings")
@Validated
@Tag(name = "Ratings", description = "Operations related to ratings management")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping
    @Operation(summary = "Create a new rating")
    public ResponseEntity<RatingDTO> createRating(@Valid @RequestBody RatingDTO ratingDTO) {
        RatingDTO created = ratingService.create(ratingDTO);
        return ResponseEntity.created(URI.create("/api/ratings/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing rating")
    public ResponseEntity<RatingDTO> updateRating(@PathVariable Long id, @Valid @RequestBody RatingDTO ratingDTO) {
        return ResponseEntity.ok(ratingService.update(id, ratingDTO));
    }

    @GetMapping
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

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a rating")
    public ResponseEntity<Void> deleteRating(@PathVariable Long id) {
        ratingService.delete(id);
        return ResponseEntity.noContent().build();
    }
}