package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.ScaleTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

public class UsersManagementController implements DashboardSubview {

    @FXML
    private TextField searchField;
    @FXML
    private ChoiceBox<String> roleFilter;
    @FXML
    private ChoiceBox<String> statusFilter;
    @FXML
    private ChoiceBox<String> sortFilter;
    @FXML
    private TableView<UserRow> usersTable;
    @FXML
    private TableColumn<UserRow, String> nameColumn;
    @FXML
    private TableColumn<UserRow, String> emailColumn;
    @FXML
    private TableColumn<UserRow, String> roleColumn;
    @FXML
    private TableColumn<UserRow, String> stateColumn;
    @FXML
    private TableColumn<UserRow, String> lastAccessColumn;
    @FXML
    private TableColumn<UserRow, String> actionsColumn;
    @FXML
    private Button addUserButton;
    @FXML
    private Label usersSummaryLabel;

    private final ObservableList<UserRow> masterUsers = FXCollections.observableArrayList(
            new UserRow("María González", "maria.gonzalez@perezzeledon.go.cr", "Administrador", "Activo", LocalDateTime.now().minusHours(2)),
            new UserRow("Carlos Rodríguez", "carlos.rodriguez@perezzeledon.go.cr", "Administrador", "Activo", LocalDateTime.now().minusMinutes(45)),
            new UserRow("Juan Pérez", "juan.perez@perezzeledon.go.cr", "Supervisor", "Activo", LocalDateTime.now().minusDays(1)),
            new UserRow("Ana Jiménez", "ana.jimenez@perezzeledon.go.cr", "Operador", "Suspendido", LocalDateTime.now().minusDays(6)),
            new UserRow("Laura Torres", "laura.torres@perezzeledon.go.cr", "Operador", "Activo", LocalDateTime.now().minusHours(6))
    );

    private FilteredList<UserRow> filteredUsers;
    private SortedList<UserRow> sortedUsers;

    @FXML
    public void initialize() {
        configureFilters();
        configureTable();
        configureAddUserButton();
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        if (filteredUsers == null) {
            filteredUsers = new FilteredList<>(masterUsers, user -> true);
            sortedUsers = new SortedList<>(filteredUsers);
            usersTable.setItems(sortedUsers);
        }
        updateFilter();
        applySort();
    }

    private void configureFilters() {
        roleFilter.setItems(FXCollections.observableArrayList("Todos los roles", "Administrador", "Supervisor", "Operador"));
        statusFilter.setItems(FXCollections.observableArrayList("Todos los estados", "Activo", "Suspendido"));
        sortFilter.setItems(FXCollections.observableArrayList("Ordenar por nombre", "Último acceso reciente"));

        roleFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        sortFilter.getSelectionModel().selectFirst();

        searchField.textProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        roleFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        statusFilter.valueProperty().addListener((obs, oldValue, newValue) -> updateFilter());
        sortFilter.valueProperty().addListener((obs, oldValue, newValue) -> applySort());
    }

    private void configureTable() {
        nameColumn.setCellValueFactory(data -> data.getValue().nameProperty());
        emailColumn.setCellValueFactory(data -> data.getValue().emailProperty());
        roleColumn.setCellValueFactory(data -> data.getValue().roleProperty());
        stateColumn.setCellValueFactory(data -> data.getValue().statusProperty());
        lastAccessColumn.setCellValueFactory(data -> data.getValue().lastAccessLabelProperty());
        actionsColumn.setCellValueFactory(data -> data.getValue().nameProperty());

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
            private final Button resetButton = buildActionButton("Resetear");
            private final Button permissionsButton = buildActionButton("Permisos");
            private final HBox container = new HBox(8, permissionsButton, resetButton);

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

    private void configureAddUserButton() {
        if (addUserButton != null) {
            addUserButton.getStyleClass().add("primary-button-raised");
            addUserButton.setOnAction(event -> animateButton(addUserButton));
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
        if (filteredUsers == null) {
            return;
        }
        String term = searchField.getText() == null ? "" : searchField.getText().toLowerCase(Locale.ROOT);
        String role = roleFilter.getValue();
        String status = statusFilter.getValue();

        filteredUsers.setPredicate(user -> matchesTerm(user, term) && matchesRole(user, role) && matchesStatus(user, status));
        updateSummary();
    }

    private void applySort() {
        if (sortedUsers == null) {
            return;
        }
        Comparator<UserRow> comparator = switch (sortFilter.getValue()) {
            case "Último acceso reciente" -> Comparator.comparing(UserRow::lastAccess).reversed();
            default -> Comparator.comparing(UserRow::name);
        };
        sortedUsers.setComparator(comparator);
        updateSummary();
    }

    private void updateSummary() {
        if (sortedUsers == null) {
            return;
        }
        long active = sortedUsers.stream().filter(user -> "Activo".equalsIgnoreCase(user.status())).count();
        usersSummaryLabel.setText(sortedUsers.size() + " usuarios · " + active + " activos");
    }

    private boolean matchesTerm(UserRow user, String term) {
        if (term == null || term.isBlank()) {
            return true;
        }
        return user.name().toLowerCase(Locale.ROOT).contains(term) || user.email().toLowerCase(Locale.ROOT).contains(term);
    }

    private boolean matchesRole(UserRow user, String role) {
        return role == null || role.startsWith("Todos") || user.role().equalsIgnoreCase(role);
    }

    private boolean matchesStatus(UserRow user, String status) {
        return status == null || status.startsWith("Todos") || user.status().equalsIgnoreCase(status);
    }

    private String statusToClass(String status) {
        return switch (status.toLowerCase(Locale.ROOT)) {
            case "activo" -> "status-available";
            case "suspendido" -> "status-booked";
            default -> "status-default";
        };
    }

    private record UserRow(String name, String email, String role, String status, LocalDateTime lastAccess) {
        javafx.beans.property.SimpleStringProperty nameProperty() {
            return new javafx.beans.property.SimpleStringProperty(name);
        }

        javafx.beans.property.SimpleStringProperty emailProperty() {
            return new javafx.beans.property.SimpleStringProperty(email);
        }

        javafx.beans.property.SimpleStringProperty roleProperty() {
            return new javafx.beans.property.SimpleStringProperty(role);
        }

        javafx.beans.property.SimpleStringProperty statusProperty() {
            return new javafx.beans.property.SimpleStringProperty(status);
        }

        javafx.beans.property.SimpleStringProperty lastAccessLabelProperty() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", new Locale("es", "CR"));
            return new javafx.beans.property.SimpleStringProperty(lastAccess.format(formatter));
        }
    }
}
