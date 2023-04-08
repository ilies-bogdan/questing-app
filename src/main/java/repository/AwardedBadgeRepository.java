package repository;

import domain.AwardedBadge;

public interface AwardedBadgeRepository extends Repository<AwardedBadge, Integer> {
    /**
     * Finds a badge with a given id that was awarded to a user with a given id.
     * @param userId - the id of the user that was awarded
     * @param badgeId - the id of the badge that was awarded
     * @return the awarded badge
     */
    AwardedBadge findByUserAndBadge(int userId, int badgeId);

    /**
     * Gets all the badges that were awarded to a user.
     * @param userId - the id of the user
     * @return iterable object containing AwardedBadge objects which refer
     *         the user with the given id
     */
    Iterable<AwardedBadge> getAllForUser(int userId);
}
