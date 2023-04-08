package service;

import domain.AwardedBadge;
import domain.Badge;
import domain.BadgeType;
import domain.User;
import repository.AwardedBadgeRepository;
import repository.BadgeRepository;
import repository.RepositoryException;
import utils.observer.Observer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BadgeService implements Service {
    private BadgeRepository badgeRepo;
    private AwardedBadgeRepository awardedBadgeRepo;
    private List<Observer> observers = new ArrayList<>();

    public BadgeService(BadgeRepository badgeRepo, AwardedBadgeRepository awardedBadgeRepo) {
        this.awardedBadgeRepo = awardedBadgeRepo;
        this.badgeRepo = badgeRepo;
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
     * Gets all the existing badges.
     * @return iterable object containing all the badges
     */
    public Iterable<Badge> getAllBadges() {
        return badgeRepo.getAll();
    }

    /**
     * Gets all badges for a user.
     * @param user - the user whose badges are being looked for
     * @return iterable object containing badges
     */
    public Iterable<Badge> getAllBadgesForUser(User user) {
        List<Badge> badges = new ArrayList<>();
        awardedBadgeRepo.getAllForUser(user.getId()).forEach(awdBadge ->
                badges.add(badgeRepo.findById(awdBadge.getBadgeId())));
        return badges;
    }

    /**
     * Checks if a user has a badge.
     * @param user - the user being checked
     * @param badge - the badge being checked
     * @return true if the user has the badge
     *         false otherwise
     */
    public boolean userHasBadge(User user, Badge badge) {
        return awardedBadgeRepo.findByUserAndBadge(user.getId(), badge.getId()) != null;
    }

    /**
     * Adds a new badge to a user.
     * @param user - the user being awarded
     * @param badge - the badge that is awarded
     * @throws RepositoryException if the add operation fails
     */
    public void addBadgeToUser(User user, Badge badge) throws RepositoryException {
        awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge.getId(), LocalDateTime.now()));
        notifyAllObservers();
    }
}
