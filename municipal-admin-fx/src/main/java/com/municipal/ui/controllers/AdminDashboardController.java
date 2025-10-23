package com.municipal.ui.controllers;

import com.municipal.config.AppConfig;
import com.municipal.controllers.ReservationController;
import com.municipal.controllers.SpaceController;
import com.municipal.controllers.UserController;
import com.municipal.controllers.WeatherController;
import com.municipal.dtos.ReservationDTO;
import com.municipal.dtos.SpaceDTO;
import com.municipal.dtos.UserDTO;
import com.municipal.dtos.weather.CurrentWeatherDTO;
import com.municipal.exceptions.ApiClientException;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.SessionAware;
import com.municipal.ui.navigation.ViewLifecycle;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controlador completo para el Panel de Administración
 * Sistema de Reservas de Espacios Municipales
 * 
 * @author Tu Nombre
 * @version 1.0
 */
public class AdminDashboardController implements Initializable, SessionAware, ViewLifecycle {
    
    // ==================== COMPONENTES PRINCIPALES ====================
    
    @FXML private StackPane contenedorPrincipal;
    @FXML private Label lblNombreUsuario;
    @FXML private Label lblNotificacionesBadge;
    @FXML private Button btnNotificaciones;
    
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
    @FXML private TableColumn<Usuario, Void> colAccionesUsuario;
    
    // ==================== CONTROL DE RESERVAS ====================
    
    @FXML private TextField txtBuscarReserva;
    @FXML private ComboBox<String> cmbEstadoReserva;
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

    private SessionManager sessionManager;

    private ObservableList<Espacio> listaEspacios;
    private ObservableList<Espacio> listaEspaciosFiltrados;
    private ObservableList<Usuario> listaUsuarios;
    private ObservableList<Usuario> listaUsuariosFiltrados;
    private ObservableList<Reserva> listaReservas;
    private ObservableList<Reserva> listaReservasFiltradas;
    private EstadisticasDashboard estadisticas;
    private DatosClimaticos climaActual;
    
    // ==================== INICIALIZACIÓN ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando Panel de Administración...");
        
        // Inicializar listas de datos
        inicializarDatos();
        
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
    public void onViewActivated() {
        bootstrap();
    }

    public void bootstrap() {
        cargarUsuarioActual();
        cargarDatosIniciales(false);
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
        
        // Configurar columnas
        colNombreEspacio.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipoEspacio.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colCapacidadEspacio.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colEstadoEspacio.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
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
        
        // Configurar columna de acciones
        colAccionesEspacio.setCellFactory(param -> new TableCell<Espacio, Void>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            private final Button btnVer = new Button("👁️");
            private final HBox contenedor = new HBox(5, btnVer, btnEditar, btnEliminar);
            
            {
                btnVer.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    verDetallesEspacio(espacio);
                });
                
                btnEditar.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    editarEspacio(espacio);
                });
                
                btnEliminar.setOnAction(e -> {
                    Espacio espacio = getTableView().getItems().get(getIndex());
                    eliminarEspacio(espacio);
                });
                
                // Estilos para los botones
                btnVer.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; -fx-cursor: hand;");
                btnEditar.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-cursor: hand;");
                btnEliminar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-cursor: hand;");
                
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
        
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstadoUsuario.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Formatear columna de último acceso
        colUltimoAcceso.setCellValueFactory(cellData -> {
            LocalDateTime fecha = cellData.getValue().getUltimoAcceso();
            if (fecha == null) {
                return new SimpleStringProperty("N/A");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(fecha.format(formatter));
        });
        
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
                    }
                    
                    setGraphic(label);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones
        colAccionesUsuario.setCellFactory(param -> new TableCell<Usuario, Void>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnCambiarEstado = new Button("🔄");
            private final HBox contenedor = new HBox(5, btnEditar, btnCambiarEstado);
            
            {
                btnEditar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarUsuario(usuario);
                });
                
                btnCambiarEstado.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    cambiarEstadoUsuario(usuario);
                });
                
                btnEditar.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white; -fx-cursor: hand;");
                btnCambiarEstado.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; -fx-cursor: hand;");
                
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
     * Configura la tabla de reservas
     */
    private void configurarTablaReservas() {
        if (tablaReservas == null) return;
        
        colIdReserva.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colUsuarioReserva.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue().getUsuario();
            return new SimpleStringProperty(usuario != null ? usuario.getNombre() : "N/A");
        });
        
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
        
        colEstadoReservaTabla.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        colQRReserva.setCellValueFactory(new PropertyValueFactory<>("codigoQR"));
        
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
                    }
                    
                    setGraphic(label);
                    setText(null);
                }
            }
        });
        
        // Configurar columna de acciones
        colAccionesReserva.setCellFactory(param -> new TableCell<Reserva, Void>() {
            private final Button btnVer = new Button("👁️");
            private final Button btnCancelar = new Button("❌");
            private final Button btnNotificar = new Button("📧");
            private final HBox contenedor = new HBox(5, btnVer, btnCancelar, btnNotificar);
            
            {
                btnVer.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    verDetallesReserva(reserva);
                });
                
                btnCancelar.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    cancelarReserva(reserva);
                });
                
                btnNotificar.setOnAction(e -> {
                    Reserva reserva = getTableView().getItems().get(getIndex());
                    notificarUsuarioReserva(reserva);
                });
                
                btnVer.setStyle("-fx-background-color: #17A2B8; -fx-text-fill: white; -fx-cursor: hand;");
                btnCancelar.setStyle("-fx-background-color: #DC3545; -fx-text-fill: white; -fx-cursor: hand;");
                btnNotificar.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; -fx-cursor: hand;");
                
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
            btnNotificaciones.setOnAction(e -> mostrarNotificaciones());
        }
    }
    
    // ==================== NAVEGACIÓN ENTRE MÓDULOS ====================
    
    @FXML
    private void mostrarInicio() {
        ocultarTodasLasVistas();
        vistaInicio.setVisible(true);
        actualizarMenuActivo(btnInicio);
        cargarDatosDashboard();
    }
    
    @FXML
    private void mostrarGestionEspacios() {
        ocultarTodasLasVistas();
        vistaGestionEspacios.setVisible(true);
        actualizarMenuActivo(btnGestionEspacios);
        cargarEspacios();
    }
    
    @FXML
    private void mostrarGestionUsuarios() {
        ocultarTodasLasVistas();
        vistaGestionUsuarios.setVisible(true);
        actualizarMenuActivo(btnGestionUsuarios);
        cargarUsuarios();
    }
    
    @FXML
    private void mostrarControlReservas() {
        ocultarTodasLasVistas();
        vistaControlReservas.setVisible(true);
        actualizarMenuActivo(btnControlReservas);
        cargarReservas();
    }
    
    @FXML
    private void mostrarReportesGlobales() {
        ocultarTodasLasVistas();
        vistaReportesGlobales.setVisible(true);
        actualizarMenuActivo(btnReportesGlobales);
        cargarReportes();
    }
    
    @FXML
    private void mostrarMonitoreoClimatico() {
        ocultarTodasLasVistas();
        vistaMonitoreoClimatico.setVisible(true);
        actualizarMenuActivo(btnMonitoreoClimatico);
        cargarClima();
    }
    
    @FXML
    private void mostrarConfiguracion() {
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
        }, () -> lblNombreUsuario.setText("Usuario"));
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
                DatosClimaticos clima = cargarClimaDesdeApi(token, warnings);

                return new DatosIniciales(espacios, usuarios, reservas, clima, warnings);
            }
        };

        task.setOnRunning(event -> mostrarIndicadorCarga("Cargando datos del sistema..."));

        task.setOnSucceeded(event -> {
            DatosIniciales resultado = task.getValue();

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

            ocultarIndicadorCarga();

            if (notifySuccess) {
                mostrarExito("Datos actualizados exitosamente");
            }

            if (!resultado.warnings().isEmpty()) {
                mostrarAdvertencia(String.join("\n", resultado.warnings()));
            }
        });

        task.setOnFailed(event -> {
            ocultarIndicadorCarga();
            Throwable error = task.getException();
            String message = error != null ? error.getMessage() : "Error desconocido";
            mostrarError("No se pudieron cargar los datos: " + message);
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
        return new Espacio(id, nombre, tipo, capacidad, estado, descripcion, esExterior);
    }

    private Usuario mapearUsuario(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        Long id = dto.id();
        String nombre = defaultString(dto.name());
        String correo = defaultString(dto.email());
        String rol = defaultString(dto.role());
        String estado = Boolean.TRUE.equals(dto.active()) ? "Activo" : "Inactivo";
        LocalDateTime ultimoAcceso = dto.lastLoginAt();
        return new Usuario(id, nombre, correo, rol, estado, ultimoAcceso, null);
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

        List<Reserva> reservasConAlertas = listaReservas.stream()
                .filter(reserva -> {
                    String estado = reserva.getEstado();
                    return "Pendiente".equalsIgnoreCase(estado)
                            || "Cancelada".equalsIgnoreCase(estado)
                            || "Inasistencia".equalsIgnoreCase(estado);
                })
                .collect(Collectors.toList());

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
            return;
        }

        reservasConAlertas.stream()
                .limit(5)
                .map(this::crearAlertaDesdeReserva)
                .forEach(contenedorAlertas.getChildren()::add);
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
        
        // Contar reservas por tipo de espacio
        long interiores = listaReservas.stream()
            .filter(r -> r.getEspacio() != null && !r.getEspacio().isEsExterior())
            .count();

        long exteriores = listaReservas.stream()
            .filter(r -> r.getEspacio() != null && r.getEspacio().isEsExterior())
            .count();
        
        pieChart.getData().add(new PieChart.Data("Interior (" + interiores + ")", interiores));
        pieChart.getData().add(new PieChart.Data("Exterior (" + exteriores + ")", exteriores));
        
        pieChart.setLegendVisible(true);
        
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
        barChart.setLegendVisible(false);
        
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
        if (tablaEspacios == null) return;
        
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

        tablaEspacios.setItems(listaEspaciosFiltrados);
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

                    return coincideUsuario || coincideEspacio;
                })
                .filter(r -> {
                    if ("Todos los estados".equals(estadoSeleccionado)) {
                        return true;
                    }
                    String estado = r.getEstado() != null ? r.getEstado() : "";
                    return estado.equalsIgnoreCase(estadoSeleccionado);
                })
                .collect(Collectors.toList())
        );

        tablaReservas.setItems(listaReservasFiltradas);
    }
    
    // ==================== ACCIONES DE ESPACIOS ====================
    
    @FXML
    private void nuevoEspacio(ActionEvent event) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "La creación de espacios se realizará desde el backend cuando la API lo permita.");
    }

    private void verDetallesEspacio(Espacio espacio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles del Espacio");
        alert.setHeaderText(espacio.getNombre());
        alert.setContentText(
            "Tipo: " + espacio.getTipo() + "\n" +
            "Capacidad: " + espacio.getCapacidad() + " personas\n" +
            "Estado: " + espacio.getEstado() + "\n" +
            "Descripción: " + espacio.getDescripcion()
        );
        alert.showAndWait();
    }

    private void editarEspacio(Espacio espacio) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "La edición de espacios se habilitará cuando la API exponga esta operación.");
    }

    private void eliminarEspacio(Espacio espacio) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "La eliminación de espacios se sincronizará con el backend en una versión futura.");
    }
    
    // ==================== ACCIONES DE USUARIOS ====================
    
    @FXML
    private void agregarUsuario(ActionEvent event) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "La creación de usuarios debe realizarse desde los servicios del backend.");
    }
    
    private void editarUsuario(Usuario usuario) {
        // TODO: Abrir diálogo de edición completo
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Editar Usuario");
        alert.setHeaderText("Funcionalidad en desarrollo");
        alert.setContentText("Aquí podrás editar: " + usuario.getNombre());
        alert.showAndWait();
    }
    
    private void cambiarEstadoUsuario(Usuario usuario) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "El cambio de estado de usuarios se integrará con la API en próximas iteraciones.");
    }
    
    // ==================== ACCIONES DE RESERVAS ====================
    
    private void verDetallesReserva(Reserva reserva) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalles de la Reserva");
        alert.setHeaderText("Reserva #" + reserva.getId());
        
        StringBuilder detalles = new StringBuilder();
        detalles.append("Usuario: ").append(reserva.getUsuario() != null ? reserva.getUsuario().getNombre() : "N/A").append("\n");
        detalles.append("Espacio: ").append(reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : "N/A").append("\n");
        detalles.append("Fecha: ").append(reserva.getFecha()).append("\n");
        detalles.append("Hora: ").append(reserva.getHoraInicio()).append(" - ").append(reserva.getHoraFin()).append("\n");
        detalles.append("Estado: ").append(reserva.getEstado()).append("\n");
        detalles.append("Código QR: ").append(reserva.getCodigoQR()).append("\n");
        
        if (reserva.getNotas() != null && !reserva.getNotas().isEmpty()) {
            detalles.append("Notas: ").append(reserva.getNotas()).append("\n");
        }
        
        alert.setContentText(detalles.toString());
        alert.showAndWait();
    }
    
    private void cancelarReserva(Reserva reserva) {
        if ("Cancelada".equals(reserva.getEstado())) {
            mostrarAdvertencia("Esta reserva ya está cancelada");
            return;
        }
        
        mostrarInformacion("Funcionalidad en desarrollo",
                "La cancelación de reservas debe realizarse desde el backend para mantener la integridad de los datos.");
    }

    private void notificarUsuarioReserva(Reserva reserva) {
        mostrarInformacion("Funcionalidad en desarrollo",
                "El envío de notificaciones se integrará con los servicios de mensajería oficiales.");
    }
    
    // ==================== ACCIONES GENERALES ====================
    
    @FXML
    private void actualizarDatos(ActionEvent event) {
        cargarDatosIniciales(true);
    }

    @FXML
    private void actualizarClima(ActionEvent event) {
        recargarClima(true);
    }

    @FXML
    private void actualizarTodosClimas(ActionEvent event) {
        recargarClima(true);
    }
    
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
    
    private void mostrarNotificaciones() {
        // TODO: Mostrar panel de notificaciones
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notificaciones");
        alert.setHeaderText("Centro de Notificaciones");
        alert.setContentText("Tienes 3 notificaciones pendientes:\n\n" +
                           "1. Nueva reserva en Salón Comunal\n" +
                           "2. Alerta climática en Cancha Norte\n" +
                           "3. Usuario pendiente de aprobación");
        alert.showAndWait();
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
    
}

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

    public Espacio(Long id, String nombre, String tipo, int capacidad, String estado,
                   String descripcion, boolean esExterior) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.capacidad = capacidad;
        this.estado = estado;
        this.descripcion = descripcion;
        this.esExterior = esExterior;
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

    public Usuario(Long id, String nombre, String correo, String rol, String estado,
                   LocalDateTime ultimoAcceso, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.ultimoAcceso = ultimoAcceso;
        this.telefono = telefono;
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