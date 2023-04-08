package service;

import domain.UserRank;
import domain.User;
import domain.validation.AbstractUserValidator;
import domain.validation.UserValidator;
import domain.validation.ValidationException;
import repository.RepositoryException;
import repository.UserRepository;
import utils.Constants;
import utils.RandomString;
import utils.observer.Observer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserService implements Service {
    private UserRepository userRepo;
    private final AbstractUserValidator userValidator;
    private List<Observer> observers;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
        this.userValidator = new UserValidator();
        this.observers = new ArrayList<>();
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyAllObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    /**
     * Adds a new user.
     * @param username - the username of the user
     * @param email - the email of the user
     * @param password - the password of the user
     * @throws ValidationException if the username is too short
     *                             if the email is invalid
     *                             if the password is too short
     * @throws RepositoryException if the saving operation fails
     * @throws ServiceException if the user already exists
     */
    public void addUser(String username, String email, String password) throws ValidationException, RepositoryException, ServiceException {
        // Avoid storing the password in the database
        // Instead, hash its value with a random string called salt and
        // store the resulted code, as well as the salt
        String salt = RandomString.getRandomString(Constants.SALT_SIZE);
        int passwordCode = Objects.hash(password + salt);
        User user = new User(username, email, passwordCode, salt, UserRank.Bronze, Constants.INITIAL_TOKEN_COUNT);
        userValidator.validate(user);
        userValidator.validatePassword(password);

        if (userRepo.findByUsername(username) != null) {
            throw new ServiceException("Username already in use!");
        }
        if (userRepo.findByEmail(email) != null) {
            throw new ServiceException("Email already in use!");
        }

        userRepo.add(user);
    }

    /**
     * Finds a user by id.
     * @param id - the id of the user that is being looked for
     * @return the user, if found
     *         null, otherwise
     */
    public User findUserById(int id) {
        return userRepo.findById(id);
    }

    /**
     * Finds a user by username.
     * @param username - the username of the user that is being looked for
     * @return the user, if found
     *         null, otherwise
     */
    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    /**
     * Updates a user.
     * @param id - the id of the user to be updated
     * @param newUsername - the new username
     * @param newEmail - the new email
     * @param newPasswordCode - the new password code
     * @param newSalt - the new salt
     * @param newRank - the new rank
     * @param newTokenCount - the new token count
     * @throws RepositoryException if the update operation fails
     */
    public void updateUser(int id, String newUsername, String newEmail, int newPasswordCode,
                           String newSalt, UserRank newRank, int newTokenCount) throws RepositoryException {
        userRepo.update(new User(newUsername, newEmail, newPasswordCode,
                newSalt, newRank, newTokenCount), id);
        notifyAllObservers();
    }

    /**
     * Checks if a password matches to the one of the user.
     * @param username - the username of the user whose password is being checked
     * @param password - the password to be checked
     * @return true, if the password matches the user's password
     *         false, otherwise
     */
    public boolean checkPassword(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            return false;
        }
        return Objects.hash(password + user.getSalt()) == user.getPasswordCode();
    }
}
