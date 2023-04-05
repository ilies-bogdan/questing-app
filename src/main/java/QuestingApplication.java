import controller.LoginController;
import controller.PopupMessage;
import domain.Rank;
import domain.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.RepositoryException;
import repository.UserRepository;
import repository.database.UserDBRepository;
import service.Service;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class QuestingApplication extends Application {
    private Service service;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException, RepositoryException {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }

        service = new Service(new UserDBRepository(props.getProperty("jdbc.url")));

        initView(primaryStage);
    }

    private void initView(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("views/login-view.fxml"));
        Scene primaryScene = new Scene(fxmlLoader.load(), 600, 540);
        primaryScene.getStylesheets()
                .add(getClass().getResource("styles/login-style.css").toExternalForm());
        primaryStage.setResizable(false);
        primaryStage.setTitle("Login");
        primaryStage.setScene(primaryScene);

        LoginController loginCtr = fxmlLoader.getController();
        loginCtr.setService(service);

        primaryStage.show();
    }
}
