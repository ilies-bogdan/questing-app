package domain.validation;

import domain.Quest;

public interface AbstractQuestValidator extends Validator<Quest> {
    /**
     * Validates a word for the quest.
     * @param word - the word to be validated
     * @throws ValidationException if the word is of invalid size
     *                             or if the word is unknown to the application
     */
    void validateWord(String word) throws ValidationException;
}
