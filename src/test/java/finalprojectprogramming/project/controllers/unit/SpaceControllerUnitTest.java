package finalprojectprogramming.project.controllers.unit;

import finalprojectprogramming.project.controllers.SpaceController;
import finalprojectprogramming.project.dtos.SpaceDTO;
import finalprojectprogramming.project.models.enums.SpaceType;
import finalprojectprogramming.project.services.space.SpaceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpaceControllerUnitTest {

    @Mock
    private SpaceService spaceService;

    @InjectMocks
    private SpaceController controller;

    private SpaceDTO dto() {
        return SpaceDTO.builder()
                .id(12L)
                .name("Main Hall")
                .type(SpaceType.AUDITORIO)
                .capacity(100)
                .active(true)
                .requiresApproval(true)
                .averageRating(4.5)
                .build();
    }

    @Test
    void createSpaceReturnsCreated() {
        when(spaceService.create(any(SpaceDTO.class))).thenReturn(dto());

        ResponseEntity<SpaceDTO> resp = controller.createSpace(dto());

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getHeaders().getLocation()).hasToString("/api/spaces/12");
        verify(spaceService).create(any(SpaceDTO.class));
    }

    @Test
    void getByIdReturnsOk() {
        when(spaceService.findById(50L)).thenReturn(dto());

        ResponseEntity<SpaceDTO> resp = controller.getSpaceById(50L);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        verify(spaceService).findById(50L);
    }

    @Test
    void changeStatusReturnsOk() {
        when(spaceService.changeStatus(12L, false)).thenReturn(dto());

        ResponseEntity<SpaceDTO> resp = controller.changeStatus(12L, false);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        verify(spaceService).changeStatus(12L, false);
    }

    @Test
    void availableReturnsOk() {
        when(spaceService.findAvailableSpaces(any(LocalDateTime.class), any(LocalDateTime.class), any(), any()))
                .thenReturn(List.of(dto()));

        ResponseEntity<List<SpaceDTO>> resp = controller.findAvailableSpaces(
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), null, 10);

        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
        verify(spaceService).findAvailableSpaces(any(LocalDateTime.class), any(LocalDateTime.class), any(), any());
    }
}
