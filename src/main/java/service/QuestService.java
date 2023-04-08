package service;

import domain.LetterStatus;
import domain.Quest;
import domain.QuestStatus;
import domain.User;
import domain.validation.*;
import repository.QuestRepository;
import repository.RepositoryException;
import repository.UserRepository;
import utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class QuestService implements Service {
    private UserRepository userRepo;
    private QuestRepository questRepo;
    private final AbstractQuestValidator questValidator;
    private List<Observer> observers = new ArrayList<>();

    public QuestService(UserRepository userRepo, QuestRepository questRepo) {
        this.userRepo = userRepo;
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

    /**
     * Validates a word.
     * @param word - the word to be validated
     * @throws ValidationException if the word is of invalid size
     *                             or if the word is unknown to the application
     */
    public void validateWord(String word) throws ValidationException {
        questValidator.validateWord(word);
    }

    /**
     * Adds a new quest.
     * @param giverId - the id of the user who posted the quest.
     * @param reward - the reward of the quest
     * @param word - the word associated with the quest
     * @throws ValidationException if the reward is less than 1
     *                             if the word is invalid
     * @throws RepositoryException if the add operation fails
     * @throws ServiceException if the user has fewer tokens than the reward
     */
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

    /**
     * Updates a quest.
     * @param id - the id of the quest to be updated.
     * @param newGiverId - the id of the new quest giver
     * @param newPlayerId - the id of the new player who accepted the quest
     * @param newDateOfPosting - the new date of posting
     * @param newReward - the new reward
     * @param newStatus - the new quest status
     * @param newWord - the new word associated with the quest
     * @throws RepositoryException if the update operation fails
     */
    public void updateQuest(int id, int newGiverId, int newPlayerId, LocalDateTime newDateOfPosting,
                            int newReward, QuestStatus newStatus, String newWord) throws RepositoryException {
        questRepo.update(new Quest(newGiverId, newPlayerId, newDateOfPosting,
                newReward, newStatus, newWord), id);
        notifyAllObservers();
    }

    /**
     * Gets all the quests a user has posted.
     * @param user - the user whose quests are being looked for
     * @return iterable object containing quests
     */
    public Iterable<Quest> getPostedQuests(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getGiverId() == user.getId())
                .collect(Collectors.toList());
    }

    /**
     * Gets all the quests that are available to a user.
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
     * Gets all the quests that a user has accepted.
     * @param user - the user whose accepted quests are being looked for
     * @return iterable object containing quests
     */
    public Iterable<Quest> getQuestJournal(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getPlayerId() == user.getId())
                .collect(Collectors.toList());
    }

    /**
     * Gets the number of quests that a user has completed.
     * @param user - the user whose completed quests are being counted
     * @return the number of quests completed by the user
     */
    public int getCompletedQuestsCount(User user) {
        return StreamSupport.stream(questRepo.getAll().spliterator(), false)
                .filter(quest -> quest.getPlayerId() == user.getId() && quest.getStatus() == QuestStatus.completed)
                .toList().size();
    }

    /**
     * Checks if a letter appears in a word.
     * @param index - the index of the letter in the word
     * @param letter - the letter
     * @param word - the word
     * @return LetterStatus.correct if the letter is on the correct position in the word
     *         LetterStatus.appears if the letter appears in the word but is not in the correct position
     *         LetterStatus.incorrect if the letter does not appear in the word at all
     */
    public LetterStatus getLetterStatus(int index, char letter, String word) {
        if (word.charAt(index) == letter) {
            return LetterStatus.correct;
        }
        if (word.contains(String.valueOf(letter))) {
            return LetterStatus.appears;
        }
        return LetterStatus.incorrect;
    }
}
