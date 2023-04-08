package domain.validation;

import domain.User;

public interface AbstractUserValidator extends Validator<User> {
    /**
     * Validates a password.
     * @param password - the password to be validated
     * @throws ValidationException if the password is too short
     */
    void validatePassword(String password) throws ValidationException;
}
