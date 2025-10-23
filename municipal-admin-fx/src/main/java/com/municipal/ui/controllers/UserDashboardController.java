package com.municipal.ui.controllers;

import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.SessionAware;
import com.municipal.ui.navigation.ViewLifecycle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.util.Locale;

/**
 * Placeholder controller for the standard user experience. It reacts to the
 * authenticated user and displays a friendly message until the dedicated
 * functionality is implemented.
 */
public class UserDashboardController implements SessionAware, ViewLifecycle {

    @FXML
    private Label lblWelcome;
    @FXML
    private Label lblSubtitle;
    @FXML
    private Button btnExplorar;
    @FXML
    private Button btnMisReservas;

    private SessionManager sessionManager;

    @Override
    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void onViewActivated() {
        actualizarMensajeBienvenida();
    }

    private void actualizarMensajeBienvenida() {
        if (lblWelcome == null) {
            return;
        }
        String nombre = sessionManager != null ? sessionManager.getUserDisplayName() : null;
        if (nombre == null || nombre.isBlank()) {
            nombre = sessionManager != null ? sessionManager.getUserEmail() : null;
        }
        if (nombre == null || nombre.isBlank()) {
            nombre = "Usuario";
        }
        lblWelcome.setText(String.format(Locale.getDefault(), "¡Hola %s!", nombre));
        if (lblSubtitle != null) {
            lblSubtitle.setText("En breve podrás gestionar tus reservas y explorar espacios disponibles.");
        }
        if (btnExplorar != null) {
            btnExplorar.setDisable(true);
        }
        if (btnMisReservas != null) {
            btnMisReservas.setDisable(true);
        }
    }
}
