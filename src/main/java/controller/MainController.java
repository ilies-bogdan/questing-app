package controller;

import domain.User;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import service.Service;

import javax.swing.text.html.ImageView;

public class MainController {
    private Service service;
    private User user;
    private Stage loginStage;

    public void setService(Service service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
    }
}
