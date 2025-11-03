package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.RatingDTO;
import finalprojectprogramming.project.services.rating.RatingService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class RatingControllerTest extends BaseControllerTest {

    @MockBean
    private RatingService ratingService;

    private RatingDTO buildRatingDto() {
        return RatingDTO.builder()
                .id(9L)
                .reservationId(2L)
                .score(5)
                .comment("Excellent")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = {"USER"})
    void createRatingReturnsCreated() throws Exception {
        RatingDTO dto = buildRatingDto();
        when(ratingService.create(any(RatingDTO.class))).thenReturn(dto);

        performPost("/api/ratings", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ratings/9"));

        verify(ratingService).create(any(RatingDTO.class));
    }

    @Test
    void createRatingFailsValidationForInvalidScore() throws Exception {
        RatingDTO invalid = RatingDTO.builder()
                .reservationId(2L)
                .score(10)
                .build();

        performPost("/api/ratings", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRatingByIdHandlesServiceException() throws Exception {
        when(ratingService.findById(77L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/ratings/77")
                .andExpect(status().isInternalServerError());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = {"ADMIN"})
    void deleteRatingReturnsNoContent() throws Exception {
        performDelete("/api/ratings/4")
                .andExpect(status().isNoContent());

        verify(ratingService).delete(eq(4L));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = {"USER"})
    void updateRatingReturnsOk() throws Exception {
        RatingDTO dto = buildRatingDto();
        when(ratingService.update(eq(9L), any(RatingDTO.class))).thenReturn(dto);

        performPut("/api/ratings/9", dto)
                .andExpect(status().isOk());

        verify(ratingService).update(eq(9L), any(RatingDTO.class));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = {"ADMIN"})
    void getAllRatingsReturnsOk() throws Exception {
        when(ratingService.findAll()).thenReturn(java.util.List.of(buildRatingDto()));

        performGet("/api/ratings")
                .andExpect(status().isOk());

        verify(ratingService).findAll();
    }

    @Test
    void getRatingByIdReturnsOk() throws Exception {
        when(ratingService.findById(9L)).thenReturn(buildRatingDto());

        performGet("/api/ratings/9")
                .andExpect(status().isOk());

        verify(ratingService).findById(9L);
    }

    @Test
    void getRatingByReservationReturnsOk() throws Exception {
        when(ratingService.findByReservation(2L)).thenReturn(buildRatingDto());

        performGet("/api/ratings/reservation/2")
                .andExpect(status().isOk());

        verify(ratingService).findByReservation(2L);
    }

    @Test
    void getRatingsBySpaceReturnsOk() throws Exception {
        when(ratingService.findBySpace(3L)).thenReturn(java.util.List.of(buildRatingDto()));

        performGet("/api/ratings/space/3")
                .andExpect(status().isOk());

        verify(ratingService).findBySpace(3L);
    }

    @Test
    void getAverageRatingReturnsOk() throws Exception {
        when(ratingService.getAverageBySpace(3L)).thenReturn(4.5);

        performGet("/api/ratings/space/3/average")
                .andExpect(status().isOk());

        verify(ratingService).getAverageBySpace(3L);
    }

    @Test
    void getRatingCountReturnsOk() throws Exception {
        when(ratingService.getCountBySpace(3L)).thenReturn(5L);

        performGet("/api/ratings/space/3/count")
                .andExpect(status().isOk());

        verify(ratingService).getCountBySpace(3L);
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = {"ADMIN"})
    void toggleVisibilityReturnsOk() throws Exception {
        when(ratingService.toggleVisibility(9L)).thenReturn(buildRatingDto());

        performPut("/api/ratings/9/toggle-visibility", null)
                .andExpect(status().isOk());

        verify(ratingService).toggleVisibility(9L);
    }

    @Test
    void incrementHelpfulReturnsOk() throws Exception {
        when(ratingService.incrementHelpful(9L)).thenReturn(buildRatingDto());

        performPut("/api/ratings/9/helpful", null)
                .andExpect(status().isOk());

        verify(ratingService).incrementHelpful(9L);
    }
}
