package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.SettingDTO;
import finalprojectprogramming.project.services.setting.SettingService;
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

@WebMvcTest(SettingController.class)
@AutoConfigureMockMvc(addFilters = false)
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
    void getSettingByKeyReturnsOk() throws Exception {
    SettingDTO dto = buildSettingDto();
    when(settingService.findByKey("timezone")).thenReturn(dto);

    performGet("/api/settings?key=timezone")
        .andExpect(status().isOk());
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

    @Test
    void getAllSettingsReturnsOk() throws Exception {
    performGet("/api/settings")
        .andExpect(status().isOk());
    verify(settingService).findAll();
    }

    @Test
    void getSettingByIdReturnsOk() throws Exception {
    when(settingService.findById(5L)).thenReturn(buildSettingDto());

    performGet("/api/settings/5")
        .andExpect(status().isOk());
    verify(settingService).findById(5L);
    }

    @Test
    void getSettingByIdNotFoundMapsTo404() throws Exception {
    when(settingService.findById(404L)).thenThrow(new finalprojectprogramming.project.exceptions.ResourceNotFoundException("not found"));

    performGet("/api/settings/404")
        .andExpect(status().isNotFound());
    }

    @Test
    void updateSettingReturnsOk() throws Exception {
    SettingDTO input = SettingDTO.builder().key("timezone").value("UTC+1").build();
    SettingDTO output = buildSettingDto();
    output.setValue("UTC+1");
    when(settingService.update(eq(5L), any(SettingDTO.class))).thenReturn(output);

    performPut("/api/settings/5", input)
        .andExpect(status().isOk());
    verify(settingService).update(eq(5L), any(SettingDTO.class));
    }

    @Test
    void updateSettingNotFoundMapsTo404() throws Exception {
    when(settingService.update(eq(5L), any(SettingDTO.class)))
        .thenThrow(new finalprojectprogramming.project.exceptions.ResourceNotFoundException("not found"));

    performPut("/api/settings/5", SettingDTO.builder().key("k").value("v").build())
        .andExpect(status().isNotFound());
    }

    @Test
    void deleteSettingNotFoundMapsTo404() throws Exception {
    final long id = 123L;
    org.mockito.Mockito.doThrow(new finalprojectprogramming.project.exceptions.ResourceNotFoundException("not found"))
        .when(settingService).delete(id);

    performDelete("/api/settings/" + id)
        .andExpect(status().isNotFound());
    }
}
