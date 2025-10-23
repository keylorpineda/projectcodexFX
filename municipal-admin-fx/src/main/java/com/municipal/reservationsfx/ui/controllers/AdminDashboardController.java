package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.FadeTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

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

    private final Map<ViewSection, Button> navigationButtons = new EnumMap<>(ViewSection.class);
    private final Map<ViewSection, ViewHolder> viewCache = new EnumMap<>(ViewSection.class);
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
        ViewHolder holder = viewCache.computeIfAbsent(section, this::loadView);
        if (holder == null) {
            return;
        }

        if (contentScroll != null) {
            contentScroll.setContent(holder.node);
            contentScroll.setVvalue(0);
        }

        applyContentTransition(holder.node);
        updateActiveNavigation(section);
        activeView = section;

        if (holder.controller != null) {
            holder.controller.onDisplay(this);
        }
    }

    private ViewHolder loadView(ViewSection section) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(section.fxmlPath));
            Node node = loader.load();
            DashboardSubview controller = null;
            Object candidate = loader.getController();
            if (candidate instanceof DashboardSubview subview) {
                controller = subview;
            }
            return new ViewHolder(node, controller);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void applyContentTransition(Node node) {
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
            ObservableList<String> styleClass = button.getStyleClass();
            if (viewSection == section) {
                if (!styleClass.contains("nav-item-active")) {
                    styleClass.add("nav-item-active");
                }
            } else {
                styleClass.remove("nav-item-active");
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
        DASHBOARD("/com/municipal/reservationsfx/ui/dashboard-overview-view.fxml"),
        SPACES("/com/municipal/reservationsfx/ui/spaces-management-view.fxml"),
        USERS("/com/municipal/reservationsfx/ui/users-management-view.fxml"),
        RESERVATIONS("/com/municipal/reservationsfx/ui/reservations-control-view.fxml"),
        REPORTS("/com/municipal/reservationsfx/ui/reports-analytics-view.fxml"),
        CLIMATE("/com/municipal/reservationsfx/ui/climate-monitoring-view.fxml"),
        SETTINGS("/com/municipal/reservationsfx/ui/system-settings-view.fxml");

        private final String fxmlPath;

        ViewSection(String fxmlPath) {
            this.fxmlPath = fxmlPath;
        }
    }

    private record ViewHolder(Node node, DashboardSubview controller) {
    }
}
