package domain;

import org.junit.jupiter.api.Test;
import utils.Constants;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AwardedBadgeTest {
    @Test
    public void testAwardedBadge() {
        LocalDateTime dateAwarded =  LocalDateTime.parse("2023-04-08 16:00", Constants.DATE_TIME_FORMATTER);
        AwardedBadge awardedBadge = new AwardedBadge(10,1, 2, dateAwarded);
        assertEquals(10, awardedBadge.getId());
        assertEquals(1, awardedBadge.getUserId());
        assertEquals(2 , awardedBadge.getBadgeId());
        assertEquals(dateAwarded, awardedBadge.getDateAwarded());

        LocalDateTime newDateAwarded =  LocalDateTime.parse("2023-03-21 17:30", Constants.DATE_TIME_FORMATTER);
        awardedBadge.setId(20);
        awardedBadge.setUserId(2);
        awardedBadge.setBadgeId(3);
        awardedBadge.setDateAwarded(newDateAwarded);
        assertEquals(20, awardedBadge.getId());
        assertEquals(2, awardedBadge.getUserId());
        assertEquals(3 , awardedBadge.getBadgeId());
        assertEquals(newDateAwarded, awardedBadge.getDateAwarded());
    }
}
