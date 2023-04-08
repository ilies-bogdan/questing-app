package domain.validation;

import domain.User;
import utils.Constants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserValidator implements AbstractUserValidator {
    @Override
    public void validate(User user) throws ValidationException {
        String errorMessage = "";

        String username = user.getUsername();
        if (username == null || username.trim().length() < Constants.MINIMUM_USERNAME_LENGTH) {
            errorMessage += "Username must be at least " + Constants.MINIMUM_USERNAME_LENGTH + " characters long!\n";
        } else {
            for (int i = 0; i < username.length(); i++) {
                if (!Constants.ALPHA_NUMERIC_STRING.contains(String.valueOf(username.charAt(i)))) {
                    errorMessage += "Username must only contain alphanumeric characters!\n";
                    break;
                }
            }
        }

        String email = user.getEmail();
        String regex = "^.+@.+[.].+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if (!matcher.find()) {
            errorMessage += "Invalid email!\n";
        }

        if (String.valueOf(user.getPasswordCode()).trim().length() == 0) {
            errorMessage += "Invalid password code!\n";
        }

        if (user.getRank() == null) {
            errorMessage += "Invalid rank!\n";
        }

        if (user.getTokenCount() < 0) {
            errorMessage += "Invalid token count!\n";
        }

        if (errorMessage.length() > 0) {
            throw new ValidationException(errorMessage);
        }
    }

    public void validatePassword(String password) throws ValidationException {
        String message = "";
        if (password == null || password.trim().length() < Constants.MINIMUM_PASSWORD_LENGTH) {
            message += "Password must be at least " + Constants.MINIMUM_PASSWORD_LENGTH + " characters long!\n";
        }

        if (message.length() > 0) {
            throw new ValidationException(message);
        }
    }
}
