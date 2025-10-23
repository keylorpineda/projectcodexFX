package com.municipal.ui.navigation;

import javafx.stage.Stage;

/**
 * Gives controllers a hook to receive the primary {@link Stage} instance.
 */
public interface StageAware {
    void setStage(Stage stage);
}
