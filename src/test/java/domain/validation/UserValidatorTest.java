package domain.validation;

import domain.UserRank;
import domain.User;
import org.junit.jupiter.api.Test;
import utils.Constants;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTest {
    private static final AbstractUserValidator userValidator = new UserValidator();

    @Test
    public void testUserValidationSuccess() {
        User user = new User(1, "username", "email@gmail.com", 1, "salt", UserRank.Bronze, 100);
        assertDoesNotThrow(() -> userValidator.validate(user));
    }

    @Test
    public void testUserValidationFailure() {
        User user = new User(0, "us", "email.com", 0, "salt", null, -1);
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userValidator.validate(user));
        assertEquals("Username must be at least " + Constants.MINIMUM_USERNAME_LENGTH + " characters long!\n" +
                "Invalid email!\nInvalid rank!\nInvalid token count!\n", thrown.getMessage());
    }

    @Test
    public void testPasswordValidationSuccess() {
        String password = "123456";
        assertDoesNotThrow(() -> userValidator.validatePassword(password));
    }

    @Test
    public void testPasswordValidationFailure() {
        String password = "12345";
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> userValidator.validatePassword(password));
        assertEquals("Password must be at least " + Constants.MINIMUM_PASSWORD_LENGTH + " characters long!\n",
                thrown.getMessage());
    }
}
