package finalprojectprogramming.project.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import finalprojectprogramming.project.dtos.AuditLogDTO;
import finalprojectprogramming.project.services.auditlog.AuditLogService;
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

@WebMvcTest(AuditLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class AuditLogControllerTest extends BaseControllerTest {

    @MockBean
    private AuditLogService auditLogService;

    private AuditLogDTO buildAuditLogDto() {
        ObjectNode details = objectMapper.createObjectNode();
        details.put("field", "value");
        return AuditLogDTO.builder()
                .id(10L)
                .userId(1L)
                .action("CREATE")
                .entityId("entity-1")
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Test
    void createAuditLogReturnsCreatedStatus() throws Exception {
        AuditLogDTO input = buildAuditLogDto();
        when(auditLogService.create(any(AuditLogDTO.class))).thenAnswer(invocation -> {
            AuditLogDTO dto = invocation.getArgument(0);
            dto.setId(10L);
            return dto;
        });

        performPost("/api/audit-logs", input)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/audit-logs/10"));

        verify(auditLogService).create(any(AuditLogDTO.class));
    }

    @Test
    void createAuditLogFailsValidationForBlankAction() throws Exception {
        AuditLogDTO invalid = AuditLogDTO.builder()
                .userId(1L)
                .action(" ")
                .timestamp(LocalDateTime.now())
                .build();

        performPost("/api/audit-logs", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAuditLogByIdHandlesServiceException() throws Exception {
        when(auditLogService.findById(99L)).thenThrow(new RuntimeException("boom"));

        performGet("/api/audit-logs/99")
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAuditLogByIdReturnsOk() throws Exception {
        when(auditLogService.findById(10L)).thenReturn(buildAuditLogDto());

        performGet("/api/audit-logs/10")
                .andExpect(status().isOk());
    }

    @Test
    void getAllAuditLogsReturnsOk() throws Exception {
        when(auditLogService.findAll()).thenReturn(java.util.List.of(buildAuditLogDto()));
        performGet("/api/audit-logs")
                .andExpect(status().isOk());
    }

    @Test
    void getAuditLogsByUserReturnsOk() throws Exception {
        when(auditLogService.findByUser(1L)).thenReturn(java.util.List.of(buildAuditLogDto()));
        performGet("/api/audit-logs/user/1")
                .andExpect(status().isOk());
    }

    @Test
    void deleteAuditLogReturnsNoContent() throws Exception {
        performDelete("/api/audit-logs/5")
                .andExpect(status().isNoContent());

        verify(auditLogService).delete(eq(5L));
    }
}
