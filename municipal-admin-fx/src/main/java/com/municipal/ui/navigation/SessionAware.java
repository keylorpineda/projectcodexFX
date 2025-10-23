package com.municipal.ui.navigation;

import com.municipal.session.SessionManager;

/**
 * Allows controllers to access the shared {@link SessionManager}.
 */
public interface SessionAware {
    void setSessionManager(SessionManager sessionManager);
}
