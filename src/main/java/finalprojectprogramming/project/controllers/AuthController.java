package finalprojectprogramming.project.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import finalprojectprogramming.project.dtos.AuthResponseDTO;
import finalprojectprogramming.project.dtos.AzureLoginRequestDTO;
import finalprojectprogramming.project.services.auth.AzureAuthenticationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {
    private final AzureAuthenticationService azureAuthenticationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AzureAuthenticationService azureAuthenticationService) {
        this.azureAuthenticationService = azureAuthenticationService;
    }

    @PostMapping(value = "/azure-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponseDTO> azureLogin(@Valid @RequestBody AzureLoginRequestDTO request) {
        LOGGER.debug("Handling Azure AD login for token payload");
        AuthResponseDTO response = azureAuthenticationService.authenticate(request.getAccessToken());
        return ResponseEntity.ok(response);
    }
}