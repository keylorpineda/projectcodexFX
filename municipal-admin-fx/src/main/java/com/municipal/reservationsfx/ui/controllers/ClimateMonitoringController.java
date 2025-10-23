package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import java.util.List;

public class ClimateMonitoringController implements DashboardSubview {

    @FXML
    private Label monitoredSpacesLabel;
    @FXML
    private Label activeAlertsLabel;
    @FXML
    private Label affectedReservationsLabel;
    @FXML
    private ListView<String> alertsList;
    @FXML
    private FlowPane spacesFlow;
    @FXML
    private Button refreshButton;

    @FXML
    public void initialize() {
        configureList();
        if (refreshButton != null) {
            refreshButton.getStyleClass().add("primary-button-raised");
            refreshButton.setOnAction(event -> refreshClimate());
        }
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        refreshClimate();
    }

    private void configureList() {
        alertsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (!getStyleClass().contains("alert-list-cell")) {
                        getStyleClass().addAll("alert-list-cell", "alert-item-warning");
                    }
                }
            }
        });
    }

    private void refreshClimate() {
        monitoredSpacesLabel.setText("6");
        activeAlertsLabel.setText("3");
        affectedReservationsLabel.setText("2");

        alertsList.getItems().setAll(
                "⚠️ Cancha Deportiva Norte · Probabilidad de lluvia intensa",
                "🌧 Parque Recreativo Sur · Precipitación ligera prevista",
                "💨 Plaza Cultural · Rachas de viento moderadas"
        );

        populateSpaces();
        animateButton(refreshButton);
    }

    private void populateSpaces() {
        spacesFlow.getChildren().clear();
        List<ClimateCard> cards = List.of(
                new ClimateCard("Cancha Deportiva Norte", "27°C", "Rainy", "Probabilidad de lluvia", "80%", "Alertas activas"),
                new ClimateCard("Parque Recreativo Sur", "25°C", "Cloudy", "Nubosidad variable", "40%", "Seguimiento"),
                new ClimateCard("Plaza Cultural", "23°C", "Breezy", "Vientos moderados", "20%", "Alerta preventiva"),
                new ClimateCard("Auditorio Municipal", "21°C", "Indoor", "Sin alertas", "5%", "Condiciones estables")
        );

        for (ClimateCard card : cards) {
            spacesFlow.getChildren().add(buildClimateCard(card));
        }
    }

    private VBox buildClimateCard(ClimateCard card) {
        VBox container = new VBox(8);
        container.getStyleClass().addAll("card", "climate-card");
        container.setPadding(new Insets(18));

        Label title = new Label(card.name());
        title.getStyleClass().add("card-title");

        Label temperature = new Label(card.temperature());
        temperature.getStyleClass().add("weather-temp-small");

        Label condition = new Label(card.condition());
        condition.getStyleClass().add("weather-condition");

        HBox detailsRow = new HBox(12);
        detailsRow.getChildren().addAll(buildDetail("🌧", card.precipitation()), buildDetail("📊", card.chance()));

        Label footer = new Label(card.footer());
        footer.getStyleClass().add("card-subtitle");

        container.getChildren().addAll(title, temperature, condition, detailsRow, footer);
        return container;
    }

    private VBox buildDetail(String icon, String text) {
        VBox box = new VBox(4);
        box.getStyleClass().add("climate-detail");
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("climate-detail-icon");
        Label textLabel = new Label(text);
        textLabel.getStyleClass().add("climate-detail-text");
        box.getChildren().addAll(iconLabel, textLabel);
        return box;
    }

    private void animateButton(Button button) {
        if (button == null) {
            return;
        }
        ScaleTransition scale = new ScaleTransition(Duration.millis(220), button);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.playFromStart();
    }

    private record ClimateCard(String name, String temperature, String condition, String precipitation, String chance, String footer) {
    }
}
