package domain.validation;

public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
