package com.municipal.ui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.net.URL;
import java.util.ResourceBundle;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Controlador completo para el Panel de Administración
 * Sistema de Reservas de Espacios Municipales
 * 
 * @author Tu Nombre
 * @version 1.0
 */
public class PanelAdministracionController implements Initializable {
    
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
    @FXML private TableColumn<Reserva, Integer> colIdReserva;
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
    
    private ObservableList<Espacio> listaEspacios;
    private ObservableList<Espacio> listaEspaciosFiltrados;
    private ObservableList<Usuario> listaUsuarios;
    private ObservableList<Usuario> listaUsuariosFiltrados;
    private ObservableList<Reserva> listaReservas;
    private ObservableList<Reserva> listaReservasFiltradas;
    private EstadisticasDashboard estadisticas;
    private DatosClimaticos climaActual;
    
    // Usuario actual del sistema
    private Usuario usuarioActual;
    
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
        
        // Cargar datos iniciales
        cargarUsuarioActual();
        cargarDatosIniciales();
        
        // Mostrar vista de inicio por defecto
        mostrarInicio();
        
        System.out.println("Panel de Administración inicializado correctamente");
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
        // Tipos de espacio
        if (cmbTipoEspacio != null) {
            cmbTipoEspacio.setItems(FXCollections.observableArrayList(
                "Todos los tipos", "Interior", "Exterior"
            ));
            cmbTipoEspacio.setValue("Todos los tipos");
        }
        
        // Estados de espacio
        if (cmbEstadoEspacio != null) {
            cmbEstadoEspacio.setItems(FXCollections.observableArrayList(
                "Todos los estados", "Disponible", "Ocupado", "Mantenimiento"
            ));
            cmbEstadoEspacio.setValue("Todos los estados");
        }
        
        // Roles de usuario
        if (cmbRolUsuario != null) {
            cmbRolUsuario.setItems(FXCollections.observableArrayList(
                "Todos los roles", "Administrador", "Supervisor", "Usuario"
            ));
            cmbRolUsuario.setValue("Todos los roles");
        }
        
        // Estados de reserva
        if (cmbEstadoReserva != null) {
            cmbEstadoReserva.setItems(FXCollections.observableArrayList(
                "Todos los estados", "Confirmada", "Pendiente", "Completada", "Cancelada"
            ));
            cmbEstadoReserva.setValue("Todos los estados");
        }
        
        // Rango de fechas para reportes
        if (cmbRangoFechas != null) {
            cmbRangoFechas.setItems(FXCollections.observableArrayList(
                "Última semana", "Último mes", "Últimos 3 meses", "Último año", "Personalizado"
            ));
            cmbRangoFechas.setValue("Último mes");
        }
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
            LocalDate fecha = cellData.getValue().getUltimoAcceso();
            if (fecha == null) {
                return new SimpleStringProperty("N/A");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(LocalDateTime.of(fecha, LocalTime.MIDNIGHT).format(formatter));
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
        // TODO: Obtener el usuario actual de la sesión
        // Por ahora, usar un usuario de ejemplo
        usuarioActual = new Usuario(
            1,
            "Carlos Rodríguez",
            "carlos.rodriguez@perezzeladon.go.cr",
            "Administrador",
            "Activo",
            LocalDate.now(),
            "8888-8888"
        );
        
        if (lblNombreUsuario != null) {
            lblNombreUsuario.setText(usuarioActual.getNombre());
        }
    }
    
    /**
     * Carga todos los datos iniciales del sistema
     */
    private void cargarDatosIniciales() {
        cargarEspacios();
        cargarUsuarios();
        cargarReservas();
        cargarDatosDashboard();
    }
    
    /**
     * Carga los datos del dashboard
     */
    private void cargarDatosDashboard() {
        // TODO: Cargar desde base de datos
        // Por ahora, usar datos de ejemplo
        
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
            lblOcupacionSemanal.setText("72.5%");
        }
        
        if (lblVariacionOcupacion != null) {
            lblVariacionOcupacion.setText("+5.2% desde la semana pasada");
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
        // TODO: Implementar carga desde base de datos
        // Por ahora, generar datos de ejemplo
        listaEspacios.clear();
        listaEspacios.addAll(generarEspaciosEjemplo());
        
        // Aplicar filtros
        filtrarEspacios();
    }
    
    /**
     * Carga los usuarios desde la base de datos
     */
    private void cargarUsuarios() {
        // TODO: Implementar carga desde base de datos
        listaUsuarios.clear();
        listaUsuarios.addAll(generarUsuariosEjemplo());
        
        // Aplicar filtros
        filtrarUsuarios();
    }
    
    /**
     * Carga las reservas desde la base de datos
     */
    private void cargarReservas() {
        // TODO: Implementar carga desde base de datos
        listaReservas.clear();
        listaReservas.addAll(generarReservasEjemplo());
        
        // Aplicar filtros
        filtrarReservas();
    }
    
    /**
     * Carga los datos climáticos actuales
     */
    private void cargarClimaActual() {
        // TODO: Implementar llamada a API de clima
        climaActual = new DatosClimaticos(
            24.0,
            "Sunny",
            10,
            12.0,
            "Sin riesgo",
            "Excelente día para actividades al aire libre"
        );
        
        if (lblTemperatura != null) {
            lblTemperatura.setText(climaActual.getTemperaturaFormateada());
        }
        
        if (lblClimaCondicion != null) {
            lblClimaCondicion.setText(climaActual.getCondicion());
        }
        
        if (lblViento != null) {
            lblViento.setText(climaActual.getVientoFormateado());
        }
        
        if (lblLluvia != null) {
            lblLluvia.setText(climaActual.getProbabilidadLluviaFormateada());
        }
    }
    
    /**
     * Carga las alertas activas
     */
    private void cargarAlertas() {
        if (contenedorAlertas == null) return;
        
        contenedorAlertas.getChildren().clear();
        
        // TODO: Cargar alertas reales desde el sistema
        // Por ahora, crear alertas de ejemplo
        
        int numAlertas = 2;
        if (lblNumAlertas != null) {
            lblNumAlertas.setText(String.valueOf(numAlertas));
        }
        
        // Alerta 1
        HBox alerta1 = crearAlertaItem(
            "Cancha Deportiva Norte",
            "Alta probabilidad de lluvia",
            "1 reserva(s) afectada(s)",
            "critical"
        );
        contenedorAlertas.getChildren().add(alerta1);
        
        // Alerta 2
        HBox alerta2 = crearAlertaItem(
            "Parque Recreativo Sur",
            "Condiciones variables",
            "1 reserva(s) afectada(s)",
            "warning"
        );
        contenedorAlertas.getChildren().add(alerta2);
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
            lblTasaAsistencia.setText("88.9%");
        }
        
        if (lblVariacionAsistencia != null) {
            lblVariacionAsistencia.setText("+2.5% vs mes anterior");
        }
        
        if (lblInasistenciasReporte != null) {
            lblInasistenciasReporte.setText("1");
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
            .filter(r -> r.getEspacio() != null && "Interior".equals(r.getEspacio().getTipo()))
            .count();
        
        long exteriores = listaReservas.stream()
            .filter(r -> r.getEspacio() != null && "Exterior".equals(r.getEspacio().getTipo()))
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
                .filter(r -> r.getEspacio() != null && r.getEspacio().getId() == espacio.getId())
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
        
        // TODO: Calcular alertas activas reales
        if (lblAlertasActivas != null) {
            lblAlertasActivas.setText("1");
        }
        
        if (lblReservasAfectadas != null) {
            lblReservasAfectadas.setText("2");
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
        
        // TODO: Obtener datos climáticos reales de la API
        // Por ahora, crear datos de ejemplo
        
        List<Espacio> espaciosExteriores = listaEspacios.stream()
            .filter(Espacio::isEsExterior)
            .collect(Collectors.toList());
        
        int col = 0;
        int row = 0;
        
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
        
        Label etiqueta = new Label("Sin riesgo");
        etiqueta.setStyle("-fx-background-color: #28A745; -fx-text-fill: white; " +
                        "-fx-padding: 4 10; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 11px;");
        
        header.getChildren().addAll(nombre, etiqueta);
        
        // Datos climáticos
        VBox datos = new VBox(10);
        datos.getChildren().addAll(
            crearFilaClima("🌡️", "Temperatura", "24°C"),
            crearFilaClima("💧", "Probabilidad de lluvia", "10%"),
            crearFilaClima("☁️", "Condición", "Sunny")
        );
        
        // Mensaje
        Label mensaje = new Label("Excelente día para actividades al aire libre");
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
                .filter(e -> busqueda.isEmpty() || e.getNombre().toLowerCase().contains(busqueda))
                .filter(e -> "Todos los tipos".equals(tipoSeleccionado) || e.getTipo().equals(tipoSeleccionado))
                .filter(e -> "Todos los estados".equals(estadoSeleccionado) || e.getEstado().equals(estadoSeleccionado))
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
                .filter(u -> busqueda.isEmpty() || 
                            u.getNombre().toLowerCase().contains(busqueda) ||
                            u.getCorreo().toLowerCase().contains(busqueda))
                .filter(u -> "Todos los roles".equals(rolSeleccionado) || u.getRol().equals(rolSeleccionado))
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
                    
                    boolean coincideUsuario = r.getUsuario() != null && 
                                             r.getUsuario().getNombre().toLowerCase().contains(busqueda);
                    boolean coincideEspacio = r.getEspacio() != null && 
                                             r.getEspacio().getNombre().toLowerCase().contains(busqueda);
                    
                    return coincideUsuario || coincideEspacio;
                })
                .filter(r -> "Todos los estados".equals(estadoSeleccionado) || r.getEstado().equals(estadoSeleccionado))
                .collect(Collectors.toList())
        );
        
        tablaReservas.setItems(listaReservasFiltradas);
    }
    
    // ==================== ACCIONES DE ESPACIOS ====================
    
    @FXML
    private void nuevoEspacio(ActionEvent event) {
        // TODO: Abrir diálogo para crear nuevo espacio
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Espacio");
        dialog.setHeaderText("Crear un nuevo espacio");
        dialog.setContentText("Nombre del espacio:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                // Crear nuevo espacio
                Espacio nuevoEspacio = new Espacio(
                    listaEspacios.size() + 1,
                    nombre,
                    "Interior",
                    50,
                    "Disponible",
                    "Nuevo espacio creado",
                    false
                );
                
                listaEspacios.add(nuevoEspacio);
                filtrarEspacios();
                mostrarExito("Espacio creado exitosamente");
            }
        });
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
        // TODO: Abrir diálogo de edición completo
        TextInputDialog dialog = new TextInputDialog(espacio.getNombre());
        dialog.setTitle("Editar Espacio");
        dialog.setHeaderText("Editar " + espacio.getNombre());
        dialog.setContentText("Nuevo nombre:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nuevoNombre -> {
            espacio.setNombre(nuevoNombre);
            tablaEspacios.refresh();
            mostrarExito("Espacio actualizado exitosamente");
        });
    }
    
    private void eliminarEspacio(Espacio espacio) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Espacio");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este espacio?");
        confirmacion.setContentText(espacio.getNombre());
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            listaEspacios.remove(espacio);
            filtrarEspacios();
            mostrarExito("Espacio eliminado exitosamente");
        }
    }
    
    // ==================== ACCIONES DE USUARIOS ====================
    
    @FXML
    private void agregarUsuario(ActionEvent event) {
        // TODO: Abrir diálogo para agregar nuevo usuario
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Usuario");
        dialog.setHeaderText("Agregar un nuevo usuario");
        dialog.setContentText("Nombre del usuario:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(nombre -> {
            if (!nombre.trim().isEmpty()) {
                Usuario nuevoUsuario = new Usuario(
                    listaUsuarios.size() + 1,
                    nombre,
                    nombre.toLowerCase().replace(" ", ".") + "@perezzeladon.go.cr",
                    "Usuario",
                    "Activo",
                    LocalDate.now(),
                    ""
                );
                
                listaUsuarios.add(nuevoUsuario);
                filtrarUsuarios();
                mostrarExito("Usuario creado exitosamente");
            }
        });
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
        String nuevoEstado = "Activo".equals(usuario.getEstado()) ? "Inactivo" : "Activo";
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cambiar Estado");
        confirmacion.setHeaderText("¿Cambiar estado del usuario?");
        confirmacion.setContentText("Se cambiará a: " + nuevoEstado);
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            usuario.setEstado(nuevoEstado);
            tablaUsuarios.refresh();
            mostrarExito("Estado actualizado exitosamente");
        }
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
        
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar Reserva");
        confirmacion.setHeaderText("¿Está seguro que desea cancelar esta reserva?");
        confirmacion.setContentText("Reserva #" + reserva.getId() + " - " + 
                                   (reserva.getEspacio() != null ? reserva.getEspacio().getNombre() : ""));
        
        Optional<ButtonType> result = confirmacion.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            reserva.setEstado("Cancelada");
            tablaReservas.refresh();
            mostrarExito("Reserva cancelada exitosamente");
            
            // TODO: Enviar notificación al usuario
        }
    }
    
    private void notificarUsuarioReserva(Reserva reserva) {
        // TODO: Implementar envío de notificación real
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notificar Usuario");
        alert.setHeaderText("Enviando notificación...");
        alert.setContentText("Se enviará una notificación a: " + 
                           (reserva.getUsuario() != null ? reserva.getUsuario().getCorreo() : ""));
        alert.showAndWait();
        
        mostrarExito("Notificación enviada exitosamente");
    }
    
    // ==================== ACCIONES GENERALES ====================
    
    @FXML
    private void actualizarDatos(ActionEvent event) {
        // Mostrar indicador de carga
        mostrarIndicadorCarga("Actualizando datos...");
        
        // Simular carga asíncrona
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000); // Simular tiempo de carga
                return null;
            }
            
            @Override
            protected void succeeded() {
                cargarDatosIniciales();
                ocultarIndicadorCarga();
                mostrarExito("Datos actualizados exitosamente");
            }
        };
        
        new Thread(task).start();
    }
    
    @FXML
    private void actualizarClima(ActionEvent event) {
        mostrarIndicadorCarga("Actualizando información climática...");
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(800);
                return null;
            }
            
            @Override
            protected void succeeded() {
                cargarReservas();
                ocultarIndicadorCarga();
                mostrarExito("Información climática actualizada");
            }
        };
        
        new Thread(task).start();
    }
    
    @FXML
    private void actualizarTodosClimas(ActionEvent event) {
        mostrarIndicadorCarga("Actualizando todos los datos climáticos...");
        
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1500);
                return null;
            }
            
            @Override
            protected void succeeded() {
                cargarClima();
                ocultarIndicadorCarga();
                mostrarExito("Datos climáticos actualizados exitosamente");
            }
        };
        
        new Thread(task).start();
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
    
    // ==================== DATOS DE EJEMPLO ====================
    
    private List<Espacio> generarEspaciosEjemplo() {
        List<Espacio> espacios = new ArrayList<>();
        
        espacios.add(new Espacio(1, "Salón Comunal Central", "Interior", 150, "Disponible",
                                 "Salón principal para eventos", false));
        espacios.add(new Espacio(2, "Cancha Deportiva Norte", "Exterior", 50, "Disponible",
                                 "Cancha multiuso", true));
        espacios.add(new Espacio(3, "Auditorio Municipal", "Interior", 200, "Ocupado",
                                 "Auditorio con equipo audiovisual", false));
        espacios.add(new Espacio(4, "Parque Recreativo Sur", "Exterior", 100, "Disponible",
                                 "Área recreativa", true));
        espacios.add(new Espacio(5, "Sala de Reuniones Este", "Interior", 30, "Disponible",
                                 "Sala ejecutiva", false));
        espacios.add(new Espacio(6, "Plaza Cultural", "Exterior", 200, "Disponible",
                                 "Plaza para eventos culturales", true));
        
        return espacios;
    }
    
    private List<Usuario> generarUsuariosEjemplo() {
        List<Usuario> usuarios = new ArrayList<>();
        
        usuarios.add(new Usuario(1, "Carlos Rodríguez", "carlos.rodriguez@perezzeladon.go.cr",
                                "Administrador", "Activo", LocalDate.now(), "8888-8888"));
        usuarios.add(new Usuario(2, "María González", "maria.gonzalez@perezzeladon.go.cr",
                                "Usuario", "Inactivo", null, "8888-7777"));
        usuarios.add(new Usuario(3, "Juan Pérez", "juan.perez@perezzeladon.go.cr",
                                "Usuario", "Activo", LocalDate.now().minusDays(1), "8888-6666"));
        usuarios.add(new Usuario(4, "Ana Mora", "ana.mora@perezzeladon.go.cr",
                                "Supervisor", "Activo", LocalDate.now(), "8888-5555"));
        usuarios.add(new Usuario(5, "Pedro Sánchez", "pedro.sanchez@perezzeladon.go.cr",
                                "Usuario", "Activo", LocalDate.now().minusDays(2), "8888-4444"));
        
        return usuarios;
    }
    
    private List<Reserva> generarReservasEjemplo() {
        List<Reserva> reservas = new ArrayList<>();
        
        List<Espacio> espacios = generarEspaciosEjemplo();
        List<Usuario> usuarios = generarUsuariosEjemplo();
        
        DatosClimaticos clima1 = new DatosClimaticos(24.0, "Sunny", 10, 12.0, "Sin riesgo",
                                                     "Excelente día");
        DatosClimaticos clima2 = new DatosClimaticos(26.0, "Rainy", 70, 15.0, "Alerta climática",
                                                     "Alta probabilidad de lluvia");
        
        reservas.add(new Reserva(1, usuarios.get(1), espacios.get(0), LocalDate.of(2025, 10, 20),
                                LocalTime.of(14, 0), LocalTime.of(18, 0), "Confirmada",
                                "QR-001", clima1, "Reunión comunitaria"));
        reservas.add(new Reserva(2, usuarios.get(1), espacios.get(1), LocalDate.of(2025, 10, 22),
                                LocalTime.of(9, 0), LocalTime.of(12, 0), "Confirmada",
                                "QR-002", clima2, "Torneo deportivo"));
        reservas.add(new Reserva(3, usuarios.get(1), espacios.get(4), LocalDate.of(2025, 10, 18),
                                LocalTime.of(10, 0), LocalTime.of(12, 0), "Completada",
                                "QR-003", null, "Reunión ejecutiva"));
        reservas.add(new Reserva(4, usuarios.get(2), espacios.get(3), LocalDate.of(2025, 10, 17),
                                LocalTime.of(15, 0), LocalTime.of(18, 0), "Confirmada",
                                "QR-004", clima1, "Actividad recreativa"));
        reservas.add(new Reserva(5, usuarios.get(4), espacios.get(0), LocalDate.of(2025, 10, 19),
                                LocalTime.of(16, 0), LocalTime.of(20, 0), "Pendiente",
                                "QR-005", clima1, "Evento cultural"));
        
        return reservas;
    }
}

// ==================== CLASES DE MODELO (Incluir en archivos separados) ====================

/**
 * Clase modelo para Espacio
 */
class Espacio {
    private int id;
    private String nombre;
    private String tipo;
    private int capacidad;
    private String estado;
    private String descripcion;
    private boolean esExterior;
    
    public Espacio(int id, String nombre, String tipo, int capacidad, String estado,
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
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
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
    private int id;
    private String nombre;
    private String correo;
    private String rol;
    private String estado;
    private LocalDate ultimoAcceso;
    private String telefono;
    
    public Usuario(int id, String nombre, String correo, String rol, String estado,
                   LocalDate ultimoAcceso, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.estado = estado;
        this.ultimoAcceso = ultimoAcceso;
        this.telefono = telefono;
    }
    
    // Getters y Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public LocalDate getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDate ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }
    
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

/**
 * Clase modelo para Reserva
 */
class Reserva {
    private int id;
    private Usuario usuario;
    private Espacio espacio;
    private LocalDate fecha;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String estado;
    private String codigoQR;
    private DatosClimaticos clima;
    private String notas;
    
    public Reserva(int id, Usuario usuario, Espacio espacio, LocalDate fecha,
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
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
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
        return String.format("%.0f°C", temperatura);
    }
    
    public String getVientoFormateado() {
        return String.format("%.0f km/h", velocidadViento);
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