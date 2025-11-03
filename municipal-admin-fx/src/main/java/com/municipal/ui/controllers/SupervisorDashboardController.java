package com.municipal.ui.controllers;

import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowAware;
import com.municipal.ui.navigation.FlowController;
import com.municipal.ui.navigation.SessionAware;
import com.municipal.ui.utils.QRScanner;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.util.Duration;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import com.municipal.dtos.ReservationAttendeeDTO;
import com.municipal.dtos.ReservationCheckInRequest;

/**
 * Segunda versión del panel de supervisor centrado en reservas confirmadas.
 * Proporciona filtro por lugar, detalle contextual y flujo de validación/registro de QR.
 */
public class SupervisorDashboardController implements Initializable, SessionAware, FlowAware {

    private static final String LOGIN_VIEW_ID = "login";
    private static final Locale LOCALE_ES_CR = new Locale("es", "CR");
    private static final DateTimeFormatter FULL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM yyyy · HH:mm", LOCALE_ES_CR);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm", LOCALE_ES_CR);
    private static final Set<String> ACCEPTED_STATUSES = Set.of("CONFIRMED", "CHECKED_IN");

    @FXML private VBox mainContainer;
    
    // Navegación
    @FXML private HBox navDashboardButton;
    @FXML private HBox navScanQRButton;
    @FXML private HBox navControlButton;
    
    // Secciones
    @FXML private VBox dashboardSection;
    @FXML private VBox scanQRSection;
    @FXML private VBox attendeeCounterSection;
    @FXML private VBox controlSection;
    
    // Usuario info
    @FXML private Label supervisorNameLabel;
    @FXML private Label supervisorEmailLabel;
    @FXML private HBox userMenuContainer;
    
    // Panel de perfil
    @FXML private StackPane profilePanelOverlay;
    @FXML private VBox profilePanel;
    @FXML private Label profileNameLabel;
    @FXML private Label profileEmailLabel;
    
    // Dashboard Section
    @FXML private Label activeEventsLabel;
    @FXML private Label todayCheckInsLabel;
    @FXML private Label pendingReservationsLabel;
    @FXML private FlowPane inProgressEventsPane;
    
    // Scan QR Section
    @FXML private StackPane cameraPreviewContainer;
    @FXML private ImageView cameraImageView;
    @FXML private Label scanningLabel;
    @FXML private Button startCameraButton;
    @FXML private Button stopCameraButton;
    @FXML private TextField qrCodeField;
    @FXML private Button scanButton;
    @FXML private VBox validationMessageBox;
    @FXML private Label validationMessageLabel;
    
    // Espacios en Uso Section
    @FXML private Label spacesInUseCountLabel;
    @FXML private Label spacesInUseTimeLabel;
    @FXML private FlowPane spacesInUsePane;
    
    // Attendee Counter Section
    @FXML private Label counterReservationUser;
    @FXML private Label counterReservationSpace;
    @FXML private Label counterReservationTime;
    @FXML private Label attendeeCountDisplay;
    @FXML private Label attendeeLimit;
    @FXML private Button decrementButton;
    @FXML private Button incrementButton;
    @FXML private Button finishCountButton;
    
    // Campos antiguos (mantener compatibilidad)
    @FXML private Label lblSupervisorName;
    @FXML private Label lblSupervisorEmail;
    @FXML private TextField txtBuscar;
    @FXML private Button btnLimpiarFiltro;
    @FXML private Button btnRefrescar;
    @FXML private Label lblEmptyState;
    @FXML private ListView<ReservationCard> lvReservas;
    @FXML private Label lblDetalleTitulo;
    @FXML private Label lblDetalleUbicacion;
    @FXML private Label lblDetalleHorario;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblDetalleNotas;
    @FXML private Label lblDetalleAsistentes;
    @FXML private Label lblDetalleIngresos;
    @FXML private Label lblDetalleCheckIn;
    @FXML private Label lblDetalleCreada;
    @FXML private Label lblDetalleActualizada;
    @FXML private Button btnValidarQr;

    private final ReservationController reservationController = new ReservationController();
    private final SpaceController spaceController = new SpaceController();

    private final ObservableList<ReservationCard> reservationEntries = FXCollections.observableArrayList();
    private FilteredList<ReservationCard> filteredReservations;

    private SessionManager sessionManager;
    private FlowController flowController;

    private final AtomicBoolean loadingGuard = new AtomicBoolean(false);
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(false);
    
    private QRScanner qrScanner;
    private volatile boolean isScanningQR = false;
    private Timeline autoRefreshTimeline;
    
    // Estado del contador de asistentes
    private int currentAttendeeCount = 0;
    private int maxAttendees = 1;
    private ReservationDTO currentReservation = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar navegación de secciones
        initializeSections();
        
        // Configurar auto-refresh
        initializeAutoRefresh();
        
        // Configurar panel de perfil
        initializeProfilePanel();
        
        // Configuraciones antiguas (compatibilidad)
        configureListView();
        configureFiltering();
        configureButtons();
        configureEmptyState();
        loadingProperty.addListener((obs, oldValue, newValue) -> updateEmptyStateMessage());
        resetDetail();
    }
    
    private void initializeSections() {
        // Mostrar dashboard por defecto
        if (dashboardSection != null) {
            showSection(dashboardSection);
            updateActiveNavButton(navDashboardButton);
        }
    }
    
    private void initializeProfilePanel() {
        // Configurar click en el menú de usuario para abrir/cerrar panel
        if (userMenuContainer != null) {
            userMenuContainer.setOnMouseClicked(event -> toggleProfilePanel());
        }
        
        // Configurar click en el overlay para cerrar el panel
        if (profilePanelOverlay != null) {
            profilePanelOverlay.setOnMouseClicked(event -> {
                if (event.getTarget() == profilePanelOverlay) {
                    closeProfilePanel();
                }
            });
        }
    }
    
    private void toggleProfilePanel() {
        if (profilePanelOverlay != null) {
            boolean isVisible = profilePanelOverlay.isVisible();
            if (isVisible) {
                closeProfilePanel();
            } else {
                openProfilePanel();
            }
        }
    }
    
    private void openProfilePanel() {
        if (profilePanelOverlay != null) {
            profilePanelOverlay.setVisible(true);
            profilePanelOverlay.setManaged(true);
        }
    }
    
    private void closeProfilePanel() {
        if (profilePanelOverlay != null) {
            profilePanelOverlay.setVisible(false);
            profilePanelOverlay.setManaged(false);
        }
    }
    
    private void initializeAutoRefresh() {
        // Timeline para actualizar datos automáticamente cada 30 segundos
        autoRefreshTimeline = new Timeline(
            new KeyFrame(Duration.seconds(30), event -> {
                if (sessionManager != null && sessionManager.getAccessToken() != null) {
                    // Actualizar dashboard si está visible
                    if (dashboardSection != null && dashboardSection.isVisible()) {
                        refreshDashboard();
                    }
                    // Actualizar espacios en uso si está visible
                    if (controlSection != null && controlSection.isVisible()) {
                        refreshSpacesInUse();
                    }
                }
            })
        );
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        updateSupervisorLabels();
        
        // Cargar datos iniciales
        loadReservations();
        refreshDashboard();
        refreshSpacesInUse();
        
        // Iniciar auto-refresh
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.play();
        }
    }

    @Override
    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    @FXML
    private void cerrarSesion() {
        // Detener auto-refresh
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
        }
        
        // Detener escaneo QR si está activo
        if (isScanningQR) {
            stopQRScanning();
        }
        
        if (sessionManager != null) {
            sessionManager.clear();
        }
        if (flowController != null) {
            flowController.showView(LOGIN_VIEW_ID);
        }
    }
    
    // ==================== MÉTODOS DE NAVEGACIÓN ====================
    
    @FXML
    private void showDashboard() {
        showSection(dashboardSection);
        updateActiveNavButton(navDashboardButton);
    }

    @FXML
    private void showScanQR() {
        showSection(scanQRSection);
        updateActiveNavButton(navScanQRButton);
    }

    @FXML
    private void showControl() {
        showSection(controlSection);
        updateActiveNavButton(navControlButton);
    }
    
    @FXML
    private void handleLogout() {
        cerrarSesion();
    }
    
    @FXML
    private void handleStartCamera() {
        if (isScanningQR) {
            return;
        }
        
        if (!QRScanner.isCameraAvailable()) {
            showValidationMessage("❌ No se detectó ninguna cámara", "error");
            return;
        }
        
        if (qrScanner == null) {
            qrScanner = new QRScanner();
        }
        
        boolean started = qrScanner.start(
            this::onQRCodeDetected,
            image -> {
                if (cameraImageView != null) {
                    cameraImageView.setImage(image);
                }
            }
        );
        
        if (started) {
            isScanningQR = true;
            if (cameraPreviewContainer != null) cameraPreviewContainer.setVisible(true);
            if (startCameraButton != null) startCameraButton.setVisible(false);
            if (stopCameraButton != null) stopCameraButton.setVisible(true);
            if (scanningLabel != null) scanningLabel.setVisible(true);
            hideValidationMessage();
        } else {
            showValidationMessage("❌ No se pudo iniciar la cámara", "error");
        }
    }
    
    @FXML
    private void handleStopCamera() {
        stopQRScanning();
    }
    
    @FXML
    private void handleScanQR() {
        if (qrCodeField == null || qrCodeField.getText() == null || qrCodeField.getText().trim().isEmpty()) {
            showValidationMessage("⚠️ Por favor ingrese un código QR", "warning");
            return;
        }
        
        String qrCode = qrCodeField.getText().trim();
        validateQRCode(qrCode);
    }
    
    /**
     * Callback cuando se detecta un código QR desde la cámara
     */
    private void onQRCodeDetected(String qrCode) {
        stopQRScanning();
        
        if (qrCodeField != null) {
            qrCodeField.setText(qrCode);
        }
        
        validateQRCode(qrCode);
    }
    
    /**
     * Detiene el escaneo QR y limpia recursos
     */
    private void stopQRScanning() {
        if (qrScanner != null) {
            qrScanner.stop();
        }
        
        isScanningQR = false;
        
        if (cameraPreviewContainer != null) cameraPreviewContainer.setVisible(false);
        if (startCameraButton != null) startCameraButton.setVisible(true);
        if (stopCameraButton != null) stopCameraButton.setVisible(false);
        if (scanningLabel != null) scanningLabel.setVisible(false);
        if (cameraImageView != null) cameraImageView.setImage(null);
    }
    
    /**
     * Valida el código QR y registra el check-in
     * Verifica la ventana temporal de 30 minutos antes y después de la reserva
     */
    private void validateQRCode(String qrCode) {
        if (sessionManager == null) {
            showValidationMessage("❌ No hay sesión activa", "error");
            return;
        }
        
        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            showValidationMessage("❌ Token de autenticación no encontrado", "error");
            return;
        }
        
        showValidationMessage("🔄 Validando código QR...", "info");
        
        Task<ReservationDTO> task = new Task<>() {
            @Override
            protected ReservationDTO call() throws Exception {
                // Buscar la reserva por código QR
                List<ReservationDTO> allReservations = reservationController.getAllReservations(token);
                
                for (ReservationDTO reservation : allReservations) {
                    if (qrCode.equals(reservation.qrCode())) {
                        return reservation;
                    }
                }
                
                throw new Exception("Código QR no encontrado");
            }
        };
        
        task.setOnSucceeded(event -> {
            ReservationDTO reservation = task.getValue();
            validateTemporalWindow(reservation, qrCode, token);
        });
        
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Error desconocido";
            showValidationMessage("❌ " + message, "error");
        });
        
        new Thread(task).start();
    }
    
    /**
     * Valida que el check-in esté dentro de la ventana temporal permitida
     * (30 minutos antes y 30 minutos después de la hora de inicio)
     */
    private void validateTemporalWindow(ReservationDTO reservation, String qrCode, String token) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = reservation.startTime();
        LocalDateTime windowStart = startTime.minusMinutes(30);
        LocalDateTime windowEnd = startTime.plusMinutes(30);
        
        // Verificar si está en estado PENDING (bloqueado/pendiente de aprobación)
        if ("PENDING".equalsIgnoreCase(reservation.status())) {
            showValidationMessage(
                "🔒 Reserva Bloqueada\n\n" +
                "Esta reserva está pendiente de aprobación por un administrador.\n" +
                "El código QR no puede ser usado hasta que la reserva sea aprobada.\n\n" +
                "Estado: PENDIENTE DE APROBACIÓN",
                "error"
            );
            return;
        }
        
        // Verificar si está en estado CANCELLED (cancelada)
        if ("CANCELLED".equalsIgnoreCase(reservation.status())) {
            showValidationMessage(
                "❌ Reserva Cancelada\n\n" +
                "Esta reserva fue cancelada y no puede ser utilizada.\n" +
                "Por favor, solicite una nueva reserva.",
                "error"
            );
            return;
        }
        
        // Verificar si ya tiene check-in
        if ("CHECKED_IN".equals(reservation.status())) {
            showValidationMessage("✅ Esta reserva ya fue registrada anteriormente", "success");
            showReservationInfo(reservation);
            return;
        }
        
        // Verificar que esté en estado CONFIRMED (confirmada)
        if (!"CONFIRMED".equalsIgnoreCase(reservation.status())) {
            showValidationMessage(
                "⚠️ Estado de Reserva Inválido\n\n" +
                "Solo las reservas confirmadas pueden realizar check-in.\n" +
                "Estado actual: " + reservation.status(),
                "warning"
            );
            return;
        }
        
        // ⚠️ VALIDACIÓN TEMPORAL DESHABILITADA - Dejar que el backend valide
        // El backend tiene la lógica correcta con la zona horaria de Costa Rica
        /*
        if (now.isBefore(windowStart)) {
            long minutesUntil = java.time.Duration.between(now, windowStart).toMinutes();
            showValidationMessage(
                String.format("⏰ Demasiado temprano. Podrá validar el QR en %d minutos (a las %s)",
                    minutesUntil,
                    windowStart.format(TIME_FORMAT)),
                "warning"
            );
            return;
        }
        
        if (now.isAfter(windowEnd)) {
            showValidationMessage(
                String.format("⏰ La ventana de check-in cerró a las %s (30 min después del inicio)",
                    windowEnd.format(TIME_FORMAT)),
                "error"
            );
            return;
        }
        */
        
        // Proceder directamente con el check-in
        performCheckIn(reservation, qrCode, token);
    }
    
    /**
     * Realiza el check-in de la reserva
     */
    private void performCheckIn(ReservationDTO reservation, String qrCode, String token) {
        showValidationMessage("🔄 Registrando ingreso...", "info");
        
        // Obtener el nombre completo del usuario del session manager
        String fullName = sessionManager.getUserDisplayName() != null ? 
            sessionManager.getUserDisplayName() : "Supervisor Sistema";
        
        // Separar nombre y apellido (si hay espacio, sino usar el mismo valor para ambos)
        String firstName = fullName;
        String lastName = fullName;
        
        if (fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : parts[0];
        }
        
        // Generar un ID de asistente automático basado en el userId del supervisor
        // Formato: SUP-{userId} (ej: SUP-1, SUP-2, etc.)
        String attendeeIdNumber = sessionManager.getUserId() != null ? 
            "SUP-" + sessionManager.getUserId() : "SUP-SYSTEM";
        
        // Crear el payload de check-in con datos del supervisor
        ReservationCheckInRequest payload = new ReservationCheckInRequest(
            qrCode,
            attendeeIdNumber,
            firstName,
            lastName
        );
        
        Task<ReservationDTO> task = new Task<>() {
            @Override
            protected ReservationDTO call() throws Exception {
                return reservationController.markCheckIn(reservation.id(), token, payload);
            }
        };
        
        task.setOnSucceeded(event -> {
            ReservationDTO updated = task.getValue();
            
            // Guardar la reserva actualizada
            currentReservation = updated;
            
            // Obtener información del espacio
            String spaceName = getSpaceName(updated.spaceId(), token);
            
            // Obtener nombre de usuario
            String userName = getUserName(updated.userId(), token);
            
            // Inicializar el contador
            currentAttendeeCount = updated.attendeeRecords() != null ? updated.attendeeRecords().size() : 1;
            maxAttendees = updated.attendees() != null ? updated.attendees() : 1;
            
            // Limpiar el campo de texto
            if (qrCodeField != null) {
                qrCodeField.clear();
            }
            
            // Recargar reservas
            loadReservations();
            
            // Navegar a la sección del contador con animación suave
            Platform.runLater(() -> {
                try {
                    Thread.sleep(500);
                    showAttendeeCounter(updated, userName, spaceName);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = "Error al registrar check-in";
            
            // Extraer mensaje específico de ApiClientException
            if (error instanceof com.municipal.exceptions.ApiClientException) {
                com.municipal.exceptions.ApiClientException apiError = 
                    (com.municipal.exceptions.ApiClientException) error;
                String responseBody = apiError.getResponseBody();
                
                // Intentar extraer el mensaje del JSON de error
                if (responseBody != null && !responseBody.isEmpty()) {
                    try {
                        // Parsear JSON simple para obtener el mensaje
                        if (responseBody.contains("\"message\"")) {
                            int start = responseBody.indexOf("\"message\"") + 11;
                            int end = responseBody.indexOf("\"", start);
                            if (end > start) {
                                message = responseBody.substring(start, end);
                            }
                        }
                    } catch (Exception e) {
                        message = "Error " + apiError.getStatusCode() + ": " + responseBody;
                    }
                }
            } else if (error != null && error.getMessage() != null) {
                message = error.getMessage();
            }
            
            showValidationMessage("❌ " + message, "error");
        });
        
        new Thread(task).start();
    }
    
    /**
     * Obtiene el nombre del espacio por ID
     */
    private String getSpaceName(Long spaceId, String token) {
        try {
            List<SpaceDTO> spaces = spaceController.loadSpaces(token);
            return spaces.stream()
                .filter(s -> s.id().equals(spaceId))
                .findFirst()
                .map(SpaceDTO::name)
                .orElse("Espacio #" + spaceId);
        } catch (Exception e) {
            return "Espacio #" + spaceId;
        }
    }
    
    /**
     * Muestra información de la reserva
     */
    private void showReservationInfo(ReservationDTO reservation) {
        String spaceName = getSpaceName(reservation.spaceId(), sessionManager.getAccessToken());
        String userName = sessionManager.getUserDisplayName() != null ? 
            sessionManager.getUserDisplayName() : "Usuario";
        System.out.println("Reserva: " + userName + " - " + spaceName);
    }
    
    /**
     * Muestra un mensaje de validación en la UI
     */
    private void showValidationMessage(String message, String type) {
        if (validationMessageBox == null || validationMessageLabel == null) {
            return;
        }
        
        Platform.runLater(() -> {
            validationMessageLabel.setText(message);
            validationMessageLabel.getStyleClass().removeAll("success", "error", "warning", "info");
            validationMessageLabel.getStyleClass().add(type);
            validationMessageBox.setVisible(true);
        });
    }
    
    /**
     * Oculta el mensaje de validación
     */
    private void hideValidationMessage() {
        if (validationMessageBox != null) {
            Platform.runLater(() -> validationMessageBox.setVisible(false));
        }
    }
    
    @FXML
    private void handleReservationSelected() {
        // TODO: Implementar lógica cuando se selecciona una reserva
        System.out.println("Reserva seleccionada");
    }
    
    private void refreshDashboard() {
        loadReservations();
        updateDashboardMetrics();
    }
    
    private void refreshSpacesInUse() {
        if (sessionManager == null) {
            return;
        }
        
        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            return;
        }
        
        Task<List<SpaceInUseData>> task = new Task<>() {
            @Override
            protected List<SpaceInUseData> call() throws Exception {
                // Cargar todas las reservaciones
                List<ReservationDTO> allReservations = reservationController.getAllReservations(token);
                LocalDateTime now = LocalDateTime.now();
                
                // Filtrar solo las reservaciones activas (en curso ahora)
                List<ReservationDTO> activeReservations = allReservations.stream()
                    .filter(r -> {
                        String status = r.status();
                        return (status != null && 
                               (status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("CHECKED_IN")))
                            && r.startTime() != null && r.startTime().isBefore(now.plusMinutes(5))
                            && r.endTime() != null && r.endTime().isAfter(now);
                    })
                    .collect(Collectors.toList());
                
                // Agrupar por espacio
                Map<Long, List<ReservationDTO>> reservationsBySpace = activeReservations.stream()
                    .filter(r -> r.spaceId() != null)
                    .collect(Collectors.groupingBy(ReservationDTO::spaceId));
                
                // Cargar información de espacios
                List<SpaceDTO> allSpaces = spaceController.loadSpaces(token);
                Map<Long, SpaceDTO> spacesById = allSpaces.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(SpaceDTO::id, space -> space));
                
                // Crear datos de espacios en uso
                return reservationsBySpace.entrySet().stream()
                    .map(entry -> {
                        Long spaceId = entry.getKey();
                        List<ReservationDTO> reservations = entry.getValue();
                        SpaceDTO space = spacesById.get(spaceId);
                        
                        if (space == null) {
                            return null;
                        }
                        
                        return new SpaceInUseData(space, reservations);
                    })
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(data -> data.space.name()))
                    .collect(Collectors.toList());
            }
        };
        
        task.setOnSucceeded(e -> {
            List<SpaceInUseData> spaces = task.getValue();
            updateSpacesInUseUI(spaces);
        });
        
        task.setOnFailed(e -> {
            System.err.println("Error al cargar espacios en uso: " + e.getSource().getException().getMessage());
            if (spacesInUseCountLabel != null) {
                spacesInUseCountLabel.setText("Error al cargar espacios");
            }
        });
        
        Thread thread = new Thread(task, "spaces-in-use-loader");
        thread.setDaemon(true);
        thread.start();
    }
    
    private void updateSpacesInUseUI(List<SpaceInUseData> spaces) {
        if (spacesInUsePane != null) {
            spacesInUsePane.getChildren().clear();
            
            for (SpaceInUseData spaceData : spaces) {
                VBox card = createSpaceCard(spaceData);
                spacesInUsePane.getChildren().add(card);
            }
        }
        
        if (spacesInUseCountLabel != null) {
            int count = spaces.size();
            spacesInUseCountLabel.setText(count + (count == 1 ? " espacio en uso" : " espacios en uso"));
        }
        
        if (spacesInUseTimeLabel != null) {
            spacesInUseTimeLabel.setText("Actualizado ahora");
        }
    }
    
    private VBox createSpaceCard(SpaceInUseData data) {
        VBox card = new VBox(12);
        card.getStyleClass().add("space-card");
        card.setPadding(new Insets(20));
        card.setMaxWidth(280);
        card.setMinWidth(280);
        
        // Header con icono y nombre del espacio
        HBox header = new HBox(8);
        header.setStyle("-fx-alignment: center-left;");
        
        Label icon = new Label("🏟️");
        icon.setStyle("-fx-font-size: 24px;");
        
        VBox nameBox = new VBox(2);
        Label name = new Label(data.space.name());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        name.setWrapText(true);
        
        if (data.space.location() != null && !data.space.location().isBlank()) {
            Label location = new Label(data.space.location());
            location.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
            location.setWrapText(true);
            nameBox.getChildren().add(location);
        }
        
        nameBox.getChildren().add(0, name);
        header.getChildren().addAll(icon, nameBox);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        
        // Información de actividades
        int activityCount = data.reservations.size();
        Label activityLabel = new Label(activityCount + (activityCount == 1 ? " actividad en curso" : " actividades en curso"));
        activityLabel.setStyle("-fx-text-fill: #059669; -fx-font-weight: 600;");
        
        // Capacidad
        if (data.space.capacity() != null) {
            int totalAttendees = data.reservations.stream()
                .mapToInt(r -> r.attendees() != null ? r.attendees() : 0)
                .sum();
            
            Label capacityLabel = new Label("Capacidad: " + totalAttendees + " / " + data.space.capacity());
            capacityLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
            card.getChildren().add(capacityLabel);
        }
        
        card.getChildren().addAll(header, activityLabel);
        
        // Agregar cursor pointer para indicar que es clickeable
        card.setStyle(card.getStyle() + "-fx-cursor: hand;");
        
        return card;
    }
    
    private void updateDashboardMetrics() {
        if (sessionManager == null) {
            return;
        }
        
        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            return;
        }
        
        Task<DashboardMetrics> task = new Task<>() {
            @Override
            protected DashboardMetrics call() throws Exception {
                List<ReservationDTO> allReservations = reservationController.getAllReservations(token);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
                LocalDateTime todayEnd = todayStart.plusDays(1);
                
                // Eventos activos (en curso ahora)
                long activeEvents = allReservations.stream()
                    .filter(r -> {
                        String status = r.status();
                        return (status != null && 
                               (status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("CHECKED_IN")))
                            && r.startTime() != null && r.startTime().isBefore(now.plusMinutes(5))
                            && r.endTime() != null && r.endTime().isAfter(now);
                    })
                    .count();
                
                // Check-ins de hoy
                long todayCheckIns = allReservations.stream()
                    .filter(r -> {
                        String status = r.status();
                        return status != null && status.equalsIgnoreCase("CHECKED_IN")
                            && r.startTime() != null 
                            && r.startTime().isAfter(todayStart) 
                            && r.startTime().isBefore(todayEnd);
                    })
                    .count();
                
                // Reservaciones pendientes (confirmadas pero no checked in)
                long pendingReservations = allReservations.stream()
                    .filter(r -> {
                        String status = r.status();
                        return status != null && status.equalsIgnoreCase("CONFIRMED");
                    })
                    .count();
                
                // Obtener eventos en progreso
                List<ReservationDTO> inProgressList = allReservations.stream()
                    .filter(r -> {
                        String status = r.status();
                        return (status != null && 
                               (status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("CHECKED_IN")))
                            && r.startTime() != null && r.startTime().isBefore(now.plusMinutes(5))
                            && r.endTime() != null && r.endTime().isAfter(now);
                    })
                    .limit(6)  // Limitar a 6 eventos
                    .collect(Collectors.toList());
                
                // Cargar espacios
                List<SpaceDTO> spaces = spaceController.loadSpaces(token);
                Map<Long, SpaceDTO> spacesById = spaces.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(SpaceDTO::id, space -> space));
                
                return new DashboardMetrics(activeEvents, todayCheckIns, pendingReservations, inProgressList, spacesById);
            }
        };
        
        task.setOnSucceeded(e -> {
            DashboardMetrics metrics = task.getValue();
            updateDashboardUI(metrics);
        });
        
        task.setOnFailed(e -> {
            System.err.println("Error al cargar métricas del dashboard: " + e.getSource().getException().getMessage());
        });
        
        Thread thread = new Thread(task, "dashboard-metrics-loader");
        thread.setDaemon(true);
        thread.start();
    }
    
    private void updateDashboardUI(DashboardMetrics metrics) {
        if (activeEventsLabel != null) {
            activeEventsLabel.setText(String.valueOf(metrics.activeEvents));
        }
        
        if (todayCheckInsLabel != null) {
            todayCheckInsLabel.setText(String.valueOf(metrics.todayCheckIns));
        }
        
        if (pendingReservationsLabel != null) {
            pendingReservationsLabel.setText(String.valueOf(metrics.pendingReservations));
        }
        
        if (inProgressEventsPane != null) {
            inProgressEventsPane.getChildren().clear();
            
            for (ReservationDTO reservation : metrics.inProgressEvents) {
                VBox card = createEventCard(reservation, metrics.spacesById);
                inProgressEventsPane.getChildren().add(card);
            }
        }
    }
    
    private VBox createEventCard(ReservationDTO reservation, Map<Long, SpaceDTO> spacesById) {
        VBox card = new VBox(10);
        card.getStyleClass().add("event-card");
        card.setPadding(new Insets(16));
        card.setMaxWidth(240);
        card.setMinWidth(240);
        
        // Nombre del espacio
        SpaceDTO space = reservation.spaceId() != null ? spacesById.get(reservation.spaceId()) : null;
        String spaceName = space != null && space.name() != null ? space.name() : "Espacio sin nombre";
        
        Label nameLabel = new Label(spaceName);
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");
        nameLabel.setWrapText(true);
        
        // Horario
        String timeRange = "";
        if (reservation.startTime() != null && reservation.endTime() != null) {
            timeRange = reservation.startTime().format(TIME_FORMAT) + " - " + 
                       reservation.endTime().format(TIME_FORMAT);
        }
        
        Label timeLabel = new Label("🕐 " + timeRange);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        // Asistentes
        String attendeesText = "";
        if (reservation.attendees() != null) {
            attendeesText = "👥 " + reservation.attendees() + " asistentes";
        }
        
        Label attendeesLabel = new Label(attendeesText);
        attendeesLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        
        // Estado
        String status = reservation.status() != null ? reservation.status() : "";
        String statusText = status.equalsIgnoreCase("CHECKED_IN") ? "✓ En curso" : "⏱ Confirmada";
        String statusColor = status.equalsIgnoreCase("CHECKED_IN") ? "#059669" : "#f59e0b";
        
        Label statusLabel = new Label(statusText);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: " + statusColor + ";");
        
        card.getChildren().addAll(nameLabel, timeLabel, attendeesLabel, statusLabel);
        
        return card;
    }
    
    // Clases auxiliares para almacenar datos
    private static class SpaceInUseData {
        final SpaceDTO space;
        final List<ReservationDTO> reservations;
        
        SpaceInUseData(SpaceDTO space, List<ReservationDTO> reservations) {
            this.space = space;
            this.reservations = reservations;
        }
    }
    
    private static class DashboardMetrics {
        final long activeEvents;
        final long todayCheckIns;
        final long pendingReservations;
        final List<ReservationDTO> inProgressEvents;
        final Map<Long, SpaceDTO> spacesById;
        
        DashboardMetrics(long activeEvents, long todayCheckIns, long pendingReservations, 
                        List<ReservationDTO> inProgressEvents, Map<Long, SpaceDTO> spacesById) {
            this.activeEvents = activeEvents;
            this.todayCheckIns = todayCheckIns;
            this.pendingReservations = pendingReservations;
            this.inProgressEvents = inProgressEvents;
            this.spacesById = spacesById;
        }
    }
    
    private void showSection(VBox section) {
        // Detener escaneo QR si está activo al cambiar de sección
        if (section != scanQRSection && isScanningQR) {
            stopQRScanning();
        }
        
        if (dashboardSection != null) dashboardSection.setVisible(false);
        if (scanQRSection != null) scanQRSection.setVisible(false);
        if (controlSection != null) controlSection.setVisible(false);
        
        if (section != null) {
            section.setVisible(true);
            
            // Limpiar mensaje de validación al mostrar scan QR section
            if (section == scanQRSection) {
                hideValidationMessage();
            }
        }
    }
    
    private void updateActiveNavButton(HBox activeButton) {
        // Remover clase activa de todos los botones
        if (navDashboardButton != null) navDashboardButton.getStyleClass().remove("active");
        if (navScanQRButton != null) navScanQRButton.getStyleClass().remove("active");
        if (navControlButton != null) navControlButton.getStyleClass().remove("active");
        
        // Agregar clase activa al botón seleccionado
        if (activeButton != null && !activeButton.getStyleClass().contains("active")) {
            activeButton.getStyleClass().add("active");
        }
    }

    @FXML
    private void limpiarFiltro() {
        if (txtBuscar != null) {
            txtBuscar.clear();
        }
    }

    @FXML
    private void recargarReservas() {
        loadReservations();
    }

    @FXML
    private void iniciarValidacionQr() {
        ReservationCard selected = lvReservas != null ? lvReservas.getSelectionModel().getSelectedItem() : null;
        if (selected == null) {
            return;
        }

        CheckInDialog dialog = new CheckInDialog(selected);
        configureDialog(dialog);
        Optional<AttendeeRecord> result = dialog.showAndWait();
        result.ifPresent(attendee -> processCheckIn(selected, attendee));
    }

    private void processCheckIn(ReservationCard selected, AttendeeRecord attendee) {
        if (sessionManager == null) {
            showError("No se puede validar el QR sin una sesión activa.", null);
            return;
        }
        if (selected.id() == null) {
            showError("La reserva seleccionada no tiene un identificador válido.", null);
            return;
        }
        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            showError("No se encontró el token de autenticación del supervisor.", null);
            return;
        }
        if (btnValidarQr != null) {
            btnValidarQr.setDisable(true);
        }

  ReservationCheckInRequest payload = new ReservationCheckInRequest(
                selected.qrCode(),
                attendee.idNumber(),
                attendee.firstName(),
                attendee.lastName());

        Task<ReservationCard> task = new Task<>() {
            @Override
            protected ReservationCard call() throws Exception {
                ReservationDTO updated = reservationController.markCheckIn(selected.id(), token, payload);
                return mergeReservation(selected, updated);
            }
        };

        task.setOnSucceeded(event -> {
            ReservationCard updated = task.getValue();
            replaceReservationEntry(updated);
            updateDetail(updated);
            showSuccess(String.format(LOCALE_ES_CR,
                     "Ingreso registrado para %s (%s). Total ingresados: %d de %d.",
                    attendee.fullName(),
                    attendee.idNumber(),
                    updated.attendeeRecords().size(),
                    updated.attendees() != null ? updated.attendees() : 0));
            if (btnValidarQr != null) {
                btnValidarQr.setDisable(false);
            }
        });

        task.setOnFailed(event -> {
            Throwable cause = task.getException();
            showError("No se pudo registrar el ingreso de la reserva.", cause);
            if (btnValidarQr != null) {
                btnValidarQr.setDisable(false);
            }
        });

        Thread thread = new Thread(task, "supervisor-checkin-" + selected.id());
        thread.setDaemon(true);
        thread.start();
    }

    private ReservationCard mergeReservation(ReservationCard existing, ReservationDTO dto) {
        return new ReservationCard(
                dto.id(),
                existing.spaceName(),
                existing.location(),
                dto.startTime(),
                dto.endTime(),
                dto.notes(),
                dto.attendees(),
                dto.status(),
                dto.createdAt(),
                dto.updatedAt(),
                dto.qrCode(),
               dto.checkinAt(),
                mapAttendees(dto));
    }

    private void replaceReservationEntry(ReservationCard updated) {
        for (int i = 0; i < reservationEntries.size(); i++) {
            ReservationCard current = reservationEntries.get(i);
            if (Objects.equals(current.id(), updated.id())) {
                reservationEntries.set(i, updated);
                if (lvReservas != null) {
                    lvReservas.getSelectionModel().select(updated);
                }
                return;
            }
        }
    }

    private void configureListView() {
        if (lvReservas == null) {
            return;
        }

        filteredReservations = new FilteredList<>(reservationEntries, entry -> true);
        SortedList<ReservationCard> sortedList = new SortedList<>(filteredReservations);
        sortedList.setComparator(Comparator.comparing(ReservationCard::startTime,
                Comparator.nullsLast(LocalDateTime::compareTo)).reversed());

        lvReservas.setItems(sortedList);
        lvReservas.setCellFactory(listView -> new ReservationCardCell());
        lvReservas.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> updateDetail(newSelection));
    }

    private void configureFiltering() {
        if (txtBuscar == null) {
            return;
        }
        txtBuscar.textProperty().addListener((obs, oldValue, newValue) -> {
            String query = newValue == null ? "" : newValue.trim().toLowerCase(LOCALE_ES_CR);
            if (filteredReservations != null) {
                filteredReservations.setPredicate(entry -> matchesQuery(entry, query));
            }
            updateEmptyStateMessage();
        });
    }

    private void configureButtons() {
        if (btnLimpiarFiltro != null && txtBuscar != null) {
            btnLimpiarFiltro.disableProperty().bind(Bindings.createBooleanBinding(
                    () -> txtBuscar.getText() == null || txtBuscar.getText().isBlank(),
                    txtBuscar.textProperty()));
        }
        if (btnRefrescar != null) {
            btnRefrescar.disableProperty().bind(loadingProperty);
        }
    }

    private void configureEmptyState() {
        if (lblEmptyState == null) {
            return;
        }
        lblEmptyState.managedProperty().bind(lblEmptyState.visibleProperty());
        reservationEntries.addListener((ListChangeListener<? super ReservationCard>) change -> updateEmptyStateMessage());
        if (filteredReservations != null) {
            filteredReservations.addListener((ListChangeListener<? super ReservationCard>) change -> updateEmptyStateMessage());
        }
        updateEmptyStateMessage();
    }

    private void updateEmptyStateMessage() {
        if (lblEmptyState == null) {
            return;
        }
        boolean loading = loadingProperty.get();
        boolean empty = filteredReservations == null || filteredReservations.isEmpty();
        boolean hasQuery = txtBuscar != null && txtBuscar.getText() != null && !txtBuscar.getText().isBlank();
        if (loading) {
            lblEmptyState.setText("Cargando reservas confirmadas…");
            lblEmptyState.setVisible(true);
            return;
        }
        if (empty) {
            lblEmptyState.setText(hasQuery
                    ? "No se encontraron reservas que coincidan con el filtro."
                    : "Todavía no hay reservas confirmadas disponibles.");
            lblEmptyState.setVisible(true);
        } else {
            lblEmptyState.setVisible(false);
        }
    }

    private void loadReservations() {
        if (sessionManager == null || loadingGuard.getAndSet(true)) {
            return;
        }
        loadingProperty.set(true);
        updateEmptyStateMessage();

        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            loadingGuard.set(false);
            loadingProperty.set(false);
            updateEmptyStateMessage();
            showError("No se encontró el token de autenticación para cargar las reservas.", null);
            return;
        }

        Long selectedId = lvReservas != null && lvReservas.getSelectionModel().getSelectedItem() != null
                ? lvReservas.getSelectionModel().getSelectedItem().id()
                : null;

        Task<List<ReservationCard>> task = new Task<>() {
            @Override
            protected List<ReservationCard> call() throws Exception {
                List<SpaceDTO> spaces;
                try {
                    spaces = spaceController.loadSpaces(token);
                } catch (RuntimeException exception) {
                    spaces = List.of();
                }
                Map<Long, SpaceDTO> spacesById = spaces.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toMap(SpaceDTO::id, space -> space));

                List<ReservationDTO> reservations = reservationController.getAllReservations(token);
                return reservations.stream()
                        .filter(dto -> isAcceptedStatus(dto.status()))
                        .map(dto -> mapToCard(dto, spacesById))
                        .sorted(Comparator.comparing(ReservationCard::startTime,
                                Comparator.nullsLast(LocalDateTime::compareTo)).reversed())
                        .collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(event -> {
            loadingGuard.set(false);
            loadingProperty.set(false);
            List<ReservationCard> result = task.getValue();
            reservationEntries.setAll(result);
            updateEmptyStateMessage();
            if (lvReservas != null) {
                if (selectedId != null) {
                    result.stream()
                            .filter(card -> Objects.equals(card.id(), selectedId))
                            .findFirst()
                            .ifPresentOrElse(card -> lvReservas.getSelectionModel().select(card),
                                    () -> lvReservas.getSelectionModel().selectFirst());
                } else if (!result.isEmpty()) {
                    lvReservas.getSelectionModel().selectFirst();
                } else {
                    resetDetail();
                }
            }
        });

        task.setOnFailed(event -> {
            loadingGuard.set(false);
            loadingProperty.set(false);
            reservationEntries.clear();
            updateEmptyStateMessage();
            resetDetail();
            Throwable cause = task.getException();
            showError("No se pudieron cargar las reservas confirmadas.", cause);
        });

        Thread loader = new Thread(task, "supervisor-reservations-v2");
        loader.setDaemon(true);
        loader.start();
    }

    private ReservationCard mapToCard(ReservationDTO dto, Map<Long, SpaceDTO> spacesById) {
        SpaceDTO space = dto.spaceId() != null ? spacesById.get(dto.spaceId()) : null;
        String spaceName = space != null && space.name() != null && !space.name().isBlank()
                ? space.name()
                : dto.id() != null ? "Reserva #" + dto.id() : "Espacio sin nombre";
        String location = space != null && space.location() != null && !space.location().isBlank()
                ? space.location()
                : "Ubicación no registrada";

        return new ReservationCard(
                dto.id(),
                spaceName,
                location,
                dto.startTime(),
                dto.endTime(),
                dto.notes(),
                dto.attendees(),
                dto.status(),
                dto.createdAt(),
                dto.updatedAt(),
                dto.qrCode(),
              dto.checkinAt(),
                mapAttendees(dto));
    }

    private List<AttendeeRecord> mapAttendees(ReservationDTO dto) {
        if (dto == null || dto.attendeeRecords() == null) {
            return List.of();
        }
        return dto.attendeeRecords().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ReservationAttendeeDTO::checkInAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)))
                .map(this::toAttendeeRecord)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private AttendeeRecord toAttendeeRecord(ReservationAttendeeDTO attendee) {
        if (attendee == null) {
            return null;
        }
        return new AttendeeRecord(
                attendee.idNumber(),
                attendee.firstName(),
                attendee.lastName(),
                attendee.checkInAt());
    }

    private boolean matchesQuery(ReservationCard entry, String query) {
        if (query.isBlank()) {
            return true;
        }
        String space = entry.spaceName() != null ? entry.spaceName().toLowerCase(LOCALE_ES_CR) : "";
        String location = entry.location() != null ? entry.location().toLowerCase(LOCALE_ES_CR) : "";
        return space.contains(query) || location.contains(query);
    }

    private boolean isAcceptedStatus(String status) {
        if (status == null) {
            return false;
        }
        return ACCEPTED_STATUSES.contains(status.toUpperCase(LOCALE_ES_CR));
    }

    private void updateDetail(ReservationCard card) {
        if (card == null) {
            resetDetail();
            return;
        }
        if (lblDetalleTitulo != null) {
            lblDetalleTitulo.setText(card.spaceName());
        }
        if (lblDetalleUbicacion != null) {
            lblDetalleUbicacion.setText("Ubicación: " + card.location());
        }
        if (lblDetalleHorario != null) {
            lblDetalleHorario.setText("Horario: " + card.scheduleSummary());
        }
        if (lblDetalleEstado != null) {
            lblDetalleEstado.setText(card.statusLabel());
        }
        if (lblDetalleNotas != null) {
            lblDetalleNotas.setText(card.notes() == null || card.notes().isBlank()
                    ? "Notas: Sin notas adicionales"
                    : "Notas: " + card.notes());
        }
        if (lblDetalleAsistentes != null) {
            lblDetalleAsistentes.setText(card.capacityLabel());
        }
        if (lblDetalleIngresos != null) {
           lblDetalleIngresos.setText(buildAttendeeSummary(card));
        }
        if (lblDetalleCheckIn != null) {
            lblDetalleCheckIn.setText(card.checkInSummary());
        }
        if (lblDetalleCreada != null) {
            lblDetalleCreada.setText("Creada: " + formatDate(card.createdAt()));
        }
        if (lblDetalleActualizada != null) {
            lblDetalleActualizada.setText("Actualizada: " + formatDate(card.updatedAt()));
        }
        if (btnValidarQr != null) {
        boolean hasQr = card.qrCode() != null && !card.qrCode().isBlank();
            btnValidarQr.setDisable(!hasQr || !card.hasRemainingCapacity());   
        }
    }

    private void resetDetail() {
        if (lblDetalleTitulo != null) {
            lblDetalleTitulo.setText("Selecciona una reserva confirmada");
        }
        if (lblDetalleUbicacion != null) {
            lblDetalleUbicacion.setText("Ubicación: -");
        }
        if (lblDetalleHorario != null) {
            lblDetalleHorario.setText("Horario: -");
        }
        if (lblDetalleEstado != null) {
            lblDetalleEstado.setText("Estado: -");
        }
        if (lblDetalleNotas != null) {
            lblDetalleNotas.setText("Notas: -");
        }
        if (lblDetalleAsistentes != null) {
            lblDetalleAsistentes.setText("Capacidad confirmada: -");
        }
        if (lblDetalleIngresos != null) {
         lblDetalleIngresos.setText("Ingresos registrados: -");   
        }
        if (lblDetalleCheckIn != null) {
            lblDetalleCheckIn.setText("Último check-in registrado en el sistema: -");
        }
        if (lblDetalleCreada != null) {
            lblDetalleCreada.setText("Creada: -");
        }
        if (lblDetalleActualizada != null) {
            lblDetalleActualizada.setText("Actualizada: -");
        }
        if (btnValidarQr != null) {
            btnValidarQr.setDisable(true);
        }
    }

     private String buildAttendeeSummary(ReservationCard card) {
        if (card == null) {
            return "Ingresos registrados: -";
        }
        List<AttendeeRecord> attendees = card.attendeeRecords() != null ? card.attendeeRecords() : List.of();
        int totalAllowed = card.attendees() != null ? card.attendees() : 0;
        if (attendees.isEmpty()) {
            return String.format(LOCALE_ES_CR,
                    "Ingresos registrados: 0 de %d personas. Sin registros previos.",
                    totalAllowed);
        }
        AttendeeRecord last = attendees.get(attendees.size() - 1);
        StringBuilder builder = new StringBuilder();
        builder.append(String.format(LOCALE_ES_CR,
                "Ingresos registrados: %d de %d personas.",
                attendees.size(),
                totalAllowed));
        builder.append(System.lineSeparator());
        builder.append(String.format(LOCALE_ES_CR,
                "Último ingreso: %s (%s) a las %s.",
                last.fullName(),
                last.idNumber(),
                formatTime(last.timestamp())));
        builder.append(System.lineSeparator());
        builder.append("Listado secuencial:");
        for (int i = 0; i < attendees.size(); i++) {
            AttendeeRecord record = attendees.get(i);
            builder.append(System.lineSeparator())
                    .append(i + 1)
                    .append(". ")
                    .append(record.fullName())
                    .append(" (")
                    .append(record.idNumber())
                    .append(") · ")
                    .append(formatTime(record.timestamp()));
        }
        return builder.toString();
    }

    private void updateSupervisorLabels() {
        if (sessionManager == null) {
            return;
        }
        String displayName = sessionManager.getUserDisplayName();
        String email = sessionManager.getUserEmail();
        
        String finalName = displayName == null || displayName.isBlank() ? "Supervisor" : displayName;
        String finalEmail = email == null || email.isBlank() ? "supervisor@municipalidad.go.cr" : email;
        
        // Labels antiguos
        if (lblSupervisorName != null) {
            lblSupervisorName.setText(finalName);
        }
        if (lblSupervisorEmail != null) {
            lblSupervisorEmail.setText(finalEmail);
        }
        
        // Labels nuevos
        if (supervisorNameLabel != null) {
            supervisorNameLabel.setText(finalName);
        }
        if (supervisorEmailLabel != null) {
            supervisorEmailLabel.setText(finalEmail);
        }
        
        // Labels del panel de perfil
        if (profileNameLabel != null) {
            profileNameLabel.setText(finalName);
        }
        if (profileEmailLabel != null) {
            profileEmailLabel.setText(finalEmail);
        }
    }

    private void showError(String message, Throwable cause) {
        if (cause != null && cause.getMessage() != null) {
            showAPIError(message, cause);
        } else {
            showStyledAlert("❌ Error", message, Alert.AlertType.ERROR);
        }
    }

    private void showSuccess(String message) {
        showStyledAlert("✅ Éxito - Ingreso Registrado", message, Alert.AlertType.INFORMATION);
    }
    
    private void showWarning(String message) {
        showStyledAlert("⚠️ Advertencia", message, Alert.AlertType.WARNING);
    }
    
    /**
     * Muestra una alerta estilizada con CSS personalizado
     */
    private void showStyledAlert(String title, String message, Alert.AlertType type) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            configureDialog(alert);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            
            // Aplicar estilos CSS personalizados
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                getClass().getResource("/com/municipal/reservationsfx/styles/supervisor-dashboard.css").toExternalForm()
            );
            
            // Aplicar clases CSS según el tipo
            dialogPane.getStyleClass().add("custom-alert");
            switch (type) {
                case INFORMATION -> dialogPane.getStyleClass().add("alert-success");
                case ERROR -> dialogPane.getStyleClass().add("alert-error");
                case WARNING -> dialogPane.getStyleClass().add("alert-warning");
                case CONFIRMATION -> dialogPane.getStyleClass().add("alert-info");
            }
            
            // Mejorar el contenido con formato
            Label contentLabel = new Label(message);
            contentLabel.setWrapText(true);
            contentLabel.setMaxWidth(450);
            contentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-line-spacing: 4px;");
            dialogPane.setContent(contentLabel);
            
            // Personalizar botones
            alert.getButtonTypes().forEach(buttonType -> {
                ButtonBar.ButtonData buttonData = buttonType.getButtonData();
                if (buttonData == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    dialogPane.lookupButton(buttonType).getStyleClass().add("cancel-button");
                }
            });
            
            alert.showAndWait();
        });
    }
    
    /**
     * Muestra un error de API con detalles técnicos
     */
    private void showAPIError(String operation, Throwable error) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            configureDialog(alert);
            alert.setTitle("❌ Error de API");
            alert.setHeaderText("Error en: " + operation);
            
            // Crear contenedor de error
            VBox errorContainer = new VBox(12);
            errorContainer.getStyleClass().add("api-error-container");
            errorContainer.setMaxWidth(500);
            
            // Título del error
            Label errorTitle = new Label("Detalles del Error:");
            errorTitle.getStyleClass().add("api-error-title");
            
            // Mensaje de error
            String errorMessage = error != null ? error.getMessage() : "Error desconocido";
            Label errorLabel = new Label(errorMessage);
            errorLabel.setWrapText(true);
            errorLabel.getStyleClass().add("api-error-message");
            
            // Código de error (si existe)
            String errorCode = extractErrorCode(error);
            if (errorCode != null) {
                Label codeLabel = new Label("Código: " + errorCode);
                codeLabel.getStyleClass().add("api-error-code");
                errorContainer.getChildren().addAll(errorTitle, errorLabel, codeLabel);
            } else {
                errorContainer.getChildren().addAll(errorTitle, errorLabel);
            }
            
            // Aplicar estilos
            DialogPane dialogPane = alert.getDialogPane();
            dialogPane.getStylesheets().add(
                getClass().getResource("/com/municipal/reservationsfx/styles/supervisor-dashboard.css").toExternalForm()
            );
            dialogPane.getStyleClass().addAll("custom-alert", "alert-error");
            dialogPane.setContent(errorContainer);
            
            alert.showAndWait();
        });
    }
    
    /**
     * Extrae el código de error HTTP de una excepción
     */
    private String extractErrorCode(Throwable error) {
        if (error == null) return null;
        String message = error.getMessage();
        if (message != null) {
            if (message.contains("401")) return "401 - No autorizado";
            if (message.contains("403")) return "403 - Acceso denegado";
            if (message.contains("404")) return "404 - No encontrado";
            if (message.contains("500")) return "500 - Error del servidor";
            if (message.contains("503")) return "503 - Servicio no disponible";
        }
        return null;
    }

    private void configureDialog(Dialog<?> dialog) {
        Window owner = getWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }
    }

    private Window getWindow() {
        if (mainContainer != null && mainContainer.getScene() != null) {
            return mainContainer.getScene().getWindow();
        }
        return null;
    }

    private String formatDate(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "Sin registro";
        }
        return FULL_DATE_FORMAT.format(timestamp);
    }

    private String formatTime(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "--";
        }
        return TIME_FORMAT.format(timestamp);
    }

    private static String formatDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        String dayName = dateTime.getDayOfWeek().getDisplayName(TextStyle.FULL, LOCALE_ES_CR);
        return dayName.substring(0, 1).toUpperCase(LOCALE_ES_CR) + dayName.substring(1);
    }

    private static final class ReservationCardCell extends ListCell<ReservationCard> {
        private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

        private final VBox container = new VBox();
        private final HBox header = new HBox();
        private final Label day = new Label();
        private final Region spacer = new Region();
        private final Label status = new Label();
        private final Label title = new Label();
        private final Label schedule = new Label();
        private final Label location = new Label();

        private ReservationCardCell() {
            container.getStyleClass().add("reservation-card");
            header.getStyleClass().add("reservation-card__header");
            day.getStyleClass().add("reservation-card__day");
            status.getStyleClass().add("reservation-card__status");
            title.getStyleClass().add("reservation-card__title");
            schedule.getStyleClass().add("reservation-card__schedule");
            location.getStyleClass().add("reservation-card__location");

            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(day, spacer, status);
            container.getChildren().addAll(header, title, schedule, location);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(ReservationCard item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                container.pseudoClassStateChanged(SELECTED, false);
            } else {
                day.setText(formatDay(item.startTime()));
                status.setText(item.statusLabel());
                title.setText(item.spaceName());
                schedule.setText(item.scheduleSummary());
                location.setText(item.location());
                setGraphic(container);
                container.pseudoClassStateChanged(SELECTED, isSelected());
            }
        }

        @Override
        public void updateSelected(boolean selected) {
            super.updateSelected(selected);
            container.pseudoClassStateChanged(SELECTED, selected);
        }
    }

    private record ReservationCard(
            Long id,
            String spaceName,
            String location,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String notes,
            Integer attendees,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String qrCode,
           LocalDateTime checkinAt,
            List<AttendeeRecord> attendeeRecords) {

        private String scheduleSummary() {
            if (startTime == null && endTime == null) {
                return "Sin horario registrado";
            }
            if (startTime != null && endTime != null) {
                return FULL_DATE_FORMAT.format(startTime) + " · " + TIME_FORMAT.format(endTime);
            }
            if (startTime != null) {
                return FULL_DATE_FORMAT.format(startTime);
            }
            return "Finaliza " + FULL_DATE_FORMAT.format(endTime);
        }

        private String statusLabel() {
            if (status == null || status.isBlank()) {
                return "Sin estado";
            }
            return switch (status.toUpperCase(LOCALE_ES_CR)) {
                case "CONFIRMED" -> "Confirmada";
                case "CHECKED_IN" -> "Ingreso registrado";
                case "PENDING" -> "Pendiente";
                case "REJECTED" -> "Rechazada";
                case "CANCELLED", "CANCELED" -> "Cancelada";
                case "NO_SHOW" -> "No asistió";
                default -> status.substring(0, 1).toUpperCase(LOCALE_ES_CR)
                        + status.substring(1).toLowerCase(LOCALE_ES_CR);
            };
        }

        private String capacityLabel() {
            return "Capacidad confirmada: " + (attendees != null ? attendees : 0) + " personas";
        }

        private String checkInSummary() {
            if (checkinAt == null) {
                return "Último check-in registrado en el sistema: Sin registro";
            }
            return "Último check-in registrado en el sistema: " + FULL_DATE_FORMAT.format(checkinAt);
        }

        private int attendeeCount() {
            return attendeeRecords == null ? 0 : attendeeRecords.size();
        }

        private boolean hasRemainingCapacity() {
            if (attendees == null) {
                return true;
            }
            if (attendees <= 0) {
                return false;
            }
            return attendeeCount() < attendees;
        }
    }

    private record AttendeeRecord(String idNumber, String firstName, String lastName, LocalDateTime timestamp) {
        private String fullName() {
            return (firstName + " " + lastName).trim();
        }
    }

    private final class CheckInDialog extends Dialog<AttendeeRecord> {

        private final TextField qrField = new TextField();
        private final TextField idField = new TextField();
        private final TextField firstNameField = new TextField();
        private final TextField lastNameField = new TextField();
        private final Label errorLabel = new Label();

        private final ReservationCard reservation;

        private CheckInDialog(ReservationCard reservation) {
            this.reservation = reservation;
            setTitle("Validación de QR");
            setHeaderText("Reserva #" + reservation.id() + " · " + reservation.spaceName());

            getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
            Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
            okButton.setText("Registrar ingreso");
            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                if (!validateInputs()) {
                    event.consume();
                }
            });
            okButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
                        !reservation.hasRemainingCapacity()
                                    ||
                            isBlank(qrField.getText())
                                    || isBlank(idField.getText())
                                    || isBlank(firstNameField.getText())
                                    || isBlank(lastNameField.getText()),
                    qrField.textProperty(),
                    idField.textProperty(),
                    firstNameField.textProperty(),
                    lastNameField.textProperty()));

            GridPane content = new GridPane();
            content.setHgap(12);
            content.setVgap(10);
            content.setPadding(new Insets(10, 0, 0, 0));

            Label qrLabel = new Label("Código QR");
            Label idLabel = new Label("Número de cédula");
            Label firstNameLabel = new Label("Nombre");
            Label lastNameLabel = new Label("Apellido");

            qrField.setPromptText("Escanee o escriba el código QR");
            idField.setPromptText("Ej: 1-2345-6789");
            firstNameField.setPromptText("Nombre del asistente");
            lastNameField.setPromptText("Apellido del asistente");

            errorLabel.getStyleClass().add("validation-error");
            errorLabel.setVisible(false);

            content.addRow(0, qrLabel, qrField);
            content.addRow(1, idLabel, idField);
            content.addRow(2, firstNameLabel, firstNameField);
            content.addRow(3, lastNameLabel, lastNameField);
            GridPane.setColumnSpan(errorLabel, 2);
            content.add(errorLabel, 0, 4);

            getDialogPane().setContent(content);

            qrField.textProperty().addListener((obs, oldValue, newValue) -> clearError());
            idField.textProperty().addListener((obs, oldValue, newValue) -> clearError());
            firstNameField.textProperty().addListener((obs, oldValue, newValue) -> clearError());
            lastNameField.textProperty().addListener((obs, oldValue, newValue) -> clearError());

            URL stylesheetUrl = getClass().getResource("/com/municipal/reservationsfx/styles/supervisor-dashboard_v2.css");
            if (stylesheetUrl != null) {
                String stylesheet = stylesheetUrl.toExternalForm();
                if (!getDialogPane().getStylesheets().contains(stylesheet)) {
                    getDialogPane().getStylesheets().add(stylesheet);
                }
            }

            setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    return new AttendeeRecord(
                            idField.getText().trim(),
                            firstNameField.getText().trim(),
                            lastNameField.getText().trim(),
                            LocalDateTime.now());
                }
                return null;
            });

            if (!reservation.hasRemainingCapacity()) {
                showValidationError("La reserva ya alcanzó el máximo de asistentes registrados.");
            }
        }

        private boolean validateInputs() {
            if (reservation.qrCode() == null || reservation.qrCode().isBlank()) {
                showValidationError("La reserva no tiene un código QR asignado.");
                return false;
            }
            String expectedQr = reservation.qrCode().trim();
            String providedQr = qrField.getText() != null ? qrField.getText().trim() : "";
            if (!expectedQr.equalsIgnoreCase(providedQr)) {
                showValidationError("El código QR no coincide con la reserva seleccionada.");
                return false;
            }
             if (!reservation.hasRemainingCapacity()) {
                showValidationError("La reserva ya alcanzó el máximo de asistentes registrados.");
                return false;
            }
            return true;
        }
        

        private void showValidationError(String message) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }

        private void clearError() {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        }

        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
    
    // ============================
    // ATTENDEE COUNTER METHODS
    // ============================
    
    /**
     * Muestra la sección del contador de asistentes
     */
    private void showAttendeeCounter(ReservationDTO reservation, String userName, String spaceName) {
        if (counterReservationUser != null) {
            counterReservationUser.setText("Usuario: " + userName);
        }
        if (counterReservationSpace != null) {
            counterReservationSpace.setText("Espacio: " + spaceName);
        }
        if (counterReservationTime != null) {
            counterReservationTime.setText(String.format("Horario: %s - %s",
                    reservation.startTime().format(TIME_FORMAT),
                    reservation.endTime().format(TIME_FORMAT)));
        }
        updateCounterDisplay();
        if (attendeeLimit != null) {
            attendeeLimit.setText("Máximo: " + maxAttendees + " personas");
        }
        showSection(attendeeCounterSection);
    }
    
    @FXML
    private void handleIncrementAttendee() {
        if (currentAttendeeCount < maxAttendees) {
            currentAttendeeCount++;
            updateCounterDisplay();
            playCounterAnimation(incrementButton);
        }
    }
    
    @FXML
    private void handleDecrementAttendee() {
        if (currentAttendeeCount > 0) {
            currentAttendeeCount--;
            updateCounterDisplay();
            playCounterAnimation(decrementButton);
        }
    }
    
    @FXML
    private void handleBackToScan() {
        currentAttendeeCount = 0;
        currentReservation = null;
        showScanQR();
    }
    
    @FXML
    private void handleFinishCount() {
        if (currentReservation == null) return;
        int finalCount = currentAttendeeCount;
        currentAttendeeCount = 0;
        currentReservation = null;
        showValidationMessage("✅ Control finalizado: " + finalCount + " asistentes", "success");
        Platform.runLater(() -> {
            try { Thread.sleep(2000); showScanQR(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
    }
    
    private void updateCounterDisplay() {
        if (attendeeCountDisplay != null) {
            attendeeCountDisplay.setText(String.valueOf(currentAttendeeCount));
        }
    }
    
    private void playCounterAnimation(Button button) {
        if (button == null) return;
        ScaleTransition scaleUp = new ScaleTransition(javafx.util.Duration.millis(100), button);
        scaleUp.setToX(1.15);
        scaleUp.setToY(1.15);
        ScaleTransition scaleDown = new ScaleTransition(javafx.util.Duration.millis(100), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleUp.setOnFinished(e -> scaleDown.play());
        scaleUp.play();
    }
    
    private String getUserName(Long userId, String token) {
        return userId != null ? "Usuario #" + userId : "Usuario desconocido";
    }
}
