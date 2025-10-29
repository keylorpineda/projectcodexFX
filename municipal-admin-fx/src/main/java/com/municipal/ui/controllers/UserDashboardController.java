package com.municipal.reservationsfx.ui.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.municipal.dtos.ReservationDTO;
import com.municipal.ui.utils.NodeVisibilityUtils;
import com.municipal.ui.utils.QRCodeGenerator;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


public class UserDashboardController {

    // API Configuration
    private static final String API_BASE_URL = "http://localhost:8080/api";
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String authToken;
    private Long currentUserId;

    // Main Container
    @FXML private VBox mainContainer;
    @FXML private ScrollPane contentScroll;

    // Navigation Buttons
    @FXML private Button navDashboardButton;
    @FXML private Button navSpacesButton;
    @FXML private Button navMyReservationsButton;
    @FXML private Button navReportsButton;
    @FXML private Button navLogoutButton;

    // Header User Info
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

    // Content Sections
    @FXML private VBox dashboardSection;
    @FXML private VBox spacesSection;
    @FXML private VBox myReservationsSection;
    @FXML private VBox reportsSection;

    // Dashboard Section Elements
    @FXML private Label activeReservationsLabel;
    @FXML private Label noShowsLabel;
    @FXML private Label completedReservationsLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherConditionLabel;
    @FXML private Label weatherWindLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherMessageLabel;

    // Spaces Section Elements
    @FXML private TextField searchSpaceField;
    @FXML private ChoiceBox<String> spaceTypeChoice;
    @FXML private ChoiceBox<String> capacityChoice;
    @FXML private DatePicker datePicker;
    @FXML private FlowPane spacesFlowPane;
    @FXML private Label spacesCountLabel;
    @FXML private StackPane spacesLoadingOverlay;
    @FXML private Label spacesLoadingLabel;
    @FXML private Label spacesStatusLabel;

    // Reservations Section Elements
    @FXML private TableView<ReservationData> reservationsTable;
    @FXML private TableColumn<ReservationData, String> reservationSpaceColumn;
    @FXML private TableColumn<ReservationData, String> reservationDateColumn;
    @FXML private TableColumn<ReservationData, String> reservationTimeColumn;
    @FXML private TableColumn<ReservationData, String> reservationEventColumn;
    @FXML private TableColumn<ReservationData, String> reservationStatusColumn;
    @FXML private TableColumn<ReservationData, Void> reservationActionsColumn;
    @FXML private Label reservationsCountLabel;
    @FXML private StackPane reservationsLoadingOverlay;
    @FXML private Label reservationsLoadingLabel;
    @FXML private Label reservationsStatusLabel;

    // Reports Section Elements
    @FXML private Label totalReservationsReportLabel;
    @FXML private Label attendanceRateLabel;
    @FXML private Label favoriteSpaceLabel;
    @FXML private PieChart spacesDistributionChart;
    @FXML private PieChart attendanceChart;
    @FXML private Label attendancePercentageLabel;

    // Section Management
    private enum Section {
        DASHBOARD, SPACES, MY_RESERVATIONS, REPORTS
    }

    private Map<Section, Node> sectionMap;
    private Map<Section, Button> navigationButtonMap;
    private ObservableList<ReservationData> reservationsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupSections();
        setupNavigationButtons();
        setupDefaultValues();
        setupTableColumns();
        
        // Show dashboard by default
        navigateToSection(Section.DASHBOARD);
        
        // Load initial data if user is authenticated
        if (authToken != null && currentUserId != null) {
            loadDashboardData();
        }
    }

    private void setupSections() {
        sectionMap = new EnumMap<>(Section.class);
        sectionMap.put(Section.DASHBOARD, dashboardSection);
        sectionMap.put(Section.SPACES, spacesSection);
        sectionMap.put(Section.MY_RESERVATIONS, myReservationsSection);
        sectionMap.put(Section.REPORTS, reportsSection);
    }

    private void setupNavigationButtons() {
        navigationButtonMap = new EnumMap<>(Section.class);
        navigationButtonMap.put(Section.DASHBOARD, navDashboardButton);
        navigationButtonMap.put(Section.SPACES, navSpacesButton);
        navigationButtonMap.put(Section.MY_RESERVATIONS, navMyReservationsButton);
        navigationButtonMap.put(Section.REPORTS, navReportsButton);
    }

    private void setupDefaultValues() {
        // Set default user info
        if (userNameLabel != null) {
            userNameLabel.setText("Usuario");
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText("Ciudadano");
        }

        // Initialize choice box items
        if (spaceTypeChoice != null) {
            spaceTypeChoice.getItems().addAll("Todos", "SALA", "CANCHA", "AUDITORIO");
            spaceTypeChoice.setValue("Todos");
        }
        if (capacityChoice != null) {
            capacityChoice.getItems().addAll("Todas", "1-50", "51-100", "100+");
            capacityChoice.setValue("Todas");
        }
    }

    private void setupTableColumns() {
        if (reservationsTable != null) {
            reservationsTable.setItems(reservationsList);
            
            reservationSpaceColumn.setCellValueFactory(new PropertyValueFactory<>("spaceName"));
            reservationDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            reservationTimeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
            reservationEventColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));
            reservationStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            
            // Setup status column with badges
            reservationStatusColumn.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        Label badge = new Label(status);
                        badge.getStyleClass().add("status-badge");
                        
                        switch (status.toUpperCase()) {
                            case "CONFIRMADA":
                            case "CONFIRMED":
                                badge.getStyleClass().add("status-confirmed");
                                break;
                            case "COMPLETADA":
                            case "COMPLETED":
                                badge.getStyleClass().add("status-completed");
                                break;
                            case "CANCELADA":
                            case "CANCELLED":
                                badge.getStyleClass().add("status-cancelled");
                                break;
                            default:
                                badge.getStyleClass().add("status-pending");
                        }
                        
                        setGraphic(badge);
                        setText(null);
                    }
                }
            });
            
            // Setup actions column with buttons
            reservationActionsColumn.setCellFactory(column -> new TableCell<>() {
                private final Button viewButton = new Button("👁");
                private final Button cancelButton = new Button("✖");
                private final HBox actionBox = new HBox(5, viewButton, cancelButton);
                
                {
                    viewButton.getStyleClass().add("action-button");
                    cancelButton.getStyleClass().add("action-button");
                    actionBox.setAlignment(Pos.CENTER);
                    
                    viewButton.setOnAction(event -> {
                        ReservationData reservation = getTableView().getItems().get(getIndex());
                        handleViewReservation(reservation);
                    });
                    
                    cancelButton.setOnAction(event -> {
                        ReservationData reservation = getTableView().getItems().get(getIndex());
                        handleCancelReservation(reservation);
                    });
                }
                
                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        ReservationData reservation = getTableView().getItems().get(getIndex());
                        // Only show cancel button for CONFIRMED reservations
                        if ("CONFIRMADA".equals(reservation.getStatus()) || "CONFIRMED".equals(reservation.getStatus())) {
                            actionBox.getChildren().setAll(viewButton, cancelButton);
                        } else {
                            actionBox.getChildren().setAll(viewButton);
                        }
                        setGraphic(actionBox);
                    }
                }
            });
        }
    }

    private void navigateToSection(Section targetSection) {
        // Hide all sections
        sectionMap.values().forEach(section -> NodeVisibilityUtils.hide(section));

        // Show target section with fade transition
        Node targetNode = sectionMap.get(targetSection);
        if (targetNode != null) {
            NodeVisibilityUtils.show(targetNode);
            applyFadeTransition(targetNode);
        }

        // Update navigation button styles
        updateNavigationStyles(targetSection);

        // Reset scroll position
        if (contentScroll != null) {
            contentScroll.setVvalue(0);
        }
    }

    private void updateNavigationStyles(Section activeSection) {
        navigationButtonMap.forEach((section, button) -> {
            if (button != null) {
                if (section == activeSection) {
                    button.getStyleClass().removeAll("nav-item");
                    button.getStyleClass().add("nav-item-active");
                } else {
                    button.getStyleClass().removeAll("nav-item-active");
                    if (!button.getStyleClass().contains("nav-item")) {
                        button.getStyleClass().add("nav-item");
                    }
                }
            }
        });
    }

    private void applyFadeTransition(Node node) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }

    // Navigation Handlers
    @FXML
    private void handleNavigateDashboard() {
        navigateToSection(Section.DASHBOARD);
        loadDashboardData();
    }

    @FXML
    private void handleNavigateSpaces() {
        navigateToSection(Section.SPACES);
        loadAvailableSpaces();
    }

    @FXML
    private void handleNavigateMyReservations() {
        navigateToSection(Section.MY_RESERVATIONS);
        loadUserReservations();
    }

    @FXML
    private void handleNavigateReports() {
        navigateToSection(Section.REPORTS);
        loadPersonalReports();
    }

    @FXML
    private void handleLogout() {
        authToken = null;
        currentUserId = null;
        // TODO: Navigate back to login screen
        showAlert("Sesión cerrada", "Has cerrado sesión exitosamente", Alert.AlertType.INFORMATION);
    }

    // Action Handlers
    @FXML
    private void handleApplyFilters() {
        loadAvailableSpaces();
    }

    @FXML
    private void handleNewReservation() {
        navigateToSection(Section.SPACES);
    }

    // API Methods
    
    private void loadDashboardData() {
        if (currentUserId == null) return;
        
        loadUserReservations();
        loadWeatherData();
    }

    private void loadAvailableSpaces() {
        if (spacesLoadingOverlay != null) {
            NodeVisibilityUtils.show(spacesLoadingOverlay);
        }
        
        // Build query parameters
        StringBuilder queryParams = new StringBuilder("?available=true");
        
        if (spaceTypeChoice != null && !"Todos".equals(spaceTypeChoice.getValue())) {
            queryParams.append("&type=").append(spaceTypeChoice.getValue());
        }
        
        if (datePicker != null && datePicker.getValue() != null) {
            queryParams.append("&date=").append(datePicker.getValue());
        }
        
        if (searchSpaceField != null && !searchSpaceField.getText().isEmpty()) {
            queryParams.append("&search=").append(searchSpaceField.getText());
        }

        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/spaces" + queryParams.toString()))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<Map<String, Object>> spaces = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    
                    Platform.runLater(() -> {
                        displaySpaces(spaces);
                        if (spacesCountLabel != null) {
                            spacesCountLabel.setText("Espacios disponibles (" + spaces.size() + ")");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Error", "No se pudieron cargar los espacios", Alert.AlertType.ERROR);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Error al cargar espacios: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            } finally {
                Platform.runLater(() -> {
                    if (spacesLoadingOverlay != null) {
                        NodeVisibilityUtils.hide(spacesLoadingOverlay);
                    }
                });
            }
        });
    }

    private void displaySpaces(List<Map<String, Object>> spaces) {
        if (spacesFlowPane == null) return;
        
        spacesFlowPane.getChildren().clear();
        
        for (Map<String, Object> space : spaces) {
            VBox spaceCard = createSpaceCard(space);
            spacesFlowPane.getChildren().add(spaceCard);
        }
    }

    private VBox createSpaceCard(Map<String, Object> space) {
        VBox card = new VBox(10);
        card.getStyleClass().add("space-card");
        card.setPrefWidth(280);
        card.setPadding(new Insets(0));
        
        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("space-image-container");
        imageContainer.setPrefHeight(180);
        
        // Load space image if available
        ImageView imageView = new ImageView();
        imageView.setFitWidth(280);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(false);
        
        if (space.containsKey("images") && space.get("images") != null) {
            List<?> images = (List<?>) space.get("images");
            if (!images.isEmpty() && images.get(0) instanceof Map) {
                Map<?, ?> imageData = (Map<?, ?>) images.get(0);
                String imageUrl = (String) imageData.get("imageUrl");
                if (imageUrl != null) {
                    try {
                        imageView.setImage(new Image(imageUrl, true));
                    } catch (Exception e) {
                        // Use default image on error
                    }
                }
            }
        }
        
        imageContainer.getChildren().add(imageView);
        
        // Status badge
        Label statusBadge = new Label("Disponible");
        statusBadge.getStyleClass().addAll("space-status-badge", "space-available");
        StackPane.setAlignment(statusBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(statusBadge, new Insets(10));
        imageContainer.getChildren().add(statusBadge);
        
        // Content
        VBox content = new VBox(8);
        content.setPadding(new Insets(16));
        
        Label nameLabel = new Label((String) space.get("name"));
        nameLabel.getStyleClass().add("space-name");
        
        Label descLabel = new Label((String) space.get("description"));
        descLabel.getStyleClass().add("space-description");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(40);
        
        HBox infoBox = new HBox(15);
        Label capacityLabel = new Label("👥 " + space.get("capacity"));
        Label typeLabel = new Label("📍 " + space.get("type"));
        infoBox.getChildren().addAll(capacityLabel, typeLabel);
        
        Button reserveButton = new Button("Reservar espacio");
        reserveButton.getStyleClass().add("primary-button");
        reserveButton.setMaxWidth(Double.MAX_VALUE);
        reserveButton.setOnAction(e -> handleReserveSpace(space));
        
        content.getChildren().addAll(nameLabel, descLabel, infoBox, reserveButton);
        
        card.getChildren().addAll(imageContainer, content);
        
        return card;
    }

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

        String qrValue = buildQrPayload(spaceId, request.startTime(), request.endTime());
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("userId", currentUserId);
        payload.put("spaceId", spaceId);
        payload.put("startTime", request.startTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("endTime", request.endTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        payload.put("status", "CONFIRMED");
        payload.put("qrCode", qrValue);
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
                    WritableImage qrImage = QRCodeGenerator.generate(qrValue);
                    Platform.runLater(() -> {
                        loadUserReservations();
                        showReservationQrDialog(spaceName, created.startTime(), created.endTime(), qrValue, qrImage);
                    });
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
    
    }

    private void loadUserReservations() {
        if (currentUserId == null) {
            Platform.runLater(() -> {
                if (reservationsCountLabel != null) {
                    reservationsCountLabel.setText("Reservas (0)");
                }
            });
            return;
        }
        
        if (reservationsLoadingOverlay != null) {
            NodeVisibilityUtils.show(reservationsLoadingOverlay);
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/reservations/user/" + currentUserId))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<Map<String, Object>> reservations = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    
                    Platform.runLater(() -> {
                        updateReservationsTable(reservations);
                        updateDashboardMetrics(reservations);
                        if (reservationsCountLabel != null) {
                            reservationsCountLabel.setText("Reservas (" + reservations.size() + ")");
                        }
                    });
                } else {
                    Platform.runLater(() -> {
                        showAlert("Error", "No se pudieron cargar las reservas", Alert.AlertType.ERROR);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Error al cargar reservas: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            } finally {
                Platform.runLater(() -> {
                    if (reservationsLoadingOverlay != null) {
                        NodeVisibilityUtils.hide(reservationsLoadingOverlay);
                    }
                });
            }
        });
    }

    private void updateReservationsTable(List<Map<String, Object>> reservations) {
        reservationsList.clear();
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Map<String, Object> reservation : reservations) {
            try {
                Long id = ((Number) reservation.get("id")).longValue();
                String spaceName = getSpaceName(reservation);
                String status = (String) reservation.get("status");
                String eventType = (String) reservation.get("eventType");
                
                LocalDateTime startTime = LocalDateTime.parse((String) reservation.get("startTime"));
                String date = startTime.format(dateFormatter);
                String time = startTime.format(timeFormatter);
                
                if (reservation.containsKey("endTime")) {
                    LocalDateTime endTime = LocalDateTime.parse((String) reservation.get("endTime"));
                    time += " - " + endTime.format(timeFormatter);
                }
                
                reservationsList.add(new ReservationData(id, spaceName, date, time, eventType, status));
            } catch (Exception e) {
                System.err.println("Error processing reservation: " + e.getMessage());
            }
        }
    }

    private String getSpaceName(Map<String, Object> reservation) {
        if (reservation.containsKey("space") && reservation.get("space") instanceof Map) {
            Map<?, ?> space = (Map<?, ?>) reservation.get("space");
            return (String) space.get("name");
        }
        return "Espacio desconocido";
    }

    private void updateDashboardMetrics(List<Map<String, Object>> reservations) {
        int active = 0;
        int noShows = 0;
        int completed = 0;
        
        for (Map<String, Object> reservation : reservations) {
            String status = (String) reservation.get("status");
            if (status != null) {
                switch (status.toUpperCase()) {
                    case "CONFIRMED":
                    case "CONFIRMADA":
                        active++;
                        break;
                    case "NO_SHOW":
                        noShows++;
                        break;
                    case "COMPLETED":
                    case "COMPLETADA":
                        completed++;
                        break;
                }
            }
        }
        
        if (activeReservationsLabel != null) {
            activeReservationsLabel.setText(String.valueOf(active));
        }
        if (noShowsLabel != null) {
            noShowsLabel.setText(String.valueOf(noShows));
        }
        if (completedReservationsLabel != null) {
            completedReservationsLabel.setText(String.valueOf(completed));
        }
    }

    private void loadWeatherData() {
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/weather/current"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    Map<String, Object> weather = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<Map<String, Object>>() {}
                    );
                    
                    Platform.runLater(() -> updateWeatherDisplay(weather));
                }
            } catch (Exception e) {
                System.err.println("Error loading weather: " + e.getMessage());
            }
        });
    }

    private void updateWeatherDisplay(Map<String, Object> weather) {
        try {
            if (weatherTempLabel != null && weather.containsKey("temperature")) {
                weatherTempLabel.setText(weather.get("temperature") + "°C");
            }
            if (weatherConditionLabel != null && weather.containsKey("description")) {
                weatherConditionLabel.setText((String) weather.get("description"));
            }
            if (weatherWindLabel != null && weather.containsKey("windSpeed")) {
                weatherWindLabel.setText("Viento: " + weather.get("windSpeed") + " km/h");
            }
            if (weatherHumidityLabel != null && weather.containsKey("humidity")) {
                weatherHumidityLabel.setText("Humedad: " + weather.get("humidity") + "%");
            }
            if (weatherIconLabel != null && weather.containsKey("icon")) {
                weatherIconLabel.setText(getWeatherIcon((String) weather.get("icon")));
            }
            if (weatherMessageLabel != null) {
                weatherMessageLabel.setText("Excelente día para actividades al aire libre");
            }
        } catch (Exception e) {
            System.err.println("Error updating weather display: " + e.getMessage());
        }
    }

    private String getWeatherIcon(String icon) {
        // Map OpenWeather icons to emoji
        switch (icon) {
            case "01d": return "☀";
            case "01n": return "🌙";
            case "02d": case "02n": return "⛅";
            case "03d": case "03n": return "☁";
            case "04d": case "04n": return "☁";
            case "09d": case "09n": return "🌧";
            case "10d": case "10n": return "🌦";
            case "11d": case "11n": return "⛈";
            case "13d": case "13n": return "🌨";
            case "50d": case "50n": return "🌫";
            default: return "☀";
        }
    }

    private void loadPersonalReports() {
        if (currentUserId == null) return;
        
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/reservations/user/" + currentUserId))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    List<Map<String, Object>> reservations = objectMapper.readValue(
                        response.body(), 
                        new TypeReference<List<Map<String, Object>>>() {}
                    );
                    
                    Platform.runLater(() -> generateReports(reservations));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void generateReports(List<Map<String, Object>> reservations) {
        // Total reservations
        if (totalReservationsReportLabel != null) {
            totalReservationsReportLabel.setText(String.valueOf(reservations.size()));
        }
        
        // Calculate attendance rate
        long attended = reservations.stream()
            .filter(r -> "COMPLETED".equals(r.get("status")))
            .count();
        double attendanceRate = reservations.isEmpty() ? 0 : (attended * 100.0 / reservations.size());
        
        if (attendanceRateLabel != null) {
            attendanceRateLabel.setText(String.format("%.1f%%", attendanceRate));
        }
        if (attendancePercentageLabel != null) {
            attendancePercentageLabel.setText(String.format("%.1f%%", attendanceRate));
        }
        
        // Find favorite space
        Map<String, Long> spaceCount = reservations.stream()
            .collect(Collectors.groupingBy(
                r -> getSpaceName(r),
                Collectors.counting()
            ));
        
        String favoriteSpace = spaceCount.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("N/A");
        
        if (favoriteSpaceLabel != null) {
            favoriteSpaceLabel.setText(favoriteSpace);
        }
        
        // Update charts
        updateChartsData(reservations, spaceCount);
    }

    private void updateChartsData(List<Map<String, Object>> reservations, Map<String, Long> spaceCount) {
        // Spaces distribution chart
        if (spacesDistributionChart != null) {
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            spaceCount.forEach((space, count) -> 
                pieChartData.add(new PieChart.Data(space, count))
            );
            spacesDistributionChart.setData(pieChartData);
        }
        
        // Attendance chart
        if (attendanceChart != null) {
            long attended = reservations.stream()
                .filter(r -> "COMPLETED".equals(r.get("status")))
                .count();
            long noShows = reservations.stream()
                .filter(r -> "NO_SHOW".equals(r.get("status")))
                .count();
            
            ObservableList<PieChart.Data> attendanceData = FXCollections.observableArrayList(
                new PieChart.Data("Asistidas", attended),
                new PieChart.Data("Inasistencias", noShows)
            );
            attendanceChart.setData(attendanceData);
        }
    }

    private void handleViewReservation(ReservationData reservation) {
        showAlert("Ver Reserva", "Detalles de la reserva #" + reservation.getId(), Alert.AlertType.INFORMATION);
    }

    private void handleCancelReservation(ReservationData reservation) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Cancelar Reserva");
        confirmation.setHeaderText("¿Estás seguro de cancelar esta reserva?");
        confirmation.setContentText(reservation.getSpaceName() + " - " + reservation.getDate());
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cancelReservation(reservation.getId());
            }
        });
    }

    private void cancelReservation(Long reservationId) {
        CompletableFuture.runAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_BASE_URL + "/reservations/" + reservationId + "/cancel"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.noBody())
                    .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        showAlert("Éxito", "Reserva cancelada exitosamente", Alert.AlertType.INFORMATION);
                        loadUserReservations();
                    } else {
                        showAlert("Error", "No se pudo cancelar la reserva", Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Error", "Error al cancelar: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
    }

    // Public methods for initialization
    public void setAuthToken(String token) {
        this.authToken = token;
    }

    public void setCurrentUserId(Long userId) {
        this.currentUserId = userId;
    }

    public void setUserInfo(String userName, String userRole) {
        if (userNameLabel != null) {
            userNameLabel.setText(userName);
        }
        if (userRoleLabel != null) {
            userRoleLabel.setText(userRole);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

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

        public ReservationData(Long id, String spaceName, String date, String time, String eventType, String status) {
            this.id = id;
            this.spaceName = spaceName;
            this.date = date;
            this.time = time;
            this.eventType = eventType;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getSpaceName() { return spaceName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getEventType() { return eventType; }
        public String getStatus() { return status; }
    }
}