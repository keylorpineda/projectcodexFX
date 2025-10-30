package com.municipal.ui.controllers;

import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowAware;
import com.municipal.ui.navigation.FlowController;
import com.municipal.ui.navigation.SessionAware;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

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

    @FXML private BorderPane rootPane;
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

    private final Map<Long, List<AttendeeRecord>> sessionCheckIns = new HashMap<>();

    private SessionManager sessionManager;
    private FlowController flowController;

    private final AtomicBoolean loadingGuard = new AtomicBoolean(false);
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty(false);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        configureFiltering();
        configureButtons();
        configureEmptyState();
        loadingProperty.addListener((obs, oldValue, newValue) -> updateEmptyStateMessage());
        resetDetail();
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

        Task<ReservationCard> task = new Task<>() {
            @Override
            protected ReservationCard call() throws Exception {
                ReservationDTO updated = reservationController.markCheckIn(selected.id(), token);
                return mergeReservation(selected, updated);
            }
        };

        task.setOnSucceeded(event -> {
            ReservationCard updated = task.getValue();
            replaceReservationEntry(updated);
            registerLocalCheckIn(updated.id(), attendee);
            updateDetail(updated);
            showSuccess(String.format(LOCALE_ES_CR,
                    "Ingreso registrado para %s (%s).",
                    attendee.fullName(),
                    attendee.idNumber()));
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
                dto.checkinAt());
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

    private void registerLocalCheckIn(Long reservationId, AttendeeRecord attendee) {
        sessionCheckIns.computeIfAbsent(reservationId, key -> new ArrayList<>()).add(attendee);
    }

    private int getSessionCheckInCount(Long reservationId) {
        return sessionCheckIns.getOrDefault(reservationId, List.of()).size();
    }

    private AttendeeRecord getLastSessionCheckIn(Long reservationId) {
        List<AttendeeRecord> records = sessionCheckIns.get(reservationId);
        if (records == null || records.isEmpty()) {
            return null;
        }
        return records.get(records.size() - 1);
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
            sessionCheckIns.keySet().retainAll(result.stream()
                    .map(ReservationCard::id)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet()));
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
                dto.checkinAt());
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
            AttendeeRecord last = getLastSessionCheckIn(card.id());
            int count = getSessionCheckInCount(card.id());
            if (last == null) {
                lblDetalleIngresos.setText("Ingresos registrados en esta sesión: Ninguno.");
            } else {
                lblDetalleIngresos.setText(String.format(LOCALE_ES_CR,
                        "Ingresos registrados en esta sesión: %d · Último: %s (%s) a las %s",
                        count,
                        last.fullName(),
                        last.idNumber(),
                        formatTime(last.timestamp())));
            }
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
            btnValidarQr.setDisable(card.qrCode() == null || card.qrCode().isBlank());
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
            lblDetalleIngresos.setText("Ingresos registrados en esta sesión: -");
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

    private void updateSupervisorLabels() {
        if (sessionManager == null) {
            return;
        }
        if (lblSupervisorName != null) {
            String displayName = sessionManager.getUserDisplayName();
            lblSupervisorName.setText(displayName == null || displayName.isBlank() ? "Supervisor" : displayName);
        }
        if (lblSupervisorEmail != null) {
            String email = sessionManager.getUserEmail();
            lblSupervisorEmail.setText(email == null || email.isBlank()
                    ? "supervisor@municipalidad.go.cr"
                    : email);
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
        if (rootPane != null && rootPane.getScene() != null) {
            return rootPane.getScene().getWindow();
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
            LocalDateTime checkinAt) {

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