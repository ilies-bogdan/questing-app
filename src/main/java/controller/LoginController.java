package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import service.BadgeService;
import service.QuestService;
import service.UserService;

import java.io.IOException;

public class LoginController {
    private UserService userSrv;
    private QuestService questSrv;
    private BadgeService badgeSrv;
    @FXML
    private Label labelSignUp;
    @FXML
    private TextField textFieldUsername;
    @FXML
    private PasswordField passwordField;

    public void setUserSrv(UserService userSrv) {
        this.userSrv = userSrv;
    }

    public void setQuestSrv(QuestService questSrv) {
        this.questSrv = questSrv;
    }

    public void setBadgeSrv(BadgeService badgeSrv) {
        this.badgeSrv = badgeSrv;
    }

    /**
     * Handles the sign-up request by opning the sign-up window.
     */
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
        signupCtr.setService(userSrv);
        Stage loginStage  = (Stage) textFieldUsername.getScene().getWindow();
        signupCtr.setLoginStage(loginStage);
        loginStage.hide();

        stage.show();
    }

    /**
     * Handles the login request by either granting access or revoking it based on credentials.
     */
    public void handleLoginRequest(ActionEvent event) throws IOException {
        String username = textFieldUsername.getText();
        String password = passwordField.getText();

        if (userSrv.checkPassword(username, password)) {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/questing-view.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(fxmlLoader.load(), 1200, 600);
            scene.getStylesheets()
                    .add(getClass().getClassLoader().getResource("styles/questing-style.css").toExternalForm());
            stage.setResizable(false);
            stage.setTitle("Questie");
            stage.setScene(scene);

            QuestingController questingCtr = fxmlLoader.getController();
            questingCtr.setUserSrv(userSrv);
            questingCtr.setQuestSrv(questSrv);
            questingCtr.setBadgeSrv(badgeSrv);
            questingCtr.setUser(userSrv.findUserByUsername(username));

            textFieldUsername.clear();
            passwordField.clear();
            stage.show();
        } else {
            PopupMessage.showErrorMessage("Invalid login credentials!");
        }
    }
}
