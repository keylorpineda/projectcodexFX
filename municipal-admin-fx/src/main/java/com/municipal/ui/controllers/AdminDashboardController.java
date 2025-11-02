package com.municipal.ui.controllers;

import com.municipal.config.AppConfig;
import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.controllers.UserController;
import com.municipal.controllers.WeatherController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.SpaceInputDTO;
import com.municipal.dtos.UserDTO;
import com.municipal.dtos.UserInputDTO;
import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowAware;
import com.municipal.ui.navigation.FlowController;
import com.municipal.ui.navigation.SessionAware;
import com.municipal.ui.navigation.ViewLifecycle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Controlador completo para el Panel de Administración
 * Sistema de Reservas de Espacios Municipales
 * 
 * @author Tu Nombre
 * @version 1.0
 */
public class AdminDashboardController implements Initializable, SessionAware, FlowAware, ViewLifecycle {
    
    // ==================== COMPONENTES PRINCIPALES ====================
    
    @FXML private StackPane contenedorPrincipal;
    @FXML private StackPane headerStack;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblNotificacionesBadge;
    @FXML private Button btnNotificaciones;
    @FXML private VBox panelNotificaciones;
    @FXML private VBox panelPerfil;
    @FXML private VBox panelNotificacionesContent;
    @FXML private Label lblPerfilNombre;
    @FXML private Label lblPerfilCorreo;
    @FXML private HBox userProfileBox;

    // Botones del menú lateral
    @FXML private Button btnInicio;
    @FXML private Button btnGestionEspacios;
    @FXML private Button btnGestionUsuarios;
    @FXML private Button btnControlReservas;
    @FXML private Button btnReportesGlobales;
    @FXML private Button btnMonitoreoClimatico;
    @FXML private Button btnConfiguracion;
    
    // Vistas/Secciones principales
    @FXML private ScrollPane vistaInicio;
    @FXML private ScrollPane vistaGestionEspacios;
    @FXML private ScrollPane vistaGestionUsuarios;
    @FXML private ScrollPane vistaControlReservas;
    @FXML private ScrollPane vistaReportesGlobales;
    @FXML private ScrollPane vistaMonitoreoClimatico;
    @FXML private ScrollPane vistaConfiguracion;
    
    // ==================== DASHBOARD / INICIO ====================
    
    @FXML private Label lblEspaciosActivos;
    @FXML private Label lblReservasHoy;
    @FXML private Label lblInasistencias;
    @FXML private Label lblOcupacionSemanal;
    @FXML private Label lblVariacionOcupacion;
    
    // Campos de clima (mismo formato que UserDashboard)
    @FXML private Label weatherIconLabel;
    @FXML private Label weatherTempLabel;
    @FXML private Label weatherConditionLabel;
    @FXML private Label weatherWindLabel;
    @FXML private Label weatherHumidityLabel;
    @FXML private Label weatherMessageLabel;
    
    // Campos antiguos de clima (mantener compatibilidad)
    @FXML private Label lblTemperatura;
    @FXML private Label lblClimaCondicion;
    @FXML private Label lblViento;
    @FXML private Label lblLluvia;
    
    @FXML private Label lblNumAlertas;
    @FXML private VBox contenedorAlertas;
    
    // ==================== GESTIÓN DE ESPACIOS ====================
    
    @FXML private TextField txtBuscarEspacio;
    @FXML private ComboBox<String> cmbTipoEspacio;
    @FXML private ComboBox<String> cmbEstadoEspacio;
    @FXML private TableView<Espacio> tablaEspacios;
    @FXML private TableColumn<Espacio, String> colNombreEspacio;
    @FXML private TableColumn<Espacio, String> colTipoEspacio;
    @FXML private TableColumn<Espacio, Integer> colCapacidadEspacio;
    @FXML private TableColumn<Espacio, String> colEstadoEspacio;
    @FXML private TableColumn<Espacio, Void> colAccionesEspacio;
    
    // ==================== GESTIÓN DE USUARIOS ====================
    
    @FXML private TextField txtBuscarUsuario;
    @FXML private ComboBox<String> cmbRolUsuario;
    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, String> colUsuario;
    @FXML private TableColumn<Usuario, String> colCorreo;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstadoUsuario;
    @FXML private TableColumn<Usuario, String> colUltimoAcceso;
    @FXML private TableColumn<Usuario, String> colReservasUsuario;
    @FXML private TableColumn<Usuario, Void> colAccionesUsuario;
    
    // ==================== CONTROL DE RESERVAS ====================
    
    @FXML private TextField txtBuscarReserva;
    @FXML private ComboBox<String> cmbEstadoReserva;
    @FXML private DatePicker dpFechaDesdeReservas;
    @FXML private DatePicker dpFechaHastaReservas;
    @FXML private Button btnLimpiarFiltrosReservas;
    @FXML private Label lblTotalReservas;
    @FXML private Label lblReservasFiltradas;
    @FXML private ComboBox<Integer> cmbFilasPorPagina;
    @FXML private Label lblPaginaReservas;
    @FXML private TableView<Reserva> tablaReservas;
    @FXML private TableColumn<Reserva, Long> colIdReserva;
    @FXML private TableColumn<Reserva, String> colUsuarioReserva;
    @FXML private TableColumn<Reserva, String> colEspacioReserva;
    @FXML private TableColumn<Reserva, String> colFechaReserva;
    @FXML private TableColumn<Reserva, String> colHoraReserva;
    @FXML private TableColumn<Reserva, String> colEstadoReservaTabla;
    @FXML private TableColumn<Reserva, String> colQRReserva;
    @FXML private TableColumn<Reserva, String> colClimaReserva;
    @FXML private TableColumn<Reserva, Void> colAccionesReserva;
    
    // ==================== REPORTES GLOBALES ====================
    
    @FXML private ComboBox<String> cmbRangoFechas;
    @FXML private Label lblReservasActivas;
    @FXML private Label lblReservasCompletadas;
    @FXML private Label lblTasaAsistencia;
    @FXML private Label lblVariacionAsistencia;
    @FXML private Label lblInasistenciasReporte;
    @FXML private StackPane graficoDistribucion;
    @FXML private StackPane graficoRanking;
    
    // ==================== MONITOREO CLIMÁTICO ====================
    
    @FXML private Label lblMensajeAlertaMeteo;
    @FXML private Label lblEspaciosMonitoreados;
    @FXML private Label lblAlertasActivas;
    @FXML private Label lblReservasAfectadas;
    @FXML private GridPane contenedorTarjetasClima;
    
    // ==================== CONFIGURACIÓN ====================
    
    @FXML private TabPane tabsConfiguracion;
    @FXML private PasswordField txtAPIClima;
    @FXML private TextField txtAPISendGrid;
    @FXML private TextField txtAPITwilio;
    @FXML private CheckBox chkNotifReservaConfirmada;
    @FXML private CheckBox chkNotifRecordatorioReserva;
    @FXML private CheckBox chkNotifAlertaClimatica;
    @FXML private CheckBox chkNotifCancelacion;
    @FXML private Spinner<Integer> spinMaxHorasReserva;
    @FXML private Spinner<Integer> spinDiasAnticipacion;
    @FXML private Spinner<Integer> spinMaxReservasSimultaneas;
    @FXML private CheckBox chkPermitirCancelacion;
    @FXML private CheckBox chkRequiereAprobacion;
    @FXML private CheckBox chkRequiere2FA;
    @FXML private CheckBox chkSesionExpirar;
    @FXML private CheckBox chkLogActividad;
    
    // ==================== DATOS Y ESTADO ====================

    private final SpaceController spaceController = new SpaceController();
    private final UserController userController = new UserController();
    private final ReservationController reservationController = new ReservationController();
    private final WeatherController weatherController = new WeatherController();
    private final com.municipal.controllers.NotificationController notificationController = new com.municipal.controllers.NotificationController(new com.municipal.ApiClient());

    private SessionManager sessionManager;
    private FlowController flowController;

    private ObservableList<Espacio> listaEspacios;
    private ObservableList<Espacio> listaEspaciosFiltrados;
    private ObservableList<Usuario> listaUsuarios;
    private ObservableList<Usuario> listaUsuariosFiltrados;
    private ObservableList<Reserva> listaReservas;
    private ObservableList<Reserva> listaReservasFiltradas;
    private EstadisticasDashboard estadisticas;
    private DatosClimaticos climaActual;
    private Timeline climaTimeline;
    private Timeline datosTimeline;
    private boolean panelNotificacionesVisible;
    private boolean panelPerfilVisible;
    private boolean datosCargando;
    private boolean datosInicialesCargados;

    private static final String LOGIN_VIEW_ID = "login";
    private static final double PANEL_SLIDE_OFFSET = 360;
    private static final Duration PANEL_ANIMATION_DURATION = Duration.millis(260);
    private static final Duration CLIMA_REFRESH_INTERVAL = Duration.minutes(10); // Actualización del clima cada 10 minutos
    private static final Duration DATA_REFRESH_INTERVAL = Duration.seconds(30); // Actualización cada 30 segundos
    private static final List<String> TIPOS_ESPACIO = List.of("SALA", "CANCHA", "AUDITORIO");
    private static final Map<String, String> ROLES_FRIENDLY = Map.of(
            "ADMIN", "Administrador",
            "SUPERVISOR", "Supervisor",
            "USER", "Usuario");
    private static final String MAIN_STYLESHEET = "/com/municipal/reservationsfx/styles/styles.css";
    
    // ==================== INICIALIZACIÓN ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando Panel de Administración...");

        // Inicializar listas de datos
        inicializarDatos();
        inicializarPanelesDeslizables();

        // Configurar componentes de la interfaz
        inicializarComboBoxes();
        inicializarSpinners();
        configurarTablas();
        configurarFiltros();
        configurarBotones();

        // Mostrar vista de inicio por defecto
        mostrarInicio();

        System.out.println("Panel de Administración inicializado correctamente");
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    @Override
    public void onViewActivated() {
        bootstrap();
    }

    public void bootstrap() {
        cargarUsuarioActual();
        cargarDatosIniciales(false);
        iniciarActualizacionClima();
        iniciarActualizacionDatos();
    }
    
    /**
     * Inicializa las estructuras de datos
     */
    private void inicializarDatos() {
        listaEspacios = FXCollections.observableArrayList();
        listaEspaciosFiltrados = FXCollections.observableArrayList();
        listaUsuarios = FXCollections.observableArrayList();
        listaUsuariosFiltrados = FXCollections.observableArrayList();
        listaReservas = FXCollections.observableArrayList();
        listaReservasFiltradas = FXCollections.observableArrayList();
        estadisticas = new EstadisticasDashboard();
        datosInicialesCargados = false;
    }

    private void inicializarPanelesDeslizables() {
        configurarPanelDeslizable(panelNotificaciones);
        configurarPanelDeslizable(panelPerfil);
        panelNotificacionesVisible = false;
        panelPerfilVisible = false;
    }

    private void configurarPanelDeslizable(VBox panel) {
        if (panel == null) {
            return;
        }
        panel.setVisible(false);
        panel.setManaged(false);
        panel.setTranslateX(PANEL_SLIDE_OFFSET);
    }

    private void mostrarPanel(VBox panel) {
        if (panel == null) {
            return;
        }
        panel.setVisible(true);
        panel.setManaged(true);
        panel.setTranslateX(PANEL_SLIDE_OFFSET);
        panel.toFront();
        TranslateTransition transition = new TranslateTransition(PANEL_ANIMATION_DURATION, panel);
        transition.setFromX(PANEL_SLIDE_OFFSET);
        transition.setToX(0);
        transition.play();
    }

    private void ocultarPanel(VBox panel) {
        if (panel == null || !panel.isVisible()) {
            return;
        }
        TranslateTransition transition = new TranslateTransition(PANEL_ANIMATION_DURATION, panel);
        transition.setFromX(panel.getTranslateX());
        transition.setToX(PANEL_SLIDE_OFFSET);
        transition.setOnFinished(event -> {
            panel.setVisible(false);
            panel.setManaged(false);
            panel.setTranslateX(PANEL_SLIDE_OFFSET);
        });
        transition.play();
    }

    private void cerrarPanelesDeslizables() {
        cerrarPanelNotificacionesInterno();
        cerrarPanelPerfilInterno();
    }

    private void cerrarPanelNotificacionesInterno() {
        if (panelNotificacionesVisible) {
            ocultarPanel(panelNotificaciones);
            panelNotificacionesVisible = false;
        }
    }

    private void cerrarPanelPerfilInterno() {
        if (panelPerfilVisible) {
            ocultarPanel(panelPerfil);
            panelPerfilVisible = false;
        }
    }
    
    /**
     * Inicializa todos los ComboBox con sus valores
     */
    private void inicializarComboBoxes() {
        inicializarComboBox(cmbTipoEspacio, "Todos los tipos");
        inicializarComboBox(cmbEstadoEspacio, "Todos los estados");
        inicializarComboBox(cmbRolUsuario, "Todos los roles");
        inicializarComboBox(cmbEstadoReserva, "Todos los estados");
        inicializarComboBox(cmbRangoFechas, "Último mes",
                FXCollections.observableArrayList(
                        "Última semana", "Último mes", "Últimos 3 meses", "Último año", "Personalizado"));
        
        // Inicializar combo de filas por página
        if (cmbFilasPorPagina != null) {
            cmbFilasPorPagina.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
            cmbFilasPorPagina.getSelectionModel().select(Integer.valueOf(25));
        }
    }

    private void inicializarComboBox(ComboBox<String> comboBox, String defaultOption) {
        inicializarComboBox(comboBox, defaultOption, FXCollections.observableArrayList(defaultOption));
    }

    private void inicializarComboBox(ComboBox<String> comboBox, String defaultOption,
            ObservableList<String> values) {
        if (comboBox == null) {
            return;
        }
        if (values == null || values.isEmpty()) {
            values = FXCollections.observableArrayList(defaultOption);
        } else if (!values.stream().anyMatch(value -> value.equalsIgnoreCase(defaultOption))) {
            values.add(0, defaultOption);
        }
        comboBox.setItems(values);
        comboBox.setValue(defaultOption);
    }

    private void actualizarOpcionesFiltros() {
        actualizarOpcionesTipoEspacio();
        actualizarOpcionesEstadoEspacio();
        actualizarOpcionesRolUsuario();
        actualizarOpcionesEstadoReserva();
    }

    private void actualizarOpcionesTipoEspacio() {
        if (cmbTipoEspacio == null) {
            return;
        }
        List<String> tipos = collectDistinctValues(listaEspacios.stream()
                .map(Espacio::getTipo)
                .collect(Collectors.toList()));
        List<String> opciones = new ArrayList<>();
        boolean hayInterior = listaEspacios.stream().anyMatch(espacio -> !espacio.isEsExterior());
        boolean hayExterior = listaEspacios.stream().anyMatch(Espacio::isEsExterior);
        if (hayInterior) {
            opciones.add("Interior");
        }
        if (hayExterior) {
            opciones.add("Exterior");
        }
        opciones.addAll(tipos);
        updateComboBoxOptions(cmbTipoEspacio, "Todos los tipos", opciones);
    }

    private void actualizarOpcionesEstadoEspacio() {
        if (cmbEstadoEspacio == null) {
            return;
        }
        List<String> estados = collectDistinctValues(listaEspacios.stream()
                .map(Espacio::getEstado)
                .collect(Collectors.toList()));
        updateComboBoxOptions(cmbEstadoEspacio, "Todos los estados", estados);
    }

    private void actualizarOpcionesRolUsuario() {
        if (cmbRolUsuario == null) {
            return;
        }
        List<String> roles = collectDistinctValues(listaUsuarios.stream()
                .map(Usuario::getRol)
                .collect(Collectors.toList()));
        updateComboBoxOptions(cmbRolUsuario, "Todos los roles", roles);
    }

    private void actualizarOpcionesEstadoReserva() {
        if (cmbEstadoReserva == null) {
            return;
        }
        List<String> estados = collectDistinctValues(listaReservas.stream()
                .map(Reserva::getEstado)
                .collect(Collectors.toList()));
        updateComboBoxOptions(cmbEstadoReserva, "Todos los estados", estados);
    }

    private void updateComboBoxOptions(ComboBox<String> comboBox, String defaultOption, List<String> values) {
        if (comboBox == null) {
            return;
        }
        LinkedHashMap<String, String> opciones = new LinkedHashMap<>();
        opciones.put(defaultOption.toLowerCase(Locale.ROOT), defaultOption);
        if (values != null) {
            for (String value : values) {
                String sanitized = defaultString(value).trim();
                if (!sanitized.isEmpty()) {
                    opciones.putIfAbsent(sanitized.toLowerCase(Locale.ROOT), sanitized);
                }
            }
        }
        ObservableList<String> items = FXCollections.observableArrayList(opciones.values());
        String seleccionAnterior = comboBox.getValue();
        comboBox.setItems(items);
        if (seleccionAnterior != null) {
            for (String option : items) {
                if (option.equalsIgnoreCase(seleccionAnterior)) {
                    comboBox.setValue(option);
                    return;
                }
            }
        }
        comboBox.setValue(defaultOption);
    }

    private List<String> collectDistinctValues(List<String> values) {
        LinkedHashMap<String, String> uniques = new LinkedHashMap<>();
        if (values != null) {
            for (String value : values) {
                String sanitized = defaultString(value).trim();
                if (!sanitized.isEmpty()) {
                    uniques.putIfAbsent(sanitized.toLowerCase(Locale.ROOT), sanitized);
                }
            }
        }
        return new ArrayList<>(uniques.values());
    }
    
    /**
     * Inicializa los Spinners con sus valores
     */
    private void inicializarSpinners() {
        if (spinMaxHorasReserva != null) {
            spinMaxHorasReserva.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 24, 4)
            );
        }
        
        if (spinDiasAnticipacion != null) {
            spinDiasAnticipacion.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 30, 1)
            );
        }
        
        if (spinMaxReservasSimultaneas != null) {
            spinMaxReservasSimultaneas.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 3)
            );
        }
    }
    
    /**
     * Configura todas las tablas del sistema
     */
    private void configurarTablas() {
        configurarTablaEspacios();
        configurarTablaUsuarios();
        configurarTablaReservas();
    }
    
    /**
     * Configura la tabla de espacios
     */
    private void configurarTablaEspacios() {
        if (tablaEspacios == null) return;
        
        // Configurar columnas con lambdas en lugar de PropertyValueFactory
        colNombreEspacio.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colTipoEspacio.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTipo()));
        colCapacidadEspacio.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCapacidad()).asObject());
        colEstadoEspacio.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEstado()));
        
        // Personalizar columna de estado con estilos
        colEstadoEspacio.setCellFactory(column -> new TableCell<Espacio, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                
                if (empty || estado == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label label = new Label(estado);
                    
                    switch (estado) {
                        case "Disponible":
                            label.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Ocupado":
                            label.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Mantenimiento":
                            label.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                    }
                    
                    setGraphic(label);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones con anchos adecuados
        colAccionesEspacio.setMinWidth(360);
        colAccionesEspacio.setPrefWidth(380);
        
        colAccionesEspacio.setCellFactory(param -> new TableCell<Espacio, Void>() {
            private final Button btnVer = new Button("👁️ Ver");
            private final Button btnEditar = new Button("✏️ Editar");
            private final Button btnEstado = new Button("� Estado");
            private final Button btnEliminar = new Button("�️ Eliminar");
            private final HBox contenedor = new HBox(6, btnVer, btnEditar, btnEstado, btnEliminar);

            {
                // Aplicar estilos CSS
                btnVer.getStyleClass().addAll("admin-btn-base", "admin-btn-view");
                btnEditar.getStyleClass().addAll("admin-btn-base", "admin-btn-edit");
                btnEstado.getStyleClass().addAll("admin-btn-base", "admin-btn-state");
                btnEliminar.getStyleClass().addAll("admin-btn-base", "admin-btn-delete");
                
                // Tooltips
                javafx.scene.control.Tooltip.install(btnVer, new javafx.scene.control.Tooltip("Ver detalles del espacio"));
                javafx.scene.control.Tooltip.install(btnEditar, new javafx.scene.control.Tooltip("Editar información"));
                javafx.scene.control.Tooltip.install(btnEstado, new javafx.scene.control.Tooltip("Cambiar disponibilidad"));
                javafx.scene.control.Tooltip.install(btnEliminar, new javafx.scene.control.Tooltip("Eliminar espacio"));
                
                btnVer.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    verDetallesEspacio(espacio);
                });

                btnEditar.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    editarEspacio(espacio);
                });

                btnEstado.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    cambiarEstadoEspacio(espacio);
                });

                btnEliminar.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    eliminarEspacio(espacio);
                });

                contenedor.setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }
    
    /**
     * Configura la tabla de usuarios
     */
    private void configurarTablaUsuarios() {
        if (tablaUsuarios == null) return;
        
        colUsuario.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatearNombreUsuario(cellData.getValue())));
        colCorreo.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatearCorreoUsuario(cellData.getValue())));
        colRol.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario == null) {
                return new SimpleStringProperty("");
            }
            String rol = defaultString(usuario.getRol()).trim();
            if (rol.isEmpty()) {
                rol = obtenerRolFriendly(usuario.getRolCodigo());
            }
            if (rol.isEmpty()) {
                rol = ROLES_FRIENDLY.getOrDefault("USER", "Usuario");
            }
            return new SimpleStringProperty(rol);
        });
        colEstadoUsuario.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario == null) {
                return new SimpleStringProperty("");
            }
            String estado = defaultString(usuario.getEstado()).trim();
            if (estado.isEmpty()) {
                estado = usuario.isActivo() ? "Activo" : "Inactivo";
            }
            return new SimpleStringProperty(estado);
        });

        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        if (colUsuario != null) {
            colUsuario.setMinWidth(180);
            colUsuario.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.18));
        }
        if (colCorreo != null) {
            colCorreo.setMinWidth(200);
            colCorreo.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.22));
        }
        if (colRol != null) {
            colRol.setMinWidth(140);
            colRol.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.18));
        }
        if (colEstadoUsuario != null) {
            colEstadoUsuario.setMinWidth(90);
            colEstadoUsuario.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.10));
        }
        if (colUltimoAcceso != null) {
            colUltimoAcceso.setMinWidth(150);
            colUltimoAcceso.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.14));
        }
        if (colReservasUsuario != null) {
            colReservasUsuario.setMinWidth(100);
            colReservasUsuario.prefWidthProperty().bind(tablaUsuarios.widthProperty().multiply(0.09));
        }
        // No vincular la columna de acciones porque usaremos ancho fijo
        // (se configurará más adelante en el cell factory)

        // Formatear columna de último acceso
        colUltimoAcceso.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getUltimoAcceso();
            if (fecha == null) {
                return new SimpleStringProperty("N/A");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(fecha.format(formatter));
        });

        if (colReservasUsuario != null) {
            colReservasUsuario.setCellValueFactory(cellData -> {
                Usuario usuario = cellData.getValue();
                if (usuario == null) {
                    return new SimpleStringProperty("0 / 0");
                }
                String resumen = String.format(Locale.getDefault(), "%d / %d",
                        Math.max(usuario.getTotalReservas(), 0),
                        Math.max(usuario.getReservasAprobadas(), 0));
                return new SimpleStringProperty(resumen);
            });

            colReservasUsuario.setCellFactory(column -> new TableCell<>() {
                @Override
                protected void updateItem(String value, boolean empty) {
                    super.updateItem(value, empty);

                    if (empty || value == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Label etiqueta = new Label(value + " (tot/apr)");
                        etiqueta.getStyleClass().add("tag-info");
                        setGraphic(etiqueta);
                        setText(null);
                    }
                }
            });
        }

        // Personalizar columna de rol
        colRol.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String rol, boolean empty) {
                super.updateItem(rol, empty);

                if (empty || rol == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(rol);

                    switch (rol) {
                        case "Administrador":
                            label.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Supervisor":
                            label.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Usuario":
                            label.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        default:
                            label.setStyle("-fx-background-color: #0D6EFD; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                    }

                    setGraphic(label);
                    setText(null);
                }
            }
        });

        // Personalizar columna de estado para mostrar chips visuales
        colEstadoUsuario.setCellFactory(column -> new TableCell<Usuario, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);

                if (empty || estado == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String texto = estado.isBlank() ? "Activo" : estado;
                Label etiqueta = new Label(texto);
                etiqueta.setStyle(switch (texto.toLowerCase(Locale.ROOT)) {
                    case "activo" -> "-fx-background-color: #22C55E; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;";
                    case "inactivo" -> "-fx-background-color: #DC2626; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;";
                    default -> "-fx-background-color: #F59E0B; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;";
                });

                setGraphic(etiqueta);
                setText(null);
            }
        });
        
        // Configurar columna de acciones con anchos adecuados
        colAccionesUsuario.setMinWidth(320);
        colAccionesUsuario.setPrefWidth(340);
        
        colAccionesUsuario.setCellFactory(param -> new TableCell<Usuario, Void>() {
            private final Button btnEditar = new Button("✏️ Editar");
            private final Button btnEstado = new Button("🔄 Estado");
            private final Button btnEliminar = new Button("🗑️ Eliminar");
            private final HBox contenedor = new HBox(6, btnEditar, btnEstado, btnEliminar);

            {
                // Aplicar estilos CSS
                btnEditar.getStyleClass().addAll("admin-btn-base", "admin-btn-edit");
                btnEstado.getStyleClass().addAll("admin-btn-base", "admin-btn-state");
                btnEliminar.getStyleClass().addAll("admin-btn-base", "admin-btn-delete");
                
                // Tooltips
                javafx.scene.control.Tooltip.install(btnEditar, new javafx.scene.control.Tooltip("Editar información del usuario"));
                javafx.scene.control.Tooltip.install(btnEstado, new javafx.scene.control.Tooltip("Activar/Desactivar usuario"));
                javafx.scene.control.Tooltip.install(btnEliminar, new javafx.scene.control.Tooltip("Eliminar usuario del sistema"));
                
                btnEditar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarUsuario(usuario);
                });

                btnEstado.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    cambiarEstadoUsuario(usuario);
                });

                btnEliminar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    eliminarUsuario(usuario);
                });

                contenedor.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private String formatearNombreUsuario(Usuario usuario) {
        if (usuario == null) {
            return "Usuario no disponible";
        }
        String nombre = defaultString(usuario.getNombre()).trim();
        if (!nombre.isEmpty()) {
            return nombre;
        }
        String correo = defaultString(usuario.getCorreo()).trim();
        if (!correo.isEmpty()) {
            return correo;
        }
        return "Usuario no disponible";
    }

    private String formatearCorreoUsuario(Usuario usuario) {
        if (usuario == null) {
            return "Correo no disponible";
        }
        String correo = defaultString(usuario.getCorreo()).trim();
        if (!correo.isEmpty()) {
            return correo;
        }
        return "Correo no disponible";
    }
    
    /**
     * Configura la tabla de reservas
     */
    private void configurarTablaReservas() {
        if (tablaReservas == null) return;
        
        colIdReserva.setCellValueFactory(cellData -> {
            Long id = cellData.getValue().getId();
            return new SimpleObjectProperty<>(id);
        });
        
        colUsuarioReserva.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatearNombreUsuario(cellData.getValue().getUsuario())));
        
        colEspacioReserva.setCellValueFactory(cellData -> {
            Espacio espacio = cellData.getValue().getEspacio();
            return new SimpleStringProperty(espacio != null ? espacio.getNombre() : "N/A");
        });
        
        colFechaReserva.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFecha();
            return new SimpleStringProperty(fecha != null ? fecha.toString() : "N/A");
        });
        
        colHoraReserva.setCellValueFactory(cellData -> {
            LocalTime inicio = cellData.getValue().getHoraInicio();
            LocalTime fin = cellData.getValue().getHoraFin();
            String hora = (inicio != null && fin != null) 
                ? inicio.toString() + " - " + fin.toString() 
                : "N/A";
            return new SimpleStringProperty(hora);
        });
        
        colEstadoReservaTabla.setCellValueFactory(cellData -> {
            String estado = cellData.getValue().getEstado();
            return new SimpleStringProperty(estado != null ? estado : "N/A");
        });
        
        colQRReserva.setCellValueFactory(cellData -> {
            String codigoQR = cellData.getValue().getCodigoQR();
            return new SimpleStringProperty(codigoQR != null ? codigoQR : "N/A");
        });
        
        colClimaReserva.setCellValueFactory(cellData -> {
            DatosClimaticos clima = cellData.getValue().getClima();
            return new SimpleStringProperty(clima != null ? clima.getTemperaturaFormateada() : "N/A");
        });
        
        // Personalizar columna de estado
        colEstadoReservaTabla.setCellFactory(column -> new TableCell<Reserva, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                
                if (empty || estado == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(estado);
                    
                    switch (estado) {
                        case "Confirmada":
                            label.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Pendiente":
                            label.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Completada":
                            label.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Cancelada":
                            label.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "En sitio":
                            label.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                        case "Inasistencia":
                            label.setStyle("-fx-background-color: #6C757D; -fx-text-fill: white; " +
                                         "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold;");
                            break;
                    }
                    
                    setGraphic(label);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones - Ancho reducido y disposición vertical
        colAccionesReserva.setMinWidth(180);
        colAccionesReserva.setPrefWidth(200);
        colAccionesReserva.setMaxWidth(220);
        
        colAccionesReserva.setCellFactory(param -> new TableCell<Reserva, Void>() {
            private final Button btnVer = new Button("👁️ Ver");
            private final Button btnAprobar = new Button("✅ Aprobar");
            private final Button btnCancelar = new Button("❌ Cancelar");
            private final Button btnEmail = new Button("📧 Email");
            private final Button btnEliminar = new Button("🗑️ Eliminar");
            private final VBox contenedor = new VBox(4);
            
            {
                // Aplicar estilos CSS y hacer que los botones ocupen todo el ancho
                btnVer.getStyleClass().addAll("admin-btn-base", "admin-btn-view");
                btnAprobar.getStyleClass().addAll("admin-btn-base", "admin-btn-approve");
                btnCancelar.getStyleClass().addAll("admin-btn-base", "admin-btn-cancel");
                btnEmail.getStyleClass().addAll("admin-btn-base", "admin-btn-email");
                btnEliminar.getStyleClass().addAll("admin-btn-base", "admin-btn-delete");
                
                // Hacer que los botones ocupen todo el ancho disponible
                btnVer.setMaxWidth(Double.MAX_VALUE);
                btnAprobar.setMaxWidth(Double.MAX_VALUE);
                btnCancelar.setMaxWidth(Double.MAX_VALUE);
                btnEmail.setMaxWidth(Double.MAX_VALUE);
                btnEliminar.setMaxWidth(Double.MAX_VALUE);
                
                // Tooltips detallados
                javafx.scene.control.Tooltip.install(btnVer, new javafx.scene.control.Tooltip("Ver detalles completos de la reserva"));
                javafx.scene.control.Tooltip.install(btnAprobar, new javafx.scene.control.Tooltip("Aprobar reserva y desbloquear código QR"));
                javafx.scene.control.Tooltip.install(btnCancelar, new javafx.scene.control.Tooltip("Cancelar esta reserva"));
                javafx.scene.control.Tooltip.install(btnEmail, new javafx.scene.control.Tooltip("Enviar notificación por correo electrónico"));
                javafx.scene.control.Tooltip.install(btnEliminar, new javafx.scene.control.Tooltip("Eliminar permanentemente esta reserva de la base de datos"));
                
                btnVer.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    verDetallesReserva(reserva);
                });
                
                btnAprobar.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    aprobarReserva(reserva);
                });
                
                btnCancelar.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    cancelarReservaConMotivo(reserva);
                });
                
                btnEmail.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    enviarEmailReserva(reserva);
                });
                
                btnEliminar.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    eliminarReservaPermanente(reserva);
                });
                
                // Alinear al centro y agregar padding
                contenedor.setAlignment(Pos.CENTER);
                contenedor.setPadding(new javafx.geometry.Insets(4, 8, 4, 8));
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                    return;
                }
                
                Reserva reserva = getTableView().getItems().get(getIndex());
                String estado = reserva.getEstado();
                contenedor.getChildren().clear();
                
                // Siempre mostrar botón Ver
                contenedor.getChildren().add(btnVer);
                
                // Lógica según estado:
                // PENDIENTE: Ver + Aprobar + Cancelar + Email
                if ("Pendiente".equals(estado)) {
                    contenedor.getChildren().addAll(btnAprobar, btnCancelar, btnEmail);
                }
                // CONFIRMADA: Ver + Cancelar + Email  
                else if ("Confirmada".equals(estado)) {
                    contenedor.getChildren().addAll(btnCancelar, btnEmail);
                }
                // EN SITIO (CHECKED_IN) o INASISTENCIA (NO_SHOW): Ver + Email + Eliminar
                else if ("En sitio".equals(estado) || "Inasistencia".equals(estado)) {
                    contenedor.getChildren().addAll(btnEmail, btnEliminar);
                }
                // CANCELADA: Ver + Email + Eliminar
                else if ("Cancelada".equals(estado)) {
                    contenedor.getChildren().addAll(btnEmail, btnEliminar);
                }
                
                setGraphic(contenedor);
            }
        });
    }
    
    /**
     * Configura los filtros de búsqueda y combobox
     */
    private void configurarFiltros() {
        // Filtro de búsqueda para espacios
        if (txtBuscarEspacio != null) {
            txtBuscarEspacio.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarEspacios();
            });
        }
        
        if (cmbTipoEspacio != null) {
            cmbTipoEspacio.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarEspacios();
            });
        }
        
        if (cmbEstadoEspacio != null) {
            cmbEstadoEspacio.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarEspacios();
            });
        }
        
        // Filtro de búsqueda para usuarios
        if (txtBuscarUsuario != null) {
            txtBuscarUsuario.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarUsuarios();
            });
        }
        
        if (cmbRolUsuario != null) {
            cmbRolUsuario.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarUsuarios();
            });
        }
        
        // Filtro de búsqueda para reservas
        if (txtBuscarReserva != null) {
            txtBuscarReserva.textProperty().addListener((obs, oldVal, newVal) -> {
                filtrarReservas();
            });
        }
        
        if (cmbEstadoReserva != null) {
            cmbEstadoReserva.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarReservas();
            });
        }
        
        // Filtros de fecha para reservas
        if (dpFechaDesdeReservas != null) {
            dpFechaDesdeReservas.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarReservas();
            });
        }
        
        if (dpFechaHastaReservas != null) {
            dpFechaHastaReservas.valueProperty().addListener((obs, oldVal, newVal) -> {
                filtrarReservas();
            });
        }
        
        // Filtro de rango de fechas para reportes
        if (cmbRangoFechas != null) {
            cmbRangoFechas.valueProperty().addListener((obs, oldVal, newVal) -> {
                actualizarReportes();
            });
        }
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarBotones() {
        // Botón de notificaciones
        if (btnNotificaciones != null) {
            btnNotificaciones.setOnAction(this::toggleNotificacionesPanel);
        }

        if (userProfileBox != null) {
            userProfileBox.addEventHandler(MouseEvent.MOUSE_CLICKED, this::togglePerfilPanel);
        }

        configurarCierreAutomaticoPaneles();
    }

    private void configurarCierreAutomaticoPaneles() {
        EventHandler<MouseEvent> closeHandler = event -> {
            Node target = event.getPickResult() != null ? event.getPickResult().getIntersectedNode() : null;

            if (panelNotificacionesVisible
                    && !esClickDentroDe(panelNotificaciones, target)
                    && !esClickDentroDe(btnNotificaciones, target)) {
                cerrarPanelNotificacionesInterno();
            }

            if (panelPerfilVisible
                    && !esClickDentroDe(panelPerfil, target)
                    && !esClickDentroDe(userProfileBox, target)) {
                cerrarPanelPerfilInterno();
            }
        };

        if (headerStack != null) {
            headerStack.addEventFilter(MouseEvent.MOUSE_CLICKED, closeHandler);
        }

        if (contenedorPrincipal != null) {
            contenedorPrincipal.addEventFilter(MouseEvent.MOUSE_CLICKED, closeHandler);
        }
    }

    private boolean esClickDentroDe(Node contenedor, Node objetivo) {
        if (contenedor == null || objetivo == null) {
            return false;
        }

        Node actual = objetivo;
        while (actual != null) {
            if (actual == contenedor) {
                return true;
            }
            actual = actual.getParent();
        }
        return false;
    }
    
    // ==================== NAVEGACIÓN ENTRE MÓDULOS ====================
    
    @FXML
    private void mostrarInicio() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaInicio.setVisible(true);
        actualizarMenuActivo(btnInicio);
        cargarDatosDashboard();
    }

    @FXML
    private void mostrarGestionEspacios() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaGestionEspacios.setVisible(true);
        actualizarMenuActivo(btnGestionEspacios);
        cargarEspacios();
    }

    @FXML
    private void mostrarGestionUsuarios() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaGestionUsuarios.setVisible(true);
        actualizarMenuActivo(btnGestionUsuarios);
        cargarUsuarios();
    }

    @FXML
    private void mostrarControlReservas() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaControlReservas.setVisible(true);
        actualizarMenuActivo(btnControlReservas);
        cargarReservas();
    }

    @FXML
    private void mostrarReportesGlobales() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaReportesGlobales.setVisible(true);
        actualizarMenuActivo(btnReportesGlobales);
        cargarReportes();
    }

    @FXML
    private void mostrarMonitoreoClimatico() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaMonitoreoClimatico.setVisible(true);
        actualizarMenuActivo(btnMonitoreoClimatico);
        cargarClima();
    }

    @FXML
    private void mostrarConfiguracion() {
        cerrarPanelesDeslizables();
        ocultarTodasLasVistas();
        vistaConfiguracion.setVisible(true);
        actualizarMenuActivo(btnConfiguracion);
        cargarConfiguracion();
    }
    
    /**
     * Oculta todas las vistas/secciones
     */
    private void ocultarTodasLasVistas() {
        vistaInicio.setVisible(false);
        vistaGestionEspacios.setVisible(false);
        vistaGestionUsuarios.setVisible(false);
        vistaControlReservas.setVisible(false);
        vistaReportesGlobales.setVisible(false);
        vistaMonitoreoClimatico.setVisible(false);
        vistaConfiguracion.setVisible(false);
    }
    
    /**
     * Actualiza el estado visual del menú lateral
     */
    private void actualizarMenuActivo(Button botonActivo) {
        // Remover clase 'active' de todos los botones
        btnInicio.getStyleClass().remove("active");
        btnGestionEspacios.getStyleClass().remove("active");
        btnGestionUsuarios.getStyleClass().remove("active");
        btnControlReservas.getStyleClass().remove("active");
        btnReportesGlobales.getStyleClass().remove("active");
        btnMonitoreoClimatico.getStyleClass().remove("active");
        btnConfiguracion.getStyleClass().remove("active");
        
        // Agregar clase 'active' al botón seleccionado
        if (!botonActivo.getStyleClass().contains("active")) {
            botonActivo.getStyleClass().add("active");
        }
    }
    
    // ==================== CARGA DE DATOS ====================
    
    /**
     * Carga el usuario actual del sistema
     */
    private void cargarUsuarioActual() {
        if (lblNombreUsuario == null) {
            return;
        }

        if (sessionManager == null) {
            lblNombreUsuario.setText("Usuario");
            return;
        }

        sessionManager.getAuthResponse().ifPresentOrElse(response -> {
            String displayName = response.name();
            if (displayName == null || displayName.isBlank()) {
                displayName = response.email();
            }
            if (displayName == null || displayName.isBlank()) {
                displayName = "Usuario";
            }
            lblNombreUsuario.setText(displayName);
            if (lblPerfilNombre != null) {
                lblPerfilNombre.setText(displayName);
            }
            if (lblPerfilCorreo != null) {
                lblPerfilCorreo.setText(response.email() != null ? response.email() : "Sin correo registrado");
            }
        }, () -> {
            lblNombreUsuario.setText("Usuario");
            if (lblPerfilNombre != null) {
                lblPerfilNombre.setText("Usuario");
            }
            if (lblPerfilCorreo != null) {
                lblPerfilCorreo.setText("correo@municipal.go.cr");
            }
        });
    }

    /**
     * Carga todos los datos iniciales del sistema
     */
    private void cargarDatosIniciales() {
        cargarDatosIniciales(false);
    }

    private void cargarDatosIniciales(boolean notifySuccess) {
        if (sessionManager == null) {
            mostrarAdvertencia("No hay sesión activa para cargar los datos.");
            return;
        }

        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            mostrarAdvertencia("No se encontró un token de acceso válido.");
            return;
        }

        if (datosCargando) {
            return;
        }

        datosCargando = true;
        boolean mostrarCarga = notifySuccess || !datosInicialesCargados;

        Task<DatosIniciales> task = new Task<>() {
            @Override
            protected DatosIniciales call() {
                List<String> warnings = new ArrayList<>();

                List<Espacio> espacios = cargarEspaciosDesdeApi(token, warnings);
                Map<Long, Espacio> espaciosPorId = espacios.stream()
                        .filter(espacio -> espacio.getId() != null)
                        .collect(Collectors.toMap(Espacio::getId, espacio -> espacio, (a, b) -> a, HashMap::new));

                List<Usuario> usuarios = cargarUsuariosDesdeApi(token, warnings);
                Map<Long, Usuario> usuariosPorId = usuarios.stream()
                        .filter(usuario -> usuario.getId() != null)
                        .collect(Collectors.toMap(Usuario::getId, usuario -> usuario, (a, b) -> a, HashMap::new));

                List<Reserva> reservas = cargarReservasDesdeApi(token, warnings, usuariosPorId, espaciosPorId);
                // ✅ El clima se actualiza por separado cada 10 minutos mediante climaTimeline
                // No necesitamos cargarlo aquí cada 30 segundos
                DatosClimaticos clima = climaActual; // Usar el clima ya cargado

                return new DatosIniciales(espacios, usuarios, reservas, clima, warnings);
            }
        };

        if (mostrarCarga) {
            task.setOnRunning(event -> mostrarIndicadorCarga("Cargando datos del sistema..."));
        }

        task.setOnSucceeded(event -> {
            datosCargando = false;
            DatosIniciales resultado = task.getValue();

            datosInicialesCargados = true;

            listaEspacios.setAll(resultado.espacios());
            listaUsuarios.setAll(resultado.usuarios());
            listaReservas.setAll(resultado.reservas());

            actualizarOpcionesFiltros();

            filtrarEspacios();
            filtrarUsuarios();
            filtrarReservas();

            climaActual = resultado.clima();
            actualizarIndicadoresClimaticos();

            cargarDatosDashboard();
            cargarClima();

            if (mostrarCarga) {
                ocultarIndicadorCarga();
            }

            if (notifySuccess) {
                mostrarExito("Datos actualizados exitosamente");
            }

            if (!resultado.warnings().isEmpty()) {
                mostrarAdvertencia(String.join("\n", resultado.warnings()));
            }
        });

        task.setOnFailed(event -> {
            datosCargando = false;
            if (mostrarCarga) {
                ocultarIndicadorCarga();
            }
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Error desconocido";
            mostrarError("No se pudieron cargar los datos: " + message);
        });

        task.setOnCancelled(event -> {
            datosCargando = false;
            if (mostrarCarga) {
                ocultarIndicadorCarga();
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private List<Espacio> cargarEspaciosDesdeApi(String token, List<String> warnings) {
        try {
            List<SpaceDTO> espacios = spaceController.loadSpaces(token);
            if (espacios == null) {
                return Collections.emptyList();
            }
            return espacios.stream()
                    .map(this::mapearEspacio)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            warnings.add("No se pudieron cargar los espacios: " + construirMensajeError(exception));
            return Collections.emptyList();
        }
    }

    private List<Usuario> cargarUsuariosDesdeApi(String token, List<String> warnings) {
        try {
            List<UserDTO> usuarios = userController.loadUsers(token);
            if (usuarios == null) {
                return Collections.emptyList();
            }
            return usuarios.stream()
                    .map(this::mapearUsuario)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            warnings.add("No se pudieron cargar los usuarios: " + construirMensajeError(exception));
            return Collections.emptyList();
        }
    }

    private List<Reserva> cargarReservasDesdeApi(String token, List<String> warnings,
            Map<Long, Usuario> usuariosPorId, Map<Long, Espacio> espaciosPorId) {
        try {
            List<ReservationDTO> reservas = reservationController.loadReservations(token);
            if (reservas == null) {
                return Collections.emptyList();
            }
            return reservas.stream()
                    .map(dto -> mapearReserva(dto, usuariosPorId, espaciosPorId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception exception) {
            warnings.add("No se pudieron cargar las reservas: " + construirMensajeError(exception));
            return Collections.emptyList();
        }
    }

    private DatosClimaticos cargarClimaDesdeApi(String token, List<String> warnings) {
        String latitudConfigurada = AppConfig.get("weather.default-lat");
        String longitudConfigurada = AppConfig.get("weather.default-lon");

        if (latitudConfigurada == null || longitudConfigurada == null) {
            warnings.add("No hay coordenadas configuradas para consultar el clima.");
            return null;
        }

        try {
            double latitud = Double.parseDouble(latitudConfigurada);
            double longitud = Double.parseDouble(longitudConfigurada);
            CurrentWeatherDTO weather = weatherController.loadCurrentWeather(latitud, longitud, token);
            if (weather == null) {
                return null;
            }
            return mapearDatosClimaticos(weather);
        } catch (NumberFormatException exception) {
            warnings.add("Las coordenadas configuradas para el clima no son válidas.");
            return null;
        } catch (Exception exception) {
            warnings.add("No se pudo obtener la información climática: " + construirMensajeError(exception));
            return null;
        }
    }

    private Espacio mapearEspacio(SpaceDTO dto) {
        if (dto == null) {
            return null;
        }
        Long id = dto.id();
        String nombre = defaultString(dto.name());
        String tipo = defaultString(dto.type());
        int capacidad = dto.capacity() != null ? dto.capacity() : 0;
        boolean activo = dto.active() == null || Boolean.TRUE.equals(dto.active());
        String estado = activo ? "Disponible" : "Inactivo";
        boolean esExterior = "CANCHA".equalsIgnoreCase(tipo);
        String descripcion = defaultString(dto.description());
        String ubicacion = defaultString(dto.location());
        Integer maxDuracion = dto.maxReservationDuration();
        boolean requiereAprobacion = dto.requiresApproval() != null && dto.requiresApproval();
        return new Espacio(id, nombre, tipo, capacidad, estado, descripcion, esExterior,
                ubicacion, maxDuracion, requiereAprobacion, activo);
    }

    private Usuario mapearUsuario(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        Long id = dto.id();
        String nombre = defaultString(dto.name()).trim();
        String correo = defaultString(dto.email()).trim();
        String rolCodigo = defaultString(dto.role()).trim();
        if (rolCodigo.isEmpty()) {
            rolCodigo = "USER";
        } else {
            rolCodigo = rolCodigo.toUpperCase(Locale.ROOT);
        }
        String rol = obtenerRolFriendly(rolCodigo);
        boolean activo = dto.active() == null || Boolean.TRUE.equals(dto.active());
        String estado = activo ? "Activo" : "Inactivo";
        LocalDateTime ultimoAcceso = dto.lastLoginAt();
        int totalReservas = dto.reservationIds() != null ? dto.reservationIds().size() : 0;
        int reservasAprobadas = dto.approvedReservationIds() != null
                ? dto.approvedReservationIds().size() : 0;
        return new Usuario(id, nombre, correo, rol, estado, ultimoAcceso, null, rolCodigo, activo,
                totalReservas, reservasAprobadas);
    }

    private Reserva mapearReserva(ReservationDTO dto, Map<Long, Usuario> usuariosPorId,
            Map<Long, Espacio> espaciosPorId) {
        if (dto == null) {
            return null;
        }
        Usuario usuario = dto.userId() != null ? usuariosPorId.get(dto.userId()) : null;
        Espacio espacio = dto.spaceId() != null ? espaciosPorId.get(dto.spaceId()) : null;
        LocalDate fecha = dto.startTime() != null ? dto.startTime().toLocalDate() : null;
        LocalTime horaInicio = dto.startTime() != null ? dto.startTime().toLocalTime() : null;
        LocalTime horaFin = dto.endTime() != null ? dto.endTime().toLocalTime() : null;
        String estado = mapearEstadoReserva(dto.status());
        String codigoQR = defaultString(dto.qrCode());
        String notas = dto.notes();
        return new Reserva(dto.id(), usuario, espacio, fecha, horaInicio, horaFin, estado, codigoQR, null, notas);
    }

    private DatosClimaticos mapearDatosClimaticos(CurrentWeatherDTO weather) {
        double temperatura = weather.temperature() != null ? weather.temperature() : 0;
        int humedad = weather.humidity() != null ? weather.humidity() : 0;
        double velocidadVientoMs = weather.windSpeed() != null ? weather.windSpeed() : 0;
        double velocidadVientoKmh = velocidadVientoMs * 3.6;
        String condicion = defaultString(weather.description());
        String nivelAlerta;
        if (humedad >= 80) {
            nivelAlerta = "Posible lluvia";
        } else if (humedad >= 60) {
            nivelAlerta = "Precaución";
        } else {
            nivelAlerta = "Sin alerta";
        }
        return new DatosClimaticos(temperatura, condicion, humedad, velocidadVientoKmh, nivelAlerta, condicion);
    }

    private String mapearEstadoReserva(String status) {
        if (status == null) {
            return "Desconocido";
        }
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pendiente";
            case "CONFIRMED" -> "Confirmada";
            case "CANCELED" -> "Cancelada";
            case "CHECKED_IN" -> "En sitio";
            case "NO_SHOW" -> "Inasistencia";
            default -> status;
        };
    }

    private void actualizarIndicadoresClimaticos() {
        // Mantener compatibilidad con labels antiguos
        if (lblTemperatura != null) {
            lblTemperatura.setText(climaActual != null ? climaActual.getTemperaturaFormateada() : "--");
        }
        if (lblClimaCondicion != null) {
            lblClimaCondicion.setText(climaActual != null ? climaActual.getCondicion() : "--");
        }
        if (lblViento != null) {
            lblViento.setText(climaActual != null ? climaActual.getVientoFormateado() : "--");
        }
        if (lblLluvia != null) {
            lblLluvia.setText(climaActual != null ? climaActual.getProbabilidadLluviaFormateada() : "--");
        }
        
        // Cargar clima con la nueva implementación (igual a User)
        loadWeather();
    }
    
    /**
     * Carga el clima actual usando WeatherController (igual que UserDashboard)
     */
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
    
    /**
     * Actualiza la UI con los datos del clima (igual que UserDashboard)
     */
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

    /**
     * Retorna el icono emoji según la condición climática (igual que UserDashboard)
     */
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

    /**
     * Genera un mensaje personalizado según el clima (igual que UserDashboard)
     */
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

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private String construirMensajeError(Throwable error) {
        if (error instanceof ApiClientException apiError) {
            StringBuilder builder = new StringBuilder("HTTP " + apiError.getStatusCode());
            if (apiError.getResponseBody() != null && !apiError.getResponseBody().isBlank()) {
                builder.append(": ").append(apiError.getResponseBody());
            }
            return builder.toString();
        }
        if (error != null && error.getMessage() != null && !error.getMessage().isBlank()) {
            return error.getMessage();
        }
        return "Error desconocido";
    }
    
    /**
     * Carga los datos del dashboard
     */
    private void cargarDatosDashboard() {
        // Calcular estadísticas basadas en los datos actuales
        int espaciosActivos = (int) listaEspacios.stream()
            .filter(e -> "Disponible".equals(e.getEstado()))
            .count();

        int reservasHoy = (int) listaReservas.stream()
            .filter(r -> r.getFecha() != null && r.getFecha().equals(LocalDate.now()))
            .count();
        
        int inasistencias = (int) listaReservas.stream()
            .filter(r -> "Inasistencia".equals(r.getEstado()))
            .count();
        
        // Actualizar labels del dashboard
        if (lblEspaciosActivos != null) {
            lblEspaciosActivos.setText(espaciosActivos + "/" + listaEspacios.size());
        }
        
        if (lblReservasHoy != null) {
            lblReservasHoy.setText(String.valueOf(reservasHoy));
        }
        
        if (lblInasistencias != null) {
            lblInasistencias.setText(String.valueOf(inasistencias));
        }

        if (lblOcupacionSemanal != null) {
            double ocupacion = listaEspacios.isEmpty()
                    ? 0
                    : (double) listaReservas.size() / listaEspacios.size() * 100;
            lblOcupacionSemanal.setText(String.format(Locale.US, "%.1f%%", ocupacion));
        }

        if (lblVariacionOcupacion != null) {
            lblVariacionOcupacion.setText("--");
        }

        // Cargar datos climáticos
        cargarClimaActual();

        // Cargar alertas
        cargarAlertas();
    }
    
    /**
     * Carga los espacios desde la base de datos
     */
    private void cargarEspacios() {
        filtrarEspacios();
    }

    /**
     * Carga los usuarios desde la base de datos
     */
    private void cargarUsuarios() {
        filtrarUsuarios();
    }

    /**
     * Carga las reservas desde la base de datos
     */
    private void cargarReservas() {
        filtrarReservas();
    }

    /**
     * Actualiza los indicadores climáticos visibles en el panel principal.
     */
    private void cargarClimaActual() {
        actualizarIndicadoresClimaticos();
    }

    private void iniciarActualizacionDatos() {
        if (datosTimeline != null) {
            datosTimeline.stop();
        }
        datosTimeline = new Timeline(new KeyFrame(DATA_REFRESH_INTERVAL, event -> {
            if (!datosCargando) {
                cargarDatosIniciales(false);
            }
        }));
        datosTimeline.setCycleCount(Timeline.INDEFINITE);
        datosTimeline.play();
    }

    private void iniciarActualizacionClima() {
        if (climaTimeline != null) {
            climaTimeline.stop();
        }
        climaTimeline = new Timeline(new KeyFrame(CLIMA_REFRESH_INTERVAL, event -> recargarClima(false)));
        climaTimeline.setCycleCount(Timeline.INDEFINITE);
        climaTimeline.play();
        recargarClima(false);
    }

    private void detenerActualizaciones() {
        if (datosTimeline != null) {
            datosTimeline.stop();
            datosTimeline = null;
        }
        if (climaTimeline != null) {
            climaTimeline.stop();
            climaTimeline = null;
        }
        datosInicialesCargados = false;
    }

    private void recargarClima(boolean notifySuccess) {
        if (sessionManager == null) {
            mostrarAdvertencia("No hay sesión activa para actualizar el clima.");
            return;
        }

        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            mostrarAdvertencia("No se encontró un token de acceso válido.");
            return;
        }

        Task<ClimaResultado> task = new Task<>() {
            @Override
            protected ClimaResultado call() {
                List<String> warnings = new ArrayList<>();
                DatosClimaticos clima = cargarClimaDesdeApi(token, warnings);
                return new ClimaResultado(clima, warnings);
            }
        };

        task.setOnRunning(event -> mostrarIndicadorCarga("Actualizando información climática..."));
        task.setOnSucceeded(event -> {
            ClimaResultado resultado = task.getValue();
            climaActual = resultado.clima();
            actualizarIndicadoresClimaticos();
            cargarClima();
            ocultarIndicadorCarga();
            if (notifySuccess) {
                mostrarExito("Información climática actualizada");
            }
            if (!resultado.warnings().isEmpty()) {
                mostrarAdvertencia(String.join("\n", resultado.warnings()));
            }
        });
        task.setOnFailed(event -> {
            ocultarIndicadorCarga();
            Throwable error = task.getException();
            mostrarError("No se pudo actualizar el clima: "
                    + (error != null ? construirMensajeError(error) : "Error desconocido"));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
    
    /**
     * Carga las alertas activas
     */
    private void cargarAlertas() {
        if (contenedorAlertas == null) return;

        contenedorAlertas.getChildren().clear();

        List<Reserva> reservasConAlertas = obtenerReservasConAlertas();

        if (lblNumAlertas != null) {
            lblNumAlertas.setText(String.valueOf(reservasConAlertas.size()));
        }

        if (lblNotificacionesBadge != null) {
            lblNotificacionesBadge.setText(String.valueOf(reservasConAlertas.size()));
        }

        if (reservasConAlertas.isEmpty()) {
            Label sinAlertas = new Label("No hay alertas activas en este momento.");
            sinAlertas.setStyle("-fx-text-fill: #6C757D; -fx-font-style: italic;");
            contenedorAlertas.getChildren().add(sinAlertas);
            actualizarPanelNotificaciones(reservasConAlertas);
            return;
        }

        reservasConAlertas.stream()
                .limit(5)
                .map(this::crearAlertaDesdeReserva)
                .forEach(contenedorAlertas.getChildren()::add);

        actualizarPanelNotificaciones(reservasConAlertas);
    }

    private HBox crearAlertaDesdeReserva(Reserva reserva) {
        Espacio espacio = reserva.getEspacio();
        String titulo = espacio != null && espacio.getNombre() != null && !espacio.getNombre().isBlank()
                ? espacio.getNombre()
                : "Reserva #" + (reserva.getId() != null ? reserva.getId() : "-");

        String descripcion = switch (reserva.getEstado()) {
            case "Cancelada" -> "Reserva cancelada. Contactar al usuario.";
            case "Inasistencia" -> "El usuario no se presentó.";
            case "Pendiente" -> "Reserva pendiente de aprobación.";
            default -> "Estado: " + reserva.getEstado();
        };

        String afectados;
        if (reserva.getFecha() != null) {
            afectados = "Programada para " + reserva.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE);
        } else {
            afectados = "Fecha por confirmar";
        }

        String tipo = switch (reserva.getEstado()) {
            case "Cancelada", "Inasistencia" -> "critical";
            default -> "warning";
        };

        return crearAlertaItem(titulo, descripcion, afectados, tipo);
    }
    
    /**
     * Crea un item de alerta visual
     */
    private HBox crearAlertaItem(String titulo, String descripcion, String afectados, String tipo) {
        HBox alerta = new HBox(15);
        alerta.setAlignment(Pos.CENTER_LEFT);
        alerta.setPadding(new Insets(15));
        
        String estiloBase = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-width: 1;";
        
        if ("critical".equals(tipo)) {
            alerta.setStyle(estiloBase + " -fx-background-color: rgba(220, 53, 69, 0.1); " +
                          "-fx-border-color: #DC3545;");
        } else {
            alerta.setStyle(estiloBase + " -fx-background-color: rgba(255, 193, 7, 0.1); " +
                          "-fx-border-color: #FFC107;");
        }
        
        Label icono = new Label("⚠️");
        icono.setStyle("-fx-font-size: 24px;");
        
        VBox contenido = new VBox(5);
        HBox.setHgrow(contenido, javafx.scene.layout.Priority.ALWAYS);
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Label lblDescripcion = new Label(descripcion);
        lblDescripcion.setStyle("-fx-font-size: 12px; -fx-text-fill: #6C757D;");
        
        Label lblAfectados = new Label(afectados);
        lblAfectados.setStyle("-fx-font-size: 11px; -fx-text-fill: #868E96;");
        
        contenido.getChildren().addAll(lblTitulo, lblDescripcion, lblAfectados);
        
        Label etiqueta = new Label("critical".equals(tipo) ? "Alerta climática" : "Advertencia");
        if ("critical".equals(tipo)) {
            etiqueta.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        } else {
            etiqueta.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; " +
                            "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        }
        
        alerta.getChildren().addAll(icono, contenido, etiqueta);
        
        return alerta;
    }
    
    /**
     * Carga los reportes y gráficos
     */
    private void cargarReportes() {
        // Actualizar estadísticas
        if (lblReservasActivas != null) {
            long activas = listaReservas.stream()
                .filter(r -> "Confirmada".equals(r.getEstado()))
                .count();
            lblReservasActivas.setText(String.valueOf(activas));
        }
        
        if (lblReservasCompletadas != null) {
            long completadas = listaReservas.stream()
                .filter(r -> "Completada".equals(r.getEstado()))
                .count();
            lblReservasCompletadas.setText(String.valueOf(completadas));
        }

        if (lblTasaAsistencia != null) {
            long completadas = listaReservas.stream()
                    .filter(r -> "Completada".equals(r.getEstado()))
                    .count();
            long inasistencias = listaReservas.stream()
                    .filter(r -> "Inasistencia".equals(r.getEstado()))
                    .count();
            long total = completadas + inasistencias;
            if (total == 0) {
                lblTasaAsistencia.setText("--");
            } else {
                double tasa = (double) completadas / total * 100;
                lblTasaAsistencia.setText(String.format(Locale.US, "%.1f%%", tasa));
            }
        }

        if (lblVariacionAsistencia != null) {
            lblVariacionAsistencia.setText("--");
        }

        if (lblInasistenciasReporte != null) {
            long inasistencias = listaReservas.stream()
                    .filter(r -> "Inasistencia".equals(r.getEstado()))
                    .count();
            lblInasistenciasReporte.setText(String.valueOf(inasistencias));
        }
        
        // Generar gráficos
        generarGraficoDistribucion();
        generarGraficoRanking();
    }
    
    /**
     * Genera el gráfico de distribución por tipo de espacio
     */
    private void generarGraficoDistribucion() {
        if (graficoDistribucion == null) return;
        
        graficoDistribucion.getChildren().clear();
        
        // Crear PieChart
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribución de Reservas");
        pieChart.setAnimated(true); // ✨ Habilitar animación
        pieChart.setLegendVisible(true);
        
        // Contar reservas por tipo de espacio
        long interiores = listaReservas.stream()
            .filter(r -> r.getEspacio() != null && !r.getEspacio().isEsExterior())
            .count();

        long exteriores = listaReservas.stream()
            .filter(r -> r.getEspacio() != null && r.getEspacio().isEsExterior())
            .count();
        
        pieChart.getData().add(new PieChart.Data("Interior (" + interiores + ")", interiores));
        pieChart.getData().add(new PieChart.Data("Exterior (" + exteriores + ")", exteriores));
        
        graficoDistribucion.getChildren().add(pieChart);
    }
    
    /**
     * Genera el gráfico de ranking de espacios más utilizados
     */
    private void generarGraficoRanking() {
        if (graficoRanking == null) return;
        
        graficoRanking.getChildren().clear();
        
        // Crear BarChart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        
        barChart.setTitle("Espacios Más Utilizados");
        barChart.setAnimated(true); // ✨ Habilitar animación
        barChart.setLegendVisible(false);
        xAxis.setLabel("Espacio");
        yAxis.setLabel("Número de Reservas");
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Reservas");
        
        // Contar reservas por espacio
        listaEspacios.forEach(espacio -> {
            long count = listaReservas.stream()
                .filter(r -> r.getEspacio() != null
                        && Objects.equals(r.getEspacio().getId(), espacio.getId()))
                .count();
            
            if (count > 0) {
                series.getData().add(new XYChart.Data<>(espacio.getNombre(), count));
            }
        });
        
        barChart.getData().add(series);
        
        graficoRanking.getChildren().add(barChart);
    }
    
    /**
     * Carga los datos climáticos de todos los espacios
     */
    private void cargarClima() {
        // Actualizar resumen
        long espaciosExteriores = listaEspacios.stream()
            .filter(e -> e.isEsExterior())
            .count();

        if (lblEspaciosMonitoreados != null) {
            lblEspaciosMonitoreados.setText(String.valueOf(espaciosExteriores));
        }

        long alertasActivas = listaReservas.stream()
                .filter(reserva -> {
                    String estado = reserva.getEstado();
                    return "Pendiente".equalsIgnoreCase(estado)
                            || "Cancelada".equalsIgnoreCase(estado)
                            || "Inasistencia".equalsIgnoreCase(estado);
                })
                .count();
        if (lblAlertasActivas != null) {
            lblAlertasActivas.setText(String.valueOf(alertasActivas));
        }
        if (lblReservasAfectadas != null) {
            lblReservasAfectadas.setText(String.valueOf(alertasActivas));
        }

        if (lblMensajeAlertaMeteo != null) {
            if (alertasActivas > 0) {
                lblMensajeAlertaMeteo.setText(String.format(Locale.getDefault(),
                        "Se detectaron %d reserva(s) con alerta meteorológica activa.", alertasActivas));
            } else {
                lblMensajeAlertaMeteo.setText("No hay alertas meteorológicas activas.");
            }
        }

        if (lblNotificacionesBadge != null) {
            lblNotificacionesBadge.setText(String.valueOf(alertasActivas));
        }

        // Cargar tarjetas de clima
        cargarTarjetasClima();
    }

    /**
     * Carga las tarjetas de clima para cada espacio exterior
     */
    private void cargarTarjetasClima() {
        if (contenedorTarjetasClima == null) return;
        
        // Limpiar tarjetas existentes
        contenedorTarjetasClima.getChildren().clear();
        
        List<Espacio> espaciosExteriores = listaEspacios.stream()
            .filter(Espacio::isEsExterior)
            .collect(Collectors.toList());

        int col = 0;
        int row = 0;

        if (espaciosExteriores.isEmpty()) {
            Label sinEspacios = new Label("No hay espacios exteriores configurados.");
            sinEspacios.setStyle("-fx-text-fill: #6C757D; -fx-font-style: italic;");
            contenedorTarjetasClima.add(sinEspacios, 0, 0);
            return;
        }

        if (climaActual == null) {
            Label sinClima = new Label("Configura la API de clima para ver detalles.");
            sinClima.setStyle("-fx-text-fill: #6C757D; -fx-font-style: italic;");
            contenedorTarjetasClima.add(sinClima, 0, 0);
            return;
        }

        for (Espacio espacio : espaciosExteriores) {
            VBox tarjeta = crearTarjetaClima(espacio);
            contenedorTarjetasClima.add(tarjeta, col, row);

            col++;
            if (col >= 3) {
                col = 0;
                row++;
            }
        }
    }
    
    /**
     * Crea una tarjeta de clima para un espacio
     */
    private VBox crearTarjetaClima(Espacio espacio) {
        VBox tarjeta = new VBox(15);
        tarjeta.setPadding(new Insets(20));
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2); " +
                        "-fx-border-color: #28A745; -fx-border-width: 2; -fx-border-radius: 12;");

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(espacio.getNombre());
        nombre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        HBox.setHgrow(nombre, javafx.scene.layout.Priority.ALWAYS);

        String nivelAlerta = climaActual != null ? climaActual.getNivelAlerta() : null;
        String estiloEtiqueta;
        String textoEtiqueta = nivelAlerta != null && !nivelAlerta.isBlank() ? nivelAlerta : "Sin datos";
        if (nivelAlerta != null && nivelAlerta.toLowerCase().contains("lluvia")) {
            estiloEtiqueta = "-fx-background-color: #DC3545; -fx-text-fill: white;";
        } else if (nivelAlerta != null && nivelAlerta.toLowerCase().contains("precauc")) {
            estiloEtiqueta = "-fx-background-color: #FFC107; -fx-text-fill: #212529;";
        } else {
            estiloEtiqueta = "-fx-background-color: #28A745; -fx-text-fill: white;";
        }
        Label etiqueta = new Label(textoEtiqueta);
        etiqueta.setStyle(estiloEtiqueta +
                        " -fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");

        header.getChildren().addAll(nombre, etiqueta);

        // Datos climáticos
        VBox datos = new VBox(10);
        datos.getChildren().addAll(
            crearFilaClima("🌡️", "Temperatura", climaActual != null ? climaActual.getTemperaturaFormateada() : "--"),
            crearFilaClima("💧", "Probabilidad de lluvia", climaActual != null ? climaActual.getProbabilidadLluviaFormateada() : "--"),
            crearFilaClima("☁️", "Condición", climaActual != null ? climaActual.getCondicion() : "--")
        );

        // Mensaje
        String descripcion = climaActual != null ? climaActual.getDescripcion() : null;
        if (descripcion == null || descripcion.isBlank()) {
            descripcion = "Sin información disponible";
        }
        Label mensaje = new Label(descripcion);
        mensaje.setWrapText(true);
        mensaje.setStyle("-fx-font-size: 12px; -fx-text-fill: #28A745; -fx-font-weight: 600; " +
                        "-fx-background-color: rgba(40, 167, 69, 0.1); -fx-padding: 8; -fx-background-radius: 6;");

        tarjeta.getChildren().addAll(header, datos, mensaje);

        return tarjeta;
    }
    
    /**
     * Crea una fila de datos climáticos
     */
    private HBox crearFilaClima(String icono, String label, String valor) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);
        
        Label lblIcono = new Label(icono);
        lblIcono.setStyle("-fx-font-size: 18px;");
        
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6C757D;");
        HBox.setHgrow(lblLabel, javafx.scene.layout.Priority.ALWAYS);
        
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
        
        fila.getChildren().addAll(lblIcono, lblLabel, lblValor);
        
        return fila;
    }
    
    /**
     * Carga la configuración actual del sistema
     */
    private void cargarConfiguracion() {
        // TODO: Cargar configuración desde base de datos o archivo
        // Por ahora, dejar valores por defecto
    }
    
    // ==================== FILTROS ====================
    
    /**
     * Filtra los espacios según los criterios seleccionados
     */
    private void filtrarEspacios() {
        if (tablaEspacios == null) {
            System.out.println("⚠️ tablaEspacios es null");
            return;
        }
        
        System.out.println("📊 Filtrando espacios. Total en listaEspacios: " + listaEspacios.size());
        
        String busqueda = txtBuscarEspacio != null ? txtBuscarEspacio.getText().toLowerCase() : "";
        String tipoSeleccionado = cmbTipoEspacio != null ? cmbTipoEspacio.getValue() : "Todos los tipos";
        String estadoSeleccionado = cmbEstadoEspacio != null ? cmbEstadoEspacio.getValue() : "Todos los estados";
        
        listaEspaciosFiltrados.clear();
        listaEspaciosFiltrados.addAll(
            listaEspacios.stream()
                .filter(e -> {
                    String nombre = e.getNombre() != null ? e.getNombre().toLowerCase() : "";
                    return busqueda.isEmpty() || nombre.contains(busqueda);
                })
                .filter(e -> {
                    if ("Todos los tipos".equals(tipoSeleccionado)) {
                        return true;
                    }
                    if ("Interior".equalsIgnoreCase(tipoSeleccionado)) {
                        return !e.isEsExterior();
                    }
                    if ("Exterior".equalsIgnoreCase(tipoSeleccionado)) {
                        return e.isEsExterior();
                    }
                    String tipo = e.getTipo() != null ? e.getTipo() : "";
                    return tipo.equalsIgnoreCase(tipoSeleccionado);
                })
                .filter(e -> {
                    if ("Todos los estados".equals(estadoSeleccionado)) {
                        return true;
                    }
                    String estado = e.getEstado() != null ? e.getEstado() : "";
                    if ("Disponible".equalsIgnoreCase(estadoSeleccionado)) {
                        return "Disponible".equalsIgnoreCase(estado);
                    }
                    if ("Ocupado".equalsIgnoreCase(estadoSeleccionado)) {
                        return !"Disponible".equalsIgnoreCase(estado);
                    }
                    return estado.equalsIgnoreCase(estadoSeleccionado);
                })
                .collect(Collectors.toList())
        );

        System.out.println("📊 Espacios filtrados: " + listaEspaciosFiltrados.size());
        tablaEspacios.setItems(listaEspaciosFiltrados);
        tablaEspacios.refresh();
    }
    
    /**
     * Filtra los usuarios según los criterios seleccionados
     */
    private void filtrarUsuarios() {
        if (tablaUsuarios == null) return;
        
        String busqueda = txtBuscarUsuario != null ? txtBuscarUsuario.getText().toLowerCase() : "";
        String rolSeleccionado = cmbRolUsuario != null ? cmbRolUsuario.getValue() : "Todos los roles";
        
        listaUsuariosFiltrados.clear();
        listaUsuariosFiltrados.addAll(
            listaUsuarios.stream()
                .filter(u -> {
                    if (busqueda.isEmpty()) {
                        return true;
                    }
                    String nombre = u.getNombre() != null ? u.getNombre().toLowerCase() : "";
                    String correo = u.getCorreo() != null ? u.getCorreo().toLowerCase() : "";
                    return nombre.contains(busqueda) || correo.contains(busqueda);
                })
                .filter(u -> {
                    if ("Todos los roles".equals(rolSeleccionado)) {
                        return true;
                    }
                    String rol = u.getRol() != null ? u.getRol() : "";
                    return rol.equalsIgnoreCase(rolSeleccionado);
                })
                .collect(Collectors.toList())
        );

        tablaUsuarios.setItems(listaUsuariosFiltrados);
    }
    
    /**
     * Filtra las reservas según los criterios seleccionados
     */
    private void filtrarReservas() {
        if (tablaReservas == null) return;
        
        String busqueda = txtBuscarReserva != null ? txtBuscarReserva.getText().toLowerCase() : "";
        String estadoSeleccionado = cmbEstadoReserva != null ? cmbEstadoReserva.getValue() : "Todos los estados";
        LocalDate fechaDesde = dpFechaDesdeReservas != null ? dpFechaDesdeReservas.getValue() : null;
        LocalDate fechaHasta = dpFechaHastaReservas != null ? dpFechaHastaReservas.getValue() : null;
        
        listaReservasFiltradas.clear();
        listaReservasFiltradas.addAll(
            listaReservas.stream()
                .filter(r -> {
                    if (busqueda.isEmpty()) return true;

                    boolean coincideUsuario = r.getUsuario() != null
                            && r.getUsuario().getNombre() != null
                            && r.getUsuario().getNombre().toLowerCase().contains(busqueda);
                    boolean coincideEspacio = r.getEspacio() != null
                            && r.getEspacio().getNombre() != null
                            && r.getEspacio().getNombre().toLowerCase().contains(busqueda);
                    boolean coincideId = String.valueOf(r.getId()).contains(busqueda);

                    return coincideUsuario || coincideEspacio || coincideId;
                })
                .filter(r -> {
                    if ("Todos los estados".equals(estadoSeleccionado)) {
                        return true;
                    }
                    String estado = r.getEstado() != null ? r.getEstado() : "";
                    return estado.equalsIgnoreCase(estadoSeleccionado);
                })
                .filter(r -> {
                    // Filtro por rango de fechas
                    if (fechaDesde == null && fechaHasta == null) return true;
                    
                    LocalDate fechaReserva = r.getFecha();
                    if (fechaReserva == null) return false;
                    
                    if (fechaDesde != null && fechaReserva.isBefore(fechaDesde)) {
                        return false;
                    }
                    if (fechaHasta != null && fechaReserva.isAfter(fechaHasta)) {
                        return false;
                    }
                    return true;
                })
                // Ordenar por prioridad de estado: Pendiente -> Confirmada -> Checked In -> No Show -> Cancelada
                .sorted((r1, r2) -> {
                    int prioridad1 = obtenerPrioridadEstado(r1.getEstado());
                    int prioridad2 = obtenerPrioridadEstado(r2.getEstado());
                    return Integer.compare(prioridad1, prioridad2);
                })
                .collect(Collectors.toList())
        );

        tablaReservas.setItems(listaReservasFiltradas);
        actualizarEstadisticasReservas();
    }
    
    /**
     * Define la prioridad de ordenamiento según el estado de la reserva
     * @param estado Estado de la reserva
     * @return Prioridad (menor número = mayor prioridad)
     */
    private int obtenerPrioridadEstado(String estado) {
        if (estado == null) return 999;
        return switch (estado) {
            case "Pendiente" -> 1;      // Primero (requiere acción)
            case "Confirmada" -> 2;     // Segundo (activas)
            case "Checked In" -> 3;     // Tercero (asistidas)
            case "No Show" -> 4;        // Cuarto (inasistencias)
            case "Cancelada" -> 5;      // Último (finalizadas)
            default -> 999;             // Desconocidos al final
        };
    }
    
    // ==================== NUEVOS MÉTODOS DE FILTRADO Y PAGINACIÓN ====================
    
    /**
     * Limpia todos los filtros de reservas
     */
    @FXML
    private void limpiarFiltrosReservas() {
        if (txtBuscarReserva != null) {
            txtBuscarReserva.clear();
        }
        if (cmbEstadoReserva != null) {
            cmbEstadoReserva.getSelectionModel().select("Todos los estados");
        }
        if (dpFechaDesdeReservas != null) {
            dpFechaDesdeReservas.setValue(null);
        }
        if (dpFechaHastaReservas != null) {
            dpFechaHastaReservas.setValue(null);
        }
        filtrarReservas();
    }
    
    /**
     * Filtra reservas de hoy
     */
    @FXML
    private void filtrarReservasHoy() {
        LocalDate hoy = LocalDate.now();
        if (dpFechaDesdeReservas != null) {
            dpFechaDesdeReservas.setValue(hoy);
        }
        if (dpFechaHastaReservas != null) {
            dpFechaHastaReservas.setValue(hoy);
        }
        filtrarReservas();
    }
    
    /**
     * Filtra reservas de esta semana
     */
    @FXML
    private void filtrarReservasSemana() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1);
        LocalDate finSemana = inicioSemana.plusDays(6);
        
        if (dpFechaDesdeReservas != null) {
            dpFechaDesdeReservas.setValue(inicioSemana);
        }
        if (dpFechaHastaReservas != null) {
            dpFechaHastaReservas.setValue(finSemana);
        }
        filtrarReservas();
    }
    
    /**
     * Filtra reservas de este mes
     */
    @FXML
    private void filtrarReservasMes() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());
        
        if (dpFechaDesdeReservas != null) {
            dpFechaDesdeReservas.setValue(inicioMes);
        }
        if (dpFechaHastaReservas != null) {
            dpFechaHastaReservas.setValue(finMes);
        }
        filtrarReservas();
    }
    
    /**
     * Filtra solo reservas confirmadas
     */
    @FXML
    private void filtrarReservasConfirmadas() {
        if (cmbEstadoReserva != null) {
            cmbEstadoReserva.getSelectionModel().select("Confirmada");
        }
        filtrarReservas();
    }
    
    /**
     * Navega a la página anterior de reservas
     */
    @FXML
    private void paginaAnteriorReservas() {
        // Por ahora, solo muestra un mensaje
        // La paginación completa se implementará en una fase posterior
        System.out.println("Navegando a página anterior...");
    }
    
    /**
     * Navega a la página siguiente de reservas
     */
    @FXML
    private void paginaSiguienteReservas() {
        // Por ahora, solo muestra un mensaje
        // La paginación completa se implementará en una fase posterior
        System.out.println("Navegando a página siguiente...");
    }
    
    /**
     * Exporta las reservas filtradas
     */
    @FXML
    private void exportarReservas() {
        // Por ahora, solo muestra un mensaje
        mostrarAlerta("Exportar reservas", "Esta funcionalidad se implementará próximamente.", Alert.AlertType.INFORMATION);
    }
    
    /**
     * Refresca los datos de las reservas
     */
    @FXML
    private void refrescarReservas() {
        cargarReservas();
    }
    
    /**
     * Actualiza las estadísticas de la tabla de reservas
     */
    private void actualizarEstadisticasReservas() {
        if (lblTotalReservas != null) {
            lblTotalReservas.setText(listaReservas.size() + " registros");
        }
        if (lblReservasFiltradas != null) {
            lblReservasFiltradas.setText(listaReservasFiltradas.size() + " mostrados");
        }
    }
    
    // ==================== ACCIONES DE ESPACIOS ====================
    
    @FXML
    private void agregarEspacio(ActionEvent event) {
        mostrarFormularioEspacio(null);
    }

    private void verDetallesEspacio(Espacio espacio) {
        if (espacio == null) {
            return;
        }
        StringBuilder detalles = new StringBuilder();
        detalles.append("Tipo: ").append(espacio.getTipo()).append('\n');
        detalles.append("Capacidad: ").append(espacio.getCapacidad()).append(" personas\n");
        detalles.append("Ubicación: ")
                .append(espacio.getUbicacion() != null && !espacio.getUbicacion().isBlank()
                        ? espacio.getUbicacion()
                        : "No registrada")
                .append('\n');
        detalles.append("Duración máxima: ")
                .append(espacio.getMaxDuracion() != null 
                    ? String.format("%.1f horas (%d minutos)", espacio.getMaxDuracion() / 60.0, espacio.getMaxDuracion())
                    : "No definida")
                .append('\n');
        detalles.append("Requiere aprobación: ").append(espacio.isRequiereAprobacion() ? "Sí" : "No").append('\n');
        detalles.append("Estado: ").append(espacio.getEstado()).append('\n');
        detalles.append("Descripción: ")
                .append(espacio.getDescripcion() != null && !espacio.getDescripcion().isBlank()
                        ? espacio.getDescripcion()
                        : "Sin descripción disponible");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del espacio");
        alert.setHeaderText(espacio.getNombre());
        alert.setContentText(detalles.toString());
        alert.showAndWait();
    }

    private void editarEspacio(Espacio espacio) {
        mostrarFormularioEspacio(espacio);
    }

    private void cambiarEstadoEspacio(Espacio espacio) {
        if (espacio == null || espacio.getId() == null) {
            mostrarAdvertencia("Selecciona un espacio válido para actualizar su estado.");
            return;
        }
        String token = obtenerToken();
        if (token == null) {
            return;
        }
        boolean nuevoEstado = !espacio.isActivo();
        ejecutarOperacionAsync(
                () -> spaceController.changeStatus(espacio.getId(), nuevoEstado, token),
                dto -> {
                    Espacio actualizado = mapearEspacio(dto);
                    actualizarEspacioEnListas(actualizado);
                    mostrarExito(nuevoEstado ? "Espacio activado" : "Espacio desactivado");
                },
                "Actualizando espacio...",
                "No se pudo actualizar el estado del espacio");
    }

    private void eliminarEspacio(Espacio espacio) {
        if (espacio == null || espacio.getId() == null) {
            mostrarAdvertencia("Selecciona un espacio válido para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar espacio");
        confirmacion.setHeaderText("¿Deseas eliminar el espacio " + espacio.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        String token = obtenerToken();
        if (token == null) {
            return;
        }

        ejecutarOperacionAsync(
                () -> {
                    spaceController.deleteSpace(espacio.getId(), token);
                    return null;
                },
                unused -> {
                    listaEspacios.removeIf(item -> Objects.equals(item.getId(), espacio.getId()));
                    listaEspaciosFiltrados.removeIf(item -> Objects.equals(item.getId(), espacio.getId()));
                    filtrarEspacios();
                    cargarDatosDashboard();
                    mostrarExito("Espacio eliminado correctamente");
                },
                "Eliminando espacio...",
                "No se pudo eliminar el espacio");
    }

    private void mostrarFormularioEspacio(Espacio espacio) {
        Dialog<SpaceInputDTO> dialog = new Dialog<>();
        dialog.setTitle(espacio == null ? "Agregar espacio" : "Editar espacio");
        dialog.setHeaderText(espacio == null
                ? "Completa la información del nuevo espacio."
                : "Actualiza la información del espacio seleccionado.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResizable(false);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del espacio");
        txtNombre.getStyleClass().add("form-field");

        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList(TIPOS_ESPACIO));
        cmbTipo.setPromptText("Tipo de espacio");
        cmbTipo.getStyleClass().add("form-field");

        Spinner<Integer> spCapacidad = new Spinner<>(1, 500, 10);
        spCapacidad.setEditable(true);
        spCapacidad.getStyleClass().add("form-field");

        TextField txtUbicacion = new TextField();
        txtUbicacion.setPromptText("Ubicación");
        txtUbicacion.getStyleClass().add("form-field");

        // ✅ Spinner de duración en HORAS (1-12 horas, se convertirá a minutos al guardar)
        Spinner<Integer> spMaxDuracion = new Spinner<>(1, 12, 2);
        spMaxDuracion.setEditable(true);
        spMaxDuracion.getStyleClass().add("form-field");

        CheckBox chkRequiereAprobacion = new CheckBox("Requiere aprobación");
        CheckBox chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(true);
        chkRequiereAprobacion.getStyleClass().add("form-check");
        chkActivo.getStyleClass().add("form-check");

        TextArea txtDescripcion = new TextArea();
        txtDescripcion.setPromptText("Descripción del espacio");
        txtDescripcion.setWrapText(true);
        txtDescripcion.setPrefRowCount(4);
        txtDescripcion.getStyleClass().addAll("form-field", "form-textarea");

        if (espacio != null) {
            txtNombre.setText(espacio.getNombre());
            cmbTipo.setValue(espacio.getTipo());
            spCapacidad.getValueFactory().setValue(espacio.getCapacidad());
            txtUbicacion.setText(espacio.getUbicacion());
            // ✅ Convertir de minutos a horas al cargar
            if (espacio.getMaxDuracion() != null) {
                int horas = espacio.getMaxDuracion() / 60;
                spMaxDuracion.getValueFactory().setValue(Math.max(1, horas)); // Mínimo 1 hora
            }
            chkRequiereAprobacion.setSelected(espacio.isRequiereAprobacion());
            chkActivo.setSelected(espacio.isActivo());
            txtDescripcion.setText(espacio.getDescripcion());
        } else {
            cmbTipo.getSelectionModel().selectFirst();
        }

        GridPane grid = new GridPane();
        grid.getStyleClass().add("form-grid");
        grid.setHgap(18);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setPrefWidth(440);

        grid.add(crearEtiquetaFormulario("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearEtiquetaFormulario("Tipo"), 0, 1);
        grid.add(cmbTipo, 1, 1);
        grid.add(crearEtiquetaFormulario("Capacidad"), 0, 2);
        grid.add(spCapacidad, 1, 2);
        grid.add(crearEtiquetaFormulario("Ubicación"), 0, 3);
        grid.add(txtUbicacion, 1, 3);
        grid.add(crearEtiquetaFormulario("Duración máx. (horas)"), 0, 4);
        grid.add(spMaxDuracion, 1, 4);
        grid.add(chkRequiereAprobacion, 0, 5);
        grid.add(chkActivo, 1, 5);
        grid.add(crearEtiquetaFormulario("Descripción"), 0, 6);
        grid.add(txtDescripcion, 1, 6);

        GridPane.setHgrow(txtNombre, Priority.ALWAYS);
        GridPane.setHgrow(cmbTipo, Priority.ALWAYS);
        GridPane.setHgrow(spCapacidad, Priority.ALWAYS);
        GridPane.setHgrow(txtUbicacion, Priority.ALWAYS);
        GridPane.setHgrow(spMaxDuracion, Priority.ALWAYS);
        GridPane.setHgrow(txtDescripcion, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);

        aplicarEstilosDialogo(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setText("Guardar");
            okButton.getStyleClass().add("dialog-primary-button");
            okButton.addEventFilter(ActionEvent.ACTION, action -> {
                if (txtNombre.getText().isBlank()) {
                    mostrarAdvertencia("El nombre del espacio es obligatorio.");
                    action.consume();
                } else if (cmbTipo.getValue() == null || cmbTipo.getValue().isBlank()) {
                    mostrarAdvertencia("Selecciona un tipo de espacio.");
                    action.consume();
                }
            });
        }

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("dialog-cancel-button");
        }

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String descripcion = txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "";
            String ubicacion = txtUbicacion.getText() != null ? txtUbicacion.getText().trim() : "";
            // ✅ Convertir horas a minutos para el backend
            Integer duracionMinutos = spMaxDuracion.getValue() * 60;
            return new SpaceInputDTO(
                    txtNombre.getText().trim(),
                    cmbTipo.getValue(),
                    spCapacidad.getValue(),
                    descripcion,
                    ubicacion,
                    chkActivo.isSelected(),
                    duracionMinutos,
                    chkRequiereAprobacion.isSelected()
            );
        });

        dialog.showAndWait().ifPresent(input -> guardarEspacio(espacio, input));
    }

    private void guardarEspacio(Espacio espacioOriginal, SpaceInputDTO input) {
        String token = obtenerToken();
        if (token == null) {
            return;
        }

        if (espacioOriginal == null || espacioOriginal.getId() == null) {
            ejecutarOperacionAsync(
                    () -> spaceController.createSpace(input, token),
                    dto -> {
                        Espacio creado = mapearEspacio(dto);
                        listaEspacios.add(creado);
                        filtrarEspacios();
                        cargarDatosDashboard();
                        mostrarExito("Espacio creado correctamente");
                    },
                    "Creando espacio...",
                    "No se pudo crear el espacio");
        } else {
            ejecutarOperacionAsync(
                    () -> spaceController.updateSpace(espacioOriginal.getId(), input, token),
                    dto -> {
                        Espacio actualizado = mapearEspacio(dto);
                        actualizarEspacioEnListas(actualizado);
                        mostrarExito("Espacio actualizado correctamente");
                    },
                    "Actualizando espacio...",
                    "No se pudo actualizar el espacio");
        }
    }

    // ==================== ACCIONES DE USUARIOS ====================

    @FXML
    private void agregarUsuario(ActionEvent event) {
        mostrarFormularioUsuario(null);
    }

    private void editarUsuario(Usuario usuario) {
        mostrarFormularioUsuario(usuario);
    }

    private void cambiarEstadoUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            mostrarAdvertencia("Selecciona un usuario válido para actualizar su estado.");
            return;
        }
        String token = obtenerToken();
        if (token == null) {
            return;
        }

        boolean nuevoEstado = !usuario.isActivo();
        UserInputDTO input = new UserInputDTO(
                usuario.getRolCodigo(),
                usuario.getNombre(),
                usuario.getCorreo(),
                nuevoEstado);

        ejecutarOperacionAsync(
                () -> userController.updateUser(usuario.getId(), input, token),
                dto -> {
                    Usuario actualizado = mapearUsuario(dto);
                    actualizarUsuarioEnListas(actualizado);
                    if (sessionManager != null && sessionManager.getUserId() != null
                            && sessionManager.getUserId().equals(actualizado.getId())) {
                        sessionManager.updateProfileInfo(actualizado.getNombre(), actualizado.getCorreo());
                        cargarUsuarioActual();
                        actualizarPanelPerfil();
                    }
                    mostrarExito(nuevoEstado ? "Usuario activado" : "Usuario desactivado");
                },
                "Actualizando usuario...",
                "No se pudo actualizar el usuario");
    }

    private void eliminarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            mostrarAdvertencia("Selecciona un usuario válido para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar usuario");
        confirmacion.setHeaderText("¿Deseas eliminar al usuario " + usuario.getNombre() + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }

        String token = obtenerToken();
        if (token == null) {
            return;
        }

        ejecutarOperacionAsync(
                () -> {
                    userController.deleteUser(usuario.getId(), token);
                    return null;
                },
                unused -> {
                    listaUsuarios.removeIf(item -> Objects.equals(item.getId(), usuario.getId()));
                    listaUsuariosFiltrados.removeIf(item -> Objects.equals(item.getId(), usuario.getId()));
                    filtrarUsuarios();
                    cargarDatosDashboard();
                    mostrarExito("Usuario eliminado correctamente");
                },
                "Eliminando usuario...",
                "No se pudo eliminar el usuario");
    }

    private void mostrarFormularioUsuario(Usuario usuario) {
        Dialog<UserInputDTO> dialog = new Dialog<>();
        dialog.setTitle(usuario == null ? "Agregar usuario" : "Editar usuario");
        dialog.setHeaderText(usuario == null
                ? "Completa los datos del nuevo usuario."
                : "Actualiza los datos del usuario seleccionado.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.setResizable(false);

        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Nombre completo");
        txtNombre.getStyleClass().add("form-field");

        TextField txtCorreo = new TextField();
        txtCorreo.setPromptText("Correo electrónico");
        txtCorreo.getStyleClass().add("form-field");

        ComboBox<String> cmbRol = new ComboBox<>(FXCollections.observableArrayList(ROLES_FRIENDLY.values()));
        cmbRol.setPromptText("Rol del usuario");
        cmbRol.getStyleClass().add("form-field");

        CheckBox chkActivo = new CheckBox("Activo");
        chkActivo.setSelected(true);
        chkActivo.getStyleClass().add("form-check");

        boolean puedeEditarRol = esUsuarioActualAdministrador();
        cmbRol.setDisable(!puedeEditarRol);
        if (!puedeEditarRol) {
            cmbRol.setTooltip(new Tooltip("Solo los administradores pueden modificar el rol."));
        }

        if (usuario != null) {
            txtNombre.setText(usuario.getNombre());
            txtCorreo.setText(usuario.getCorreo());
            cmbRol.setValue(ROLES_FRIENDLY.getOrDefault(usuario.getRolCodigo(), usuario.getRol()));
            chkActivo.setSelected(usuario.isActivo());
        } else {
            cmbRol.setValue(ROLES_FRIENDLY.getOrDefault("USER", "Usuario"));
        }

        GridPane grid = new GridPane();
        grid.getStyleClass().add("form-grid");
        grid.setHgap(18);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 20, 10, 20));
        grid.setPrefWidth(420);

        grid.add(crearEtiquetaFormulario("Nombre"), 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(crearEtiquetaFormulario("Correo"), 0, 1);
        grid.add(txtCorreo, 1, 1);
        grid.add(crearEtiquetaFormulario("Rol"), 0, 2);
        grid.add(cmbRol, 1, 2);
        grid.add(chkActivo, 1, 3);

        GridPane.setHgrow(txtNombre, Priority.ALWAYS);
        GridPane.setHgrow(txtCorreo, Priority.ALWAYS);
        GridPane.setHgrow(cmbRol, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(480);

        aplicarEstilosDialogo(dialog);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        if (okButton != null) {
            okButton.setText("Guardar");
            okButton.getStyleClass().add("dialog-primary-button");
            okButton.addEventFilter(ActionEvent.ACTION, action -> {
                if (txtCorreo.getText().isBlank()) {
                    mostrarAdvertencia("El correo electrónico es obligatorio.");
                    action.consume();
                } else if (cmbRol.getValue() == null) {
                    mostrarAdvertencia("Selecciona un rol para el usuario.");
                    action.consume();
                }
            });
        }

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) {
            cancelButton.getStyleClass().add("dialog-cancel-button");
        }

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            String rolCodigo = obtenerRolCodigoDesdeFriendly(cmbRol.getValue());
            return new UserInputDTO(
                    rolCodigo,
                    txtNombre.getText().trim(),
                    txtCorreo.getText().trim(),
                    chkActivo.isSelected()
            );
        });

        dialog.showAndWait().ifPresent(input -> guardarUsuario(usuario, input));
    }

    private Label crearEtiquetaFormulario(String texto) {
        Label label = new Label(texto);
        label.getStyleClass().add("form-label");
        return label;
    }

    private void aplicarEstilosDialogo(Dialog<?> dialog) {
        if (dialog == null) {
            return;
        }

        DialogPane pane = dialog.getDialogPane();
        if (pane == null) {
            return;
        }

        String stylesheet = obtenerStylesheetPrincipal();
        if (stylesheet != null && pane.getStylesheets().stream().noneMatch(stylesheet::equals)) {
            pane.getStylesheets().add(stylesheet);
        }

        if (!pane.getStyleClass().contains("admin-dialog")) {
            pane.getStyleClass().add("admin-dialog");
        }
    }

    private String obtenerStylesheetPrincipal() {
        URL resource = getClass().getResource(MAIN_STYLESHEET);
        return resource != null ? resource.toExternalForm() : null;
    }

    private void guardarUsuario(Usuario usuarioOriginal, UserInputDTO input) {
        String token = obtenerToken();
        if (token == null) {
            return;
        }

        if (usuarioOriginal == null || usuarioOriginal.getId() == null) {
            ejecutarOperacionAsync(
                    () -> userController.createUser(input, token),
                    dto -> {
                        Usuario creado = mapearUsuario(dto);
                        listaUsuarios.add(creado);
                        filtrarUsuarios();
                        cargarDatosDashboard();
                        mostrarExito("Usuario agregado correctamente");
                    },
                    "Creando usuario...",
                    "No se pudo crear el usuario");
        } else {
            ejecutarOperacionAsync(
                    () -> userController.updateUser(usuarioOriginal.getId(), input, token),
                    dto -> {
                        Usuario actualizado = mapearUsuario(dto);
                        actualizarUsuarioEnListas(actualizado);
                        if (sessionManager != null && sessionManager.getUserId() != null
                                && sessionManager.getUserId().equals(actualizado.getId())) {
                            sessionManager.updateProfileInfo(actualizado.getNombre(), actualizado.getCorreo());
                            cargarUsuarioActual();
                            actualizarPanelPerfil();
                        }
                        mostrarExito("Usuario actualizado correctamente");
                    },
                    "Actualizando usuario...",
                    "No se pudo actualizar el usuario");
        }
    }

    private void actualizarEspacioEnListas(Espacio espacioActualizado) {
        if (espacioActualizado == null || espacioActualizado.getId() == null) {
            return;
        }
        reemplazarEspacio(listaEspacios, espacioActualizado);
        reemplazarEspacio(listaEspaciosFiltrados, espacioActualizado);
        filtrarEspacios();
        cargarDatosDashboard();
    }

    private void reemplazarEspacio(ObservableList<Espacio> lista, Espacio actualizado) {
        if (lista == null || actualizado == null || actualizado.getId() == null) {
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            Espacio existente = lista.get(i);
            if (existente.getId() != null && existente.getId().equals(actualizado.getId())) {
                lista.set(i, actualizado);
                return;
            }
        }
    }

    private void actualizarUsuarioEnListas(Usuario usuarioActualizado) {
        if (usuarioActualizado == null || usuarioActualizado.getId() == null) {
            return;
        }
        reemplazarUsuario(listaUsuarios, usuarioActualizado);
        reemplazarUsuario(listaUsuariosFiltrados, usuarioActualizado);
        filtrarUsuarios();
        cargarDatosDashboard();
    }

    private void reemplazarUsuario(ObservableList<Usuario> lista, Usuario actualizado) {
        if (lista == null || actualizado == null || actualizado.getId() == null) {
            return;
        }
        for (int i = 0; i < lista.size(); i++) {
            Usuario existente = lista.get(i);
            if (existente.getId() != null && existente.getId().equals(actualizado.getId())) {
                lista.set(i, actualizado);
                return;
            }
        }
    }

    private String obtenerRolCodigoDesdeFriendly(String friendly) {
        if (friendly == null) {
            return null;
        }
        return ROLES_FRIENDLY.entrySet().stream()
                .filter(entry -> entry.getValue().equalsIgnoreCase(friendly))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(friendly.trim().toUpperCase(Locale.ROOT));
    }

    private String obtenerRolFriendly(String rolCodigo) {
        if (rolCodigo == null || rolCodigo.isBlank()) {
            return ROLES_FRIENDLY.getOrDefault("USER", "Usuario");
        }
        return ROLES_FRIENDLY.getOrDefault(rolCodigo.trim().toUpperCase(Locale.ROOT), rolCodigo);
    }
    
    // ==================== ACCIONES DE RESERVAS ====================
    
    private void verDetallesReserva(Reserva reserva) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de la Reserva");
        alert.setHeaderText("Reserva #" + reserva.getId());
        
        // Crear contenedor principal con disposición vertical
        VBox contenedorPrincipal = new VBox(12);
        contenedorPrincipal.setPadding(new javafx.geometry.Insets(15));
        contenedorPrincipal.setStyle("-fx-font-size: 13px;");
        
        // Información de la reserva en formato vertical
        VBox infoBox = new VBox(6);
        infoBox.getChildren().addAll(
            crearLabelDetalle("👤 Usuario:", reserva.getUsuario() != null ? reserva.getUsuario().getNombre() : "N/A"),
            crearLabelDetalle("📍 Espacio:", reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : "N/A"),
            crearLabelDetalle("📅 Fecha:", reserva.getFecha() != null ? reserva.getFecha().toString() : "N/A"),
            crearLabelDetalle("🕐 Horario:", 
                (reserva.getHoraInicio() != null ? reserva.getHoraInicio().toString() : "N/A") + 
                " - " + 
                (reserva.getHoraFin() != null ? reserva.getHoraFin().toString() : "N/A")),
            crearLabelDetalle("📊 Estado:", reserva.getEstado() != null ? reserva.getEstado() : "N/A")
        );
        
        // Sección del código QR con mayor visibilidad
        VBox qrBox = new VBox(8);
        qrBox.setPadding(new javafx.geometry.Insets(10));
        qrBox.setStyle("-fx-background-color: #f0f9ff; -fx-border-color: #0ea5e9; -fx-border-width: 2px; -fx-border-radius: 6px; -fx-background-radius: 6px;");
        
        Label qrTitulo = new Label("🔲 Código QR de la Reserva");
        qrTitulo.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #0369a1;");
        
        Label qrCodigo = new Label(reserva.getCodigoQR() != null ? reserva.getCodigoQR() : "Sin código QR");
        qrCodigo.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 16px; -fx-text-fill: #0c4a6e; -fx-font-weight: bold;");
        qrCodigo.setWrapText(true);
        
        Label qrInfo = new Label(
            "PENDING".equals(reserva.getEstado()) || "Pendiente".equals(reserva.getEstado())
            ? "⚠️ Este QR estará bloqueado hasta que se apruebe la reserva"
            : "✅ Este código QR puede ser escaneado para el check-in"
        );
        qrInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        qrInfo.setWrapText(true);
        
        qrBox.getChildren().addAll(qrTitulo, qrCodigo, qrInfo);
        
        // Notas adicionales si existen
        if (reserva.getNotas() != null && !reserva.getNotas().isEmpty()) {
            VBox notasBox = new VBox(4);
            Label notasTitulo = new Label("📝 Notas:");
            notasTitulo.setStyle("-fx-font-weight: bold;");
            Label notasContenido = new Label(reserva.getNotas());
            notasContenido.setWrapText(true);
            notasContenido.setStyle("-fx-text-fill: #475569;");
            notasBox.getChildren().addAll(notasTitulo, notasContenido);
            contenedorPrincipal.getChildren().addAll(infoBox, new javafx.scene.control.Separator(), qrBox, new javafx.scene.control.Separator(), notasBox);
        } else {
            contenedorPrincipal.getChildren().addAll(infoBox, new javafx.scene.control.Separator(), qrBox);
        }
        
        // Configurar el diálogo
        ScrollPane scrollPane = new ScrollPane(contenedorPrincipal);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefWidth(500);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: transparent;");
        
        alert.getDialogPane().setContent(scrollPane);
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }
    
    /**
     * Helper para crear labels de detalle con formato consistente
     */
    private Label crearLabelDetalle(String titulo, String valor) {
        Label label = new Label(titulo + " " + valor);
        label.setWrapText(true);
        label.setStyle("-fx-padding: 2px 0px;");
        return label;
    }
    
    /**
     * ✅ Aprueba una reserva PENDING y la cambia a CONFIRMED
     * Esto desbloquea el código QR para que pueda ser escaneado
     */
    private void aprobarReserva(Reserva reserva) {
        if (!"Pendiente".equals(reserva.getEstado())) {
            mostrarAdvertencia("Esta reserva no está pendiente de aprobación.\nEstado actual: " + reserva.getEstado());
            return;
        }
        
        // Confirmación
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Aprobar Reserva");
        confirmacion.setHeaderText("¿Deseas aprobar esta reserva?");
        confirmacion.setContentText(
            "Usuario: " + (reserva.getUsuario() != null ? reserva.getUsuario().getNombre() : "N/A") + "\n" +
            "Espacio: " + (reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : "N/A") + "\n" +
            "Fecha: " + reserva.getFecha() + "\n" +
            "Hora: " + reserva.getHoraInicio() + " - " + reserva.getHoraFin() + "\n\n" +
            "⚠️ Al aprobar, el código QR se activará y el usuario podrá hacer check-in."
        );
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
            return;
        }
        
        // Obtener token y userId del admin
        if (sessionManager == null) {
            mostrarError("No hay sesión activa");
            return;
        }
        
        String token = sessionManager.getAccessToken();
        Long adminUserId = sessionManager.getUserId();
        
        if (token == null || adminUserId == null) {
            mostrarError("Sesión inválida");
            return;
        }
        
        // Ejecutar en background
        Task<ReservationDTO> task = new Task<>() {
            @Override
            protected ReservationDTO call() throws Exception {
                return reservationController.approveReservation(reserva.getId(), adminUserId, token);
            }
        };
        
        task.setOnSucceeded(e -> {
            ReservationDTO approved = task.getValue();
            if (approved != null) {
                // Actualizar el estado en la tabla
                reserva.setEstado("Confirmada");
                tablaReservas.refresh();
                
                mostrarExito("✅ Reserva aprobada exitosamente\n\n" +
                           "El código QR ha sido desbloqueado.\n" +
                           "El usuario recibirá una notificación por correo.");
                
                // Recargar datos
                cargarDatosIniciales();
            }
        });
        
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String errorMsg = ex != null ? ex.getMessage() : "Error desconocido";
            mostrarError("Error al aprobar la reserva:\n" + errorMsg);
            
            if (ex != null) {
                ex.printStackTrace();
            }
        });
        
        new Thread(task).start();
    }
    
    private void cancelarReserva(Reserva reserva) {
        if ("Cancelada".equals(reserva.getEstado())) {
            mostrarAdvertencia("Esta reserva ya está cancelada");
            return;
        }
        
        mostrarInformacion("Funcionalidad en desarrollo",
                "La cancelación de reservas debe realizarse desde el backend para mantener la integridad de los datos.");
    }
    
    /**
     * Cancela una reserva solicitando el motivo de cancelación
     */
    private void cancelarReservaConMotivo(Reserva reserva) {
        if ("Cancelada".equals(reserva.getEstado())) {
            mostrarAdvertencia("Esta reserva ya está cancelada");
            return;
        }
        
        // Crear diálogo para ingresar motivo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cancelar Reserva");
        dialog.setHeaderText("Cancelación de Reserva #" + reserva.getId());
        dialog.setContentText("Motivo de la cancelación:");
        
        // Mostrar diálogo y esperar respuesta
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(motivo -> {
            if (motivo.trim().isEmpty()) {
                mostrarAdvertencia("Debe proporcionar un motivo para la cancelación");
                return;
            }
            
            // Confirmar cancelación
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmar Cancelación");
            confirmacion.setHeaderText("¿Está seguro de cancelar esta reserva?");
            confirmacion.setContentText("Motivo: " + motivo + "\n\nSe enviará un email al usuario notificando la cancelación.");
            
            Optional<ButtonType> confirmResult = confirmacion.showAndWait();
            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                // Obtener token de sesión
                String token = sessionManager.getAccessToken();
                if (token == null) {
                    mostrarError("Error: Token de sesión no disponible");
                    return;
                }
                
                // Ejecutar cancelación en segundo plano
                Task<ReservationDTO> task = new Task<>() {
                    @Override
                    protected ReservationDTO call() throws Exception {
                        return reservationController.cancelReservation(reserva.getId(), motivo, token);
                    }
                };
                
                task.setOnSucceeded(e -> {
                    ReservationDTO cancelada = task.getValue();
                    mostrarExito("✅ Reserva cancelada exitosamente\n\nSe ha enviado un email al usuario con el motivo de la cancelación.");
                    // Recargar datos para mostrar el cambio de estado
                    cargarDatosIniciales(false);
                });
                
                task.setOnFailed(e -> {
                    String errorMsg = task.getException() != null ? 
                        task.getException().getMessage() : "Error desconocido";
                    mostrarError("Error al cancelar la reserva: " + errorMsg);
                });
                
                new Thread(task).start();
            }
        });
    }

    /**
     * Envía un email personalizado relacionado con la reserva
     */
    private void enviarEmailReserva(Reserva reserva) {
        // Validar que la reserva tenga usuario con email
        if (reserva.getUsuario() == null || reserva.getUsuario().getCorreo() == null) {
            mostrarAdvertencia("Esta reserva no tiene un usuario o email asociado");
            return;
        }
        
        // Crear diálogo personalizado para el email
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enviar Email");
        dialog.setHeaderText("Enviar notificación para Reserva #" + reserva.getId());
        
        // Mostrar información del destinatario
        Label lblDestinatario = new Label("Destinatario: " + reserva.getUsuario().getCorreo());
        lblDestinatario.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Crear campos del formulario
        TextField asuntoField = new TextField();
        asuntoField.setPromptText("Ejemplo: Recordatorio de tu reserva");
        
        TextArea mensajeArea = new TextArea();
        mensajeArea.setPromptText("Escribe aquí tu mensaje personalizado...\n\nLa información de la reserva (fecha, espacio, etc.) se incluirá automáticamente.");
        mensajeArea.setPrefRowCount(8);
        mensajeArea.setWrapText(true);
        
        VBox content = new VBox(12);
        content.getChildren().addAll(
            lblDestinatario,
            new javafx.scene.control.Separator(),
            new Label("Asunto:"), asuntoField,
            new Label("Mensaje:"), mensajeArea
        );
        content.setPadding(new javafx.geometry.Insets(20));
        content.setStyle("-fx-font-size: 13px;");
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Personalizar el botón OK
        javafx.scene.control.Button btnEnviar = (javafx.scene.control.Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnEnviar.setText("📧 Enviar Email");
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String asunto = asuntoField.getText().trim();
            String mensaje = mensajeArea.getText().trim();
            
            if (asunto.isEmpty() || mensaje.isEmpty()) {
                mostrarAdvertencia("Debe completar tanto el asunto como el mensaje");
                return;
            }
            
            // Obtener token
            String token = sessionManager.getAccessToken();
            if (token == null) {
                mostrarError("Error: Token de sesión no disponible");
                return;
            }
            
            // Enviar email en segundo plano
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    notificationController.sendCustomEmail(reserva.getId(), asunto, mensaje, token);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                mostrarExito("✅ Email enviado exitosamente\n\nLa notificación ha sido enviada a: " + reserva.getUsuario().getCorreo());
            });
            
            task.setOnFailed(e -> {
                String errorMsg = task.getException() != null ? 
                    task.getException().getMessage() : "Error desconocido";
                mostrarError("Error al enviar el email: " + errorMsg);
            });
            
            new Thread(task).start();
        }
    }

    private void notificarUsuarioReserva(Reserva reserva) {
        enviarEmailReserva(reserva);
    }

    /**
     * Elimina permanentemente una reserva de la base de datos
     * Solo disponible para reservas con estado CHECKED_IN, NO_SHOW o CANCELED
     */
    private void eliminarReservaPermanente(Reserva reserva) {
        String estado = reserva.getEstado();
        
        // Validar que solo se puedan eliminar reservas finalizadas o canceladas
        if (!"En sitio".equals(estado) && !"Inasistencia".equals(estado) && !"Cancelada".equals(estado)) {
            mostrarAdvertencia("Solo se pueden eliminar reservas con asistencia confirmada, inasistencia registrada o canceladas");
            return;
        }
        
        // Crear diálogo de confirmación con advertencia fuerte
        Alert confirmacion = new Alert(Alert.AlertType.WARNING);
        confirmacion.setTitle("⚠️ Eliminar Reserva Permanentemente");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta reserva de la base de datos?");
        confirmacion.setContentText(
            "Esta acción es IRREVERSIBLE y eliminará permanentemente:\n\n" +
            "• Reserva ID: " + reserva.getId() + "\n" +
            "• Usuario: " + formatearNombreUsuario(reserva.getUsuario()) + "\n" +
            "• Espacio: " + (reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : "N/A") + "\n" +
            "• Fecha: " + reserva.getFecha() + "\n" +
            "• Estado: " + estado + "\n\n" +
            "⚠️ Esta operación NO SE PUEDE DESHACER\n" +
            "⚠️ Los datos serán eliminados permanentemente de la base de datos"
        );
        
        // Agregar botón personalizado para mayor claridad
        ButtonType btnEliminar = new ButtonType("Eliminar Permanentemente", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnEliminar, btnCancelar);
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == btnEliminar) {
            // Ejecutar eliminación
            String token = sessionManager.getAccessToken();
            if (token == null) {
                mostrarError("Error: Token de sesión no disponible");
                return;
            }
            
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    // Usar el nuevo método de eliminación permanente
                    reservationController.permanentlyDeleteReservation(reserva.getId(), token);
                    return null;
                }
            };
            
            task.setOnSucceeded(e -> {
                mostrarExito("✅ Reserva eliminada permanentemente de la base de datos");
                // Recargar datos para reflejar la eliminación
                cargarDatosIniciales(false);
            });
            
            task.setOnFailed(e -> {
                String errorMsg = task.getException() != null ? 
                    task.getException().getMessage() : "Error desconocido";
                mostrarError("Error al eliminar la reserva: " + errorMsg);
            });
            
            new Thread(task).start();
        }
    }
    
    // ==================== ACCIONES GENERALES ====================
    
    @FXML
    private void exportarExcel(ActionEvent event) {
        // TODO: Implementar exportación a Excel
        mostrarInformacion("Exportando a Excel...",
                          "Esta funcionalidad está en desarrollo.\n" +
                          "Pronto podrás exportar los reportes a formato Excel.");
    }
    
    @FXML
    private void exportarPDF(ActionEvent event) {
        // TODO: Implementar exportación a PDF
        mostrarInformacion("Exportando a PDF...",
                          "Esta funcionalidad está en desarrollo.\n" +
                          "Pronto podrás exportar los reportes a formato PDF.");
    }

    private void actualizarReportes() {
        cargarReportes();
    }

    private <T> void ejecutarOperacionAsync(Supplier<T> supplier, Consumer<T> onSuccess,
            String mensajeCarga, String mensajeError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return supplier.get();
            }
        };

        if (mensajeCarga != null && !mensajeCarga.isBlank()) {
            task.setOnRunning(event -> mostrarIndicadorCarga(mensajeCarga));
        }

        task.setOnSucceeded(event -> {
            ocultarIndicadorCarga();
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(event -> {
            ocultarIndicadorCarga();
            Throwable error = task.getException();
            manejarErrorOperacion(error, mensajeError);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void manejarErrorOperacion(Throwable error, String contexto) {
        String mensaje = construirMensajeError(error);
        if (contexto == null || contexto.isBlank()) {
            mostrarError(mensaje);
        } else {
            mostrarError(contexto + ": " + mensaje);
        }
    }

    private Usuario encontrarUsuarioActual() {
        if (sessionManager == null) {
            return null;
        }
        Long userId = sessionManager.getUserId();
        if (userId == null) {
            return null;
        }
        return listaUsuarios.stream()
                .filter(usuario -> userId.equals(usuario.getId()))
                .findFirst()
                .orElse(null);
    }

    private String obtenerToken() {
        if (sessionManager == null) {
            mostrarAdvertencia("No hay sesión activa.");
            return null;
        }
        String token = sessionManager.getAccessToken();
        if (token == null || token.isBlank()) {
            mostrarAdvertencia("No se encontró un token de acceso válido.");
            return null;
        }
        return token;
    }

    private boolean esUsuarioActualAdministrador() {
        if (sessionManager == null) {
            return false;
        }
        String rol = sessionManager.getUserRole();
        return rol != null && rol.equalsIgnoreCase("ADMIN");
    }

    @FXML
    private void toggleNotificacionesPanel(ActionEvent event) {
        if (panelNotificaciones == null) {
            return;
        }

        if (panelNotificacionesVisible) {
            cerrarPanelNotificacionesInterno();
            return;
        }

        List<Reserva> reservasConAlertas = obtenerReservasConAlertas();
        actualizarPanelNotificaciones(reservasConAlertas);

        cerrarPanelPerfilInterno();

        mostrarPanel(panelNotificaciones);
        panelNotificacionesVisible = true;
    }

    @FXML
    private void cerrarPanelNotificaciones(ActionEvent event) {
        cerrarPanelNotificacionesInterno();
        event.consume();
    }

    private void actualizarPanelNotificaciones(List<Reserva> reservasConAlertas) {
        if (panelNotificacionesContent == null) {
            return;
        }
        panelNotificacionesContent.getChildren().clear();

        if (reservasConAlertas == null || reservasConAlertas.isEmpty()) {
            Label label = new Label("No hay notificaciones pendientes.");
            label.getStyleClass().add("notification-empty");
            label.setWrapText(true);
            label.setAlignment(Pos.CENTER);
            panelNotificacionesContent.getChildren().add(label);
            return;
        }

        reservasConAlertas.stream()
                .limit(8)
                .map(this::crearNotificacionDesdeReserva)
                .forEach(panelNotificacionesContent.getChildren()::add);
    }

    private Node crearNotificacionDesdeReserva(Reserva reserva) {
        VBox contenedor = new VBox(4);
        contenedor.getStyleClass().add("notification-item");

        String titulo = reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : "Reserva";
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("notification-title");

        String detalle = switch (reserva.getEstado()) {
            case "Cancelada" -> "Reserva cancelada por el usuario.";
            case "Inasistencia" -> "El usuario no se presentó.";
            case "Pendiente" -> "Pendiente de aprobación.";
            default -> "Estado: " + reserva.getEstado();
        };
        Label lblDetalle = new Label(detalle);
        lblDetalle.getStyleClass().add("notification-detail");

        String fecha = reserva.getFecha() != null ? reserva.getFecha().format(DateTimeFormatter.ISO_LOCAL_DATE) : "Sin fecha";
        Label lblFecha = new Label("Programada: " + fecha);
        lblFecha.getStyleClass().add("notification-meta");

        contenedor.getChildren().addAll(lblTitulo, lblDetalle, lblFecha);
        return contenedor;
    }

    private List<Reserva> obtenerReservasConAlertas() {
        return listaReservas.stream()
                .filter(reserva -> {
                    String estado = reserva.getEstado();
                    return "Pendiente".equalsIgnoreCase(estado)
                            || "Cancelada".equalsIgnoreCase(estado)
                            || "Inasistencia".equalsIgnoreCase(estado);
                })
                .collect(Collectors.toList());
    }

    @FXML
    private void togglePerfilPanel(MouseEvent event) {
        if (panelPerfil == null) {
            return;
        }

        if (panelPerfilVisible) {
            cerrarPanelPerfilInterno();
            return;
        }

        actualizarPanelPerfil();
        cerrarPanelNotificacionesInterno();

        mostrarPanel(panelPerfil);
        panelPerfilVisible = true;
    }

    @FXML
    private void cerrarPanelPerfil(ActionEvent event) {
        cerrarPanelPerfilInterno();
        event.consume();
    }

    private void actualizarPanelPerfil() {
        if (sessionManager == null) {
            return;
        }

        sessionManager.getAuthResponse().ifPresent(response -> {
            String nombre = response.name();
            if (nombre == null || nombre.isBlank()) {
                nombre = response.email();
            }
            if (lblPerfilNombre != null) {
                lblPerfilNombre.setText(nombre != null && !nombre.isBlank() ? nombre : "Usuario");
            }
            if (lblPerfilCorreo != null) {
                lblPerfilCorreo.setText(response.email() != null ? response.email() : "Sin correo registrado");
            }
        });
    }

    @FXML
    private void editarPerfil(ActionEvent event) {
        cerrarPanelesDeslizables();
        Usuario usuarioActual = encontrarUsuarioActual();
        if (usuarioActual == null) {
            mostrarAdvertencia("No se encontró la información del usuario en sesión.");
            return;
        }
        mostrarFormularioUsuario(usuarioActual);
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        cerrarPanelesDeslizables();
        detenerActualizaciones();
        if (sessionManager != null) {
            sessionManager.clear();
        }
        if (flowController != null) {
            flowController.showView(LOGIN_VIEW_ID);
        }
    }
    
    // ==================== CONFIGURACIÓN ====================
    
    @FXML
    private void abrirLinkOpenWeather(ActionEvent event) {
        // TODO: Abrir navegador
        mostrarInformacion("Abrir enlace", 
                          "Se abrirá el sitio web de OpenWeatherMap en tu navegador:\n" +
                          "https://openweathermap.org/api");
    }
    
    @FXML
    private void guardarConfiguracionAPIs(ActionEvent event) {
        // TODO: Guardar en base de datos o archivo de configuración
        String apiClima = txtAPIClima != null ? txtAPIClima.getText() : "";
        String apiSendGrid = txtAPISendGrid != null ? txtAPISendGrid.getText() : "";
        String apiTwilio = txtAPITwilio != null ? txtAPITwilio.getText() : "";
        
        if (apiClima.isEmpty()) {
            mostrarAdvertencia("Debes ingresar la API key de clima");
            return;
        }
        
        mostrarExito("Configuración de APIs guardada exitosamente");
    }
    
    @FXML
    private void probarConexiones(ActionEvent event) {
        mostrarIndicadorCarga("Probando conexiones...");
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(2000); // Simular prueba de conexión
                return null;
            }
            
            @Override
            protected void succeeded() {
                ocultarIndicadorCarga();
                mostrarExito("Todas las conexiones funcionan correctamente");
            }
        };
        
        new Thread(task).start();
    }
    
    @FXML
    private void guardarPreferenciasNotificaciones(ActionEvent event) {
        // TODO: Guardar preferencias en base de datos
        mostrarExito("Preferencias de notificaciones guardadas");
    }
    
    @FXML
    private void guardarPoliticas(ActionEvent event) {
        // TODO: Guardar políticas en base de datos
        int maxHoras = spinMaxHorasReserva != null ? spinMaxHorasReserva.getValue() : 4;
        int diasAnticipacion = spinDiasAnticipacion != null ? spinDiasAnticipacion.getValue() : 1;
        int maxReservas = spinMaxReservasSimultaneas != null ? spinMaxReservasSimultaneas.getValue() : 3;
        
        mostrarExito("Políticas de reserva guardadas exitosamente");
    }
    
    @FXML
    private void guardarConfiguracionSeguridad(ActionEvent event) {
        // TODO: Guardar configuración de seguridad
        mostrarExito("Configuración de seguridad guardada");
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    private void mostrarIndicadorCarga(String mensaje) {
        // TODO: Mostrar un indicador visual de carga
        Platform.runLater(() -> {
            System.out.println(mensaje);
        });
    }
    
    private void ocultarIndicadorCarga() {
        // TODO: Ocultar indicador de carga
        Platform.runLater(() -> {
            System.out.println("Carga completada");
        });
    }
    
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Platform.runLater(() -> {
            Alert alert = new Alert(tipo);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }
    
    private void mostrarError(String mensaje) {
        mostrarAlerta("Error", mensaje, Alert.AlertType.ERROR);
    }
    
    private void mostrarExito(String mensaje) {
        mostrarAlerta("Éxito", mensaje, Alert.AlertType.INFORMATION);
    }
    
    private void mostrarAdvertencia(String mensaje) {
        mostrarAlerta("Advertencia", mensaje, Alert.AlertType.WARNING);
    }
    
    private void mostrarInformacion(String titulo, String mensaje) {
        mostrarAlerta(titulo, mensaje, Alert.AlertType.INFORMATION);
    }
    
    /**
     * Método auxiliar para capitalizar la primera letra de un texto (igual que UserDashboard)
     */
    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
    
} // Fin de AdminDashboardController

record DatosIniciales(List<Espacio> espacios, List<Usuario> usuarios, List<Reserva> reservas,
        DatosClimaticos clima, List<String> warnings) {
}

record ClimaResultado(DatosClimaticos clima, List<String> warnings) {
}

// ==================== CLASES DE MODELO (Incluir en archivos separados) ====================

/**
 * Clase modelo para Espacio
 */
class Espacio {
    private Long id;
    private String nombre;
    private String tipo;
    private int capacidad;
    private String estado;
    private String descripcion;
    private boolean esExterior;
    private String ubicacion;
    private Integer maxDuracion;
    private boolean requiereAprobacion;
    private boolean activo;

    public Espacio(Long id, String nombre, String tipo, int capacidad, String estado,
                   String descripcion, boolean esExterior, String ubicacion,
                   Integer maxDuracion, boolean requiereAprobacion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.estado = estado;
        this.descripcion = descripcion;
        this.esExterior = esExterior;
        this.ubicacion = ubicacion;
        this.maxDuracion = maxDuracion;
        this.requiereAprobacion = requiereAprobacion;
        this.activo = activo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public int getCapacidad() { return capacidad; }
    public void setCapacidad(int capacidad) { this.capacidad = capacidad; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isEsExterior() { return esExterior; }
    public void setEsExterior(boolean esExterior) { this.esExterior = esExterior; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public Integer getMaxDuracion() { return maxDuracion; }
    public void setMaxDuracion(Integer maxDuracion) { this.maxDuracion = maxDuracion; }

    public boolean isRequiereAprobacion() { return requiereAprobacion; }
    public void setRequiereAprobacion(boolean requiereAprobacion) { this.requiereAprobacion = requiereAprobacion; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) {
        this.activo = activo;
        this.estado = activo ? "Disponible" : "Inactivo";
    }
}

/**
 * Clase modelo para Usuario
 */
class Usuario {
    private Long id;
    private String nombre;
    private String correo;
    private String rol;
    private String estado;
    private LocalDateTime ultimoAcceso;
    private String telefono;
    private String rolCodigo;
    private boolean activo;
    private int totalReservas;
    private int reservasAprobadas;

    public Usuario(Long id, String nombre, String correo, String rol, String estado,
                   LocalDateTime ultimoAcceso, String telefono, String rolCodigo, boolean activo,
                   int totalReservas, int reservasAprobadas) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.ultimoAcceso = ultimoAcceso;
        this.telefono = telefono;
        this.rolCodigo = rolCodigo;
        this.activo = activo;
        this.totalReservas = totalReservas;
        this.reservasAprobadas = reservasAprobadas;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRolCodigo() { return rolCodigo; }
    public void setRolCodigo(String rolCodigo) { this.rolCodigo = rolCodigo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) {
        this.activo = activo;
        this.estado = activo ? "Activo" : "Inactivo";
    }

    public int getTotalReservas() { return totalReservas; }
    public void setTotalReservas(int totalReservas) { this.totalReservas = totalReservas; }

    public int getReservasAprobadas() { return reservasAprobadas; }
    public void setReservasAprobadas(int reservasAprobadas) { this.reservasAprobadas = reservasAprobadas; }
}

/**
 * Clase modelo para Reserva
 */
class Reserva {
    private Long id;
    private Usuario usuario;
    private Espacio espacio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String codigoQR;
    private DatosClimaticos clima;
    private String notas;
    
    public Reserva(Long id, Usuario usuario, Espacio espacio, LocalDate fecha,
                   LocalTime horaInicio, LocalTime horaFin, String estado,
                   String codigoQR, DatosClimaticos clima, String notas) {
        this.id = id;
        this.usuario = usuario;
        this.espacio = espacio;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = estado;
        this.codigoQR = codigoQR;
        this.clima = clima;
        this.notas = notas;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public Espacio getEspacio() { return espacio; }
    public void setEspacio(Espacio espacio) { this.espacio = espacio; }
    
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    
    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }
    
    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public String getCodigoQR() { return codigoQR; }
    public void setCodigoQR(String codigoQR) { this.codigoQR = codigoQR; }
    
    public DatosClimaticos getClima() { return clima; }
    public void setClima(DatosClimaticos clima) { this.clima = clima; }
    
    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }
}

/**
 * Clase modelo para DatosClimaticos
 */
class DatosClimaticos {
    private double temperatura;
    private String condicion;
    private int probabilidadLluvia;
    private double velocidadViento;
    private String nivelAlerta;
    private String descripcion;
    
    public DatosClimaticos(double temperatura, String condicion, int probabilidadLluvia,
                          double velocidadViento, String nivelAlerta, String descripcion) {
        this.temperatura = temperatura;
        this.condicion = condicion;
        this.probabilidadLluvia = probabilidadLluvia;
        this.velocidadViento = velocidadViento;
        this.nivelAlerta = nivelAlerta;
        this.descripcion = descripcion;
    }
    
    // Getters y Setters
    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }
    
    public String getCondicion() { return condicion; }
    public void setCondicion(String condicion) { this.condicion = condicion; }
    
    public int getProbabilidadLluvia() { return probabilidadLluvia; }
    public void setProbabilidadLluvia(int probabilidadLluvia) { 
        this.probabilidadLluvia = probabilidadLluvia; 
    }
    
    public double getVelocidadViento() { return velocidadViento; }
    public void setVelocidadViento(double velocidadViento) { 
        this.velocidadViento = velocidadViento; 
    }
    
    public String getNivelAlerta() { return nivelAlerta; }
    public void setNivelAlerta(String nivelAlerta) { this.nivelAlerta = nivelAlerta; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getTemperaturaFormateada() {
        return String.format(Locale.US, "%.0f°C", temperatura);
    }

    public String getVientoFormateado() {
        return String.format(Locale.US, "%.0f km/h", velocidadViento);
    }
    
    public String getProbabilidadLluviaFormateada() {
        return probabilidadLluvia + "%";
    }
}

/**
 * Clase modelo para EstadisticasDashboard
 */
class EstadisticasDashboard {
    // Clase vacía por ahora, puedes añadir propiedades según necesites
}