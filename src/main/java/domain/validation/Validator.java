package domain.validation;

public interface Validator<T> {
    /**
     * Validates an entity
     * @param t - the entity to be validated
     * @throws ValidationException if the entity is invalid
     */
    void validate(T t) throws ValidationException;
}
