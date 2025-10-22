package com.municipal.reservationsfx.ui.controllers;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.util.List;

final class DashboardCardFactory {

    private DashboardCardFactory() {
    }

    static List<Pane> createSpaceCards() {
        return List.of(
                buildSpaceCard("Salón Comunal Central", "Interior", "150 personas", "Disponible"),
                buildSpaceCard("Cancha Deportiva Norte", "Exterior", "80 personas", "Reservado"),
                buildSpaceCard("Auditorio Municipal", "Interior", "300 personas", "Disponible"),
                buildSpaceCard("Plaza Cultural", "Exterior", "200 personas", "Mantenimiento")
        );
    }

    static void populateReservations(GridPane grid) {
        grid.getChildren().clear();
        String[][] reservations = new String[][]{
                {"María González", "Salón Comunal Central", "2025-10-22", "09:00", "Confirmada", "QR", "24°C"},
                {"Carlos Rodríguez", "Auditorio Municipal", "2025-10-22", "14:00", "Confirmada", "QR", "22°C"},
                {"Juan Pérez", "Cancha Norte", "2025-10-23", "16:00", "Pendiente", "QR", "26°C"},
                {"Ana Jiménez", "Parque Recreativo", "2025-10-23", "18:00", "Cancelada", "N/A", "25°C"}
        };

        int rowIndex = 1;
        for (String[] reservation : reservations) {
            for (int columnIndex = 0; columnIndex < reservation.length; columnIndex++) {
                Label label = new Label(reservation[columnIndex]);
                label.getStyleClass().add("table-cell");
                GridPane.setMargin(label, new Insets(6, 8, 6, 8));
                grid.add(label, columnIndex, rowIndex);
            }
            rowIndex++;
        }
    }

    private static Pane buildSpaceCard(String name, String type, String capacity, String status) {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("card", "space-card");
        card.setPadding(new Insets(16));

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("card-title");

        Label typeLabel = new Label(type);
        typeLabel.getStyleClass().add("muted-label");

        Label capacityLabel = new Label(capacity);
        capacityLabel.getStyleClass().add("muted-label");

        Label statusLabel = new Label(status);
        statusLabel.getStyleClass().addAll("status-pill", statusToClass(status));

        Button detailsButton = new Button("Ver detalles");
        detailsButton.getStyleClass().add("pill-button");

        card.getChildren().addAll(nameLabel, typeLabel, capacityLabel, statusLabel, detailsButton);
        return card;
    }

    private static String statusToClass(String status) {
        return switch (status.toLowerCase()) {
            case "disponible" -> "status-available";
            case "reservado" -> "status-booked";
            case "mantenimiento" -> "status-maintenance";
            default -> "status-default";
        };
    }
}
