package domain.validation;

import domain.Quest;
import domain.QuestStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class QuestValidatorTest {
    private static final AbstractQuestValidator questValidator = new QuestValidator();

    @Test
    public void testQuestValidatorSuccess() {
        Quest quest = new Quest(1, 2, 3, LocalDateTime.MAX, 40, QuestStatus.posted, "words");
        assertDoesNotThrow(() -> questValidator.validate(quest));
    }

    @Test
    public void testQuestValidatorFailure() {
        Quest questInvalidReward = new Quest(1, 2, 3, LocalDateTime.MAX, 0, QuestStatus.posted, "words");
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> questValidator.validate(questInvalidReward));
        assertEquals("Reward must be greater than 0!\n", validationException.getMessage());
        Quest questInvalidWord = new Quest(1, 2, 3, LocalDateTime.MAX, 1, QuestStatus.posted, "qwert");
        validationException = assertThrows(ValidationException.class,
                () -> questValidator.validate(questInvalidWord));
        assertEquals("Unknown word!\n", validationException.getMessage());
    }
}
