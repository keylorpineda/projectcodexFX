package com.municipal.reservationsfx.ui.controllers;

import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.municipal.reservationsfx.auth.AzureAuthService;
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
            showStatus("Bienvenido " + result.account().username(), "status-success");
            loadAdminDashboard();
        });

        task.setOnFailed(event -> {
            Throwable error = task.getException();
            showStatus("No se pudo iniciar sesión: " + error.getMessage(), "status-error");
            progressIndicator.setVisible(false);
            progressIndicator.setManaged(false);
            azureLoginButton.setDisable(false);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void loadAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/municipal/reservationsfx/ui/admin-dashboard.fxml"));
            Parent dashboard = loader.load();
            AdminDashboardController controller = loader.getController();
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
