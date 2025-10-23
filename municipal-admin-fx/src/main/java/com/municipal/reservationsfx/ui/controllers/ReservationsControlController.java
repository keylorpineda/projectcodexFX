package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ReservationsControlController implements DashboardSubview {

    @FXML
    private TextField searchField;
    @FXML
    private DatePicker dateFilter;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private ChoiceBox<String> typeFilter;
    @FXML
    private TableView<ReservationRow> reservationsTable;
    @FXML
    private TableColumn<ReservationRow, String> userColumn;
    @FXML
    private TableColumn<ReservationRow, String> spaceColumn;
    @FXML
    private TableColumn<ReservationRow, String> dateColumn;
    @FXML
    private TableColumn<ReservationRow, String> timeColumn;
    @FXML
    private TableColumn<ReservationRow, String> stateColumn;
    @FXML
    private TableColumn<ReservationRow, String> qrColumn;
    @FXML
    private TableColumn<ReservationRow, String> weatherColumn;
    @FXML
    private TableColumn<ReservationRow, String> actionsColumn;
    @FXML
    private Button exportButton;
    @FXML
    private Label reservationsSummaryLabel;

    private final ObservableList<ReservationRow> masterReservations = FXCollections.observableArrayList(
            new ReservationRow("María González", "Salón Comunal Central", LocalDate.of(2025, 10, 22), "09:00", "Confirmada", "QR", "24°C", "Interior"),
            new ReservationRow("Carlos Rodríguez", "Auditorio Municipal", LocalDate.of(2025, 10, 22), "14:00", "Confirmada", "QR", "22°C", "Interior"),
            new ReservationRow("Juan Pérez", "Cancha Deportiva Norte", LocalDate.of(2025, 10, 23), "16:00", "Pendiente", "QR", "26°C", "Exterior"),
            new ReservationRow("Ana Jiménez", "Parque Recreativo Sur", LocalDate.of(2025, 10, 23), "18:00", "Cancelada", "N/A", "25°C", "Exterior"),
            new ReservationRow("Laura Torres", "Plaza Cultural", LocalDate.of(2025, 10, 24), "19:00", "Confirmada", "QR", "23°C", "Exterior")
    );

    private FilteredList<ReservationRow> filteredReservations;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", new Locale("es", "CR"));

    @FXML
    public void initialize() {
        configureFilters();
        configureTable();
        configureExportButton();
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        if (filteredReservations == null) {
            filteredReservations = new FilteredList<>(masterReservations, reservation -> true);
            reservationsTable.setItems(filteredReservations);
        }
        updateFilter();
    }

    private void configureFilters() {
        statusFilter.setItems(FXCollections.observableArrayList("Todos los estados", "Confirmada", "Pendiente", "Cancelada"));
        typeFilter.setItems(FXCollections.observableArrayList("Todos los tipos", "Interior", "Exterior"));

        statusFilter.getSelectionModel().selectFirst();
        typeFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        typeFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        dateFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
    }

    private void configureTable() {
        userColumn.setCellValueFactory(data -> data.getValue().userProperty());
        spaceColumn.setCellValueFactory(data -> data.getValue().spaceProperty());
        dateColumn.setCellValueFactory(data -> data.getValue().dateLabelProperty(dateFormatter));
        timeColumn.setCellValueFactory(data -> data.getValue().timeProperty());
        stateColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        qrColumn.setCellValueFactory(data -> data.getValue().qrProperty());
        weatherColumn.setCellValueFactory(data -> data.getValue().weatherProperty());
        actionsColumn.setCellValueFactory(data -> data.getValue().userProperty());

        stateColumn.setCellFactory(column -> new TableCell<>() {
            private final Label pill = new Label();

            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    pill.setText(status);
                    pill.getStyleClass().setAll("status-pill", statusToClass(status));
                    setGraphic(pill);
                    setText(null);
                }
            }
        });

        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private final Button detailsButton = buildActionButton("Detalle");
            private final Button cancelButton = buildActionButton("Cancelar");
            private final HBox container = new HBox(8, detailsButton, cancelButton);

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });
    }

    private void configureExportButton() {
        if (exportButton != null) {
            exportButton.getStyleClass().add("primary-button-raised");
            exportButton.setOnAction(event -> animateButton(exportButton));
        }
    }

    private Button buildActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("pill-button");
        button.setOnAction(event -> animateButton(button));
        return button;
    }

    private void animateButton(Button button) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(220), button);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.playFromStart();
    }

    private void updateFilter() {
        if (filteredReservations == null) {
            return;
        }
        String term = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT);
        String status = statusFilter.getValue();
        String type = typeFilter.getValue();
        LocalDate date = dateFilter.getValue();

        filteredReservations.setPredicate(reservation ->
                matchesTerm(reservation, term) &&
                        matchesStatus(reservation, status) &&
                        matchesType(reservation, type) &&
                        matchesDate(reservation, date));

        long confirmed = filteredReservations.stream().filter(reservation -> "Confirmada".equalsIgnoreCase(reservation.status())).count();
        reservationsSummaryLabel.setText(filteredReservations.size() + " reservas · " + confirmed + " confirmadas");
    }

    private boolean matchesTerm(ReservationRow reservation, String term) {
        if (term == null || term.isBlank()) {
            return true;
        }
        return reservation.user().toLowerCase(Locale.ROOT).contains(term)
                || reservation.space().toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean matchesStatus(ReservationRow reservation, String status) {
        return status == null || status.startsWith("Todos") || reservation.status().equalsIgnoreCase(status);
    }

    private boolean matchesType(ReservationRow reservation, String type) {
        return type == null || type.startsWith("Todos") || reservation.type().equalsIgnoreCase(type);
    }

    private boolean matchesDate(ReservationRow reservation, LocalDate date) {
        return date == null || reservation.date().equals(date);
    }

    private String statusToClass(String status) {
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "confirmada" -> "status-available";
            case "pendiente" -> "status-warning";
            case "cancelada" -> "status-booked";
            default -> "status-default";
        };
    }

    private record ReservationRow(String user, String space, LocalDate date, String time, String status, String qr, String weather, String type) {
        javafx.beans.property.SimpleStringProperty userProperty() {
            return new javafx.beans.property.SimpleStringProperty(user);
        }

        javafx.beans.property.SimpleStringProperty spaceProperty() {
            return new javafx.beans.property.SimpleStringProperty(space);
        }

        javafx.beans.property.SimpleStringProperty dateLabelProperty(DateTimeFormatter formatter) {
            return new javafx.beans.property.SimpleStringProperty(date.format(formatter));
        }

        javafx.beans.property.SimpleStringProperty timeProperty() {
            return new javafx.beans.property.SimpleStringProperty(time);
        }

        javafx.beans.property.SimpleStringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }

        javafx.beans.property.SimpleStringProperty qrProperty() {
            return new javafx.beans.property.SimpleStringProperty(qr);
        }

        javafx.beans.property.SimpleStringProperty weatherProperty() {
            return new javafx.beans.property.SimpleStringProperty(weather);
        }
    }
}
