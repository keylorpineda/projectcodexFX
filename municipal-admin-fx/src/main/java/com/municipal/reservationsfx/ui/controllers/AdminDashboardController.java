package com.municipal.reservationsfx.ui.controllers;

import com.municipal.reservationsfx.ui.utils.NodeVisibilityUtils;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
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

    private final Map<ViewSection, Button> navigationButtons = new EnumMap<>(ViewSection.class);
    private final Map<ViewSection, Node> sectionRegistry = new EnumMap<>(ViewSection.class);
    private final List<Node> contentSections = new ArrayList<>();
    private ViewSection activeView = null;

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
        showView(ViewSection.DASHBOARD);
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

    /**
     * Ensures the dashboard is ready after being loaded dynamically.
     * This is typically invoked by the login flow once the scene switches
     * to the administrator dashboard.
     */
    public void bootstrap() {
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
