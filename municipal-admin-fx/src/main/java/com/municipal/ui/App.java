package com.municipal.ui;

import com.municipal.config.AppConfig;
import com.municipal.session.SessionManager;
import com.municipal.ui.navigation.FlowController;
import com.municipal.ui.navigation.ViewConfig;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

public class App extends Application {

    private static final String LOGIN_VIEW = "login";
    private static final String ADMIN_VIEW = "admin-dashboard";
    private static final String USER_VIEW = "user-dashboard";

    @Override
    public void start(Stage stage) throws Exception {
        AppConfig.load();

        SessionManager sessionManager = new SessionManager();
        FlowController flowController = new FlowController(stage, sessionManager);

        flowController.registerView(LOGIN_VIEW, new ViewConfig(
                "/com/municipal/reservationsfx/ui/login-view.fxml",
                List.of(
                        "/com/municipal/reservationsfx/styles/styles.css",
                        "/com/municipal/reservationsfx/styles/login-view.css"
                )));

        flowController.registerView(ADMIN_VIEW, new ViewConfig(
                "/com/municipal/reservationsfx/ui/admin-dashboard.fxml",
                List.of(
                        "/com/municipal/reservationsfx/styles/styles.css",
                        "/com/municipal/reservationsfx/styles/admin-dashboard.css"
                )));

        flowController.registerView(USER_VIEW, new ViewConfig(
                "/com/municipal/reservationsfx/ui/user-dashboard.fxml",
                List.of(
                        "/com/municipal/reservationsfx/styles/styles.css",
                        "/com/municipal/reservationsfx/styles/user-dashboard.css"
                )));

        flowController.setDefaultRoleView(USER_VIEW);
        flowController.registerRoleRoute("ADMIN", ADMIN_VIEW);
        flowController.registerRoleRoute("SUPER_ADMIN", ADMIN_VIEW);
        flowController.registerRoleRoute("MANAGER", ADMIN_VIEW);

        stage.setTitle("Sistema de Reservas Municipales");
        stage.setMinWidth(860);
        stage.setMinHeight(640);
        stage.centerOnScreen();

        flowController.showView(LOGIN_VIEW);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
