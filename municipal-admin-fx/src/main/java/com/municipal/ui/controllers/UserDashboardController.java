package com.municipal.ui.controllers;

import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.controllers.WeatherController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowAware;
import com.municipal.ui.navigation.FlowController;
import com.municipal.ui.navigation.SessionAware;
import com.municipal.ui.navigation.ViewLifecycle;
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

/**
 * Controlador del Dashboard de Usuario (Ciudadano)
 * Implementa SessionAware, FlowAware y ViewLifecycle para recibir inyección automática de dependencias
 */
public class UserDashboardController implements SessionAware, FlowAware, ViewLifecycle {

    // ==================== DEPENDENCIES ====================
    
    private SessionManager sessionManager;
    private FlowController flowController;
    private ReservationController reservationController;
    private SpaceController spaceController;
    private WeatherController weatherController;

    // ==================== FXML - HEADER ====================
    
    @FXML private VBox mainContainer;
    @FXML private HBox userMenuContainer;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // ==================== FXML - SIDEBAR NAVIGATION ====================
    
    @FXML private HBox navDashboardButton;
    @FXML private HBox navSpacesButton;
    @FXML private HBox navMyReservationsButton;
    @FXML private HBox navMyQRCodesButton;
    @FXML private HBox navReportsButton;

    // ==================== FXML - CONTENT SCROLL ====================
    
    @FXML private ScrollPane contentScroll;

    // ==================== FXML - DASHBOARD SECTION ====================
    
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

    // ==================== FXML - SPACES SECTION ====================
    
    @FXML private VBox spacesSection;
    @FXML private Label spacesCountLabel;
    @FXML private TextField searchSpaceField;
    @FXML private ChoiceBox<String> spaceTypeChoice;
    @FXML private ChoiceBox<String> capacityChoice;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane spacesFlowPane;
    @FXML private StackPane spacesLoadingOverlay;

    // ==================== FXML - MY RESERVATIONS SECTION ====================
    
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

    // ==================== FXML - MY QR CODES SECTION ====================
    
    @FXML private VBox myQRCodesSection;
    @FXML private FlowPane qrCodesFlowPane;
    @FXML private Label qrCodesCountLabel;

    // ==================== FXML - REPORTS SECTION ====================
    
    @FXML private VBox reportsSection;
    @FXML private Label totalReservationsReportLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label favoriteSpaceLabel;
    @FXML private PieChart spacesDistributionChart;
    @FXML private PieChart attendanceChart;
    @FXML private Label attendancePercentageLabel;

    // ==================== DATA ====================
    
    private final ObservableList<ReservationDTO> reservationsList = FXCollections.observableArrayList();
    private final ObservableList<SpaceDTO> spacesList = FXCollections.observableArrayList();
    private final List<SpaceDTO> allSpaces = new ArrayList<>();

    // ==================== NAVIGATION ====================
    
    private enum Section {
        DASHBOARD, SPACES, MY_RESERVATIONS, MY_QR_CODES, REPORTS
    }

    private Section currentSection = Section.DASHBOARD;

    // ==================== STATUS MAPPING ====================
    
    private static final Map<String, String> STATUS_MAP = Map.of(
        "PENDING", "Pendiente",
        "CONFIRMED", "Confirmada",
        "CANCELLED", "Cancelada",
        "COMPLETED", "Completada",
        "NO_SHOW", "Inasistencia"
    );

    // ==================== INITIALIZATION ====================

    @FXML
    public void initialize() {
        System.out.println("🔄 UserDashboardController - initialize() llamado");
        
        // ✅ Inicializar controladores usando constructores sin parámetros
        this.reservationController = new ReservationController();
        this.spaceController = new SpaceController();
        this.weatherController = new WeatherController();
        
        // Configurar UI
        setupUserMenu();
        setupNavigationHandlers();
        setupFilters();
        setupTableColumns();
        
        System.out.println("✅ UserDashboardController - initialize() completado");
    }

    // ==================== INTERFACE IMPLEMENTATIONS ====================

    /**
     * Implementación de SessionAware - Inyección automática del SessionManager
     */
    @Override
    public void setSessionManager(SessionManager sessionManager) {
        System.out.println("🔐 SessionManager inyectado en UserDashboardController");
        this.sessionManager = sessionManager;
        
        if (sessionManager != null && sessionManager.getUserId() != null) {
            updateUserInfo();
        } else {
            System.err.println("⚠️ SessionManager es null o no tiene usuario");
        }
    }

    /**
     * Implementación de FlowAware - Inyección automática del FlowController
     */
    @Override
    public void setFlowController(FlowController flowController) {
        System.out.println("🔀 FlowController inyectado en UserDashboardController");
        this.flowController = flowController;
    }

    /**
     * Implementación de ViewLifecycle - Se llama cuando la vista se activa
     */
    @Override
    public void onViewActivated() {
        System.out.println("👁️ Vista UserDashboard activada - Cargando datos iniciales");
        loadInitialData(true);
    }

    /**
     * Actualiza la información del usuario en la UI
     */
    private void updateUserInfo() {
        if (sessionManager != null) {
            Platform.runLater(() -> {
                if (userNameLabel != null) {
                    String displayName = sessionManager.getUserDisplayName();
                    userNameLabel.setText(displayName != null ? displayName : "Usuario");
                }
                if (userRoleLabel != null) {
                    userRoleLabel.setText("Ciudadano");
                }
            });
        }
    }

    /**
     * Carga los datos iniciales del dashboard
     */
    public void loadInitialData(boolean includeWeather) {
        System.out.println("📊 Cargando datos iniciales del dashboard...");
        
        if (sessionManager == null) {
            System.err.println("❌ No se puede cargar datos: SessionManager es null");
            showError("Error: No hay sesión activa");
            return;
        }
        
        loadReservations();
        loadSpaces();
        
        if (includeWeather) {
            loadWeather();
        }
        
        updateDashboardMetrics();
    }

    // ==================== USER MENU ====================

    private void setupUserMenu() {
        if (userMenuContainer != null) {
            userMenuContainer.setOnMouseClicked(this::showUserMenu);
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
        if (sessionManager == null) {
            showError("No hay información de usuario disponible");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mi Perfil");
        alert.setHeaderText("Información del Usuario");
        
        String displayName = sessionManager.getUserDisplayName();
        String info = String.format("Nombre: %s\nRol: Ciudadano", 
            displayName != null ? displayName : "N/A");
        alert.setContentText(info);
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
                performLogout();
            }
        });
    }

    private void performLogout() {
        try {
            // Limpiar sesión usando el método correcto
            if (sessionManager != null) {
                sessionManager.clear();
            }

            // Navegar al login usando FlowController inyectado
            if (flowController != null) {
                flowController.showView("login");
            } else {
                showError("Error: No se pudo cerrar sesión correctamente");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error al cerrar sesión: " + e.getMessage());
        }
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
    private void handleNavigateMyQRCodes() {
        navigateToSection(Section.MY_QR_CODES);
    }

    @FXML
    private void handleNavigateReports() {
        navigateToSection(Section.REPORTS);
    }

    private void navigateToSection(Section section) {
        currentSection = section;

        updateSectionVisibility(section);
        updateNavigationStyles(section);
        loadSectionData(section);

        if (contentScroll != null) {
            contentScroll.setVvalue(0);
        }
    }

    private void updateSectionVisibility(Section section) {
        if (dashboardSection != null) {
            dashboardSection.setVisible(section == Section.DASHBOARD);
            dashboardSection.setManaged(section == Section.DASHBOARD);
        }

        if (spacesSection != null) {
            spacesSection.setVisible(section == Section.SPACES);
            spacesSection.setManaged(section == Section.SPACES);
        }

        if (myReservationsSection != null) {
            myReservationsSection.setVisible(section == Section.MY_RESERVATIONS);
            myReservationsSection.setManaged(section == Section.MY_RESERVATIONS);
        }

        if (myQRCodesSection != null) {
            myQRCodesSection.setVisible(section == Section.MY_QR_CODES);
            myQRCodesSection.setManaged(section == Section.MY_QR_CODES);
        }

        if (reportsSection != null) {
            reportsSection.setVisible(section == Section.REPORTS);
            reportsSection.setManaged(section == Section.REPORTS);
        }
    }

    private void updateNavigationStyles(Section section) {
        setNavigationStyle(navDashboardButton, section == Section.DASHBOARD);
        setNavigationStyle(navSpacesButton, section == Section.SPACES);
        setNavigationStyle(navMyReservationsButton, section == Section.MY_RESERVATIONS);
        setNavigationStyle(navMyQRCodesButton, section == Section.MY_QR_CODES);
        setNavigationStyle(navReportsButton, section == Section.REPORTS);
    }

    private void loadSectionData(Section section) {
        switch (section) {
            case SPACES -> loadSpaces();
            case MY_RESERVATIONS -> loadReservations();
            case MY_QR_CODES -> loadQRCodes();
            case REPORTS -> updateReportsData();
            case DASHBOARD -> updateDashboardMetrics();
        }
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
            System.err.println("❌ No se puede cargar reservas: dependencias null");
            return;
        }

        // ✅ Obtener userId del SessionManager correctamente
        Long userId = sessionManager.getUserId();
        String token = sessionManager.getAccessToken();

        if (userId == null || token == null) {
            System.err.println("❌ No se puede cargar reservas: userId o token null");
            return;
        }

        showLoadingOverlay(reservationsLoadingOverlay, true);

        Task<List<ReservationDTO>> task = new Task<>() {
            @Override
            protected List<ReservationDTO> call() throws Exception {
                // ✅ Usar el método correcto del ReservationController
                return reservationController.getReservationsByUserId(userId, token);
            }
        };

        task.setOnSucceeded(e -> {
            List<ReservationDTO> data = task.getValue();
            reservationsList.clear();
            if (data != null) {
                reservationsList.addAll(data);
                System.out.println("✅ Cargadas " + data.size() + " reservas");
            }
            reservationsTable.setItems(reservationsList);
            updateReservationsCount();
            updateDashboardMetrics();
            showLoadingOverlay(reservationsLoadingOverlay, false);
        });

        task.setOnFailed(e -> {
            String errorMsg = task.getException() != null ? 
                task.getException().getMessage() : "Error desconocido";
            System.err.println("❌ Error al cargar reservas: " + errorMsg);
            showError("Error al cargar reservas: " + errorMsg);
            showLoadingOverlay(reservationsLoadingOverlay, false);
        });

        new Thread(task).start();
    }

    private void loadSpaces() {
        if (spaceController == null) {
            System.err.println("❌ No se puede cargar espacios: spaceController null");
            return;
        }

        String token = sessionManager != null ? sessionManager.getAccessToken() : null;
        showLoadingOverlay(spacesLoadingOverlay, true);

        Task<List<SpaceDTO>> task = new Task<>() {
            @Override
            protected List<SpaceDTO> call() throws Exception {
                // ✅ Mostrar todos los espacios activos del sistema
                // El usuario puede ver todos los espacios y cuando intente reservar
                // se verificará la disponibilidad para la fecha/hora específica
                return spaceController.loadSpaces(token);
            }
        };

        task.setOnSucceeded(e -> {
            List<SpaceDTO> data = task.getValue();
            allSpaces.clear();
            spacesList.clear();
            if (data != null) {
                allSpaces.addAll(data);
                spacesList.addAll(data);
                System.out.println("✅ Cargados " + data.size() + " espacios");
            }
            displaySpaces(spacesList);
            updateSpacesCount();
            showLoadingOverlay(spacesLoadingOverlay, false);
        });

        task.setOnFailed(e -> {
            String errorMsg = task.getException() != null ? 
                task.getException().getMessage() : "Error desconocido";
            System.err.println("❌ Error al cargar espacios: " + errorMsg);
            showError("Error al cargar espacios: " + errorMsg);
            showLoadingOverlay(spacesLoadingOverlay, false);
        });

        new Thread(task).start();
    }

    private void loadWeather() {
        if (weatherController == null) {
            System.err.println("❌ No se puede cargar clima: weatherController null");
            return;
        }

        Task<CurrentWeatherDTO> task = new Task<>() {
            @Override
            protected CurrentWeatherDTO call() throws Exception {
                // ✅ Usar coordenadas configuradas en application.properties
                // Pérez Zeledón, Costa Rica: 9.3640, -83.7139
                String token = sessionManager != null ? sessionManager.getAccessToken() : null;
                return weatherController.loadCurrentWeather(9.3640, -83.7139, token);
            }
        };

        task.setOnSucceeded(e -> {
            CurrentWeatherDTO weather = task.getValue();
            if (weather != null) {
                updateWeatherUI(weather);
                System.out.println("✅ Clima cargado correctamente");
            }
        });

        task.setOnFailed(e -> {
            System.err.println("❌ Error al cargar clima: " + 
                (task.getException() != null ? task.getException().getMessage() : "Error desconocido"));
            if (weatherMessageLabel != null) {
                weatherMessageLabel.setText("No se pudo cargar la información del clima");
            }
        });

        new Thread(task).start();
    }

    private void showLoadingOverlay(StackPane overlay, boolean show) {
        if (overlay != null) {
            Platform.runLater(() -> {
                overlay.setVisible(show);
                overlay.setManaged(show);
            });
        }
    }

    private void updateWeatherUI(CurrentWeatherDTO weather) {
        Platform.runLater(() -> {
            // ✅ Usar los campos correctos de CurrentWeatherDTO
            if (weatherTempLabel != null) {
                weatherTempLabel.setText(String.format("%.1f°C", weather.temperature()));
            }
            if (weatherConditionLabel != null) {
                weatherConditionLabel.setText(capitalizeFirst(weather.description()));
            }
            if (weatherWindLabel != null) {
                weatherWindLabel.setText(String.format("Viento: %.1f km/h", weather.windSpeed()));
            }
            if (weatherHumidityLabel != null) {
                weatherHumidityLabel.setText(String.format("Humedad: %d%%", weather.humidity()));
            }
            if (weatherIconLabel != null) {
                String icon = getWeatherIcon(weather.description());
                weatherIconLabel.setText(icon);
            }
            if (weatherMessageLabel != null) {
                String message = getWeatherMessage(weather.description(), weather.temperature());
                weatherMessageLabel.setText(message);
            }
        });
    }

    private String getWeatherIcon(String condition) {
        if (condition == null) return "🌤️";
        
        return switch (condition.toLowerCase()) {
            case "clear", "despejado" -> "☀️";
            case "clouds", "nublado" -> "☁️";
            case "rain", "lluvia" -> "🌧️";
            case "drizzle", "llovizna" -> "🌦️";
            case "thunderstorm", "tormenta" -> "⛈️";
            case "snow", "nieve" -> "❄️";
            case "mist", "fog", "niebla" -> "🌫️";
            default -> "🌤️";
        };
    }

    private String getWeatherMessage(String condition, double temp) {
        if (condition == null) {
            return "✨ Información del clima no disponible.";
        }
        
        String lowerCondition = condition.toLowerCase();
        if (lowerCondition.contains("rain") || lowerCondition.contains("lluvia") || 
            lowerCondition.contains("thunderstorm") || lowerCondition.contains("tormenta")) {
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
            List<SpaceDTO> searchList = !spacesList.isEmpty() ? spacesList : allSpaces;
            SpaceDTO space = searchList.stream()
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
                "Todos", "SALA", "CANCHA", "AUDITORIO"
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

        if (searchSpaceField != null && !searchSpaceField.getText().isEmpty()) {
            String searchText = searchSpaceField.getText().toLowerCase();
            filtered = filtered.stream()
                    .filter(s -> s.name().toLowerCase().contains(searchText))
                    .collect(Collectors.toList());
        }

        if (spaceTypeChoice != null) {
            String type = spaceTypeChoice.getValue();
            if (type != null && !"Todos".equals(type)) {
                filtered = filtered.stream()
                        .filter(s -> type.equalsIgnoreCase(s.type()))
                        .collect(Collectors.toList());
            }
        }

        if (capacityChoice != null) {
            String capacity = capacityChoice.getValue();
            if (capacity != null && !"Todas".equals(capacity)) {
                filtered = filtered.stream()
                        .filter(s -> matchesCapacity(s.capacity(), capacity))
                        .collect(Collectors.toList());
            }
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

        Platform.runLater(() -> {
            spacesFlowPane.getChildren().clear();

            for (SpaceDTO space : spaces) {
                VBox card = createSpaceCard(space);
                spacesFlowPane.getChildren().add(card);
            }
        });
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

        // ✅ Usar active en lugar de available
        boolean isAvailable = space.active();
        String status = isAvailable ? "✓ Disponible" : "✖ No disponible";
        Label availabilityLabel = new Label(status);
        availabilityLabel.getStyleClass().add(isAvailable ? "space-available" : "space-unavailable");

        Button reserveBtn = new Button("📅 Reservar espacio");
        reserveBtn.getStyleClass().add("primary-button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setDisable(!isAvailable);
        reserveBtn.setOnAction(e -> handleReserveSpace(space));

        card.getChildren().addAll(nameLabel, typeBox, capacityLabel, availabilityLabel, reserveBtn);
        return card;
    }

    private String getSpaceIcon(String type) {
        if (type == null) return "🏢";
        
        return switch (type.toUpperCase()) {
            case "AUDITORIO" -> "🎭";
            case "SALA" -> "🏛️";
            case "CANCHA" -> "⚽";
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

        // ✅ Grid con mejor espaciado y estructura
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setPrefWidth(440);

        // ✅ DatePicker con estilo
        DatePicker reservationDatePicker = new DatePicker(LocalDate.now());
        reservationDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });
        reservationDatePicker.getStyleClass().add("form-field");
        reservationDatePicker.setPrefWidth(260);
        
        // ✅ Spinners de hora inicio con mejor configuración
        Spinner<Integer> startHour = new Spinner<>(8, 20, 9);
        startHour.setEditable(true);
        startHour.setPrefWidth(70);
        startHour.getStyleClass().add("form-field");
        
        Spinner<Integer> startMinute = new Spinner<>(0, 59, 0, 15);
        startMinute.setEditable(true);
        startMinute.setPrefWidth(70);
        startMinute.getStyleClass().add("form-field");
        
        // ✅ Spinners de hora fin con mejor configuración
        Spinner<Integer> endHour = new Spinner<>(8, 20, 11);
        endHour.setEditable(true);
        endHour.setPrefWidth(70);
        endHour.getStyleClass().add("form-field");
        
        Spinner<Integer> endMinute = new Spinner<>(0, 59, 0, 15);
        endMinute.setEditable(true);
        endMinute.setPrefWidth(70);
        endMinute.getStyleClass().add("form-field");

        // ✅ TextField con estilo
        TextField notesField = new TextField();
        notesField.setPromptText("Descripción del evento...");
        notesField.getStyleClass().add("form-field");
        notesField.setPrefWidth(260);

        // ✅ Labels con estilo
        Label lblFecha = new Label("Fecha:");
        lblFecha.getStyleClass().add("form-label");
        Label lblHoraInicio = new Label("Hora inicio:");
        lblHoraInicio.getStyleClass().add("form-label");
        Label lblHoraFin = new Label("Hora fin:");
        lblHoraFin.getStyleClass().add("form-label");
        Label lblNotas = new Label("Notas:");
        lblNotas.getStyleClass().add("form-label");

        // ✅ HBox para horas con mejor alineación
        HBox startTimeBox = new HBox(8, startHour, new Label(":"), startMinute);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        HBox endTimeBox = new HBox(8, endHour, new Label(":"), endMinute);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(lblFecha, 0, 0);
        grid.add(reservationDatePicker, 1, 0);
        
        grid.add(lblHoraInicio, 0, 1);
        grid.add(startTimeBox, 1, 1);
        
        grid.add(lblHoraFin, 0, 2);
        grid.add(endTimeBox, 1, 2);
        
        grid.add(lblNotas, 0, 3);
        grid.add(notesField, 1, 3);

        // ✅ Configurar crecimiento horizontal
        GridPane.setHgrow(reservationDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(notesField, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);

        // ✅ Aplicar estilos a los botones del diálogo
        Button okButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        if (okButton != null) {
            okButton.getStyleClass().add("dialog-primary-button");
        }
        
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.setText("Cancelar");
            cancelButton.getStyleClass().add("dialog-cancel-button");
        }

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                LocalTime start = LocalTime.of(startHour.getValue(), startMinute.getValue());
                LocalTime end = LocalTime.of(endHour.getValue(), endMinute.getValue());
                return new ReservationData(reservationDatePicker.getValue(), start, end, notesField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(data -> {
            if (validateReservationData(data, space)) {
                createReservation(space, data);
            }
        });
    }

    private boolean validateReservationData(ReservationData data, SpaceDTO space) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = LocalDateTime.of(data.date(), data.startTime());
        LocalDateTime endDateTime = LocalDateTime.of(data.date(), data.endTime());

        if (startDateTime.isBefore(now)) {
            showError("No puedes reservar en una fecha/hora pasada");
            return false;
        }

        if (data.startTime().isAfter(data.endTime()) || data.startTime().equals(data.endTime())) {
            showError("La hora de inicio debe ser antes de la hora de fin");
            return false;
        }

        long minutes = java.time.Duration.between(startDateTime, endDateTime).toMinutes();
        if (minutes < 30) {
            showError("La reserva debe ser de al menos 30 minutos");
            return false;
        }

        // ✅ Validar duración máxima permitida por el espacio
        if (space.maxReservationDuration() != null && minutes > space.maxReservationDuration()) {
            showError(String.format(
                "⚠️ Duración Máxima Excedida\n\n" +
                "Este espacio permite reservas de máximo %d minutos (%.1f horas).\n" +
                "La duración solicitada es de %d minutos (%.1f horas).\n\n" +
                "Para usar este espacio por más tiempo, debe crear múltiples reservas consecutivas.",
                space.maxReservationDuration(),
                space.maxReservationDuration() / 60.0,
                minutes,
                minutes / 60.0
            ));
            return false;
        }

        return true;
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
                // ✅ Generar QR único y seguro para esta reserva
                // Formato: RES-{userId}-{spaceId}-{timestamp}-{random}
                String uniqueQR = String.format("RES-%d-%d-%d-%s",
                    userId,
                    space.id(),
                    System.currentTimeMillis(),
                    java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                );
                
                // ✅ Crear ReservationDTO con QR único
                ReservationDTO newReservation = new ReservationDTO(
                    null,              // id
                    userId,            // userId
                    space.id(),        // spaceId
                    startDateTime,     // startTime
                    endDateTime,       // endTime
                    "PENDING",         // status (será convertido a enum en backend)
                    uniqueQR,          // qrCode (único para cada reserva)
                    null,              // canceledAt
                    null,              // checkinAt
                    data.notes(),      // notes
                    1,                 // attendees (mínimo 1, requerido por backend)
                    null,              // approvedByUserId
                    null,              // weatherCheck
                    null,              // cancellationReason
                    null,              // createdAt
                    null,              // updatedAt
                    null,              // ratingId
                    new java.util.ArrayList<>(),  // notificationIds (lista vacía, no null)
                    new java.util.ArrayList<>()   // attendeeRecords (lista vacía, no null)
                );
                
                // ✅ Usar el método correcto del ReservationController
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
            String errorMsg = ex != null ? ex.getMessage() : "Error desconocido";
            
            // ✅ Mejorar mensaje si es error de duración
            if (errorMsg.contains("duration exceeds") || errorMsg.contains("Reservation duration")) {
                showError(String.format(
                    "⚠️ Duración Máxima Excedida\n\n" +
                    "Este espacio permite reservas de máximo %d minutos (%.1f horas).\n\n" +
                    "Para usar este espacio por más tiempo, debe crear múltiples reservas consecutivas.",
                    space.maxReservationDuration() != null ? space.maxReservationDuration() : 0,
                    space.maxReservationDuration() != null ? space.maxReservationDuration() / 60.0 : 0
                ));
            } else {
                showError("Error al crear la reserva: " + errorMsg);
            }
            
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    private void handleViewReservation(ReservationDTO res) {
        List<SpaceDTO> searchList = !spacesList.isEmpty() ? spacesList : allSpaces;
        SpaceDTO space = searchList.stream()
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
                // ✅ Usar el método correcto del ReservationController
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
            String errorMsg = ex != null ? ex.getMessage() : "Error desconocido";
            showError("Error al cancelar: " + errorMsg);
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    // ==================== METRICS ====================

    private void updateDashboardMetrics() {
        Platform.runLater(() -> {
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

            if (activeReservationsLabel != null) {
                activeReservationsLabel.setText(String.valueOf(active));
            }
            if (noShowsLabel != null) {
                noShowsLabel.setText(String.valueOf(noShows));
            }
            if (completedReservationsLabel != null) {
                completedReservationsLabel.setText(String.valueOf(completed));
            }
        });
    }

    private void updateReservationsCount() {
        Platform.runLater(() -> {
            if (reservationsCountLabel != null) {
                reservationsCountLabel.setText("Reservas (" + reservationsList.size() + ")");
            }
        });
    }

    private void updateSpacesCount() {
        Platform.runLater(() -> {
            if (spacesCountLabel != null) {
                spacesCountLabel.setText("Espacios disponibles (" + spacesList.size() + ")");
            }
        });
    }

    // ==================== REPORTS ====================

    private void updateReportsData() {
        Platform.runLater(() -> {
            if (totalReservationsReportLabel != null) {
                totalReservationsReportLabel.setText(String.valueOf(reservationsList.size()));
            }

            long completed = reservationsList.stream()
                .filter(r -> "COMPLETED".equals(r.status())).count();
            long noShows = reservationsList.stream()
                .filter(r -> "NO_SHOW".equals(r.status())).count();
            long total = completed + noShows;
            double rate = total > 0 ? (completed * 100.0 / total) : 0;

            if (attendanceRateLabel != null) {
                attendanceRateLabel.setText(String.format("%.1f%%", rate));
            }

            Map<Long, Long> spaceFreq = reservationsList.stream()
                    .collect(Collectors.groupingBy(ReservationDTO::spaceId, Collectors.counting()));

            if (!spaceFreq.isEmpty()) {
                Long favSpaceId = Collections.max(spaceFreq.entrySet(), Map.Entry.comparingByValue()).getKey();
                List<SpaceDTO> searchList = !spacesList.isEmpty() ? spacesList : allSpaces;
                SpaceDTO favSpace = searchList.stream()
                    .filter(s -> s.id().equals(favSpaceId)).findFirst().orElse(null);
                if (favoriteSpaceLabel != null) {
                    favoriteSpaceLabel.setText(favSpace != null ? favSpace.name() : "N/A");
                }
            } else {
                if (favoriteSpaceLabel != null) {
                    favoriteSpaceLabel.setText("N/A");
                }
            }

            updateSpacesDistributionChart(spaceFreq);
            updateAttendanceChart(completed, noShows);
            
            if (attendancePercentageLabel != null) {
                attendancePercentageLabel.setText(String.format("%.0f%%", rate));
            }
        });
    }

    private void updateSpacesDistributionChart(Map<Long, Long> spaceFreq) {
        if (spacesDistributionChart == null) {
            return;
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        spaceFreq.forEach((spaceId, count) -> {
            List<SpaceDTO> searchList = !spacesList.isEmpty() ? spacesList : allSpaces;
            SpaceDTO space = searchList.stream()
                .filter(s -> s.id().equals(spaceId)).findFirst().orElse(null);
            String name = space != null ? space.name() : "Espacio #" + spaceId;
            pieData.add(new PieChart.Data(name, count));
        });

        spacesDistributionChart.setData(pieData);
    }

    private void updateAttendanceChart(long completed, long noShows) {
        if (attendanceChart == null) {
            return;
        }

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

    // ==================== QR CODES SECTION ====================

    private void loadQRCodes() {
        if (qrCodesFlowPane == null) {
            return;
        }

        qrCodesFlowPane.getChildren().clear();

        // Filtrar reservas activas (PENDING, CONFIRMED, CHECKED_IN)
        List<ReservationDTO> activeReservations = reservationsList.stream()
            .filter(r -> "PENDING".equals(r.status()) || 
                        "CONFIRMED".equals(r.status()) || 
                        "CHECKED_IN".equals(r.status()))
            .toList();

        if (qrCodesCountLabel != null) {
            qrCodesCountLabel.setText(String.valueOf(activeReservations.size()));
        }

        if (activeReservations.isEmpty()) {
            Label noQRLabel = new Label("No tienes reservas activas");
            noQRLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #6b7280; -fx-padding: 40px;");
            qrCodesFlowPane.getChildren().add(noQRLabel);
            return;
        }

        for (ReservationDTO reservation : activeReservations) {
            qrCodesFlowPane.getChildren().add(createQRCodeCard(reservation));
        }
    }

    private VBox createQRCodeCard(ReservationDTO reservation) {
        VBox card = new VBox(15);
        card.getStyleClass().add("qr-code-card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(300);
        card.setAlignment(javafx.geometry.Pos.CENTER);

        // Buscar información del espacio
        SpaceDTO space = allSpaces.stream()
            .filter(s -> s.id().equals(reservation.spaceId()))
            .findFirst().orElse(null);

        String spaceName = space != null ? space.name() : "Espacio #" + reservation.spaceId();

        // Título
        Label titleLabel = new Label(spaceName);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        // Fecha y hora
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String dateText = reservation.startTime().format(dateFormatter);
        String timeText = reservation.startTime().format(timeFormatter) + " - " + 
                         reservation.endTime().format(timeFormatter);

        Label dateLabel = new Label("📅 " + dateText);
        Label timeLabel = new Label("🕒 " + timeText);
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4b5563;");

        // Estado
        String statusText = STATUS_MAP.getOrDefault(reservation.status(), reservation.status());
        Label statusLabel = new Label("Estado: " + statusText);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #10b981; -fx-font-weight: bold;");

        // Generar imagen QR
        try {
            javafx.scene.image.WritableImage qrImage = 
                com.municipal.ui.utils.QRCodeGenerator.generate(reservation.qrCode(), 200);
            javafx.scene.image.ImageView qrImageView = new javafx.scene.image.ImageView(qrImage);
            qrImageView.setFitWidth(200);
            qrImageView.setFitHeight(200);
            qrImageView.setPreserveRatio(true);

            // Código QR texto (para referencia)
            Label qrTextLabel = new Label("ID: " + reservation.qrCode().substring(0, Math.min(20, reservation.qrCode().length())) + "...");
            qrTextLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-font-family: monospace;");

            // Mensaje de validación
            Label validationLabel = new Label("✓ Válido 30 min antes del inicio");
            validationLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6366f1; -fx-padding: 5 0 0 0;");

            card.getChildren().addAll(titleLabel, dateLabel, timeLabel, statusLabel, 
                                     qrImageView, qrTextLabel, validationLabel);

        } catch (Exception e) {
            Label errorLabel = new Label("Error generando QR");
            errorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef4444;");
            card.getChildren().addAll(titleLabel, dateLabel, timeLabel, errorLabel);
            e.printStackTrace();
        }

        return card;
    }

    // ==================== DATA CLASSES ====================

    private record ReservationData(
        LocalDate date, 
        LocalTime startTime, 
        LocalTime endTime, 
        String notes
    ) {}
}