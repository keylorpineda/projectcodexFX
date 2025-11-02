package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SpaceImageDTO;
import finalprojectprogramming.project.services.spaceimage.SpaceImageService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SpaceImageController.class)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class SpaceImageControllerTest extends BaseControllerTest {

    @MockBean
    private SpaceImageService spaceImageService;

    private SpaceImageDTO buildSpaceImageDto() {
        return SpaceImageDTO.builder()
                .id(20L)
                .spaceId(3L)
                .imageUrl("https://example.com/img.png")
                .description("Main hall")
                .active(true)
                .displayOrder(1)
                .uploadedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createSpaceImageReturnsCreated() throws Exception {
        SpaceImageDTO dto = buildSpaceImageDto();
        when(spaceImageService.create(any(SpaceImageDTO.class))).thenReturn(dto);

        performPost("/api/space-images", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/space-images/20"));

        verify(spaceImageService).create(any(SpaceImageDTO.class));
    }

    @Test
    void createSpaceImageFailsValidationForMissingUrl() throws Exception {
        SpaceImageDTO invalid = SpaceImageDTO.builder()
                .spaceId(3L)
                .imageUrl(" ")
                .active(true)
                .displayOrder(0)
                .build();

        performPost("/api/space-images", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSpaceImageByIdHandlesServiceException() throws Exception {
        when(spaceImageService.findById(44L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/space-images/44")
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteSpaceImageReturnsNoContent() throws Exception {
        performDelete("/api/space-images/8")
                .andExpect(status().isNoContent());

        verify(spaceImageService).delete(eq(8L));
    }
}
