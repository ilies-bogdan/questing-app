package controller;

import domain.*;
import domain.validation.ValidationException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import repository.AwardedBadgeRepository;
import repository.RepositoryException;
import service.BadgeService;
import service.QuestService;
import service.UserService;
import utils.Constants;

import java.util.List;
import java.util.Optional;

public class GameController {
    private UserService userSrv;
    private QuestService questSrv;
    private BadgeService badgeSrv;
    private Quest quest;
    private int guessCount = 0;
    @FXML
    private GridPane gridPane;
    @FXML
    private TextField textFieldGuess;
    @FXML
    private Button buttonGuess;

    public void setUserSrv(UserService userSrv) {
        this.userSrv = userSrv;
    }

    public void setQuestService(QuestService questSrv) {
        this.questSrv = questSrv;
    }

    public void setBadgeSrv(BadgeService badgeSrv) {
        this.badgeSrv = badgeSrv;
    }

    public void setQuest(Quest quest) {
        this.quest = quest;
        initModel();
    }

    private void initModel() {
        Stage stage  = (Stage) gridPane.getScene().getWindow();
        stage.setOnCloseRequest(event -> handleCloseRequest());
        for (int i = 0; i < Constants.MAX_GUESS_COUNT; i++) {
            for (int j = 0; j < Constants.WORD_SIZE; j++) {
                TextField textField = new TextField();
                textField.setEditable(false);
                gridPane.add(textField, j, i);
                GridPane.setConstraints(textField, j, i);
            }
        }
    }

    private void handleCloseRequest() {
        if (quest.getStatus() != QuestStatus.accepted) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Are you sure?");
        alert.setContentText("If you quit now, you will fail the quest.");
        Optional<ButtonType> option =  alert.showAndWait();
        if (option.isEmpty() || option.get() == ButtonType.CANCEL) {
            return;
        }
        handleFailQuest();
        ((Stage) gridPane.getScene().getWindow()).close();
    }

    public void handleGuess(ActionEvent event) {
        String guess = textFieldGuess.getText();
        textFieldGuess.clear();

        try {
            questSrv.validateWord(guess);
        } catch (ValidationException e) {
            PopupMessage.showErrorMessage(e.getMessage());
            return;
        }

        ObservableList<Node> nodes = gridPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof TextField && GridPane.getRowIndex(node) == guessCount) {
                int index = GridPane.getColumnIndex(node);
                char letter = guess.charAt(index);
                ((TextField) node).setText(String.valueOf(letter).toUpperCase());
                node.getStyleClass().add(questSrv.getLetterStatus(index, letter, quest.getWord()).toString());
            }
        }

        guessCount += 1;

        if (quest.getWord().equals(guess)) {
            handleCompleteQuest();
            return;
        }

        if (guessCount == Constants.MAX_GUESS_COUNT) {
            handleFailQuest();
        }
    }

    private void handleCompleteQuest() {
        try {
            quest.setStatus(QuestStatus.completed);
            questSrv.updateQuest(quest.getId(), quest.getGiverId(), quest.getPlayerId(), quest.getDateOfPosting(),
                    quest.getReward(), QuestStatus.completed, quest.getWord());
            User player = userSrv.findUserById(quest.getPlayerId());
            player.setTokenCount(player.getTokenCount() + quest.getReward());
            player.updateUserRank();
            userSrv.updateUser(player.getId(), player.getUsername(), player.getEmail(), player.getPasswordCode(),
                    player.getSalt(), player.getRank(), player.getTokenCount());
        } catch (RepositoryException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
        textFieldGuess.setDisable(true);
        buttonGuess.setDisable(true);
        PopupMessage.showInformationMessage("Quest Completed!");
        awardBadges();
    }

    private void handleFailQuest() {
        try {
            quest.setStatus(QuestStatus.failed);
            questSrv.updateQuest(quest.getId(), quest.getGiverId(), quest.getPlayerId(), quest.getDateOfPosting(),
                    quest.getReward(), QuestStatus.failed, quest.getWord());
            User giver = userSrv.findUserById(quest.getGiverId());
            giver.setTokenCount(giver.getTokenCount() + quest.getReward());
            giver.updateUserRank();
            userSrv.updateUser(giver.getId(), giver.getUsername(), giver.getEmail(), giver.getPasswordCode(),
                    giver.getSalt(), giver.getRank(), giver.getTokenCount());
        } catch (RepositoryException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
        textFieldGuess.setDisable(true);
        buttonGuess.setDisable(true);
        PopupMessage.showInformationMessage("Quest Failed!");
    }

    private void awardBadges() {
        User player = userSrv.findUserById(quest.getPlayerId());
        for (Badge badge : badgeSrv.getAllBadges()) {
            if (!badgeSrv.userHasBadge(player, badge)) {
                if (badge.getType() == BadgeType.complete) {
                    if (questSrv.getCompletedQuestsCount(player) >= badge.getRequirement()) {
                        try {
                            badgeSrv.addBadgeToUser(player, badge);
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
