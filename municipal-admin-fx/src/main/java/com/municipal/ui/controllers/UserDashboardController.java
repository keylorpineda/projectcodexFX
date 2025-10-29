package com.municipal.ui.controllers;

<<<<<<< HEAD
import com.municipal.config.AppConfig;
import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.controllers.WeatherController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.SessionAware;
=======
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.municipal.dtos.ReservationDTO;
import com.municipal.ui.utils.NodeVisibilityUtils;
import com.municipal.ui.utils.QRCodeGenerator;
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
<<<<<<< HEAD
import javafx.scene.input.MouseEvent;
=======
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447
import javafx.scene.layout.*;
import javafx.util.Duration;
import com.google.zxing.WriterException;

<<<<<<< HEAD
=======
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

<<<<<<< HEAD
/**
 * Controlador del Panel de Usuario
 * Gestiona reservas, espacios disponibles y reportes personales
 */
public class UserDashboardController implements SessionAware {
=======

public class UserDashboardController {
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447

    // ==================== CONTROLADORES ====================
    private final ReservationController reservationController = new ReservationController();
    private final SpaceController spaceController = new SpaceController();
    private final WeatherController weatherController = new WeatherController();

    private SessionManager sessionManager;

    // ==================== COMPONENTES FXML ====================
    @FXML private VBox mainContainer;
    @FXML private ScrollPane contentScroll;

    // Navegación
    @FXML private HBox navDashboardButton;
    @FXML private HBox navSpacesButton;
    @FXML private HBox navMyReservationsButton;
    @FXML private HBox navReportsButton;
    @FXML private HBox navLogoutButton;

    // Header
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // Secciones
    @FXML private VBox dashboardSection;
    @FXML private VBox spacesSection;
    @FXML private VBox myReservationsSection;
    @FXML private VBox reportsSection;

    // Dashboard
    @FXML private Label activeReservationsLabel;
    @FXML private Label noShowsLabel;
    @FXML private Label completedReservationsLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherConditionLabel;
    @FXML private Label weatherWindLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherMessageLabel;

    // Espacios
    @FXML private TextField searchSpaceField;
    @FXML private ChoiceBox<String> spaceTypeChoice;
    @FXML private ChoiceBox<String> capacityChoice;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane spacesFlowPane;
    @FXML private Label spacesCountLabel;
    @FXML private StackPane spacesLoadingOverlay;

    // Reservas
    @FXML private TableView<ReservationDTO> reservationsTable;
    @FXML private TableColumn<ReservationDTO, String> reservationSpaceColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationDateColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationTimeColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationEventColumn;
    @FXML private TableColumn<ReservationDTO, String> reservationStatusColumn;
    @FXML private TableColumn<ReservationDTO, Void> reservationActionsColumn;
    @FXML private Label reservationsCountLabel;
    @FXML private StackPane reservationsLoadingOverlay;

    // Reportes
    @FXML private Label totalReservationsReportLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label attendancePercentageLabel;
    @FXML private Label favoriteSpaceLabel;
    @FXML private PieChart spacesDistributionChart;
    @FXML private PieChart attendanceChart;

    // ==================== DATOS ====================
    private ObservableList<ReservationDTO> reservationsList = FXCollections.observableArrayList();
    private ObservableList<SpaceDTO> spacesList = FXCollections.observableArrayList();
    private ObservableList<SpaceDTO> filteredSpacesList = FXCollections.observableArrayList();
    private CurrentWeatherDTO currentWeather;
    
    private Timeline weatherTimeline;
    private Timeline dataTimeline;
    private boolean isLoadingData = false;
    private boolean initialDataLoaded = false;

    // ==================== CONSTANTES ====================
    private static final Duration WEATHER_REFRESH_INTERVAL = Duration.seconds(30);
    private static final Duration DATA_REFRESH_INTERVAL = Duration.minutes(2);
    
    // Tipos de espacios según backend
    private static final List<String> SPACE_TYPES = List.of("SALA", "CANCHA", "AUDITORIO");
    
    // Mapeo de estados de reserva
    private static final Map<String, String> STATUS_MAP = Map.of(
        "PENDING", "Pendiente",
        "CONFIRMED", "Confirmada",
        "CANCELED", "Cancelada",
        "CHECKED_IN", "En sitio",
        "COMPLETED", "Completada",
        "NO_SHOW", "Inasistencia"
    );
    
    private enum Section {
        DASHBOARD, SPACES, MY_RESERVATIONS, REPORTS
    }

    // ==================== INICIALIZACIÓN ====================

    @FXML
    public void initialize() {
        System.out.println("✅ Inicializando UserDashboardController...");
        setupDefaultValues();
        setupTableColumns();
        navigateToSection(Section.DASHBOARD);
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        System.out.println("✅ SessionManager configurado");
        loadUserInfo();
        loadInitialData(false);
        startWeatherUpdates();
        startDataUpdates();
    }

    private void setupDefaultValues() {
        // Tipos de espacio
        if (spaceTypeChoice != null) {
            ObservableList<String> types = FXCollections.observableArrayList();
            types.add("Todos");
            types.addAll(SPACE_TYPES);
            spaceTypeChoice.setItems(types);
            spaceTypeChoice.setValue("Todos");
            spaceTypeChoice.valueProperty().addListener((obs, old, val) -> filterSpaces());
        }
        
        // Capacidades
        if (capacityChoice != null) {
            capacityChoice.setItems(FXCollections.observableArrayList(
                "Todas", "1-50", "51-100", "100+"
            ));
            capacityChoice.setValue("Todas");
            capacityChoice.valueProperty().addListener((obs, old, val) -> filterSpaces());
        }

        // Búsqueda
        if (searchSpaceField != null) {
            searchSpaceField.textProperty().addListener((obs, old, val) -> filterSpaces());
        }
    }

    private void loadUserInfo() {
        if (sessionManager == null || userNameLabel == null) return;
        
        sessionManager.getAuthResponse().ifPresent(auth -> {
            String name = auth.name() != null && !auth.name().isEmpty() ? auth.name() : auth.email();
            userNameLabel.setText(name);
            
            if (userRoleLabel != null) {
                String role = auth.role() != null ? auth.role() : "USER";
                userRoleLabel.setText(role);
            }
        });
    }

    // ==================== CARGA DE DATOS ====================

    private void loadInitialData(boolean notifySuccess) {
        if (sessionManager == null) {
            showWarning("No hay sesión activa");
            return;
        }

        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            showWarning("Token de acceso no válido");
            return;
        }

        if (isLoadingData) return;

        isLoadingData = true;
        boolean showLoading = notifySuccess || !initialDataLoaded;

        Task<DataResult> task = new Task<>() {
            @Override
            protected DataResult call() {
                List<String> warnings = new ArrayList<>();
                
                // Cargar espacios
                List<SpaceDTO> spaces = loadSpacesFromApi(token, warnings);
                
                // Cargar reservas filtradas por usuario
                List<ReservationDTO> reservations = loadUserReservationsFromApi(token, warnings);

                // Cargar clima
                CurrentWeatherDTO weather = loadWeatherFromApi(token, warnings);

                return new DataResult(spaces, reservations, weather, warnings);
            }
        };

        if (showLoading) {
            task.setOnRunning(e -> showLoadingIndicator("Cargando datos..."));
        }

        task.setOnSucceeded(e -> {
            isLoadingData = false;
            DataResult result = task.getValue();
            initialDataLoaded = true;

            spacesList.setAll(result.spaces());
            reservationsList.setAll(result.reservations());
            currentWeather = result.weather();

            filterSpaces();
            updateDashboardMetrics();
            updateWeatherDisplay();

            if (showLoading) hideLoadingIndicator();
            if (notifySuccess) showSuccess("Datos actualizados");
            if (!result.warnings().isEmpty()) {
                showWarning(String.join("\n", result.warnings()));
            }
        });

        task.setOnFailed(e -> {
            isLoadingData = false;
            if (showLoading) hideLoadingIndicator();
            showError("Error al cargar datos: " + buildErrorMessage(task.getException()));
        });

        new Thread(task).start();
    }

    private List<SpaceDTO> loadSpacesFromApi(String token, List<String> warnings) {
        try {
            List<SpaceDTO> spaces = spaceController.loadSpaces(token);
            return spaces != null ? spaces : Collections.emptyList();
        } catch (Exception e) {
            warnings.add("Espacios: " + buildErrorMessage(e));
            return Collections.emptyList();
        }
    }

    private List<ReservationDTO> loadUserReservationsFromApi(String token, List<String> warnings) {
        try {
            List<ReservationDTO> all = reservationController.loadReservations(token);
            if (all == null) return Collections.emptyList();

            Long userId = sessionManager != null ? sessionManager.getUserId() : null;
            if (userId == null) {
                warnings.add("No se pudo obtener el userId");
                return Collections.emptyList();
            }

            return all.stream()
                    .filter(r -> userId.equals(r.userId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            warnings.add("Reservas: " + buildErrorMessage(e));
            return Collections.emptyList();
        }
    }

    private CurrentWeatherDTO loadWeatherFromApi(String token, List<String> warnings) {
        String latStr = AppConfig.get("weather.default-lat");
        String lonStr = AppConfig.get("weather.default-lon");

        if (latStr == null || lonStr == null) {
            warnings.add("Coordenadas climáticas no configuradas");
            return null;
        }

        try {
            double lat = Double.parseDouble(latStr);
            double lon = Double.parseDouble(lonStr);
            return weatherController.loadCurrentWeather(lat, lon, token);
        } catch (NumberFormatException e) {
            warnings.add("Coordenadas inválidas");
            return null;
        } catch (Exception e) {
            warnings.add("Clima: " + buildErrorMessage(e));
            return null;
        }
    }

    private String buildErrorMessage(Throwable error) {
        if (error instanceof ApiClientException apiError) {
            return "HTTP " + apiError.getStatusCode() + 
                   (apiError.getResponseBody() != null ? ": " + apiError.getResponseBody() : "");
        }
        return error != null && error.getMessage() != null ? error.getMessage() : "Error desconocido";
    }

    // ==================== ACTUALIZACIONES AUTOMÁTICAS ====================

    private void startWeatherUpdates() {
        if (weatherTimeline != null) weatherTimeline.stop();
        weatherTimeline = new Timeline(new KeyFrame(WEATHER_REFRESH_INTERVAL, e -> reloadWeather(false)));
        weatherTimeline.setCycleCount(Timeline.INDEFINITE);
        weatherTimeline.play();
    }

    private void startDataUpdates() {
        if (dataTimeline != null) dataTimeline.stop();
        dataTimeline = new Timeline(new KeyFrame(DATA_REFRESH_INTERVAL, e -> {
            if (!isLoadingData) loadInitialData(false);
        }));
        dataTimeline.setCycleCount(Timeline.INDEFINITE);
        dataTimeline.play();
    }

    private void stopUpdates() {
        if (weatherTimeline != null) { weatherTimeline.stop(); weatherTimeline = null; }
        if (dataTimeline != null) { dataTimeline.stop(); dataTimeline = null; }
        initialDataLoaded = false;
    }

    private void reloadWeather(boolean notify) {
        if (sessionManager == null) return;
        String token = sessionManager.getAccessToken();
        if (token == null) return;

        Task<WeatherResult> task = new Task<>() {
            @Override
            protected WeatherResult call() {
                List<String> warnings = new ArrayList<>();
                CurrentWeatherDTO weather = loadWeatherFromApi(token, warnings);
                return new WeatherResult(weather, warnings);
            }
        };

        task.setOnSucceeded(e -> {
            WeatherResult result = task.getValue();
            currentWeather = result.weather();
            updateWeatherDisplay();
            if (notify) showSuccess("Clima actualizado");
        });

        new Thread(task).start();
    }

    // ==================== CONFIGURACIÓN DE TABLA ====================

    private void setupTableColumns() {
        if (reservationsTable == null) return;
        
        reservationsTable.setItems(reservationsList);
        
        // Espacio
        if (reservationSpaceColumn != null) {
            reservationSpaceColumn.setCellValueFactory(data -> {
                Long spaceId = data.getValue().spaceId();
                SpaceDTO space = spacesList.stream()
                        .filter(s -> s.id() != null && s.id().equals(spaceId))
                        .findFirst().orElse(null);
                String name = space != null ? space.name() : "Espacio #" + spaceId;
                return new javafx.beans.property.SimpleStringProperty(name);
            });
        }
        
        // Fecha
        if (reservationDateColumn != null) {
            reservationDateColumn.setCellValueFactory(data -> {
                return new javafx.beans.property.SimpleStringProperty(
                    data.getValue().startTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            });
        }
        
        // Horario
        if (reservationTimeColumn != null) {
            reservationTimeColumn.setCellValueFactory(data -> {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
                String start = data.getValue().startTime().format(fmt);
                String end = data.getValue().endTime().format(fmt);
                return new javafx.beans.property.SimpleStringProperty(start + " - " + end);
            });
        }
        
        // Tipo de evento
        if (reservationEventColumn != null) {
            reservationEventColumn.setCellValueFactory(data -> {
                String notes = data.getValue().notes();
                return new javafx.beans.property.SimpleStringProperty(notes != null ? notes : "N/A");
            });
        }
        
        // Estado con badges
        if (reservationStatusColumn != null) {
            reservationStatusColumn.setCellValueFactory(data -> {
                String status = data.getValue().status();
                return new javafx.beans.property.SimpleStringProperty(STATUS_MAP.getOrDefault(status, status));
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
                        } else if ("Pendiente".equals(displayStatus)) {
                            badge.getStyleClass().add("status-pending");
                        }
                        
                        setGraphic(badge);
                        setAlignment(Pos.CENTER);
                    }
                }
            });
        }
        
        // Acciones
        if (reservationActionsColumn != null) {
            reservationActionsColumn.setCellFactory(col -> new TableCell<>() {
                private final Button viewBtn = new Button("👁");
                private final Button cancelBtn = new Button("✖");
                private final HBox box = new HBox(5, viewBtn, cancelBtn);
                
                {
                    viewBtn.getStyleClass().add("action-button");
                    cancelBtn.getStyleClass().add("action-button");
                    box.setAlignment(Pos.CENTER);
                    
                    viewBtn.setOnAction(e -> {
                        ReservationDTO res = getTableView().getItems().get(getIndex());
                        handleViewReservation(res);
                    });
                    
                    cancelBtn.setOnAction(e -> {
                        ReservationDTO res = getTableView().getItems().get(getIndex());
                        handleCancelReservation(res);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        ReservationDTO res = getTableView().getItems().get(getIndex());
                        String status = res.status();
                        
                        // Solo mostrar cancelar si está confirmada
                        if ("CONFIRMED".equals(status)) {
                            box.getChildren().setAll(viewBtn, cancelBtn);
                        } else {
                            box.getChildren().setAll(viewBtn);
                        }
                        setGraphic(box);
                    }
                }
            });
        }
    }

    // ==================== NAVEGACIÓN ====================

    private void navigateToSection(Section section) {
        // Ocultar todas
        if (dashboardSection != null) { dashboardSection.setVisible(false); dashboardSection.setManaged(false); }
        if (spacesSection != null) { spacesSection.setVisible(false); spacesSection.setManaged(false); }
        if (myReservationsSection != null) { myReservationsSection.setVisible(false); myReservationsSection.setManaged(false); }
        if (reportsSection != null) { reportsSection.setVisible(false); reportsSection.setManaged(false); }
        
        // Mostrar la seleccionada
        switch (section) {
            case DASHBOARD:
                if (dashboardSection != null) {
                    dashboardSection.setVisible(true);
                    dashboardSection.setManaged(true);
                    applyFadeTransition(dashboardSection);
                    updateDashboardMetrics();
                }
                break;
            case SPACES:
                if (spacesSection != null) {
                    spacesSection.setVisible(true);
                    spacesSection.setManaged(true);
                    applyFadeTransition(spacesSection);
                    filterSpaces();
                }
                break;
            case MY_RESERVATIONS:
                if (myReservationsSection != null) {
                    myReservationsSection.setVisible(true);
                    myReservationsSection.setManaged(true);
                    applyFadeTransition(myReservationsSection);
                }
                break;
            case REPORTS:
                if (reportsSection != null) {
                    reportsSection.setVisible(true);
                    reportsSection.setManaged(true);
                    applyFadeTransition(reportsSection);
                    generateReports();
                }
                break;
        }
        
        updateNavigationStyles(section);
    }

    private void updateNavigationStyles(Section active) {
        if (navDashboardButton != null) navDashboardButton.getStyleClass().remove("active");
        if (navSpacesButton != null) navSpacesButton.getStyleClass().remove("active");
        if (navMyReservationsButton != null) navMyReservationsButton.getStyleClass().remove("active");
        if (navReportsButton != null) navReportsButton.getStyleClass().remove("active");
        
        switch (active) {
            case DASHBOARD: if (navDashboardButton != null) navDashboardButton.getStyleClass().add("active"); break;
            case SPACES: if (navSpacesButton != null) navSpacesButton.getStyleClass().add("active"); break;
            case MY_RESERVATIONS: if (navMyReservationsButton != null) navMyReservationsButton.getStyleClass().add("active"); break;
            case REPORTS: if (navReportsButton != null) navReportsButton.getStyleClass().add("active"); break;
        }
    }

    private void applyFadeTransition(Node node) {
        if (node == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(300), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    @FXML
    private void handleNavigateDashboard(MouseEvent e) { navigateToSection(Section.DASHBOARD); }

    @FXML
    private void handleNavigateSpaces(MouseEvent e) { navigateToSection(Section.SPACES); }
    @FXML
    private void handleNavigateSpaces() { navigateToSection(Section.SPACES); }

    @FXML
    private void handleNavigateMyReservations(MouseEvent e) { navigateToSection(Section.MY_RESERVATIONS); }
    @FXML
    private void handleNavigateMyReservations() { navigateToSection(Section.MY_RESERVATIONS); }

    @FXML
    private void handleNavigateReports(MouseEvent e) { navigateToSection(Section.REPORTS); }
    @FXML
    private void handleNavigateReports() { navigateToSection(Section.REPORTS); }

    @FXML
    private void handleLogout(MouseEvent e) {
        stopUpdates();
        if (sessionManager != null) sessionManager.clear();
        showAlert("Logout", "Sesión cerrada", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleApplyFilters() { filterSpaces(); }

    @FXML
    private void handleNewReservation() {
        showAlert("Nueva Reserva", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
    }

<<<<<<< HEAD
    // ==================== FILTROS Y VISUALIZACIÓN ====================
=======
      @FXML
    private void handleGenerateTestQr() {
        LocalDateTime start = LocalDate.now().plusDays(1).atTime(10, 0);
        LocalDateTime end = start.plusHours(2);
        Long demoSpaceId = 9999L;
        String demoSpaceName = "Demostración - Espacio Municipal";

        String qrValue = buildQrPayload(demoSpaceId, start, end);

        try {
            WritableImage qrImage = QRCodeGenerator.generate(qrValue);
            showReservationQrDialog(demoSpaceName, start, end, qrValue, qrImage);
        } catch (WriterException e) {
            showAlert("Error", "No se pudo generar el QR de prueba: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    // API Methods
    
    private void loadDashboardData() {
        if (currentUserId == null) return;
        
        loadUserReservations();
        loadWeatherData();
    }
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447

    private void filterSpaces() {
        String search = searchSpaceField != null ? searchSpaceField.getText().toLowerCase() : "";
        String type = spaceTypeChoice != null ? spaceTypeChoice.getValue() : "Todos";
        String capacity = capacityChoice != null ? capacityChoice.getValue() : "Todas";

        filteredSpacesList.clear();
        filteredSpacesList.addAll(spacesList.stream()
            .filter(s -> {
                // Búsqueda
                if (!search.isEmpty()) {
                    String name = s.name() != null ? s.name().toLowerCase() : "";
                    String desc = s.description() != null ? s.description().toLowerCase() : "";
                    if (!name.contains(search) && !desc.contains(search)) return false;
                }
                
                // Tipo
                if (!"Todos".equals(type)) {
                    if (!type.equals(s.type())) return false;
                }
                
                // Capacidad
                if (!"Todas".equals(capacity)) {
                    int cap = s.capacity() != null ? s.capacity() : 0;
                    switch (capacity) {
                        case "1-50": if (cap < 1 || cap > 50) return false; break;
                        case "51-100": if (cap < 51 || cap > 100) return false; break;
                        case "100+": if (cap <= 100) return false; break;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList())
        );

        displaySpaces();
        
        if (spacesCountLabel != null) {
            spacesCountLabel.setText("Espacios disponibles (" + filteredSpacesList.size() + ")");
        }
    }

    private void displaySpaces() {
        if (spacesFlowPane == null) return;
        spacesFlowPane.getChildren().clear();
        
        for (SpaceDTO space : filteredSpacesList) {
            VBox card = createSpaceCard(space);
            spacesFlowPane.getChildren().add(card);
        }
    }

    private VBox createSpaceCard(SpaceDTO space) {
        VBox card = new VBox(10);
        card.getStyleClass().add("space-card");
        card.setPrefWidth(280);
        card.setPadding(new Insets(0));
        
        // Imagen
        StackPane imgContainer = new StackPane();
        imgContainer.getStyleClass().add("space-image-container");
        imgContainer.setPrefHeight(180);
        
        ImageView img = new ImageView();
        img.setFitWidth(280);
        img.setFitHeight(180);
        img.setPreserveRatio(false);
        imgContainer.getChildren().add(img);
        
        // Badge
        boolean active = space.active() != null && space.active();
        Label badge = new Label(active ? "Disponible" : "No disponible");
        badge.getStyleClass().addAll("space-status-badge", active ? "space-available" : "space-occupied");
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        StackPane.setMargin(badge, new Insets(10));
        imgContainer.getChildren().add(badge);
        
        // Contenido
        VBox content = new VBox(8);
        content.setPadding(new Insets(16));
        
        Label name = new Label(space.name() != null ? space.name() : "Sin nombre");
        name.getStyleClass().add("space-name");
        
        Label desc = new Label(space.description() != null ? space.description() : "");
        desc.getStyleClass().add("space-description");
        desc.setWrapText(true);
        desc.setMaxHeight(40);
        
        HBox info = new HBox(15);
        Label capLabel = new Label("👥 " + (space.capacity() != null ? space.capacity() : 0));
        Label typeLabel = new Label("📍 " + (space.type() != null ? space.type() : "N/A"));
        info.getChildren().addAll(capLabel, typeLabel);
        
        Button reserveBtn = new Button("Reservar espacio");
        reserveBtn.getStyleClass().add("primary-button");
        reserveBtn.setMaxWidth(Double.MAX_VALUE);
        reserveBtn.setOnAction(e -> handleReserveSpace(space));
        reserveBtn.setDisable(!active);
        
        content.getChildren().addAll(name, desc, info, reserveBtn);
        card.getChildren().addAll(imgContainer, content);
        
        return card;
    }

<<<<<<< HEAD
    private void handleReserveSpace(SpaceDTO space) {
        showAlert("Reservar", "Funcionalidad para: " + space.name(), Alert.AlertType.INFORMATION);
=======
    private void handleReserveSpace(Map<String, Object> space) {
    if (authToken == null || currentUserId == null) {
            showAlert("Iniciar sesión", "Debes iniciar sesión para crear una reserva", Alert.AlertType.WARNING);
            return;
        }

        Dialog<ReservationRequest> dialog = new Dialog<>();
        dialog.setTitle("Reservar " + Objects.toString(space.get("name"), "espacio"));
        dialog.setHeaderText("Completa los datos de la reserva y generaremos el código QR por ti");
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Generar QR", ButtonBar.ButtonData.OK_DONE),
                ButtonType.CANCEL);

        LocalDate defaultDate = LocalDate.now();
        DatePicker reservationDatePicker = new DatePicker(defaultDate);

        Spinner<Integer> hourSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(6, 22, 9));
        Spinner<Integer> minuteSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 45, 0, 15));
        Spinner<Integer> durationSpinner = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 8, 2));

        int maxCapacity = Math.max(1, parseInteger(space.get("capacity"), 100));
        Spinner<Integer> attendeesSpinner = new Spinner<>(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxCapacity, Math.min(10, maxCapacity)));

        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notas para el administrador del espacio");
        notesArea.setPrefRowCount(3);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10, 5, 0, 5));

        grid.add(new Label("Fecha"), 0, 0);
        grid.add(reservationDatePicker, 1, 0);

        Label colonLabel = new Label(":");
        colonLabel.setStyle("-fx-font-weight: bold;");
        HBox startTimeBox = new HBox(6, hourSpinner, colonLabel, minuteSpinner);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);

        grid.add(new Label("Hora de inicio"), 0, 1);
        grid.add(startTimeBox, 1, 1);

        grid.add(new Label("Duración (horas)"), 0, 2);
        grid.add(durationSpinner, 1, 2);

        grid.add(new Label("Asistentes"), 0, 3);
        grid.add(attendeesSpinner, 1, 3);

        Label capacityHint = new Label("Capacidad máxima: " + maxCapacity + " personas");
        capacityHint.getStyleClass().add("form-hint");
        grid.add(capacityHint, 1, 4);

        grid.add(new Label("Notas"), 0, 5);
        grid.add(notesArea, 1, 5);
        GridPane.setColumnSpan(notesArea, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.setResizable(true);

        dialog.setResultConverter(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            LocalDate date = reservationDatePicker.getValue();
            if (date == null) {
                showAlert("Datos incompletos", "Selecciona una fecha para la reserva", Alert.AlertType.WARNING);
                return null;
            }
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            LocalTime startTime = LocalTime.of(hour, minute);
            LocalDateTime start = LocalDateTime.of(date, startTime);
            LocalDateTime end = start.plusHours(durationSpinner.getValue());
            String notes = notesArea.getText() != null ? notesArea.getText().trim() : "";
            return new ReservationRequest(start, end, attendeesSpinner.getValue(), notes);
        });

        dialog.showAndWait().ifPresent(request -> submitReservation(space, request));
    }

    private void submitReservation(Map<String, Object> space, ReservationRequest request) {
        Long spaceId = parseLong(space.get("id"));
        if (spaceId == null) {
            showAlert("Error", "No se pudo identificar el espacio seleccionado", Alert.AlertType.ERROR);
            return;
        }

        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("userId", currentUserId);
        payload.put("spaceId", spaceId);
        payload.put("startTime", request.startTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("endTime", request.endTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("status", "CONFIRMED");
        payload.put("attendees", request.attendees());
        if (!request.notes().isBlank()) {
            payload.put("notes", request.notes());
        }

        String spaceName = Objects.toString(space.get("name"), "Espacio municipal");

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/reservations"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    ReservationDTO created = objectMapper.readValue(response.body(), ReservationDTO.class);
                    String qrValue = created.getQrCode();

                    if (qrValue == null || qrValue.isBlank()) {
                        Platform.runLater(() -> showAlert("Error",
                            "La reserva se creó pero no se recibió un código QR válido del servidor.",
                            Alert.AlertType.ERROR));
                        return;
                    }

                    try {
                        WritableImage qrImage = QRCodeGenerator.generate(qrValue);
                        Platform.runLater(() -> {
                            loadUserReservations();
                            showReservationQrDialog(spaceName, created.startTime(), created.endTime(), qrValue, qrImage);
                        });
                    } catch (WriterException e) {
                        Platform.runLater(() -> showAlert("Error",
                            "La reserva se creó pero no se pudo generar el código QR: " + e.getMessage(),
                            Alert.AlertType.ERROR));
                    }
                } else {
                    String errorMessage = extractErrorMessage(response.body());
                    Platform.runLater(() -> showAlert("Error", errorMessage, Alert.AlertType.ERROR));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Error", "No se pudo crear la reserva: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        });
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "No se pudo crear la reserva. Inténtalo nuevamente.";
        }
        try {
            Map<String, Object> parsed = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});
            Object message = parsed.getOrDefault("message", parsed.get("error"));
            return message != null ? message.toString() : "No se pudo crear la reserva. Inténtalo nuevamente.";
        } catch (IOException e) {
            return "No se pudo crear la reserva: " + responseBody;
        }
    }

    private String buildQrPayload(Long spaceId, LocalDateTime start, LocalDateTime end) {
        String token = UUID.randomUUID().toString();
        return "RES|" + spaceId + "|" + start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            + "|" + end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "|" + token;
    }

    private void showReservationQrDialog(String spaceName, LocalDateTime start, LocalDateTime end,
            String qrValue, WritableImage qrImage) {
        Dialog<Void> qrDialog = new Dialog<>();
        qrDialog.setTitle("Reserva generada");
        qrDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(14);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10, 10, 10, 10));

        Label title = new Label("Reserva confirmada");
        title.getStyleClass().add("dialog-title");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String schedule = formatter.format(start) + " - " + formatter.format(end);
        Label details = new Label(spaceName + "\n" + schedule);
        details.setStyle("-fx-font-size: 13px; -fx-text-alignment: center;");
        details.setWrapText(true);
        details.setAlignment(Pos.CENTER);

        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(220);
        qrView.setFitHeight(220);
        qrView.setPreserveRatio(true);

        Label qrValueLabel = new Label(qrValue);
        qrValueLabel.getStyleClass().add("qr-value-label");
        qrValueLabel.setWrapText(true);
        qrValueLabel.setAlignment(Pos.CENTER);

        Button copyButton = new Button("Copiar código");
        copyButton.getStyleClass().add("primary-button");
        copyButton.setOnAction(event -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent contentData = new ClipboardContent();
            contentData.putString(qrValue);
            clipboard.setContent(contentData);
        });

        content.getChildren().addAll(title, details, qrView, qrValueLabel, copyButton);
        qrDialog.getDialogPane().setContent(content);
        qrDialog.setResizable(false);
        qrDialog.showAndWait();
    }

    private Long parseLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            try {
                return Long.parseLong(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private int parseInteger(Object value, int defaultValue) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String string && !string.isBlank()) {
            try {
                return Integer.parseInt(string);
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447
    }

    // ==================== MÉTRICAS ====================

    private void updateDashboardMetrics() {
        long active = reservationsList.stream().filter(r -> "CONFIRMED".equals(r.status())).count();
        long completed = reservationsList.stream().filter(r -> "COMPLETED".equals(r.status())).count();
        long noShows = reservationsList.stream().filter(r -> "NO_SHOW".equals(r.status())).count();
        
        if (activeReservationsLabel != null) activeReservationsLabel.setText(String.valueOf(active));
        if (completedReservationsLabel != null) completedReservationsLabel.setText(String.valueOf(completed));
        if (noShowsLabel != null) noShowsLabel.setText(String.valueOf(noShows));
        if (reservationsCountLabel != null) reservationsCountLabel.setText("Reservas (" + reservationsList.size() + ")");
    }

    private void updateWeatherDisplay() {
        if (currentWeather == null) {
            if (weatherTempLabel != null) weatherTempLabel.setText("--°C");
            if (weatherConditionLabel != null) weatherConditionLabel.setText("No disponible");
            if (weatherWindLabel != null) weatherWindLabel.setText("Viento: -- km/h");
            if (weatherHumidityLabel != null) weatherHumidityLabel.setText("Humedad: --%");
            if (weatherIconLabel != null) weatherIconLabel.setText("☀");
            if (weatherMessageLabel != null) weatherMessageLabel.setText("Información no disponible");
            return;
        }

        if (weatherTempLabel != null) weatherTempLabel.setText(String.format("%.1f°C", currentWeather.temperature()));
        if (weatherConditionLabel != null) weatherConditionLabel.setText(currentWeather.description());
        if (weatherWindLabel != null) weatherWindLabel.setText(String.format("Viento: %.1f km/h", currentWeather.windSpeed() * 3.6));
        if (weatherHumidityLabel != null) weatherHumidityLabel.setText("Humedad: " + currentWeather.humidity() + "%");
        if (weatherIconLabel != null) weatherIconLabel.setText(getWeatherIcon(currentWeather.icon()));
        if (weatherMessageLabel != null) weatherMessageLabel.setText(getWeatherMessage(currentWeather));
    }

    private String getWeatherIcon(String icon) {
        if (icon == null) return "☀";
        return switch (icon) {
            case "01d" -> "☀";
            case "01n" -> "🌙";
            case "02d", "02n" -> "⛅";
            case "03d", "03n", "04d", "04n" -> "☁";
            case "09d", "09n" -> "🌧";
            case "10d", "10n" -> "🌦";
            case "11d", "11n" -> "⛈";
            case "13d", "13n" -> "🌨";
            case "50d", "50n" -> "🌫";
            default -> "☀";
        };
    }

    private String getWeatherMessage(CurrentWeatherDTO w) {
        int h = w.humidity();
        double t = w.temperature();
        
        if (h >= 80) return "Posible lluvia - Considera espacios interiores";
        if (h >= 60) return "Clima húmedo - Precaución al aire libre";
        if (t >= 25) return "Día cálido, perfecto para actividades al aire libre";
        if (t >= 20) return "Excelente día para actividades al aire libre";
        if (t >= 15) return "Clima agradable, lleva una chaqueta ligera";
        return "Clima fresco, abrígate bien";
    }

    // ==================== REPORTES ====================

    private void generateReports() {
        if (totalReservationsReportLabel != null) {
            totalReservationsReportLabel.setText(String.valueOf(reservationsList.size()));
        }
        
        long attended = reservationsList.stream().filter(r -> "COMPLETED".equals(r.status())).count();
        double rate = reservationsList.isEmpty() ? 0 : (attended * 100.0 / reservationsList.size());
        
        if (attendanceRateLabel != null) attendanceRateLabel.setText(String.format("%.1f%%", rate));
        if (attendancePercentageLabel != null) attendancePercentageLabel.setText(String.format("%.1f%%", rate));
        
        // Espacio favorito
        Map<Long, Long> spaceCount = reservationsList.stream()
            .filter(r -> r.spaceId() != null)
            .collect(Collectors.groupingBy(ReservationDTO::spaceId, Collectors.counting()));
        
        if (!spaceCount.isEmpty() && favoriteSpaceLabel != null) {
            Long favId = Collections.max(spaceCount.entrySet(), Map.Entry.comparingByValue()).getKey();
            SpaceDTO fav = spacesList.stream().filter(s -> s.id() != null && s.id().equals(favId)).findFirst().orElse(null);
            favoriteSpaceLabel.setText(fav != null ? fav.name() : "Espacio #" + favId);
        }
        
        updateCharts(spaceCount);
    }

    private void updateCharts(Map<Long, Long> spaceCount) {
        // Distribución por espacio
        if (spacesDistributionChart != null && !spaceCount.isEmpty()) {
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
            spaceCount.forEach((id, count) -> {
                SpaceDTO s = spacesList.stream().filter(sp -> sp.id() != null && sp.id().equals(id)).findFirst().orElse(null);
                String label = s != null ? s.name() : "Espacio #" + id;
                data.add(new PieChart.Data(label, count));
            });
            spacesDistributionChart.setData(data);
        }
        
        // Asistencias
        if (attendanceChart != null) {
            long attended = reservationsList.stream().filter(r -> "COMPLETED".equals(r.status())).count();
            long noShows = reservationsList.stream().filter(r -> "NO_SHOW".equals(r.status())).count();
            
            ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Asistidas", attended),
                new PieChart.Data("Inasistencias", noShows)
            );
            attendanceChart.setData(data);
        }
    }

    // ==================== ACCIONES ====================

    private void handleViewReservation(ReservationDTO res) {
        SpaceDTO space = spacesList.stream().filter(s -> s.id() != null && s.id().equals(res.spaceId())).findFirst().orElse(null);
        
        StringBuilder msg = new StringBuilder();
        msg.append("Espacio: ").append(space != null ? space.name() : "N/A").append("\n");
        msg.append("Fecha: ").append(res.startTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        msg.append("Hora: ").append(res.startTime().format(DateTimeFormatter.ofPattern("HH:mm")))
           .append(" - ").append(res.endTime().format(DateTimeFormatter.ofPattern("HH:mm"))).append("\n");
        msg.append("Notas: ").append(res.notes() != null ? res.notes() : "N/A").append("\n");
        msg.append("Estado: ").append(STATUS_MAP.getOrDefault(res.status(), res.status()));
        
        showAlert("Detalles de Reserva #" + res.id(), msg.toString(), Alert.AlertType.INFORMATION);
    }

    private void handleCancelReservation(ReservationDTO res) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancelar Reserva");
        confirm.setHeaderText("¿Seguro que deseas cancelar?");
        
        SpaceDTO space = spacesList.stream().filter(s -> s.id() != null && s.id().equals(res.spaceId())).findFirst().orElse(null);
        confirm.setContentText((space != null ? space.name() : "Espacio") + " - " + 
            res.startTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                showAlert("Cancelar", "Funcionalidad en desarrollo", Alert.AlertType.INFORMATION);
            }
        });
    }

    // ==================== UTILIDADES ====================

    private void showLoadingIndicator(String msg) {
        Platform.runLater(() -> System.out.println("⏳ " + msg));
    }

    private void hideLoadingIndicator() {
        Platform.runLater(() -> System.out.println("✅ Carga completada"));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

<<<<<<< HEAD
    private void showError(String msg) { showAlert("Error", msg, Alert.AlertType.ERROR); }
    private void showSuccess(String msg) { showAlert("Éxito", msg, Alert.AlertType.INFORMATION); }
    private void showWarning(String msg) { showAlert("Advertencia", msg, Alert.AlertType.WARNING); }
}
=======
     private record ReservationRequest(LocalDateTime startTime, LocalDateTime endTime, int attendees, String notes) {
    }

    // Inner class for table data
    public static class ReservationData {
        private final Long id;
        private final String spaceName;
        private final String date;
        private final String time;
        private final String eventType;
        private final String status;
>>>>>>> 758a42a18f27a3c754dbf7fba71f22743a45a447

// ==================== RECORDS ====================

record DataResult(List<SpaceDTO> spaces, List<ReservationDTO> reservations, 
                  CurrentWeatherDTO weather, List<String> warnings) {}

record WeatherResult(CurrentWeatherDTO weather, List<String> warnings) {}