package controller;

import domain.*;
import domain.validation.ValidationException;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import repository.RepositoryException;
import service.BadgeService;
import service.QuestService;
import service.ServiceException;
import service.UserService;
import utils.Constants;
import utils.observer.Observer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuestingController implements Observer {
    private UserService userSrv;
    private QuestService questSrv;
    private BadgeService badgeSrv;
    private User user;
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

    public void setUserSrv(UserService userSrv) {
        this.userSrv = userSrv;
        userSrv.addObserver(this);
    }

    public void setQuestSrv(QuestService questSrv) {
        this.questSrv = questSrv;
        questSrv.addObserver(this);
    }

    public void setBadgeSrv(BadgeService badgeSrv) {
        this.badgeSrv = badgeSrv;
    }

    public void setUser(User user) {
        this.user = user;
        initModel();
    }

    @Override
    public void update() {
        initModel();
    }

    private void initModel() {
        user = userSrv.findUserById(user.getId());
        labelRankTokens.setText("Your rank: " + user.getRank() + " | You have " + user.getTokenCount() + " tokens.");
        modelQuestJournal.setAll((Collection<Quest>) questSrv.getQuestJournal(user));
        modelAvailableQuests.setAll((Collection<Quest>) questSrv.getAvailableQuests(user));
        modelMyQuests.setAll((Collection<Quest>) questSrv.getPostedQuests(user));
    }

    @FXML
    public void initialize() {
        tableViewQuestJournal.setPlaceholder(new Label("Journal is empty"));
        tableViewAvailableQuests.setPlaceholder(new Label("No quests available"));
        tableViewMyQuests.setPlaceholder(new Label("You have posted no quests"));
        spinnerReward.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000000));

        tableColumnQuestJournalQuestGiver.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(userSrv.findUserById(param.getValue().getGiverId()).getUsername()));
        tableColumnQuestJournalGiverRank.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(userSrv.findUserById(param.getValue().getGiverId()).getRank().toString()));
        tableColumnQuestJournalDateOfAccepting.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(param.getValue().getDateOfPosting().format(Constants.DATE_TIME_FORMATTER)));
        tableColumnQuestJournalReward.setCellValueFactory(new PropertyValueFactory<>("reward"));
        tableColumnQuestJournalStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableViewQuestJournal.setItems(modelQuestJournal);

        tableColumnAvailableQuestsQuestGiver.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(userSrv.findUserById(param.getValue().getGiverId()).getUsername()));
        tableColumnAvailableQuestsGiverRank.setCellValueFactory(param ->
                new ReadOnlyObjectWrapper<>(userSrv.findUserById(param.getValue().getGiverId()).getRank().toString()));
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

    /**
     * Handles search by filtering the view to only include the requested quests.
     */
    private void handleSearch() {
        Predicate<Quest> byPlayer = quest ->
            userSrv.findUserById(quest.getGiverId())
                    .getUsername().toLowerCase().startsWith(textFieldSearchPlayer.getText().toLowerCase());
        modelAvailableQuests.setAll(StreamSupport.stream(
                questSrv.getAvailableQuests(user).spliterator(),false)
                .filter(byPlayer)
                .collect(Collectors.toList()));
    }

    /**
     * Handles posting of new quests by validating them and adding them to the tables accordingly.
     */
    public void handlePostNewQuest(ActionEvent event) {
        String word = textFieldWord.getText();
        int reward = 0;
        try {
            reward = Integer.parseInt(spinnerReward.getEditor().textProperty().get());
        } catch (NumberFormatException e) {
            PopupMessage.showErrorMessage("Invalid reward value!");
        }

        try {
            questSrv.addQuest(user.getId(), reward, word);
            user.setTokenCount(user.getTokenCount() - reward);
            user.updateUserRank();
            userSrv.updateUser(user.getId(), user.getUsername(), user.getEmail(), user.getPasswordCode(),
                    user.getSalt(), user.getRank(), user.getTokenCount());
            textFieldWord.clear();
            spinnerReward.getEditor().textProperty().set("1");
            PopupMessage.showInformationMessage("Quest posted!");
            awardBadges();
        } catch (ValidationException | RepositoryException | ServiceException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles accepting quests by updating the corresponding data.
     */
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
            questSrv.updateQuest(quest.getId(), quest.getGiverId(), user.getId(),
                    LocalDateTime.now(), quest.getReward(), QuestStatus.accepted, quest.getWord());
            PopupMessage.showInformationMessage("Quest accepted!");
        } catch (RepositoryException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
    }

    /**
     * Handles starting quests by loading the game window.
     */
    public void handleStartQuest(ActionEvent event) throws IOException {
        if (tableViewQuestJournal.getSelectionModel().isEmpty()) {
            PopupMessage.showErrorMessage("No quest selected!");
            return;
        }

        Quest quest = tableViewQuestJournal.getSelectionModel().getSelectedItem();
        if (quest.getStatus() != QuestStatus.accepted) {
            PopupMessage.showErrorMessage("Can not redo quest!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Once started, you must finish the quest or you will fail it.");
        Optional<ButtonType> option =  alert.showAndWait();
        if (option.isEmpty() || option.get() == ButtonType.CANCEL) {
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/game-view.fxml"));
        Stage stage = new Stage();
        Scene scene = new Scene(fxmlLoader.load(), 900, 540);
        scene.getStylesheets()
                .add(getClass().getClassLoader().getResource("styles/game-style.css").toExternalForm());
        stage.setResizable(false);
        stage.setTitle("Quest");
        stage.setScene(scene);

        GameController gameCtr = fxmlLoader.getController();
        gameCtr.setUserSrv(userSrv);
        gameCtr.setQuestService(questSrv);
        gameCtr.setBadgeSrv(badgeSrv);
        gameCtr.setQuest(quest);
        stage.show();
    }

    /**
     * Handles viewing of badges by loading the badges window.
     */
    public void handleViewBadges(ActionEvent event) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("views/badge-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 540);
        Stage stage = new Stage();
        scene.getStylesheets()
                .add(getClass().getClassLoader().getResource("styles/questing-style.css").toExternalForm());
        stage.setResizable(false);
        stage.setTitle("Badges");
        stage.setScene(scene);

        BadgeController badgeCtr = fxmlLoader.getController();
        badgeCtr.setBadgeSrv(badgeSrv);
        badgeCtr.setUser(user);

        stage.show();
    }

    /**
     * Awards badges to the player according to how many quests the user posted.
     */
    private void awardBadges() {
        for (Badge badge : badgeSrv.getAllBadges()) {
            if (!badgeSrv.userHasBadge(user, badge)) {
                if (badge.getType() == BadgeType.post) {
                    if (((List<Quest>) questSrv.getPostedQuests(user)).size() >= badge.getRequirement()) {
                        try {
                            badgeSrv.addBadgeToUser(user, badge);
                            PopupMessage.showInformationMessage("You earned a badge: " + badge.getTitle());
                        } catch (RepositoryException e) {
                            PopupMessage.showErrorMessage(e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
