package domain.validation;

import controller.PopupMessage;
import domain.Quest;
import utils.Constants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuestValidator implements AbstractQuestValidator {
    private List<String> words;

    public QuestValidator() {
        words = new ArrayList<>();
        Path path = null;
        try {
            path = Paths.get(getClass().getClassLoader().getResource("data/words.txt").toURI());
        } catch (URISyntaxException e) {
            PopupMessage.showErrorMessage(e.getMessage());
        }
        try {
            List<String> lines = Files.readAllLines(path);
            words.addAll(lines);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void validate(Quest quest) throws ValidationException {
        String errorMessage = "";

        if (quest.getReward() < 1) {
            errorMessage += "Reward must be greater than 0!\n";
        }

        String word = quest.getWord();
        if (word.length() != Constants.WORD_SIZE) {
            errorMessage += "Word must have " + Constants.WORD_SIZE + " letters!";
        } else if (!words.contains(word)) {
            errorMessage += "Unknown word!\n";
        }

        if (errorMessage.length() > 0) {
            throw new ValidationException(errorMessage);
        }
    }
}
