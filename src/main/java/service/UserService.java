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

    public void addUser(String username, String email, String password) throws ValidationException, RepositoryException, ServiceException {
        // Avoid storing the password in the database
        // by hashing it with a random string and
        // storing the resulted code, as well as the random string
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

    public User findUserById(int id) {
        return userRepo.findById(id);
    }

    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void updateUser(int id, String newUsername, String newEmail, int newPasswordCode,
                           String newSalt, UserRank newRank, int newTokenCount) throws RepositoryException {
        userRepo.update(new User(newUsername, newEmail, newPasswordCode,
                newSalt, newRank, newTokenCount), id);
        notifyAllObservers();
    }

    public boolean checkPassword(String username, String password) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            return false;
        }
        return Objects.hash(password + user.getSalt()) == user.getPasswordCode();
    }
}
