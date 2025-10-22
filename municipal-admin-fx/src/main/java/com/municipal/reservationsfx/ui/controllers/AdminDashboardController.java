package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AdminDashboardController {

    @FXML
    private VBox mainContainer;
    @FXML
    private Label activeSpacesLabel;
    @FXML
    private Label todayReservationsLabel;
    @FXML
    private Label weeklyOccupancyLabel;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private ListView<String> alertsList;
    @FXML
    private GridPane reservationsGrid;
    @FXML
    private FlowPane spacesFlow;

    @FXML
    public void initialize() {
        FadeTransition fade = new FadeTransition(Duration.millis(600), mainContainer);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    public void bootstrap() {
        activeSpacesLabel.setText("5 / 6");
        todayReservationsLabel.setText("3");
        weeklyOccupancyLabel.setText("72.5%");
        totalUsersLabel.setText("48");

        alertsList.getItems().setAll(
                "⚠️ Alerta meteorológica: Posible tormenta eléctrica",
                "ℹ️ Recordatorio: Actualizar reporte semanal"
        );

        spacesFlow.getChildren().clear();
        spacesFlow.getChildren().addAll(DashboardCardFactory.createSpaceCards());

        DashboardCardFactory.populateReservations(reservationsGrid);
    }
}
