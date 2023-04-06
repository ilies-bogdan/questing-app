package service;

import domain.Quest;
import domain.QuestStatus;
import domain.Rank;
import domain.User;
import domain.validation.*;
import repository.QuestRepository;
import repository.RepositoryException;
import repository.UserRepository;
import utils.Constants;
import utils.RandomString;
import utils.observer.Observable;
import utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuestingService implements Observable {
    private UserRepository userRepo;
    private final AbstractUserValidator userValidator;
    private QuestRepository questRepo;
    private final AbstractQuestValidator questValidator;
    private List<Observer> observers = new ArrayList<>();

    public QuestingService(UserRepository userRepo, QuestRepository questRepo) {
        this.userRepo = userRepo;
        this.userValidator = new UserValidator();
        this.questRepo = questRepo;
        this.questValidator = new QuestValidator();
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
        User user = new User(username, email, passwordCode, salt, Rank.Bronze, Constants.INITIAL_TOKEN_COUNT);
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
                           String newSalt, Rank newRank, int newTokenCount) throws RepositoryException {
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

    public void addQuest(int giverId, int reward, String word) throws ValidationException, RepositoryException, ServiceException {
        Quest quest = new Quest(giverId, 0, LocalDateTime.now(), reward, QuestStatus.posted, word.toLowerCase());
        User user = userRepo.findById(giverId);
        if (reward > user.getTokenCount()) {
            throw new ServiceException("Insufficient tokens!\n");
        }
        questValidator.validate(quest);
        questRepo.add(quest);
        notifyAllObservers();
    }

    public Quest findQuestById(int id) {
        return questRepo.findById(id);
    }

    public void updateQuest(int id, int newGiverId, int newPlayerId, LocalDateTime newDateOfPosting,
                            int newReward, QuestStatus newStatus, String newWord) throws RepositoryException {
        questRepo.update(new Quest(newGiverId, newPlayerId, newDateOfPosting,
                newReward, newStatus, newWord), id);
        notifyAllObservers();
    }

    /**
     * Finds all the quests a user has posted
     * @param user - the user whose quests are being looked for
     * @return iterable object containing quests
     */
    public Iterable<Quest> getPostedQuests(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getGiverId() == user.getId())
                .collect(Collectors.toList());
    }

    /**
     * Finds all the quests that are available to a user
     * @param user - the user whose available quests are being looked for
     * @return iterable object containing quests
     */
    public Iterable<Quest> getAvailableQuests(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getGiverId() != user.getId() &&
                        quest.getPlayerId() != user.getId() &&
                        quest.getStatus().equals(QuestStatus.posted))
                .collect(Collectors.toList());
    }

    /**
     * Finds all the quests that a user has accepted
     * @param user - the user whose accepted quests are being looked for
     * @return iterable object containing quests
     */
    public Iterable<Quest> getQuestJournal(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getPlayerId() == user.getId())
                .collect(Collectors.toList());
    }
}
