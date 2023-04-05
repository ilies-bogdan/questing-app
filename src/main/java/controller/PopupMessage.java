package controller;

import javafx.scene.control.Alert;

public class PopupMessage {
    public static void showInformationMessage(String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Info");
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showErrorMessage(String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(content);
        alert.showAndWait();
    }
}
