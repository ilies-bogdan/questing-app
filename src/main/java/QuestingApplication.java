import controller.LoginController;
import controller.PopupMessage;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import repository.UserRepository;
import repository.database.QuestDBRepository;
import repository.database.UserDBRepository;
import service.QuestService;
import service.UserService;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class QuestingApplication extends Application {
    private UserService userSrv;
    private QuestService questSrv;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }

        String url = props.getProperty("jdbc.url");
        UserRepository userRepo = new UserDBRepository(url);
        userSrv = new UserService(userRepo);
        questSrv = new QuestService(userRepo, new QuestDBRepository(url));

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
        loginCtr.setUserSrv(userSrv);
        loginCtr.setQuestSrv(questSrv);

        primaryStage.show();
    }
}
