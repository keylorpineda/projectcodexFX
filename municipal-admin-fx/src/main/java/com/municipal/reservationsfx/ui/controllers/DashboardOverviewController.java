package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardOverviewController implements DashboardSubview {

    @FXML
    private Label activeSpacesLabel;
    @FXML
    private Label todayReservationsLabel;
    @FXML
    private Label missedLabel;
    @FXML
    private Label weeklyOccupancyLabel;
    @FXML
    private Label weeklyTrendLabel;
    @FXML
    private Label weatherUpdatedLabel;
    @FXML
    private Label weatherTempLabel;
    @FXML
    private Label weatherConditionLabel;
    @FXML
    private Label weatherWindLabel;
    @FXML
    private Label weatherRainLabel;
    @FXML
    private Label weatherIconLabel;
    @FXML
    private Label weatherMessageLabel;
    @FXML
    private FlowPane spacesFlow;
    @FXML
    private GridPane reservationsGrid;
    @FXML
    private ListView<String> alertsList;
    @FXML
    private Label alertCountBadge;
    @FXML
    private Button refreshButton;

    private AdminDashboardController host;

    @FXML
    public void initialize() {
        if (alertsList != null) {
            alertsList.setCellFactory(list -> new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        getStyleClass().removeAll("alert-list-cell", "alert-item-warning");
                    } else {
                        setText(item);
                        if (!getStyleClass().contains("alert-list-cell")) {
                            getStyleClass().addAll("alert-list-cell", "alert-item-warning");
                        }
                    }
                }
            });
        }

        if (refreshButton != null) {
            refreshButton.setOnAction(event -> bootstrap());
        }
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        this.host = host;
        bootstrap();
    }

    private void bootstrap() {
        activeSpacesLabel.setText("5 / 6");
        todayReservationsLabel.setText("3");
        missedLabel.setText("5");
        weeklyOccupancyLabel.setText("72.5%");
        weeklyTrendLabel.setText("+5.2% ↑");

        weatherUpdatedLabel.setText("Actualizado " + DateTimeFormatter.ofPattern("HH:mm").format(java.time.LocalTime.now()));
        weatherTempLabel.setText("24°C");
        weatherConditionLabel.setText("Soleado");
        weatherWindLabel.setText("Viento 12 km/h");
        weatherRainLabel.setText("Lluvia 10%");
        weatherIconLabel.setText("☀️");
        weatherMessageLabel.setText("Excelente día para actividades al aire libre");

        populateAlerts();
        populateSpaces();
        populateReservations();
        playRefreshAnimation();
    }

    private void populateAlerts() {
        List<String> alerts = List.of(
                "⚠️ Cancha Deportiva Norte · Alta probabilidad de lluvia",
                "☁️ Parque Recreativo Sur · Condiciones variables"
        );
        alertsList.getItems().setAll(alerts);
        alertCountBadge.setText(String.valueOf(alerts.size()));
    }

    private void populateSpaces() {
        spacesFlow.getChildren().setAll(DashboardCardFactory.createSpaceCards());
    }

    private void populateReservations() {
        reservationsGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);
        DashboardCardFactory.populateReservations(reservationsGrid);
    }

    private void playRefreshAnimation() {
        FadeTransition fade = new FadeTransition(Duration.millis(280), spacesFlow);
        fade.setFromValue(0.85);
        fade.setToValue(1);
        fade.playFromStart();
    }

    @FXML
    private void handleManageSpaces() {
        if (host != null) {
            host.navigateTo(AdminDashboardController.ViewSection.SPACES);
        }
    }

    @FXML
    private void handleViewUsers() {
        if (host != null) {
            host.navigateTo(AdminDashboardController.ViewSection.USERS);
        }
    }

    @FXML
    private void handleControlReservations() {
        if (host != null) {
            host.navigateTo(AdminDashboardController.ViewSection.RESERVATIONS);
        }
    }

    @FXML
    private void handleClimateMonitoring() {
        if (host != null) {
            host.navigateTo(AdminDashboardController.ViewSection.CLIMATE);
        }
    }
}
