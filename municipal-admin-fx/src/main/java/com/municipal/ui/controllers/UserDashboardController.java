package com.municipal.ui.controllers;

import com.municipal.config.AppConfig;
import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.controllers.WeatherController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class UserDashboardController {

    // Session & Controllers
    private SessionManager sessionManager;
    private ReservationController reservationController;
    private SpaceController spaceController;
    private WeatherController weatherController;
    private String token;

    // FXML - Header
    @FXML private VBox mainContainer;
    @FXML private HBox userMenuContainer;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // FXML - Sidebar Navigation
    @FXML private HBox navDashboardButton;
    @FXML private HBox navSpacesButton;
    @FXML private HBox navMyReservationsButton;
    @FXML private HBox navReportsButton;

    // FXML - Content Scroll
    @FXML private ScrollPane contentScroll;

    // FXML - Dashboard Section
    @FXML private VBox dashboardSection;
    @FXML private Label activeReservationsLabel;
    @FXML private Label noShowsLabel;
    @FXML private Label completedReservationsLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherConditionLabel;
    @FXML private Label weatherWindLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherMessageLabel;

    // FXML - Spaces Section
    @FXML private VBox spacesSection;
    @FXML private Label spacesCountLabel;
    @FXML private TextField searchSpaceField;
    @FXML private ChoiceBox<String> spaceTypeChoice;
    @FXML private ChoiceBox<String> capacityChoice;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane spacesFlowPane;
    @FXML private StackPane spacesLoadingOverlay;

    // FXML - My Reservations Section
    @FXML private VBox myReservationsSection;
    @FXML private Label reservationsCountLabel;
    @FXML private TableView<ReservationDTO> reservationsTable;
    @FXML private TableColumn<ReservationDTO, String> reservationSpaceColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationDateColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationTimeColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationEventColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationStatusColumn;
    @FXML private TableColumn<ReservationDTO, Void> reservationActionsColumn;
    @FXML private StackPane reservationsLoadingOverlay;

    // FXML - Reports Section
    @FXML private VBox reportsSection;
    @FXML private Label totalReservationsReportLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label favoriteSpaceLabel;
    @FXML private PieChart spacesDistributionChart;
    @FXML private PieChart attendanceChart;
    @FXML private Label attendancePercentageLabel;

    // Data
    private ObservableList<ReservationDTO> reservationsList = FXCollections.observableArrayList();
    private ObservableList<SpaceDTO> spacesList = FXCollections.observableArrayList();
    private List<SpaceDTO> allSpaces = new ArrayList<>();

    // Navigation
    private enum Section {
        DASHBOARD, SPACES, MY_RESERVATIONS, REPORTS
    }

    private Section currentSection = Section.DASHBOARD;

    // Status mapping
    private static final Map<String, String> STATUS_MAP = Map.of(
        "PENDING", "Pendiente",
        "CONFIRMED", "Confirmada",
        "CANCELLED", "Cancelada",
        "COMPLETED", "Completada",
        "NO_SHOW", "Inasistencia"
    );

    @FXML
    public void initialize() {
        setupUserMenu();
        setupNavigationHandlers();
        setupFilters();
        setupTableColumns();
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.token = sessionManager.getAccessToken();

        if (sessionManager.getCurrentUser() != null) {
            userNameLabel.setText(sessionManager.getCurrentUser().name());
            userRoleLabel.setText("Ciudadano");
        }
    }

    public void setControllers(ReservationController reservationController,
                              SpaceController spaceController,
                              WeatherController weatherController) {
        this.reservationController = reservationController;
        this.spaceController = spaceController;
        this.weatherController = weatherController;
    }

    public void loadInitialData(boolean loadWeather) {
        loadReservations();
        loadSpaces();
        if (loadWeather) {
            loadWeather();
        }
        updateDashboardMetrics();
    }

    // ==================== USER MENU ====================

    private void setupUserMenu() {
        if (userMenuContainer != null) {
            userMenuContainer.setOnMouseClicked(event -> showUserMenu(event));
            userMenuContainer.setStyle("-fx-cursor: hand;");
        }
    }

    private void showUserMenu(MouseEvent event) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem profileItem = new MenuItem("👤 Mi perfil");
        profileItem.setOnAction(e -> showProfile());

        MenuItem settingsItem = new MenuItem("⚙️ Configuración");
        settingsItem.setOnAction(e -> showSettings());

        SeparatorMenuItem separator = new SeparatorMenuItem();

        MenuItem logoutItem = new MenuItem("🚪 Cerrar sesión");
        logoutItem.setOnAction(e -> handleLogout());

        contextMenu.getItems().addAll(profileItem, settingsItem, separator, logoutItem);
        contextMenu.show(userMenuContainer, event.getScreenX(), event.getScreenY());
    }

    private void showProfile() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mi Perfil");
        alert.setHeaderText("Información del Usuario");
        
        if (sessionManager.getCurrentUser() != null) {
            String info = "Nombre: " + sessionManager.getCurrentUser().name() + "\n" +
                         "Email: " + sessionManager.getCurrentUser().email() + "\n" +
                         "Rol: Ciudadano";
            alert.setContentText(info);
        }
        
        alert.showAndWait();
    }

    private void showSettings() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración de Usuario");
        alert.setContentText("Funcionalidad en desarrollo");
        alert.showAndWait();
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar Sesión");
        confirm.setHeaderText("¿Estás seguro que deseas cerrar sesión?");
        confirm.setContentText("Tendrás que iniciar sesión nuevamente para acceder al sistema.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                sessionManager.clearSession();
                FlowController flowController = (FlowController) mainContainer.getScene().getWindow().getUserData();
                if (flowController != null) {
                    try {
                        flowController.navigateToLogin((Stage) mainContainer.getScene().getWindow());
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Error al cerrar sesión: " + e.getMessage());
                    }
                }
            }
        });
    }

    // ==================== NAVIGATION ====================

    private void setupNavigationHandlers() {
        setNavigationStyle(navDashboardButton, true);
    }

    @FXML
    private void handleNavigateDashboard() {
        navigateToSection(Section.DASHBOARD);
    }

    @FXML
    private void handleNavigateSpaces() {
        navigateToSection(Section.SPACES);
    }

    @FXML
    private void handleNavigateMyReservations() {
        navigateToSection(Section.MY_RESERVATIONS);
    }

    @FXML
    private void handleNavigateReports() {
        navigateToSection(Section.REPORTS);
    }

    private void navigateToSection(Section section) {
        currentSection = section;

        dashboardSection.setVisible(section == Section.DASHBOARD);
        dashboardSection.setManaged(section == Section.DASHBOARD);

        spacesSection.setVisible(section == Section.SPACES);
        spacesSection.setManaged(section == Section.SPACES);

        myReservationsSection.setVisible(section == Section.MY_RESERVATIONS);
        myReservationsSection.setManaged(section == Section.MY_RESERVATIONS);

        reportsSection.setVisible(section == Section.REPORTS);
        reportsSection.setManaged(section == Section.REPORTS);

        setNavigationStyle(navDashboardButton, section == Section.DASHBOARD);
        setNavigationStyle(navSpacesButton, section == Section.SPACES);
        setNavigationStyle(navMyReservationsButton, section == Section.MY_RESERVATIONS);
        setNavigationStyle(navReportsButton, section == Section.REPORTS);

        if (section == Section.SPACES) {
            loadSpaces();
        } else if (section == Section.MY_RESERVATIONS) {
            loadReservations();
        } else if (section == Section.REPORTS) {
            updateReportsData();
        }

        contentScroll.setVvalue(0);
    }

    private void setNavigationStyle(HBox navItem, boolean active) {
        if (navItem != null) {
            navItem.getStyleClass().removeAll("active");
            if (active) {
                navItem.getStyleClass().add("active");
            }
        }
    }

    // ==================== DATA LOADING ====================

    private void loadReservations() {
        if (sessionManager == null || reservationController == null) {
            return;
        }

        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return;
        }

        if (reservationsLoadingOverlay != null) {
            reservationsLoadingOverlay.setVisible(true);
            reservationsLoadingOverlay.setManaged(true);
        }

        Task<List<ReservationDTO>> task = new Task<>() {
            @Override
            protected List<ReservationDTO> call() throws Exception {
                return reservationController.getReservationsByUserId(userId, token);
            }
        };

        task.setOnSucceeded(e -> {
            List<ReservationDTO> data = task.getValue();
            reservationsList.clear();
            if (data != null) {
                reservationsList.addAll(data);
            }
            reservationsTable.setItems(reservationsList);
            updateReservationsCount();
            updateDashboardMetrics();
            if (reservationsLoadingOverlay != null) {
                reservationsLoadingOverlay.setVisible(false);
                reservationsLoadingOverlay.setManaged(false);
            }
        });

        task.setOnFailed(e -> {
            showError("Error al cargar reservas: " + task.getException().getMessage());
            if (reservationsLoadingOverlay != null) {
                reservationsLoadingOverlay.setVisible(false);
                reservationsLoadingOverlay.setManaged(false);
            }
        });

        new Thread(task).start();
    }

    private void loadSpaces() {
        if (spaceController == null) {
            return;
        }

        if (spacesLoadingOverlay != null) {
            spacesLoadingOverlay.setVisible(true);
            spacesLoadingOverlay.setManaged(true);
        }

        Task<List<SpaceDTO>> task = new Task<>() {
            @Override
            protected List<SpaceDTO> call() throws Exception {
                return spaceController.getAllSpaces(token);
            }
        };

        task.setOnSucceeded(e -> {
            List<SpaceDTO> data = task.getValue();
            allSpaces.clear();
            spacesList.clear();
            if (data != null) {
                allSpaces.addAll(data);
                spacesList.addAll(data);
            }
            displaySpaces(spacesList);
            updateSpacesCount();
            if (spacesLoadingOverlay != null) {
                spacesLoadingOverlay.setVisible(false);
                spacesLoadingOverlay.setManaged(false);
            }
        });

        task.setOnFailed(e -> {
            showError("Error al cargar espacios: " + task.getException().getMessage());
            if (spacesLoadingOverlay != null) {
                spacesLoadingOverlay.setVisible(false);
                spacesLoadingOverlay.setManaged(false);
            }
        });

        new Thread(task).start();
    }

    private void loadWeather() {
        if (weatherController == null) {
            return;
        }

        Task<CurrentWeatherDTO> task = new Task<>() {
            @Override
            protected CurrentWeatherDTO call() throws Exception {
                return weatherController.getCurrentWeather();
            }
        };

        task.setOnSucceeded(e -> {
            CurrentWeatherDTO weather = task.getValue();
            if (weather != null) {
                updateWeatherUI(weather);
            }
        });

        task.setOnFailed(e -> {
            weatherMessageLabel.setText("No se pudo cargar la información del clima");
        });

        new Thread(task).start();
    }

    private void updateWeatherUI(CurrentWeatherDTO weather) {
        weatherTempLabel.setText(String.format("%.1f°C", weather.main().temp()));
        weatherConditionLabel.setText(capitalizeFirst(weather.weather().get(0).description()));
        weatherWindLabel.setText(String.format("Viento: %.1f km/h", weather.wind().speed()));
        weatherHumidityLabel.setText(String.format("Humedad: %d%%", weather.main().humidity()));

        String icon = getWeatherIcon(weather.weather().get(0).main());
        weatherIconLabel.setText(icon);

        String message = getWeatherMessage(weather.weather().get(0).main(), weather.main().temp());
        weatherMessageLabel.setText(message);
    }

    private String getWeatherIcon(String condition) {
        return switch (condition.toLowerCase()) {
            case "clear" -> "☀️";
            case "clouds" -> "☁️";
            case "rain" -> "🌧️";
            case "drizzle" -> "🌦️";
            case "thunderstorm" -> "⛈️";
            case "snow" -> "❄️";
            case "mist", "fog" -> "🌫️";
            default -> "🌤️";
        };
    }

    private String getWeatherMessage(String condition, double temp) {
        if (condition.equalsIgnoreCase("rain") || condition.equalsIgnoreCase("thunderstorm")) {
            return "⚠️ Considera reservar espacios cubiertos debido a la lluvia.";
        } else if (temp > 30) {
            return "☀️ Día caluroso. Recomendamos espacios con sombra o climatizados.";
        } else if (temp < 18) {
            return "🌡️ Clima fresco. Ideal para actividades al aire libre.";
        } else {
            return "✨ Excelente clima para cualquier actividad.";
        }
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    // ==================== TABLE SETUP ====================

    private void setupTableColumns() {
        reservationSpaceColumn.setCellValueFactory(data -> {
            Long spaceId = data.getValue().spaceId();
            SpaceDTO space = spacesList.stream()
                    .filter(s -> s.id() != null && s.id().equals(spaceId))
                    .findFirst().orElse(null);
            String name = space != null ? space.name() : "Espacio #" + spaceId;
            return new javafx.beans.property.SimpleStringProperty(name);
        });

        reservationDateColumn.setCellValueFactory(data -> {
            LocalDateTime dateTime = data.getValue().startTime();
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        reservationTimeColumn.setCellValueFactory(data -> {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            String start = data.getValue().startTime().format(timeFormatter);
            String end = data.getValue().endTime().format(timeFormatter);
            return new javafx.beans.property.SimpleStringProperty(start + " - " + end);
        });

        reservationEventColumn.setCellValueFactory(data -> {
            String notes = data.getValue().notes();
            return new javafx.beans.property.SimpleStringProperty(notes != null ? notes : "N/A");
        });

        reservationStatusColumn.setCellValueFactory(data -> {
            String status = data.getValue().status();
            String displayStatus = STATUS_MAP.getOrDefault(status, status);
            return new javafx.beans.property.SimpleStringProperty(displayStatus);
        });

        reservationStatusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String displayStatus, boolean empty) {
                super.updateItem(displayStatus, empty);
                if (empty || displayStatus == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(displayStatus);
                    badge.getStyleClass().add("status-badge");

                    if ("Confirmada".equals(displayStatus)) {
                        badge.getStyleClass().add("status-confirmed");
                    } else if ("Completada".equals(displayStatus)) {
                        badge.getStyleClass().add("status-completed");
                    } else if ("Cancelada".equals(displayStatus) || "Inasistencia".equals(displayStatus)) {
                        badge.getStyleClass().add("status-cancelled");
                    } else {
                        badge.getStyleClass().add("status-pending");
                    }

                    setGraphic(badge);
                }
            }
        });

        reservationActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁");
            private final Button cancelBtn = new Button("✖");
            private final HBox box = new HBox(5, viewBtn, cancelBtn);

            {
                viewBtn.getStyleClass().add("action-button");
                cancelBtn.getStyleClass().add("action-button");
                cancelBtn.getStyleClass().add("cancel-button");
                box.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(e -> {
                    ReservationDTO res = getTableView().getItems().get(getIndex());
                    handleViewReservation(res);
                });

                cancelBtn.setOnAction(e -> {
                    ReservationDTO res = getTableView().getItems().get(getIndex());
                    handleCancelReservationConfirm(res);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ReservationDTO res = getTableView().getItems().get(getIndex());
                    if ("CONFIRMED".equals(res.status())) {
                        box.getChildren().setAll(viewBtn, cancelBtn);
                    } else {
                        box.getChildren().setAll(viewBtn);
                    }
                    setGraphic(box);
                }
            }
        });
    }

    // ==================== FILTERS ====================

    private void setupFilters() {
        if (spaceTypeChoice != null) {
            spaceTypeChoice.setItems(FXCollections.observableArrayList(
                "Todos", "Auditorio", "Sala", "Cancha", "Parque", "Gimnasio"
            ));
            spaceTypeChoice.setValue("Todos");
        }

        if (capacityChoice != null) {
            capacityChoice.setItems(FXCollections.observableArrayList(
                "Todas", "< 50", "50-100", "100-200", "> 200"
            ));
            capacityChoice.setValue("Todas");
        }

        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
        }
    }

    @FXML
    private void handleApplyFilters() {
        List<SpaceDTO> filtered = new ArrayList<>(allSpaces);

        String searchText = searchSpaceField.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filtered = filtered.stream()
                    .filter(s -> s.name().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        String type = spaceTypeChoice.getValue();
        if (type != null && !"Todos".equals(type)) {
            filtered = filtered.stream()
                    .filter(s -> type.equalsIgnoreCase(s.type()))
                    .collect(Collectors.toList());
        }

        String capacity = capacityChoice.getValue();
        if (capacity != null && !"Todas".equals(capacity)) {
            filtered = filtered.stream()
                    .filter(s -> matchesCapacity(s.capacity(), capacity))
                    .collect(Collectors.toList());
        }

        spacesList.clear();
        spacesList.addAll(filtered);
        displaySpaces(spacesList);
        updateSpacesCount();
    }

    private boolean matchesCapacity(int capacity, String range) {
        return switch (range) {
            case "< 50" -> capacity < 50;
            case "50-100" -> capacity >= 50 && capacity <= 100;
            case "100-200" -> capacity > 100 && capacity <= 200;
            case "> 200" -> capacity > 200;
            default -> true;
        };
    }

    // ==================== SPACES DISPLAY ====================

    private void displaySpaces(List<SpaceDTO> spaces) {
        if (spacesFlowPane == null) {
            return;
        }

        spacesFlowPane.getChildren().clear();

        for (SpaceDTO space : spaces) {
            VBox card = createSpaceCard(space);
            spacesFlowPane.getChildren().add(card);
        }
    }

    private VBox createSpaceCard(SpaceDTO space) {
        VBox card = new VBox(12);
        card.getStyleClass().add("space-card");
        card.setPadding(new Insets(20));
        card.setPrefWidth(280);

        Label nameLabel = new Label(space.name());
        nameLabel.getStyleClass().add("space-card-title");

        HBox typeBox = new HBox(8);
        typeBox.setAlignment(Pos.CENTER_LEFT);
        Label typeIcon = new Label(getSpaceIcon(space.type()));
        typeIcon.getStyleClass().add("space-icon");
        Label typeLabel = new Label(space.type());
        typeLabel.getStyleClass().add("space-type");
        typeBox.getChildren().addAll(typeIcon, typeLabel);

        Label capacityLabel = new Label("👥 Capacidad: " + space.capacity() + " personas");
        capacityLabel.getStyleClass().add("space-detail");

        String status = space.available() ? "✓ Disponible" : "✖ No disponible";
        Label availabilityLabel = new Label(status);
        availabilityLabel.getStyleClass().add(space.available() ? "space-available" : "space-unavailable");

        Button reserveBtn = new Button("📅 Reservar espacio");
        reserveBtn.getStyleClass().add("primary-button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setDisable(!space.available());
        reserveBtn.setOnAction(e -> handleReserveSpace(space));

        card.getChildren().addAll(nameLabel, typeBox, capacityLabel, availabilityLabel, reserveBtn);
        return card;
    }

    private String getSpaceIcon(String type) {
        return switch (type.toLowerCase()) {
            case "auditorio" -> "🎭";
            case "sala" -> "🏛️";
            case "cancha" -> "⚽";
            case "parque" -> "🌳";
            case "gimnasio" -> "💪";
            default -> "🏢";
        };
    }

    // ==================== RESERVATION ACTIONS ====================

    @FXML
    private void handleNewReservation() {
        navigateToSection(Section.SPACES);
    }

    private void handleReserveSpace(SpaceDTO space) {
        Dialog<ReservationData> dialog = new Dialog<>();
        dialog.setTitle("Nueva Reserva");
        dialog.setHeaderText("Reservar: " + space.name());

        ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        Spinner<Integer> startHour = new Spinner<>(8, 20, 9);
        startHour.setEditable(true);
        Spinner<Integer> startMinute = new Spinner<>(0, 59, 0, 15);
        startMinute.setEditable(true);
        
        Spinner<Integer> endHour = new Spinner<>(8, 20, 11);
        endHour.setEditable(true);
        Spinner<Integer> endMinute = new Spinner<>(0, 59, 0, 15);
        endMinute.setEditable(true);

        TextField notesField = new TextField();
        notesField.setPromptText("Descripción del evento...");

        grid.add(new Label("Fecha:"), 0, 0);
        grid.add(datePicker, 1, 0);
        
        grid.add(new Label("Hora inicio:"), 0, 1);
        HBox startTimeBox = new HBox(5, startHour, new Label(":"), startMinute);
        grid.add(startTimeBox, 1, 1);
        
        grid.add(new Label("Hora fin:"), 0, 2);
        HBox endTimeBox = new HBox(5, endHour, new Label(":"), endMinute);
        grid.add(endTimeBox, 1, 2);
        
        grid.add(new Label("Notas:"), 0, 3);
        grid.add(notesField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                LocalTime start = LocalTime.of(startHour.getValue(), startMinute.getValue());
                LocalTime end = LocalTime.of(endHour.getValue(), endMinute.getValue());
                return new ReservationData(datePicker.getValue(), start, end, notesField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (data.start().isAfter(data.end()) || data.start().equals(data.end())) {
                showError("La hora de inicio debe ser antes de la hora de fin");
                return;
            }
            createReservation(space, data);
        });
    }

    private void createReservation(SpaceDTO space, ReservationData data) {
        if (sessionManager == null) {
            showError("No hay sesión activa");
            return;
        }

        Long userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();

        if (userId == null || token == null) {
            showError("Sesión inválida");
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(data.date(), data.startTime());
        LocalDateTime endDateTime = LocalDateTime.of(data.date(), data.endTime());

        Task<ReservationDTO> task = new Task<>() {
            @Override
            protected ReservationDTO call() throws Exception {
                ReservationDTO newReservation = new ReservationDTO(
                    null,
                    userId,
                    space.id(),
                    startDateTime,
                    endDateTime,
                    "PENDING",
                    null,
                    data.notes(),
                    null,
                    null,
                    null
                );
                
                return reservationController.createReservation(newReservation, token);
            }
        };

        task.setOnSucceeded(e -> {
            ReservationDTO created = task.getValue();
            if (created != null) {
                reservationsList.add(created);
            }
            showSuccess("Reserva creada exitosamente para: " + space.name());
            loadInitialData(false);
            navigateToSection(Section.MY_RESERVATIONS);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Error al crear la reserva: " + (ex != null ? ex.getMessage() : "Error desconocido"));
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    private void handleViewReservation(ReservationDTO res) {
        SpaceDTO space = spacesList.stream()
                .filter(s -> s.id() != null && s.id().equals(res.spaceId()))
                .findFirst().orElse(null);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de Reserva #" + res.id());
        alert.setHeaderText("Información completa de la reserva");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder msg = new StringBuilder();
        msg.append("Espacio: ").append(space != null ? space.name() : "N/A").append("\n");
        msg.append("Fecha: ").append(res.startTime().format(dateFormatter)).append("\n");
        msg.append("Hora: ").append(res.startTime().format(timeFormatter))
           .append(" - ").append(res.endTime().format(timeFormatter)).append("\n");
        msg.append("Notas: ").append(res.notes() != null ? res.notes() : "N/A").append("\n");
        msg.append("Estado: ").append(STATUS_MAP.getOrDefault(res.status(), res.status()));

        alert.setContentText(msg.toString());
        alert.showAndWait();
    }

    private void handleCancelReservationConfirm(ReservationDTO res) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Reserva");
        confirm.setHeaderText("¿Seguro que deseas cancelar esta reserva?");
        confirm.setContentText("Esta acción no se puede deshacer.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cancelReservation(res);
            }
        });
    }

    private void cancelReservation(ReservationDTO reservation) {
        if (sessionManager == null) {
            showError("No hay sesión activa");
            return;
        }

        String token = sessionManager.getAccessToken();
        if (token == null) {
            showError("Token inválido");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                reservationController.cancelReservation(reservation.id(), token);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            reservationsList.remove(reservation);
            showSuccess("Reserva cancelada exitosamente");
            loadInitialData(false);
            updateDashboardMetrics();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            showError("Error al cancelar: " + (ex != null ? ex.getMessage() : "Error desconocido"));
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    // ==================== METRICS ====================

    private void updateDashboardMetrics() {
        LocalDateTime now = LocalDateTime.now();

        long active = reservationsList.stream()
                .filter(r -> "CONFIRMED".equals(r.status()) && r.startTime().isAfter(now))
                .count();

        long noShows = reservationsList.stream()
                .filter(r -> "NO_SHOW".equals(r.status()))
                .count();

        long completed = reservationsList.stream()
                .filter(r -> "COMPLETED".equals(r.status()))
                .count();

        activeReservationsLabel.setText(String.valueOf(active));
        noShowsLabel.setText(String.valueOf(noShows));
        completedReservationsLabel.setText(String.valueOf(completed));
    }

    private void updateReservationsCount() {
        if (reservationsCountLabel != null) {
            reservationsCountLabel.setText("Reservas (" + reservationsList.size() + ")");
        }
    }

    private void updateSpacesCount() {
        if (spacesCountLabel != null) {
            spacesCountLabel.setText("Espacios disponibles (" + spacesList.size() + ")");
        }
    }

    // ==================== REPORTS ====================

    private void updateReportsData() {
        totalReservationsReportLabel.setText(String.valueOf(reservationsList.size()));

        long completed = reservationsList.stream().filter(r -> "COMPLETED".equals(r.status())).count();
        long noShows = reservationsList.stream().filter(r -> "NO_SHOW".equals(r.status())).count();
        long total = completed + noShows;
        double rate = total > 0 ? (completed * 100.0 / total) : 0;
        attendanceRateLabel.setText(String.format("%.1f%%", rate));

        Map<Long, Long> spaceFreq = reservationsList.stream()
                .collect(Collectors.groupingBy(ReservationDTO::spaceId, Collectors.counting()));

        if (!spaceFreq.isEmpty()) {
            Long favSpaceId = Collections.max(spaceFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
            SpaceDTO favSpace = spacesList.stream().filter(s -> s.id().equals(favSpaceId)).findFirst().orElse(null);
            favoriteSpaceLabel.setText(favSpace != null ? favSpace.name() : "N/A");
        } else {
            favoriteSpaceLabel.setText("N/A");
        }

        updateSpacesDistributionChart(spaceFreq);
        updateAttendanceChart(completed, noShows);
        attendancePercentageLabel.setText(String.format("%.0f%%", rate));
    }

    private void updateSpacesDistributionChart(Map<Long, Long> spaceFreq) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        spaceFreq.forEach((spaceId, count) -> {
            SpaceDTO space = spacesList.stream().filter(s -> s.id().equals(spaceId)).findFirst().orElse(null);
            String name = space != null ? space.name() : "Espacio #" + spaceId;
            pieData.add(new PieChart.Data(name, count));
        });

        spacesDistributionChart.setData(pieData);
    }

    private void updateAttendanceChart(long completed, long noShows) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
            new PieChart.Data("Asistidas", completed),
            new PieChart.Data("Inasistencias", noShows)
        );
        attendanceChart.setData(pieData);
    }

    // ==================== UTILITIES ====================

    private void showError(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ==================== DATA CLASSES ====================

    private record ReservationData(LocalDate date, LocalTime startTime, LocalTime endTime, String notes) {}
}