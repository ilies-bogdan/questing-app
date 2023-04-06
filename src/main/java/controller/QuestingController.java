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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuestingController implements Observer {
    private QuestingService service;
    private User user;
    private Stage loginStage;
    private ObservableList<Quest> modelMyQuests = FXCollections.observableArrayList();
    private ObservableList<Quest> modelAvailableQuests = FXCollections.observableArrayList();
    private ObservableList<Quest> modelQuestJournal = FXCollections.observableArrayList();
    @FXML
    private Label labelRankTokens;
    @FXML
    private TableView<Quest> tableViewQuestJournal;
    @FXML
    private TableColumn<Quest,String> tableColumnQuestJournalQuestGiver;
    @FXML
    private TableColumn<Quest,String> tableColumnQuestJournalGiverRank;
    @FXML
    private TableColumn<Quest,String> tableColumnQuestJournalDateOfAccepting;
    @FXML
    private TableColumn<Quest,Integer> tableColumnQuestJournalReward;
    @FXML
    private TableColumn<Quest, String> tableColumnQuestJournalStatus;
    @FXML
    private TableView<Quest> tableViewAvailableQuests;
    @FXML
    private TableColumn<Quest, String> tableColumnAvailableQuestsQuestGiver;
    @FXML
    private TableColumn<Quest, String> tableColumnAvailableQuestsGiverRank;
    @FXML
    private TableColumn<Quest, String> tableColumnAvailableQuestsDateOfPosting;
    @FXML
    private TableColumn<Quest, Integer> tableColumnAvailableQuestsReward;
    @FXML
    private TextField textFieldSearchPlayer;
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
        tableViewQuestJournal.setPlaceholder(new Label("Journal is empty"));
        tableViewAvailableQuests.setPlaceholder(new Label("No quests available"));
        tableViewMyQuests.setPlaceholder(new Label("You have posted no quests"));
        spinnerReward.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000));
        labelRankTokens.setText("Your rank: " + user.getRank() + " | You have " + user.getTokenCount() + " tokens.");
        modelQuestJournal.setAll((Collection<Quest>) service.getQuestJournal(user));
        modelAvailableQuests.setAll((Collection<Quest>) service.getAvailableQuests(user));
        modelMyQuests.setAll((Collection<Quest>) service.getPostedQuests(user));
    }

    @FXML
    public void initialize() {
        tableColumnQuestJournalQuestGiver.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(service.findUserById(param.getValue().getGiverId()).getUsername()));
        tableColumnQuestJournalGiverRank.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(service.findUserById(param.getValue().getGiverId()).getRank().toString()));
        tableColumnQuestJournalDateOfAccepting.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getDateOfPosting().format(Constants.DATE_TIME_FORMATTER)));
        tableColumnQuestJournalReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        tableColumnQuestJournalStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableViewQuestJournal.setItems(modelQuestJournal);

        tableColumnAvailableQuestsQuestGiver.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(service.findUserById(param.getValue().getGiverId()).getUsername()));
        tableColumnAvailableQuestsGiverRank.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(service.findUserById(param.getValue().getGiverId()).getRank().toString()));
        tableColumnAvailableQuestsDateOfPosting.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getDateOfPosting().format(Constants.DATE_TIME_FORMATTER)));
        tableColumnAvailableQuestsReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        tableViewAvailableQuests.setItems(modelAvailableQuests);
        textFieldSearchPlayer.textProperty().addListener(o -> handleSearch());

        tableColumnMyQuestsQuestWord.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getWord()));
        tableColumnMyQuestsDateOfPosting.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getDateOfPosting().format(Constants.DATE_TIME_FORMATTER)));
        tableColumnMyQuestsReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        tableColumnMyQuestsStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableViewMyQuests.setItems(modelMyQuests);
    }

    private void handleSearch() {
        Predicate<Quest> byPlayer = quest ->
            service.findUserById(quest.getGiverId())
                    .getUsername().toLowerCase().startsWith(textFieldSearchPlayer.getText());
        modelAvailableQuests.setAll(StreamSupport.stream(
                service.getAvailableQuests(user).spliterator(),false)
                .filter(byPlayer)
                .collect(Collectors.toList()));
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

    public void handleAcceptQuest(ActionEvent event) {
        if (tableViewAvailableQuests.getSelectionModel().isEmpty()) {
            PopupMessage.showErrorMessage("No quest selected!");
            return;
        }

        Quest quest = tableViewAvailableQuests.getSelectionModel().getSelectedItem();
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Once accepted, you can not give up the quest.");
        Optional<ButtonType> option =  alert.showAndWait();
        if (option.isEmpty() || option.get() == ButtonType.CANCEL) {
            return;
        }

        try {
            service.updateQuest(quest.getId(), quest.getGiverId(), user.getId(),
                    LocalDateTime.now(), quest.getReward(), QuestStatus.accepted, quest.getWord());
            PopupMessage.showInformationMessage("Quest accepted!");
        } catch (RepositoryException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }
}