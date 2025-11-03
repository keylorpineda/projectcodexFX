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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Authentication", description = "🔐 Authentication and authorization endpoints (Azure AD)")
public class AuthController {
    private final AzureAuthenticationService azureAuthenticationService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AzureAuthenticationService azureAuthenticationService) {
        this.azureAuthenticationService = azureAuthenticationService;
    }

    @PostMapping(value = "/azure-login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Authenticate with Azure AD",
        description = """
            Authenticates a user using Azure Active Directory access token.
            
            **Flow:**
            1. Client obtains Azure AD access token from Microsoft
            2. Sends token to this endpoint
            3. Backend validates token with Azure AD
            4. Creates/updates user in local database
            5. Returns JWT token for subsequent API calls
            
            **Use this token in all protected endpoints:**
            - Add header: `Authorization: Bearer <jwt_token>`
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Authentication successful. Returns JWT token.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponseDTO.class),
                examples = @ExampleObject(
                    name = "Successful login",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "tokenType": "Bearer",
                          "expiresIn": 86400,
                          "user": {
                            "id": 5,
                            "email": "user@example.com",
                            "name": "Juan Pérez",
                            "role": "USER",
                            "active": true
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "❌ Invalid Azure AD token or malformed request"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "❌ Azure AD token validation failed or expired"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "❌ Internal server error during authentication"
        )
    })
    public ResponseEntity<AuthResponseDTO> azureLogin(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Azure AD access token obtained from Microsoft login",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AzureLoginRequestDTO.class),
                examples = @ExampleObject(
                    name = "Azure login request",
                    value = """
                        {
                          "accessToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6..."
                        }
                        """
                )
            )
        )
        @Valid @RequestBody AzureLoginRequestDTO request
    ) {
        LOGGER.debug("Handling Azure AD login for token payload");
        AuthResponseDTO response = azureAuthenticationService.authenticate(request.getAccessToken());
        return ResponseEntity.ok(response);
    }
}