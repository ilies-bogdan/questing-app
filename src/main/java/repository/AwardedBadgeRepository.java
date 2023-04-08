package repository;

import domain.AwardedBadge;

public interface AwardedBadgeRepository extends Repository<AwardedBadge, Integer> {
    AwardedBadge findByUserAndBadge(int userId, int badgeId);
    Iterable<AwardedBadge> getAllForUser(int userId);
}
