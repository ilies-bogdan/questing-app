package controller;

import domain.validation.ValidationException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import repository.RepositoryException;
import service.ServiceException;
import service.UserService;

public class SignupController {
    private UserService userSrv;
    private Stage loginStage;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private TextField textFieldEmail;
    @FXML
    private PasswordField passwordField;

    public void setService(UserService userSrv) {
        this.userSrv = userSrv;
    }

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }

    /**
     * Handles account creation by validating the data and creating a new account.
     */
    public void handleAccountCreation(ActionEvent event) {
        String username = textFieldUsername.getText();
        String email = textFieldEmail.getText();
        String password = passwordField.getText();
        try {
            userSrv.addUser(username, email, password);
            PopupMessage.showInformationMessage("Account created successfully!");
            Stage signupStage  = (Stage) textFieldUsername.getScene().getWindow();
            signupStage.close();
            loginStage.show();
        } catch (ValidationException | RepositoryException | ServiceException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles going back to the login window.
     */
    public void handleGoBack(ActionEvent event) {
        Stage signupStage  = (Stage) textFieldUsername.getScene().getWindow();
        signupStage.close();
        loginStage.show();
    }
}
