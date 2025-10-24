package com.municipal.ui.controllers;

import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.SessionAware;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class SupervisorDashboardController implements Initializable, SessionAware {

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

    private SessionManager sessionManager;
    private List<ScrollPane> secciones;
    private List<Button> menuButtons;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        secciones = List.of(vistaInicio, vistaReservas, vistaEspacios, vistaNotificaciones, vistaUsuarios, vistaAuditoria);
        menuButtons = List.of(btnInicio, btnReservas, btnEspacios, btnNotificaciones, btnUsuarios, btnAuditoria);

        mostrarSeccion(vistaInicio, btnInicio);
        actualizarNombreSupervisor();
    }

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        actualizarNombreSupervisor();
    }

    @FXML
    private void mostrarInicio(ActionEvent event) {
        mostrarSeccion(vistaInicio, btnInicio);
    }

    @FXML
    private void mostrarReservas(ActionEvent event) {
        mostrarSeccion(vistaReservas, btnReservas);
    }

    @FXML
    private void mostrarEspacios(ActionEvent event) {
        mostrarSeccion(vistaEspacios, btnEspacios);
    }

    @FXML
    private void mostrarNotificaciones(ActionEvent event) {
        mostrarSeccion(vistaNotificaciones, btnNotificaciones);
    }

    @FXML
    private void mostrarUsuarios(ActionEvent event) {
        mostrarSeccion(vistaUsuarios, btnUsuarios);
    }

    @FXML
    private void mostrarAuditoria(ActionEvent event) {
        mostrarSeccion(vistaAuditoria, btnAuditoria);
    }

    private void mostrarSeccion(ScrollPane objetivo, Button botonActivo) {
        secciones.forEach(seccion -> {
            seccion.setVisible(false);
            seccion.setManaged(false);
        });
        objetivo.setVisible(true);
        objetivo.setManaged(true);

        menuButtons.forEach(button -> button.getStyleClass().remove("active"));
        if (!botonActivo.getStyleClass().contains("active")) {
            botonActivo.getStyleClass().add("active");
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
}