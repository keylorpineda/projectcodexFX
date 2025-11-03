package finalprojectprogramming.project.controllers;

import finalprojectprogramming.project.dtos.UserInputDTO;
import finalprojectprogramming.project.dtos.UserOutputDTO;
import finalprojectprogramming.project.models.enums.UserRole;
import finalprojectprogramming.project.services.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(BaseControllerTest.TestMethodSecurityConfig.class)
class UserControllerTest extends BaseControllerTest {

    @MockBean
    private UserService userService;

    private UserInputDTO buildUserInput() {
        return UserInputDTO.builder()
                .role(UserRole.USER)
                .name("John Doe")
                .email("john@example.com")
                .active(true)
                .build();
    }

    private UserOutputDTO buildUserOutput() {
        return UserOutputDTO.builder()
                .id(40L)
                .role(UserRole.USER)
                .name("John Doe")
                .email("john@example.com")
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUserAsAdminReturnsCreated() throws Exception {
        UserOutputDTO output = buildUserOutput();
        when(userService.create(any(UserInputDTO.class))).thenReturn(output);

        performPost("/api/users", buildUserInput())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users/40"));

        verify(userService).create(any(UserInputDTO.class));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createUserForbiddenForRegularUser() throws Exception {
        performPost("/api/users", buildUserInput())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createUserFailsValidationForMissingEmail() throws Exception {
        UserInputDTO invalid = UserInputDTO.builder()
                .role(UserRole.USER)
                .active(true)
                .build();

        performPost("/api/users", invalid)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getAllUsersAllowedForSupervisor() throws Exception {
        when(userService.findAll()).thenReturn(List.of(buildUserOutput()));

        performGet("/api/users")
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void getUserByIdReturnsOk() throws Exception {
        when(userService.findById(40L)).thenReturn(buildUserOutput());

        performGet("/api/users/40")
                .andExpect(status().isOk());

        verify(userService).findById(40L);
    }

    @Test
    @WithMockUser(roles = {"SUPERVISOR"})
    void updateUserForbiddenForSupervisor() throws Exception {
        performPut("/api/users/40", buildUserInput())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateUserAsAdminReturnsOk() throws Exception {
        when(userService.update(eq(40L), any(UserInputDTO.class))).thenReturn(buildUserOutput());

        performPut("/api/users/40", buildUserInput())
                .andExpect(status().isOk());

        verify(userService).update(eq(40L), any(UserInputDTO.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteUserReturnsNoContent() throws Exception {
        performDelete("/api/users/22")
                .andExpect(status().isNoContent());

        verify(userService).delete(22L);
    }
}
