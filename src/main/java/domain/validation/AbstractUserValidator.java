package domain.validation;

import domain.User;

public interface AbstractUserValidator extends Validator<User> {
    void validatePassword(String password) throws ValidationException;
}
