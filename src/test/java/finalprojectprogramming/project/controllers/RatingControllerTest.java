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
    void deleteRatingReturnsNoContent() throws Exception {
        performDelete("/api/ratings/4")
                .andExpect(status().isNoContent());

        verify(ratingService).delete(eq(4L));
    }
}
