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
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
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
    @FXML private HBox navSearchButton;
    
    // Secciones
    @FXML private VBox dashboardSection;
    @FXML private VBox scanQRSection;
    @FXML private VBox controlSection;
    @FXML private VBox searchSection;
    
    // Usuario info
    @FXML private Label supervisorNameLabel;
    @FXML private Label supervisorEmailLabel;
    @FXML private HBox userMenuContainer;
    
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configurar navegación de secciones
        initializeSections();
        
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

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        updateSupervisorLabels();
        loadReservations();
    }

    @Override
    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    @FXML
    private void cerrarSesion() {
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
    private void showSearch() {
        showSection(searchSection);
        updateActiveNavButton(navSearchButton);
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
        
        // Verificar si ya tiene check-in
        if ("CHECKED_IN".equals(reservation.status())) {
            showValidationMessage("✅ Esta reserva ya fue registrada anteriormente", "success");
            showReservationInfo(reservation);
            return;
        }
        
        // Verificar ventana temporal
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
        
        // Ventana válida, proceder con el check-in
        performCheckIn(reservation, qrCode, token);
    }
    
    /**
     * Realiza el check-in de la reserva
     */
    private void performCheckIn(ReservationDTO reservation, String qrCode, String token) {
        showValidationMessage("🔄 Registrando ingreso...", "info");
        
        // Crear el payload de check-in
        ReservationCheckInRequest payload = new ReservationCheckInRequest(
            qrCode,
            sessionManager.getUserId() != null ? sessionManager.getUserId().toString() : "supervisor",
            sessionManager.getUserDisplayName() != null ? sessionManager.getUserDisplayName() : "Supervisor",
            ""
        );
        
        Task<ReservationDTO> task = new Task<>() {
            @Override
            protected ReservationDTO call() throws Exception {
                return reservationController.markCheckIn(reservation.id(), token, payload);
            }
        };
        
        task.setOnSucceeded(event -> {
            ReservationDTO updated = task.getValue();
            
            // Obtener información del espacio
            String spaceName = getSpaceName(updated.spaceId(), token);
            
            // Obtener nombre de usuario del session manager
            String userName = sessionManager.getUserDisplayName() != null ? 
                sessionManager.getUserDisplayName() : "Usuario";
            
            showValidationMessage(
                String.format("✅ Check-in exitoso\n\n" +
                    "Usuario: %s\n" +
                    "Espacio: %s\n" +
                    "Horario: %s - %s\n" +
                    "Estado: ASISTIÓ",
                    userName,
                    spaceName,
                    updated.startTime().format(TIME_FORMAT),
                    updated.endTime().format(TIME_FORMAT)
                ),
                "success"
            );
            
            // Limpiar el campo de texto
            if (qrCodeField != null) {
                qrCodeField.clear();
            }
            
            // Recargar reservas si estamos en la vista antigua
            loadReservations();
            
            // Auto-navegar a la sección de Control después de 2 segundos
            Platform.runLater(() -> {
                try {
                    Thread.sleep(2000);
                    showControl();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        
        task.setOnFailed(event -> {
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Error al registrar check-in";
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
    
    @FXML
    private void refreshDashboard() {
        // TODO: Actualizar métricas del dashboard
        System.out.println("Refrescando dashboard...");
    }
    
    @FXML
    private void refreshControl() {
        // TODO: Actualizar control de ingresos
        System.out.println("Refrescando control...");
    }
    
    @FXML
    private void handleSearch() {
        // TODO: Implementar búsqueda
        System.out.println("Buscando...");
    }
    
    @FXML
    private void handleClearSearch() {
        // TODO: Limpiar búsqueda
        System.out.println("Limpiando búsqueda...");
    }
    
    private void showSection(VBox section) {
        // Detener escaneo QR si está activo al cambiar de sección
        if (section != scanQRSection && isScanningQR) {
            stopQRScanning();
        }
        
        if (dashboardSection != null) dashboardSection.setVisible(false);
        if (scanQRSection != null) scanQRSection.setVisible(false);
        if (controlSection != null) controlSection.setVisible(false);
        if (searchSection != null) searchSection.setVisible(false);
        
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
        if (navSearchButton != null) navSearchButton.getStyleClass().remove("active");
        
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
    }

    private void showError(String message, Throwable cause) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            configureDialog(alert);
            alert.setTitle("Error");
            alert.setHeaderText("Ocurrió un problema");
            String detail = cause != null && cause.getMessage() != null
                    ? message + "\n\nDetalle: " + cause.getMessage()
                    : message;
            alert.setContentText(detail);
            alert.showAndWait();
        });
    }

    private void showSuccess(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            configureDialog(alert);
            alert.setTitle("Ingreso registrado");
            alert.setHeaderText("Validación completada");
            alert.setContentText(message);
            alert.showAndWait();
        });
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
}