package com.municipal.ui.utils;

import javafx.scene.Node;

/**
 * Utility helpers to toggle visibility and layout participation for JavaFX nodes.
 */
public final class NodeVisibilityUtils {

    private NodeVisibilityUtils() {
        // Utility class
    }

    public static void show(Node node) {
        if (node == null) {
            return;
        }
        node.setVisible(true);
        node.setManaged(true);
    }

    public static void hide(Node node) {
        if (node == null) {
            return;
        }
        node.setVisible(false);
        node.setManaged(false);
    }
}
