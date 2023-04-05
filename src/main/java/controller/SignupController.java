package controller;

import domain.User;
import domain.validation.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import repository.RepositoryException;
import service.Service;
import service.ServiceException;

public class SignupController {
    private Service service;
    private Stage loginStage;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private TextField textFieldEmail;
    @FXML
    private PasswordField passwordField;

    public void setService(Service service) {
        this.service = service;
    }

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }

    public void handleAccountCreation(ActionEvent event) {
        String username = textFieldUsername.getText();
        String email = textFieldEmail.getText();
        String password = passwordField.getText();
        try {
            service.addUser(username, email, password);
            PopupMessage.showInformationMessage("Account created successfully!");
            Stage signupStage  = (Stage) textFieldUsername.getScene().getWindow();
            signupStage.close();
            loginStage.show();
        } catch (ValidationException | RepositoryException | ServiceException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }
}
