package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.services.setting.SettingService;
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

@WebMvcTest(SettingController.class)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class SettingControllerTest extends BaseControllerTest {

    @MockBean
    private SettingService settingService;

    private SettingDTO buildSettingDto() {
        return SettingDTO.builder()
                .id(5L)
                .key("timezone")
                .value("UTC")
                .description("Timezone setting")
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createSettingReturnsCreated() throws Exception {
        SettingDTO dto = buildSettingDto();
        when(settingService.create(any(SettingDTO.class))).thenReturn(dto);

        performPost("/api/settings", dto)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/settings/5"));

        verify(settingService).create(any(SettingDTO.class));
    }

    @Test
    void createSettingFailsValidationForMissingKey() throws Exception {
        SettingDTO invalid = SettingDTO.builder()
                .value("UTC")
                .build();

        performPost("/api/settings", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSettingByKeyRequiresNonBlankKey() throws Exception {
        performGet("/api/settings?key=")
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSettingByIdHandlesServiceException() throws Exception {
        when(settingService.findById(10L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/settings/10")
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteSettingReturnsNoContent() throws Exception {
        performDelete("/api/settings/9")
                .andExpect(status().isNoContent());

        verify(settingService).delete(eq(9L));
    }
}
