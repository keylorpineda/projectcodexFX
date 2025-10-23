package com.municipal.ui.navigation;

import com.municipal.session.SessionManager;
import com.municipal.ui.App;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Centralized navigation helper that orchestrates JavaFX scene changes and
 * keeps the same {@link Scene} instance to provide smooth transitions between views.
 */
public final class FlowController {

    private final Stage stage;
    private final SessionManager sessionManager;
    private final Map<String, ViewConfig> viewRegistry = new HashMap<>();
    private final Map<String, String> roleRoutes = new HashMap<>();
    private String defaultRoleViewId;
    private String currentViewId;

    public FlowController(Stage stage, SessionManager sessionManager) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager");
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public Stage getStage() {
        return stage;
    }

    public void registerView(String viewId, ViewConfig config) {
        viewRegistry.put(Objects.requireNonNull(viewId, "viewId"), Objects.requireNonNull(config, "config"));
    }

    public void setDefaultRoleView(String viewId) {
        this.defaultRoleViewId = viewId;
    }

    public void registerRoleRoute(String roleKey, String viewId) {
        String normalized = normalizeRole(roleKey);
        roleRoutes.put(normalized, viewId);
    }

    public void navigateToRole(String roleKey) {
        String normalizedRole = normalizeRole(roleKey);
        String targetView = roleRoutes.get(normalizedRole);
        if (targetView == null) {
            targetView = defaultRoleViewId;
        }
        if (targetView == null) {
            throw new IllegalStateException("No view configured for role " + normalizedRole);
        }
        showView(targetView);
    }

    public void showView(String viewId) {
        ViewConfig config = viewRegistry.get(viewId);
        if (config == null) {
            throw new IllegalArgumentException("View not registered: " + viewId);
        }

        try {
            FXMLLoader loader = new FXMLLoader(resolveResource(config.fxmlPath()));
            Parent root = loader.load();
            Object controller = loader.getController();

            injectControllerDependencies(controller);

            Scene scene = stage.getScene();
            if (scene == null) {
                scene = new Scene(root);
                stage.setScene(scene);
                MaterialFXStylesheets.DEFAULT.applyOn(scene);
            } else {
                scene.setRoot(root);
            }

            scene.getStylesheets().setAll(resolveStylesheets(config.stylesheets()));

            if (currentViewId != null && !currentViewId.equals(viewId)) {
                playFadeTransition(root);
            }
            currentViewId = viewId;

            if (controller instanceof ViewLifecycle lifecycle) {
                lifecycle.onViewActivated();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view " + viewId, exception);
        }
    }

    private void injectControllerDependencies(Object controller) {
        if (controller instanceof SessionAware sessionAware) {
            sessionAware.setSessionManager(sessionManager);
        }
        if (controller instanceof FlowAware flowAware) {
            flowAware.setFlowController(this);
        }
        if (controller instanceof StageAware stageAware) {
            stageAware.setStage(stage);
        }
    }

    private void playFadeTransition(Parent root) {
        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(320), root);
        fade.setToValue(1);
        fade.play();
    }

    private URL resolveResource(String path) {
        URL resource = App.class.getResource(path);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + path);
        }
        return resource;
    }

    private List<String> resolveStylesheets(List<String> stylesheets) {
        return stylesheets.stream()
                .map(this::resolveResource)
                .map(URL::toExternalForm)
                .toList();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.trim().toUpperCase(Locale.ROOT);
    }
}
