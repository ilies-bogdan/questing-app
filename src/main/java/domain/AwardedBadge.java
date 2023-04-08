package domain;

import java.time.LocalDateTime;

public class AwardedBadge implements Entity<Integer> {
    private int id;
    private int userId;
    private int badgeId;
    private LocalDateTime dateAwarded;

    public AwardedBadge() {
        this.userId = 0;
        this.badgeId = 0;
        this.dateAwarded = null;
    }

    public AwardedBadge(int userId, int badgeId, LocalDateTime dateAwarded) {
        this.userId = userId;
        this.badgeId = badgeId;
        this.dateAwarded = dateAwarded;
    }

    public AwardedBadge(int id, int userId, int badgeId, LocalDateTime dateAwarded) {
        this.id = id;
        this.userId = userId;
        this.badgeId = badgeId;
        this.dateAwarded = dateAwarded;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public LocalDateTime getDateAwarded() {
        return dateAwarded;
    }

    public void setDateAwarded(LocalDateTime dateAwarded) {
        this.dateAwarded = dateAwarded;
    }
}
