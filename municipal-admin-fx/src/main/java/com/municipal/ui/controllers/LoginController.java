package com.municipal.ui.controllers;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.MsalException;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.municipal.auth.AzureAuthService;
import com.municipal.controllers.AuthController;
import com.municipal.exceptions.ApiClientException;
import com.municipal.responses.AuthResponse;
import com.municipal.session.SessionManager;
import javafx.animation.FadeTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class LoginController {

    @FXML
    private StackPane root;
    @FXML
    private HBox loginGrid;
    @FXML
    private VBox cardPanel;
    @FXML
    private VBox heroPanel;
    @FXML
    private Button azureLoginButton;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label statusLabel;
    @FXML
    private HBox statusContainer;

    private Stage stage;
    private final AzureAuthService authService = new AzureAuthService();
    private final AuthController authController = new AuthController();
    private final SessionManager sessionManager = new SessionManager();
    private static final double COMPACT_BREAKPOINT = 980;

    @FXML
    public void initialize() {
        if (statusContainer != null) {
            statusContainer.setVisible(false);
            statusContainer.setManaged(false);
        }
        progressIndicator.setVisible(false);
        progressIndicator.setManaged(false);
        statusLabel.setText("");
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
        FadeTransition fade = new FadeTransition(Duration.millis(750), cardPanel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        configureResponsiveBehavior();
    }

    @FXML
    private void handleAzureLogin() {
        progressIndicator.setVisible(true);
        progressIndicator.setManaged(true);
        showStatus("Conectando con Azure AD...", "status-info");
        azureLoginButton.setDisable(true);

        Task<IAuthenticationResult> task = new Task<>() {
            @Override
            protected IAuthenticationResult call() throws Exception {
                return authService.signInInteractive().get();
            }
        };

        task.setOnSucceeded(event -> {
            IAuthenticationResult result = task.getValue();
            showStatus("Autenticando con el sistema...", "status-info");
            authenticateWithBackend(result);
        });

        task.setOnFailed(event -> {
            Throwable error = unwrapAuthenticationError(task.getException());
            showStatus(buildFriendlyAuthenticationError(error), "status-error");
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
            azureLoginButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private Throwable unwrapAuthenticationError(Throwable error) {
        if (error == null) {
            return null;
        }
        Throwable current = error;
        while (current instanceof ExecutionException || current instanceof CompletionException) {
            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
        return current;
    }

    private String buildFriendlyAuthenticationError(Throwable error) {
        if (error == null) {
            return "No se pudo iniciar sesión. Microsoft no devolvió detalles adicionales.";
        }

        System.err.println("Azure sign-in failed" + (error.getMessage() != null
                ? ": " + error.getMessage()
                : " with an unknown error"));

        if (error instanceof MsalInteractionRequiredException interactionRequired) {
            String code = normalizeErrorCode(interactionRequired.errorCode());
            if ("consent_required".equals(code) || "interaction_required".equals(code)) {
                return "Microsoft necesita que completes un paso adicional (como otorgar permisos o verificar tu identidad). Intenta nuevamente y sigue las indicaciones.";
            }
            return formatGenericMicrosoftError(code, interactionRequired.getMessage());
        }

        if (error instanceof MsalServiceException service) {
            String code = normalizeErrorCode(service.errorCode());
            if ("invalid_scope".equals(code)) {
                return "La cuenta seleccionada no tiene permisos para completar el inicio de sesión en Microsoft. Prueba con otra cuenta o contacta al administrador del sistema.";
            }
            if ("unauthorized_client".equals(code)) {
                return "La aplicación no está autorizada para iniciar sesión con esta cuenta de Microsoft. Confirma con el administrador que tu cuenta esté habilitada.";
            }
            return formatGenericMicrosoftError(code, service.getMessage());
        }

        if (error instanceof MsalClientException client) {
            String code = normalizeErrorCode(client.errorCode());
            if ("authentication_canceled".equals(code)) {
                return "El inicio de sesión se canceló antes de completarse. Vuelve a intentarlo cuando estés listo.";
            }
            if ("unknown_authority".equals(code) || "invalid_authority".equals(code)) {
                return "No se pudo validar la dirección del servicio de inicio de sesión de Microsoft configurada para la aplicación.";
            }
            return formatGenericMicrosoftError(code, client.getMessage());
        }

        if (error instanceof MsalException msal) {
            return formatGenericMicrosoftError(normalizeErrorCode(msal.errorCode()), msal.getMessage());
        }

        String message = safeErrorMessage(error.getMessage());
        if (message == null) {
            return "No se pudo iniciar sesión. Se produjo un error inesperado.";
        }
        return "No se pudo iniciar sesión. Detalle: " + message;
    }

    private String normalizeErrorCode(String code) {
        return code == null ? "" : code.trim().toLowerCase();
    }

    private String safeErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        String sanitized = message.replaceAll("\s+", " ").trim();
        if (sanitized.isEmpty()) {
            return null;
        }
        if (sanitized.length() > 180) {
            return sanitized.substring(0, 177) + "...";
        }
        return sanitized;
    }

    private String formatGenericMicrosoftError(String code, String rawMessage) {
        StringBuilder builder = new StringBuilder("No se pudo iniciar sesión con Microsoft");
        if (code != null && !code.isBlank()) {
            builder.append(" (código: ").append(code).append(")");
        }
        String sanitizedMessage = safeErrorMessage(rawMessage);
        if (sanitizedMessage != null) {
            builder.append(". Detalle: ").append(sanitizedMessage);
        }
        builder.append('.');
        return builder.toString();
    }

    private void authenticateWithBackend(IAuthenticationResult authenticationResult) {
        Task<AuthResponse> backendTask = new Task<>() {
            @Override
            protected AuthResponse call() {
                return authController.authenticateWithAzure(authenticationResult.accessToken());
            }
        };

        backendTask.setOnSucceeded(event -> {
            AuthResponse response = backendTask.getValue();
            sessionManager.storeAuthResponse(response);
            String displayName = response.name() != null && !response.name().isBlank()
                    ? response.name()
                    : authenticationResult.account().username();
            showStatus("Bienvenido " + displayName, "status-success");
            loadAdminDashboard();
        });

        backendTask.setOnFailed(event -> {
            Throwable error = backendTask.getException();
            String message;
            if (error instanceof ApiClientException apiError) {
                message = "Error " + apiError.getStatusCode();
                if (apiError.getResponseBody() != null && !apiError.getResponseBody().isBlank()) {
                    message += ": " + apiError.getResponseBody();
                }
            } else {
                message = error != null ? error.getMessage() : "Error desconocido";
            }
            showStatus("No se pudo validar el acceso: " + message, "status-error");
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
            azureLoginButton.setDisable(false);
        });

        Thread thread = new Thread(backendTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/municipal/reservationsfx/ui/admin-dashboard.fxml"));
            Parent dashboard = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.setSessionManager(sessionManager);
            controller.bootstrap();
            Scene scene = stage.getScene();
            scene.setRoot(dashboard);
            scene.getStylesheets().setAll(
                    getClass().getResource("/com/municipal/reservationsfx/styles/styles.css").toExternalForm(),
                    getClass().getResource("/com/municipal/reservationsfx/styles/admin-dashboard.css").toExternalForm()
            );
            FadeTransition fade = new FadeTransition(Duration.millis(500), dashboard);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        } catch (IOException e) {
            showStatus("Error al cargar el panel: " + e.getMessage(), "status-error");
        } finally {
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
            if (statusContainer != null) {
                statusContainer.setVisible(false);
                statusContainer.setManaged(false);
            }
            azureLoginButton.setDisable(false);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    private void configureResponsiveBehavior() {
        FadeTransition heroFade = new FadeTransition(Duration.millis(850), heroPanel);
        heroFade.setFromValue(0);
        heroFade.setToValue(1);
        heroFade.play();

        root.widthProperty().addListener((obs, oldWidth, newWidth) -> updateResponsiveState(newWidth.doubleValue()));
        updateResponsiveState(root.getWidth());

        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                updateResponsiveState(newScene.getWidth());
                newScene.widthProperty().addListener((widthObs, oldWidth, newWidth) ->
                        updateResponsiveState(newWidth.doubleValue()));
            }
        });
    }

    private void updateResponsiveState(double width) {
        if (width <= 0) {
            return;
        }
        boolean compact = width < COMPACT_BREAKPOINT;
        heroPanel.setVisible(!compact);
        heroPanel.setManaged(!compact);
        cardPanel.setPrefWidth(compact ? Double.MAX_VALUE : 500);
        if (compact) {
            if (!loginGrid.getStyleClass().contains("compact")) {
                loginGrid.getStyleClass().add("compact");
            }
        } else {
            loginGrid.getStyleClass().remove("compact");
        }
    }

    private void showStatus(String message, String styleClass) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-info", "status-success", "status-error");
        if (styleClass != null && !styleClass.isBlank()) {
            statusLabel.getStyleClass().add(styleClass);
        }
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        if (statusContainer != null) {
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
        }
    }
}
