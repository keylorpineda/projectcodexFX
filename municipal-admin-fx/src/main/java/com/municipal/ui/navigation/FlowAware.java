package com.municipal.ui.navigation;

/**
 * Marks a controller that needs a reference to the {@link FlowController}.
 */
public interface FlowAware {
    void setFlowController(FlowController flowController);
}
