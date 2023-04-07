package domain.validation;

import domain.Quest;

public interface AbstractQuestValidator extends Validator<Quest> {
    void validateWord(String word) throws ValidationException;
}
