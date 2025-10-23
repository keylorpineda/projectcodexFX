package com.municipal.ui.navigation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable configuration describing how to render a particular view.
 */
public record ViewConfig(String fxmlPath, List<String> stylesheets) {

    public ViewConfig {
        Objects.requireNonNull(fxmlPath, "fxmlPath");
        if (stylesheets == null) {
            stylesheets = List.of();
        }
        stylesheets = List.copyOf(stylesheets);
    }

    @Override
    public List<String> stylesheets() {
        return Collections.unmodifiableList(stylesheets);
    }
}
