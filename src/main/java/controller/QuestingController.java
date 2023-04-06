package controller;

import domain.Quest;
import domain.QuestStatus;
import domain.User;
import domain.validation.ValidationException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import repository.RepositoryException;
import service.QuestingService;
import service.ServiceException;
import utils.Constants;
import utils.observer.Observer;

import java.util.Collection;

public class QuestingController implements Observer {
    private QuestingService service;
    private User user;
    private Stage loginStage;
    private ObservableList<Quest> modelQuests = FXCollections.observableArrayList();
    @FXML
    private Label labelRankTokens;
    @FXML
    private TableView<Quest> tableViewMyQuests;
    @FXML
    private TableColumn<Quest, String> tableColumnMyQuestsQuestWord;
    @FXML
    private TableColumn<Quest, String> tableColumnMyQuestsDateOfPosting;
    @FXML
    private TableColumn<Quest, Integer> tableColumnMyQuestsReward;
    @FXML
    private TableColumn<Quest, QuestStatus> tableColumnMyQuestsStatus;
    @FXML
    private TextField textFieldWord;
    @FXML
    private Spinner<Integer> spinnerReward;

    public void setService(QuestingService service) {
        this.service = service;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setLoginStage(Stage loginStage) {
        this.loginStage = loginStage;
        service.addObserver(this);
        initModel();
    }

    @Override
    public void update() {
        initModel();
    }

    private void initModel() {
        spinnerReward.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000));
        labelRankTokens.setText("Your rank: " + user.getRank() + " | You have " + user.getTokenCount() + " tokens.");
        modelQuests.setAll((Collection<Quest>) service.getPostedQuests(user));
    }

    @FXML
    public void initialize() {
        tableColumnMyQuestsQuestWord.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getWord()));
        tableColumnMyQuestsDateOfPosting.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getDateOfPosting().format(Constants.DATE_TIME_FORMATTER)));
        tableColumnMyQuestsReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        tableColumnMyQuestsStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableViewMyQuests.setItems(modelQuests);
    }

    public void handlePostNewQuest(ActionEvent event) {
        String word = textFieldWord.getText();
        int reward = 0;
        try {
            reward = Integer.parseInt(spinnerReward.getEditor().textProperty().get());
        } catch (NumberFormatException e) {
            PopupMessage.showErrorMessage("Invalid reward value!");
        }

        try {
            service.addQuest(user.getId(), reward, word);
            user.setTokenCount(user.getTokenCount() - reward);
            user.updateUserRank();
            service.updateUser(user.getId(), user.getUsername(), user.getEmail(), user.getPasswordCode(),
                    user.getSalt(), user.getRank(), user.getTokenCount());
            textFieldWord.clear();
            spinnerReward.getEditor().textProperty().set("1");
            PopupMessage.showInformationMessage("Quest posted!");
        } catch (ValidationException | RepositoryException | ServiceException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }
}
