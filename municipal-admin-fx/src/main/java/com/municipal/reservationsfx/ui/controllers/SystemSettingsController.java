package com.municipal.reservationsfx.ui.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class SystemSettingsController implements DashboardSubview {

    @FXML
    private TabPane settingsTabs;
    @FXML
    private TextField weatherApiField;
    @FXML
    private TextField emailApiField;
    @FXML
    private TextField smsApiField;
    @FXML
    private TextField webhookField;
    @FXML
    private TextArea cancellationPolicyArea;
    @FXML
    private CheckBox requireApprovalCheck;
    @FXML
    private CheckBox mfaCheck;
    @FXML
    private CheckBox sessionTimeoutCheck;
    @FXML
    private CheckBox auditLogCheck;
    @FXML
    private Button saveButton;
    @FXML
    private Label statusLabel;

    private Timeline statusTimeline;

    @FXML
    public void initialize() {
        configureDefaults();
        configureSaveAction();
    }

    @Override
    public void onDisplay(AdminDashboardController host) {
        animateButton(saveButton);
    }

    private void configureDefaults() {
        weatherApiField.setText("sk_live_weather_12345");
        emailApiField.setText("sg-live-67890");
        smsApiField.setText("twilio-abcdef");
        webhookField.setText("https://municipalidad.go.cr/hooks/alertas");
        cancellationPolicyArea.setText("Las cancelaciones dentro de las 24 horas requieren aprobación del administrador.");
        requireApprovalCheck.setSelected(true);
        mfaCheck.setSelected(true);
        sessionTimeoutCheck.setSelected(true);
        auditLogCheck.setSelected(true);
    }

    private void configureSaveAction() {
        saveButton.getStyleClass().add("primary-button-raised");
        saveButton.setOnAction(event -> {
            animateButton(saveButton);
            showStatus("Cambios guardados correctamente", "status-success");
        });
    }

    private void showStatus(String message, String styleClass) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
        if (styleClass != null && !styleClass.isBlank()) {
            statusLabel.getStyleClass().add(styleClass);
        }
        if (statusTimeline != null) {
            statusTimeline.stop();
        }
        statusTimeline = new Timeline(new KeyFrame(Duration.seconds(4), evt -> {
            statusLabel.setText("Sin cambios");
            statusLabel.getStyleClass().removeAll("status-success", "status-error", "status-info");
        }));
        statusTimeline.playFromStart();
    }

    private void animateButton(Button button) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(220), button);
        scale.setFromX(1);
        scale.setFromY(1);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.playFromStart();
    }
}
