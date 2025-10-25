
package com.municipal.ui.controllers;

import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.SessionAware;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Consumer;

public class SupervisorDashboardController implements Initializable, SessionAware {

    @FXML private StackPane contenedorPrincipal;
    @FXML private Label lblNombreSupervisor;

    @FXML private Button btnInicio;
    @FXML private Button btnReservas;
    @FXML private Button btnEspacios;
    @FXML private Button btnNotificaciones;
    @FXML private Button btnUsuarios;
    @FXML private Button btnAuditoria;

    @FXML private ScrollPane vistaInicio;
    @FXML private ScrollPane vistaReservas;
    @FXML private ScrollPane vistaEspacios;
    @FXML private ScrollPane vistaNotificaciones;
    @FXML private ScrollPane vistaUsuarios;
    @FXML private ScrollPane vistaAuditoria;

    @FXML private Label lblPendientes;
    @FXML private Label lblActivas;
    @FXML private Label lblEspaciosDisp;
    @FXML private Label lblCheckins;

    @FXML private Label lblTotalReservas;
    @FXML private Label lblReservasPendientes;
    @FXML private Label lblReservasAprobadas;
    @FXML private Label lblReservasRechazadas;

    @FXML private TextField txtBuscarReserva;
    @FXML private ComboBox<String> cmbEstadoReserva;

    private SessionManager sessionManager;
    private List<ScrollPane> secciones;
    private List<Button> menuButtons;

    private int inicioPendientes;
    private int inicioActivas;
    private int inicioEspaciosDisponibles;
    private int inicioCheckins;

    private int totalReservas;
    private int reservasPendientes;
    private int reservasAprobadas;
    private int reservasRechazadas;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        secciones = List.of(vistaInicio, vistaReservas, vistaEspacios, vistaNotificaciones, vistaUsuarios, vistaAuditoria);
        menuButtons = List.of(btnInicio, btnReservas, btnEspacios, btnNotificaciones, btnUsuarios, btnAuditoria);

        inicializarMetricas();
        configurarFiltros();
        configurarBuscadores();
        configurarAcciones();

        mostrarSeccion(vistaInicio, btnInicio);
        actualizarNombreSupervisor();
    }

    private void inicializarMetricas() {
        inicioPendientes = parseLabelValue(lblPendientes);
        inicioActivas = parseLabelValue(lblActivas);
        inicioEspaciosDisponibles = parseLabelValue(lblEspaciosDisp);
        inicioCheckins = parseLabelValue(lblCheckins);

        totalReservas = parseLabelValue(lblTotalReservas);
        reservasPendientes = parseLabelValue(lblReservasPendientes);
        reservasAprobadas = parseLabelValue(lblReservasAprobadas);
        reservasRechazadas = parseLabelValue(lblReservasRechazadas);
    }

    private int parseLabelValue(Label label) {
        if (label == null) {
            return 0;
        }
        String raw = label.getText();
        if (raw == null) {
            return 0;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return 0;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private void configurarFiltros() {
        if (cmbEstadoReserva != null) {
            cmbEstadoReserva.setItems(FXCollections.observableArrayList("Todos", "Pendientes", "Aprobadas", "Rechazadas"));
            cmbEstadoReserva.getSelectionModel().selectFirst();
            cmbEstadoReserva.setOnAction(event -> mostrarInformacion(
                    "Filtro de reservas",
                    "Aplicando filtro: " + Objects.toString(cmbEstadoReserva.getValue(), "Todos")));
        }
    }

    private void configurarBuscadores() {
        if (txtBuscarReserva != null) {
            txtBuscarReserva.setOnAction(event -> mostrarInformacion(
                    "Buscar reserva",
                    "Buscando coincidencias para: " + txtBuscarReserva.getText().trim()));
        }
    }

    private void configurarAcciones() {
        bindButtonsByText(vistaInicio, "✚  Nueva Reserva", button ->
                button.setOnAction(event -> crearReservaRapida()));
        bindButtonsByStyleClass(vistaInicio, "btn-icon-success", button ->
                button.setOnAction(event -> aprobarReservaDesdeCard(button)));
        bindButtonsByStyleClass(vistaInicio, "btn-icon-danger", button ->
                button.setOnAction(event -> rechazarReservaDesdeCard(button)));
        bindButtonsByStyleClass(vistaInicio, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));

        bindButtonsByStyleClass(vistaReservas, "btn-secondary", button ->
                button.setOnAction(event -> exportarReservas()));
        bindButtonsByStyleClass(vistaReservas, "btn-success-sm", button ->
                button.setOnAction(event -> aprobarReservaDesdeCard(button)));
        bindButtonsByStyleClass(vistaReservas, "btn-danger-sm", button ->
                button.setOnAction(event -> rechazarReservaDesdeCard(button)));
        bindButtonsByStyleClass(vistaReservas, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));
        bindTabButtons(vistaReservas, "Reservas");

        bindButtonsByText(vistaEspacios, "✚  Nuevo Espacio", button ->
                button.setOnAction(event -> crearEspacio()));
        bindButtonsByStyleClass(vistaEspacios, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));
        bindButtonsByStyleClass(vistaEspacios, "btn-icon", button ->
                button.setOnAction(event -> editarEspacio(button)));

        bindButtonsByText(vistaNotificaciones, "✚  Nueva Notificación", button ->
                button.setOnAction(event -> crearNotificacion()));
        bindButtonsByStyleClass(vistaNotificaciones, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));
        bindTabButtons(vistaNotificaciones, "Notificaciones");

        bindButtonsByStyleClass(vistaUsuarios, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));
        bindButtonsByStyleClass(vistaUsuarios, "btn-danger-sm", button ->
                button.setOnAction(event -> suspenderUsuario(button)));

        bindButtonsByStyleClass(vistaAuditoria, "btn-secondary", button ->
                button.setOnAction(event -> exportarLogs()));
        bindButtonsByStyleClass(vistaAuditoria, "btn-secondary-sm", button ->
                button.setOnAction(event -> mostrarDetalles(button)));
    }

    private void bindButtonsByText(ScrollPane vista, String buttonText, Consumer<Button> config) {
        if (vista == null) {
            return;
        }
        Set<Node> nodes = vista.lookupAll(".button");
        for (Node node : nodes) {
            if (node instanceof Button button && buttonText.equals(button.getText())) {
                config.accept(button);
            }
        }
    }

    private void bindButtonsByStyleClass(ScrollPane vista, String styleClass, Consumer<Button> config) {
        if (vista == null) {
            return;
        }
        Set<Node> nodes = vista.lookupAll("." + styleClass);
        for (Node node : nodes) {
            if (node instanceof Button button) {
                config.accept(button);
            }
        }
    }

    private void bindTabButtons(ScrollPane vista, String categoria) {
        if (vista == null) {
            return;
        }
        Set<Node> nodes = vista.lookupAll(".tab-btn");
        for (Node node : nodes) {
            if (node instanceof Button button) {
                button.setOnAction(event -> activarTab(vista, button, categoria));
            }
        }
    }

    private void activarTab(ScrollPane vista, Button boton, String categoria) {
        Set<Node> nodes = vista.lookupAll(".tab-btn");
        for (Node node : nodes) {
            if (node instanceof Button tab) {
                tab.getStyleClass().remove("tab-btn-active");
            }
        }
        if (!boton.getStyleClass().contains("tab-btn-active")) {
            boton.getStyleClass().add("tab-btn-active");
        }
        mostrarInformacion("Filtro aplicado", "Mostrando " + boton.getText().trim() + " en " + categoria + ".");
    }

    private void crearReservaRapida() {
        totalReservas++;
        reservasPendientes++;
        inicioPendientes++;
        actualizarMetricas();
        mostrarInformacion("Nueva reserva", "Se registró una reserva pendiente a través del acceso rápido.");
    }

    private void aprobarReservaDesdeCard(Button origen) {
        String titulo = obtenerTextoDesdeTarjeta(origen, Arrays.asList("reservation-title", "list-item-title"));
        if (titulo.isBlank()) {
            titulo = "reserva seleccionada";
        }
        inicioPendientes = decrementarSiPositivo(inicioPendientes);
        reservasPendientes = decrementarSiPositivo(reservasPendientes);
        inicioActivas++;
        reservasAprobadas++;
        actualizarMetricas();
        mostrarInformacion("Reserva aprobada", "La " + titulo + " ha sido aprobada correctamente.");
    }

    private void rechazarReservaDesdeCard(Button origen) {
        String titulo = obtenerTextoDesdeTarjeta(origen, Arrays.asList("reservation-title", "list-item-title"));
        if (titulo.isBlank()) {
            titulo = "reserva seleccionada";
        }
        inicioPendientes = decrementarSiPositivo(inicioPendientes);
        reservasPendientes = decrementarSiPositivo(reservasPendientes);
        reservasRechazadas++;
        actualizarMetricas();
        mostrarInformacion("Reserva rechazada", "La " + titulo + " fue rechazada y se notificará al solicitante.");
    }

    private void crearEspacio() {
        mostrarInformacion("Nuevo espacio", "Abriendo asistente para registrar un nuevo espacio municipal.");
    }

    private void editarEspacio(Button origen) {
        String titulo = obtenerTextoDesdeTarjeta(origen, List.of("space-title"));
        if (titulo.isBlank()) {
            titulo = "espacio seleccionado";
        }
        mostrarInformacion("Editar espacio", "Preparando edición para " + titulo + ".");
    }

    private void crearNotificacion() {
        mostrarInformacion("Nueva notificación", "Configurando nueva notificación para los usuarios seleccionados.");
    }

    private void suspenderUsuario(Button origen) {
        String usuario = obtenerTextoDesdeTarjeta(origen, List.of("user-item-name"));
        if (usuario.isBlank()) {
            usuario = "usuario seleccionado";
        }
        mostrarInformacion("Suspender usuario", "Se ha iniciado el proceso de suspensión para " + usuario + ".");
    }

    private void exportarReservas() {
        mostrarInformacion("Exportar reservas", "Generando archivo con el estado actual de las reservas.");
    }

    private void exportarLogs() {
        mostrarInformacion("Exportar auditoría", "Descargando el registro de auditoría en formato CSV.");
    }

    private void mostrarDetalles(Button origen) {
        Parent tarjeta = encontrarTarjeta(origen);
        if (tarjeta == null) {
            mostrarInformacion("Detalles", "Mostrando información relevante del elemento seleccionado.");
            return;
        }

        if (tarjeta.getStyleClass().contains("reservation-item")) {
            String titulo = obtenerTextoDesdeTarjeta(tarjeta, "reservation-title");
            String detalles = obtenerTextoDesdeTarjeta(tarjeta, "reservation-details");
            mostrarInformacion("Detalles de reserva", titulo + "\n" + detalles);
            return;
        }

        if (tarjeta.getStyleClass().contains("list-item")) {
            String titulo = obtenerTextoDesdeTarjeta(tarjeta, "list-item-title");
            String id = obtenerTextoDesdeTarjeta(tarjeta, "list-item-id");
            mostrarInformacion("Detalles de reserva", titulo + "\n" + id);
            return;
        }

        if (tarjeta.getStyleClass().contains("space-card")) {
            String titulo = obtenerTextoDesdeTarjeta(tarjeta, "space-title");
            String capacidad = obtenerTextoDesdeTarjeta(tarjeta, "space-capacity");
            mostrarInformacion("Detalle de espacio", titulo + "\n" + capacidad);
            return;
        }

        if (tarjeta.getStyleClass().contains("notification-item")) {
            String titulo = obtenerTextoDesdeTarjeta(tarjeta, "notification-title");
            String descripcion = obtenerTextoDesdeTarjeta(tarjeta, "notification-description");
            mostrarInformacion("Detalle de notificación", titulo + "\n" + descripcion);
            return;
        }

        if (tarjeta.getStyleClass().contains("user-item")) {
            String nombre = obtenerTextoDesdeTarjeta(tarjeta, "user-item-name");
            String correo = obtenerTextoDesdeTarjeta(tarjeta, "detail-text");
            mostrarInformacion("Perfil de usuario", nombre + "\n" + correo);
            return;
        }

        if (tarjeta.getStyleClass().contains("audit-item")) {
            String accion = obtenerTextoDesdeTarjeta(tarjeta, "audit-action");
            String descripcion = obtenerTextoDesdeTarjeta(tarjeta, "audit-description");
            mostrarInformacion("Detalle de auditoría", accion + "\n" + descripcion);
        }
    }

    private Parent encontrarTarjeta(Node origen) {
        Node actual = origen;
        while (actual != null) {
            if (actual instanceof Parent parent) {
                List<String> estilos = parent.getStyleClass();
                if (estilos.contains("reservation-item") || estilos.contains("list-item") || estilos.contains("space-card")
                        || estilos.contains("notification-item") || estilos.contains("user-item") || estilos.contains("audit-item")) {
                    return parent;
                }
            }
            actual = actual.getParent();
        }
        return null;
    }

    private String obtenerTextoDesdeTarjeta(Button origen, List<String> clases) {
        Parent tarjeta = encontrarTarjeta(origen);
        if (tarjeta == null) {
            return "";
        }
        for (String clase : clases) {
            String texto = obtenerTextoDesdeTarjeta(tarjeta, clase);
            if (!texto.isBlank()) {
                return texto;
            }
        }
        return "";
    }

    private String obtenerTextoDesdeTarjeta(Parent tarjeta, String clase) {
        Node nodo = tarjeta.lookup("." + clase);
        if (nodo instanceof Label label) {
            return label.getText();
        }
        return "";
    }

    private int decrementarSiPositivo(int valor) {
        return valor > 0 ? valor - 1 : 0;
    }

    private void actualizarMetricas() {
        if (lblPendientes != null) {
            lblPendientes.setText(Integer.toString(inicioPendientes));
        }
        if (lblActivas != null) {
            lblActivas.setText(Integer.toString(inicioActivas));
        }
        if (lblEspaciosDisp != null) {
            lblEspaciosDisp.setText(Integer.toString(inicioEspaciosDisponibles));
        }
        if (lblCheckins != null) {
            lblCheckins.setText(Integer.toString(inicioCheckins));
        }
        if (lblTotalReservas != null) {
            lblTotalReservas.setText(Integer.toString(totalReservas));
        }
        if (lblReservasPendientes != null) {
            lblReservasPendientes.setText(Integer.toString(reservasPendientes));
        }
        if (lblReservasAprobadas != null) {
            lblReservasAprobadas.setText(Integer.toString(reservasAprobadas));
        }
        if (lblReservasRechazadas != null) {
            lblReservasRechazadas.setText(Integer.toString(reservasRechazadas));
        }
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        actualizarNombreSupervisor();
    }

    @FXML
    private void mostrarInicio() {
        mostrarSeccion(vistaInicio, btnInicio);
    }

    @FXML
    private void mostrarReservas() {
        mostrarSeccion(vistaReservas, btnReservas);
    }

    @FXML
    private void mostrarEspacios() {
        mostrarSeccion(vistaEspacios, btnEspacios);
    }

    @FXML
    private void mostrarNotificaciones() {
        mostrarSeccion(vistaNotificaciones, btnNotificaciones);
    }

    @FXML
    private void mostrarUsuarios() {
        mostrarSeccion(vistaUsuarios, btnUsuarios);
    }

    @FXML
    private void mostrarAuditoria() {
        mostrarSeccion(vistaAuditoria, btnAuditoria);
    }

    private void mostrarSeccion(ScrollPane objetivo, Button botonActivo) {
        secciones.forEach(seccion -> {
            seccion.setVisible(false);
            seccion.setManaged(false);
        });
        if (objetivo == null || botonActivo == null) {
            return;
        }
        for (ScrollPane seccion : secciones) {
            if (seccion != null) {
                seccion.setVisible(false);
                seccion.setManaged(false);
            }
        }
        objetivo.setVisible(true);
        objetivo.setManaged(true);

       menuButtons.forEach(button -> {
            button.getStyleClass().remove("active");
            button.getStyleClass().remove("nav-item-active");
        });

        for (Button menuButton : menuButtons) {
            if (menuButton != null) {
                menuButton.getStyleClass().remove("nav-btn-active");
            }
        }

        if (!botonActivo.getStyleClass().contains("nav-item-active")) {
            botonActivo.getStyleClass().add("nav-item-active");
        }

        if (!botonActivo.getStyleClass().contains("nav-btn-active")) {
            botonActivo.getStyleClass().add("nav-btn-active");
        }
    }

    private void actualizarNombreSupervisor() {
        if (lblNombreSupervisor == null) {
            return;
        }

        String displayName = sessionManager != null ? sessionManager.getUserDisplayName() : null;
        if (displayName == null || displayName.isBlank()) {
            lblNombreSupervisor.setText("Supervisor");
        } else {
            lblNombreSupervisor.setText(displayName);
        }
    }

    private void mostrarInformacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        configurarDialogo(alert);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void configurarDialogo(Dialog<?> dialogo) {
        Window window = obtenerVentana();
        if (window != null) {
            dialogo.initOwner(window);
        }
    }

    private Window obtenerVentana() {
        if (contenedorPrincipal != null && contenedorPrincipal.getScene() != null) {
            return contenedorPrincipal.getScene().getWindow();
        }
        return null;
    }
}