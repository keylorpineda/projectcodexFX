package com.municipal.ui;

import com.municipal.config.AppConfig;
import com.municipal.ui.controllers.LoginController;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppConfig.load();
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/municipal/reservationsfx/ui/login-view.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        MaterialFXStylesheets.DEFAULT.applyOn(scene);
        scene.getStylesheets().addAll(
                App.class.getResource("/com/municipal/reservationsfx/styles/styles.css").toExternalForm(),
                App.class.getResource("/com/municipal/reservationsfx/styles/login-view.css").toExternalForm()
        );
        stage.setTitle("Sistema de Reservas Municipales");
        stage.setScene(scene);
        stage.setMinWidth(860);
        stage.setMinHeight(640);
        stage.centerOnScreen();
        LoginController controller = loader.getController();
        controller.setStage(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
