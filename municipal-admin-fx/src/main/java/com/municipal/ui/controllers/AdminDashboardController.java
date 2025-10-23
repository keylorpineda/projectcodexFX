package com.municipal.ui.controllers;

import com.municipal.controllers.SpaceController;
import com.municipal.dtos.SpaceDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.responses.AuthResponse;
import com.municipal.session.SessionManager;
import com.municipal.ui.utils.NodeVisibilityUtils;
import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AdminDashboardController {

    @FXML
    private VBox mainContainer;
    @FXML
    private ScrollPane contentScroll;
    @FXML
    private Button navDashboardButton;
    @FXML
    private Button navSpacesButton;
    @FXML
    private Button navUsersButton;
    @FXML
    private Button navReservationsButton;
    @FXML
    private Button navReportsButton;
    @FXML
    private Button navClimateButton;
    @FXML
    private Button navSettingsButton;
    @FXML
    private VBox dashboardSection;
    @FXML
    private VBox spacesSection;
    @FXML
    private VBox usersSection;
    @FXML
    private VBox reservationsSection;
    @FXML
    private VBox reportsSection;
    @FXML
    private VBox climateSection;
    @FXML
    private VBox settingsSection;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;
    @FXML
    private Label spacesCountLabel;
    @FXML
    private TableView<SpaceDTO> spacesTable;
    @FXML
    private TableColumn<SpaceDTO, String> spaceNameColumn;
    @FXML
    private TableColumn<SpaceDTO, String> spaceCapacityColumn;
    @FXML
    private TableColumn<SpaceDTO, String> spaceStatusColumn;
    @FXML
    private TableColumn<SpaceDTO, String> spaceTypeColumn;
    @FXML
    private VBox spacesLoadingOverlay;
    @FXML
    private Label spacesLoadingLabel;
    @FXML
    private Label spacesStatusLabel;

    private final Map<ViewSection, Button> navigationButtons = new EnumMap<>(ViewSection.class);
    private final Map<ViewSection, Node> sectionRegistry = new EnumMap<>(ViewSection.class);
    private final List<Node> contentSections = new ArrayList<>();
    private ViewSection activeView = null;
    private final ObservableList<SpaceDTO> spaces = FXCollections.observableArrayList();
    private final SpaceController spaceController = new SpaceController();
    private SessionManager sessionManager;
    private boolean spacesLoaded;

    @FXML
    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.millis(600), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        navigationButtons.put(ViewSection.DASHBOARD, navDashboardButton);
        navigationButtons.put(ViewSection.SPACES, navSpacesButton);
        navigationButtons.put(ViewSection.USERS, navUsersButton);
        navigationButtons.put(ViewSection.RESERVATIONS, navReservationsButton);
        navigationButtons.put(ViewSection.REPORTS, navReportsButton);
        navigationButtons.put(ViewSection.CLIMATE, navClimateButton);
        navigationButtons.put(ViewSection.SETTINGS, navSettingsButton);

        sectionRegistry.put(ViewSection.DASHBOARD, dashboardSection);
        sectionRegistry.put(ViewSection.SPACES, spacesSection);
        sectionRegistry.put(ViewSection.USERS, usersSection);
        sectionRegistry.put(ViewSection.RESERVATIONS, reservationsSection);
        sectionRegistry.put(ViewSection.REPORTS, reportsSection);
        sectionRegistry.put(ViewSection.CLIMATE, climateSection);
        sectionRegistry.put(ViewSection.SETTINGS, settingsSection);

        contentSections.addAll(List.of(
                dashboardSection,
                spacesSection,
                usersSection,
                reservationsSection,
                reportsSection,
                climateSection,
                settingsSection
        ));

        contentSections.stream().filter(Objects::nonNull).forEach(NodeVisibilityUtils::hide);
        configureSpacesSection();
        showView(ViewSection.DASHBOARD);
    }

    private void configureSpacesSection() {
        if (spacesTable != null) {
            spacesTable.setItems(spaces);
            spacesTable.setPlaceholder(new Label("No hay espacios registrados."));
        }
        if (spaceNameColumn != null) {
            spaceNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().name())));
        }
        if (spaceCapacityColumn != null) {
            spaceCapacityColumn.setCellValueFactory(cell -> {
                Integer capacity = cell.getValue().capacity();
                return new SimpleStringProperty(capacity != null ? capacity + " personas" : "—");
            });
        }
        if (spaceStatusColumn != null) {
            spaceStatusColumn.setCellValueFactory(cell -> {
                Boolean active = cell.getValue().active();
                return new SimpleStringProperty(Boolean.TRUE.equals(active) ? "Disponible" : "Inactivo");
            });
        }
        if (spaceTypeColumn != null) {
            spaceTypeColumn.setCellValueFactory(cell -> new SimpleStringProperty(valueOrDash(cell.getValue().type())));
        }
        if (spacesStatusLabel != null) {
            spacesStatusLabel.setVisible(false);
            spacesStatusLabel.setManaged(false);
        }
        hideSpacesLoading();
        spacesLoaded = false;
        if (spacesCountLabel != null) {
            spacesCountLabel.setText("Espacios (0)");
        }
    }

    @FXML
    private void handleNavigateDashboard() {
        showView(ViewSection.DASHBOARD);
    }

    @FXML
    private void handleNavigateSpaces() {
        showView(ViewSection.SPACES);
    }

    @FXML
    private void handleRefreshSpaces() {
        spacesLoaded = false;
        loadSpaces();
    }

    @FXML
    private void handleNavigateUsers() {
        showView(ViewSection.USERS);
    }

    @FXML
    private void handleNavigateReservations() {
        showView(ViewSection.RESERVATIONS);
    }

    @FXML
    private void handleNavigateReports() {
        showView(ViewSection.REPORTS);
    }

    @FXML
    private void handleNavigateClimate() {
        showView(ViewSection.CLIMATE);
    }

    @FXML
    private void handleNavigateSettings() {
        showView(ViewSection.SETTINGS);
    }

    public void navigateTo(ViewSection section) {
        showView(section);
    }

    private void showView(ViewSection section) {
        Node node = sectionRegistry.get(section);
        if (node == null) {
            return;
        }

        contentSections.stream().filter(Objects::nonNull).forEach(NodeVisibilityUtils::hide);
        NodeVisibilityUtils.show(node);

        if (contentScroll != null) {
            contentScroll.setVvalue(0);
        }

        updateActiveNavigation(section);
        activeView = section;

        if (section == ViewSection.SPACES) {
            ensureSpacesLoaded();
        }

        FadeTransition fade = new FadeTransition(Duration.millis(320), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.playFromStart();
    }

    private void updateActiveNavigation(ViewSection section) {
        navigationButtons.forEach((viewSection, button) -> {
            if (button == null) {
                return;
            }
            if (viewSection == section) {
                if (!button.getStyleClass().contains("nav-item-active")) {
                    button.getStyleClass().add("nav-item-active");
                }
            } else {
                button.getStyleClass().remove("nav-item-active");
            }
        });
    }

    private void ensureSpacesLoaded() {
        if (!spacesLoaded) {
            loadSpaces();
        }
    }

    private void loadSpaces() {
        if (sessionManager == null || sessionManager.getAccessToken() == null || sessionManager.getAccessToken().isBlank()) {
            showSpacesStatus("No hay una sesión válida. Inicia sesión nuevamente.", "status-error");
            return;
        }

        Task<List<SpaceDTO>> task = new Task<>() {
            @Override
            protected List<SpaceDTO> call() {
                return spaceController.loadSpaces(sessionManager.getAccessToken());
            }
        };

        task.setOnRunning(event -> showSpacesLoading("Cargando espacios disponibles..."));
        task.setOnSucceeded(event -> {
            List<SpaceDTO> items = task.getValue();
            spaces.setAll(items);
            if (spacesCountLabel != null) {
                spacesCountLabel.setText("Espacios (" + items.size() + ")");
            }
            hideSpacesLoading();
            if (items.isEmpty()) {
                showSpacesStatus("No se encontraron espacios registrados.", "status-info");
            } else {
                showSpacesStatus("Espacios actualizados correctamente.", "status-success");
            }
            spacesLoaded = true;
        });
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message;
            if (error instanceof ApiClientException apiError) {
                message = "Error " + apiError.getStatusCode();
                if (apiError.getResponseBody() != null && !apiError.getResponseBody().isBlank()) {
                    message += ": " + apiError.getResponseBody();
                }
            } else {
                message = error != null ? error.getMessage() : "Error desconocido";
            }
            hideSpacesLoading();
            showSpacesStatus("No se pudieron cargar los espacios: " + message, "status-error");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showSpacesLoading(String message) {
        if (spacesLoadingOverlay != null) {
            spacesLoadingOverlay.setVisible(true);
            spacesLoadingOverlay.setManaged(true);
        }
        if (spacesLoadingLabel != null) {
            spacesLoadingLabel.setText(message);
            spacesLoadingLabel.getStyleClass().removeAll("status-success", "status-error");
            if (!spacesLoadingLabel.getStyleClass().contains("status-info")) {
                spacesLoadingLabel.getStyleClass().add("status-info");
            }
        }
        if (spacesStatusLabel != null) {
            spacesStatusLabel.setVisible(false);
            spacesStatusLabel.setManaged(false);
        }
    }

    private void hideSpacesLoading() {
        if (spacesLoadingOverlay != null) {
            spacesLoadingOverlay.setVisible(false);
            spacesLoadingOverlay.setManaged(false);
        }
    }

    private void showSpacesStatus(String message, String statusStyle) {
        if (spacesStatusLabel == null) {
            return;
        }
        spacesStatusLabel.setText(message);
        spacesStatusLabel.getStyleClass().removeAll("status-info", "status-success", "status-error");
        if (statusStyle != null && !statusStyle.isBlank()) {
            if (!spacesStatusLabel.getStyleClass().contains("status-label")) {
                spacesStatusLabel.getStyleClass().add("status-label");
            }
            if (!spacesStatusLabel.getStyleClass().contains(statusStyle)) {
                spacesStatusLabel.getStyleClass().add(statusStyle);
            }
        }
        spacesStatusLabel.setVisible(true);
        spacesStatusLabel.setManaged(true);
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        spacesLoaded = false;
        if (sessionManager != null) {
            sessionManager.getAuthResponse().ifPresent(this::applyUserInfo);
        }
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private void applyUserInfo(AuthResponse response) {
        if (response == null) {
            return;
        }
        if (userNameLabel != null && response.name() != null && !response.name().isBlank()) {
            userNameLabel.setText(response.name());
        }
        if (userRoleLabel != null && response.role() != null && !response.role().isBlank()) {
            userRoleLabel.setText(response.role());
        }
    }

    /**
     * Ensures the dashboard is ready after being loaded dynamically.
     * This is typically invoked by the login flow once the scene switches
     * to the administrator dashboard.
     */
    public void bootstrap() {
        if (sessionManager != null) {
            sessionManager.getAuthResponse().ifPresent(this::applyUserInfo);
        }
        ViewSection target = activeView != null ? activeView : ViewSection.DASHBOARD;
        showView(target);
    }

    public enum ViewSection {
        DASHBOARD,
        SPACES,
        USERS,
        RESERVATIONS,
        REPORTS,
        CLIMATE,
        SETTINGS
    }
}
