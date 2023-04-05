package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import service.Service;

import java.io.IOException;

public class LoginController {
    private Service service;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private PasswordField passwordField;

    public void setService(Service service) {
        this.service = service;
    }

    public void handleSignUpRequest(MouseEvent mouseEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/signup-view.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 600, 540);
        scene.getStylesheets()
                .add(getClass().getClassLoader().getResource("styles/login-style.css").toExternalForm());
        stage.setResizable(false);
        stage.setTitle("Sign Up");
        stage.setScene(scene);

        SignupController signupCtr = fxmlLoader.getController();
        signupCtr.setService(service);
        Stage loginStage  = (Stage) textFieldUsername.getScene().getWindow();
        signupCtr.setLoginStage(loginStage);
        loginStage.hide();

        stage.show();
    }

    public void handleLoginRequest(ActionEvent event) throws IOException {
        String username = textFieldUsername.getText();
        String password = passwordField.getText();

        if (service.checkPassword(username, password)) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/main-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load(), 1000, 600);
//            scene.getStylesheets()
//                    .add(getClass().getClassLoader().getResource("styles/login-style.css").toExternalForm());
            stage.setResizable(false);
            stage.setTitle("Questie");
            stage.setScene(scene);

            MainController mainCtr = fxmlLoader.getController();
            mainCtr.setService(service);
            mainCtr.setUser(service.findUserByUsername(username));
            Stage loginStage  = (Stage) textFieldUsername.getScene().getWindow();
            mainCtr.setLoginStage(loginStage);

            passwordField.clear();
            loginStage.hide();
            stage.show();
        } else {
            PopupMessage.showErrorMessage("Invalid login credentials!");
        }
    }
}
