package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.css.PseudoClass;
import javafx.util.Duration;

import java.util.Locale;

public class SpacesManagementController implements DashboardSubview {

    private static final PseudoClass ROW_HOVER = PseudoClass.getPseudoClass("hovered");

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> typeFilter;
    @FXML
    private ChoiceBox<String> capacityFilter;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private TableView<SpaceRow> spacesTable;
    @FXML
    private TableColumn<SpaceRow, String> nameColumn;
    @FXML
    private TableColumn<SpaceRow, String> typeColumn;
    @FXML
    private TableColumn<SpaceRow, String> capacityColumn;
    @FXML
    private TableColumn<SpaceRow, String> statusColumn;
    @FXML
    private TableColumn<SpaceRow, String> actionsColumn;
    @FXML
    private Button newSpaceButton;
    @FXML
    private Label spacesSummaryLabel;

    private final ObservableList<SpaceRow> masterSpaces = FXCollections.observableArrayList(
            new SpaceRow("Salón Comunal Central", "Interior", 150, "Disponible"),
            new SpaceRow("Cancha Deportiva Norte", "Exterior", 80, "Reservado"),
            new SpaceRow("Auditorio Municipal", "Interior", 300, "Disponible"),
            new SpaceRow("Plaza Cultural", "Exterior", 200, "Mantenimiento"),
            new SpaceRow("Parque Recreativo Sur", "Exterior", 120, "Disponible"),
            new SpaceRow("Gimnasio Municipal", "Interior", 220, "Reservado")
    );

    private FilteredList<SpaceRow> filteredSpaces;

    @FXML
    public void initialize() {
        configureFilters();
        configureTable();
        configureNewButton();
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        if (filteredSpaces == null) {
            filteredSpaces = new FilteredList<>(masterSpaces, space -> true);
            spacesTable.setItems(filteredSpaces);
        }
        updateFilter();
    }

    private void configureFilters() {
        typeFilter.setItems(FXCollections.observableArrayList("Todos los tipos", "Interior", "Exterior"));
        capacityFilter.setItems(FXCollections.observableArrayList("Todas las capacidades", "≤ 100", "101 - 200", "200+"));
        statusFilter.setItems(FXCollections.observableArrayList("Todos los estados", "Disponible", "Reservado", "Mantenimiento"));

        typeFilter.getSelectionModel().selectFirst();
        capacityFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        typeFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        capacityFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        typeColumn.setCellValueFactory(data -> data.getValue().typeProperty());
        capacityColumn.setCellValueFactory(data -> data.getValue().capacityLabelProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        actionsColumn.setCellValueFactory(data -> data.getValue().nameProperty());

        statusColumn.setCellFactory(column -> new TableCell<>() {
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
            private final Button editButton = buildActionButton("Editar");
            private final Button viewButton = buildActionButton("Detalles");
            private final HBox container = new HBox(8, viewButton, editButton);

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

        spacesTable.setRowFactory(table -> {
            TableRow<SpaceRow> row = new TableRow<>();
            row.hoverProperty().addListener((obs, wasHover, isHover) -> {
                if (!row.isEmpty()) {
                    row.pseudoClassStateChanged(ROW_HOVER, isHover);
                }
            });
            return row;
        });
    }

    private Button buildActionButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("pill-button");
        button.setOnAction(event -> animateButton(button));
        return button;
    }

    private void configureNewButton() {
        if (newSpaceButton != null) {
            newSpaceButton.getStyleClass().add("primary-button-raised");
            newSpaceButton.setOnAction(event -> animateButton(newSpaceButton));
        }
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
        if (filteredSpaces == null) {
            return;
        }
        String searchTerm = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT);
        String selectedType = typeFilter.getValue();
        String selectedCapacity = capacityFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        filteredSpaces.setPredicate(space ->
                matchesSearch(space, searchTerm) &&
                        matchesType(space, selectedType) &&
                        matchesCapacity(space, selectedCapacity) &&
                        matchesStatus(space, selectedStatus));

        long available = filteredSpaces.stream().filter(space -> "Disponible".equalsIgnoreCase(space.status())).count();
        spacesSummaryLabel.setText(filteredSpaces.size() + " espacios · " + available + " disponibles");
    }

    private boolean matchesSearch(SpaceRow space, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank()) {
            return true;
        }
        return space.name().toLowerCase(Locale.ROOT).contains(searchTerm)
                || space.type().toLowerCase(Locale.ROOT).contains(searchTerm);
    }

    private boolean matchesType(SpaceRow space, String selectedType) {
        return selectedType == null || selectedType.startsWith("Todos") || space.type().equalsIgnoreCase(selectedType);
    }

    private boolean matchesCapacity(SpaceRow space, String selectedCapacity) {
        if (selectedCapacity == null || selectedCapacity.startsWith("Todas")) {
            return true;
        }
        return switch (selectedCapacity) {
            case "≤ 100" -> space.capacity() <= 100;
            case "101 - 200" -> space.capacity() > 100 && space.capacity() <= 200;
            case "200+" -> space.capacity() > 200;
            default -> true;
        };
    }

    private boolean matchesStatus(SpaceRow space, String selectedStatus) {
        return selectedStatus == null || selectedStatus.startsWith("Todos") || space.status().equalsIgnoreCase(selectedStatus);
    }

    private String statusToClass(String status) {
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "disponible" -> "status-available";
            case "reservado" -> "status-booked";
            case "mantenimiento" -> "status-maintenance";
            default -> "status-default";
        };
    }

    private record SpaceRow(String name, String type, int capacity, String status) {
        javafx.beans.property.SimpleStringProperty nameProperty() {
            return new javafx.beans.property.SimpleStringProperty(name);
        }

        javafx.beans.property.SimpleStringProperty typeProperty() {
            return new javafx.beans.property.SimpleStringProperty(type);
        }

        javafx.beans.property.SimpleStringProperty capacityLabelProperty() {
            return new javafx.beans.property.SimpleStringProperty(capacity + " personas");
        }

        javafx.beans.property.SimpleStringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }
    }
}
