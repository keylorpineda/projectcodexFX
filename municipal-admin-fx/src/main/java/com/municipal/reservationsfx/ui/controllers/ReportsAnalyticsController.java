package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

public class ReportsAnalyticsController implements DashboardSubview {

    @FXML
    private ChoiceBox<String> rangeFilter;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Button refreshButton;
    @FXML
    private Button exportExcelButton;
    @FXML
    private Button exportPdfButton;
    @FXML
    private Label activeReservationsLabel;
    @FXML
    private Label completedReservationsLabel;
    @FXML
    private Label attendanceRateLabel;
    @FXML
    private Label attendanceTrendLabel;
    @FXML
    private Label incidentsLabel;
    @FXML
    private PieChart spaceDistributionChart;
    @FXML
    private ListView<String> topSpacesList;
    @FXML
    private TableView<MonthlySummaryRow> monthlySummaryTable;
    @FXML
    private TableColumn<MonthlySummaryRow, String> monthColumn;
    @FXML
    private TableColumn<MonthlySummaryRow, String> reservationsColumn;
    @FXML
    private TableColumn<MonthlySummaryRow, String> attendanceColumn;
    @FXML
    private TableColumn<MonthlySummaryRow, String> growthColumn;

    private final ObservableList<MonthlySummaryRow> monthlyData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureFilters();
        configureButtons();
        configureLists();
        configureTable();
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        loadMetrics();
    }

    private void configureFilters() {
        rangeFilter.setItems(FXCollections.observableArrayList("Últimos 7 días", "Últimos 30 días", "Últimos 90 días", "Personalizado"));
        rangeFilter.getSelectionModel().selectFirst();
        startDatePicker.setValue(LocalDate.now().minusDays(7));
        endDatePicker.setValue(LocalDate.now());

        rangeFilter.valueProperty().addListener((obs, oldValue, newValue) -> adjustDates(newValue));
        refreshButton.setOnAction(event -> loadMetrics());
    }

    private void configureButtons() {
        exportExcelButton.getStyleClass().add("primary-button-raised");
        exportPdfButton.getStyleClass().add("primary-button-raised");
        exportExcelButton.setOnAction(event -> animateButton(exportExcelButton));
        exportPdfButton.setOnAction(event -> animateButton(exportPdfButton));
    }

    private void configureLists() {
        topSpacesList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    if (!getStyleClass().contains("top-space-cell")) {
                        getStyleClass().add("top-space-cell");
                    }
                }
            }
        });
    }

    private void configureTable() {
        monthColumn.setCellValueFactory(data -> data.getValue().monthProperty());
        reservationsColumn.setCellValueFactory(data -> data.getValue().reservationsProperty());
        attendanceColumn.setCellValueFactory(data -> data.getValue().attendanceProperty());
        growthColumn.setCellValueFactory(data -> data.getValue().growthProperty());
        monthlySummaryTable.setItems(monthlyData);
    }

    private void adjustDates(String range) {
        LocalDate end = LocalDate.now();
        LocalDate start = switch (range) {
            case "Últimos 7 días" -> end.minusDays(7);
            case "Últimos 30 días" -> end.minusDays(30);
            case "Últimos 90 días" -> end.minusDays(90);
            default -> startDatePicker.getValue();
        };
        if (!"Personalizado".equals(range)) {
            startDatePicker.setValue(start);
            endDatePicker.setValue(end);
        }
        loadMetrics();
    }

    private void loadMetrics() {
        activeReservationsLabel.setText("2");
        completedReservationsLabel.setText("8");
        attendanceRateLabel.setText("88.9%");
        attendanceTrendLabel.setText("+12% vs mes anterior");
        incidentsLabel.setText("1");

        spaceDistributionChart.setData(FXCollections.observableArrayList(
                new PieChart.Data("Interior", 62),
                new PieChart.Data("Exterior", 38)
        ));

        topSpacesList.getItems().setAll(
                "Salón Comunal Central · 32 reservas",
                "Auditorio Municipal · 24 reservas",
                "Parque Recreativo Sur · 18 reservas",
                "Cancha Deportiva Norte · 15 reservas"
        );

        monthlyData.setAll(
                buildRow(Month.JANUARY, 58, 85, "+6%"),
                buildRow(Month.FEBRUARY, 64, 88, "+4%"),
                buildRow(Month.MARCH, 72, 91, "+8%")
        );

        animateButton(refreshButton);
    }

    private MonthlySummaryRow buildRow(Month month, int reservations, int attendance, String growth) {
        String monthLabel = month.getDisplayName(TextStyle.FULL, new Locale("es", "CR"));
        return new MonthlySummaryRow(monthLabel, reservations + "", attendance + "%", growth);
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

    private record MonthlySummaryRow(String month, String reservations, String attendance, String growth) {
        javafx.beans.property.SimpleStringProperty monthProperty() {
            return new javafx.beans.property.SimpleStringProperty(month);
        }

        javafx.beans.property.SimpleStringProperty reservationsProperty() {
            return new javafx.beans.property.SimpleStringProperty(reservations);
        }

        javafx.beans.property.SimpleStringProperty attendanceProperty() {
            return new javafx.beans.property.SimpleStringProperty(attendance);
        }

        javafx.beans.property.SimpleStringProperty growthProperty() {
            return new javafx.beans.property.SimpleStringProperty(growth);
        }
    }
}
