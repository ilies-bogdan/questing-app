package repository.database;

import controller.PopupMessage;
import domain.AwardedBadge;
import domain.Badge;
import domain.User;
import domain.UserRank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.Constants;
import utils.JdbcUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class AwardedBadgeDBRepositoryTest {
    private static JdbcUtils dbUtils;
    private static UserDBRepository userRepo;
    private static BadgeDBRepository badgeRepo;
    private static AwardedBadgeDBRepository awardedBadgeRepo;

    @BeforeAll
    public static void init() {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }
        dbUtils = new JdbcUtils(props.getProperty("jdbc.url.test"));
        userRepo = new UserDBRepository(props.getProperty("jdbc.url.test"));
        badgeRepo = new BadgeDBRepository(props.getProperty("jdbc.url.test"));
        awardedBadgeRepo = new AwardedBadgeDBRepository(props.getProperty("jdbc.url.test"));
    }

    @BeforeEach
    public void clearTables() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users")) {
            preStmtUsers.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @Test
    public void testAddAwardedBadge() {
        assertDoesNotThrow(() -> userRepo.add(new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        Badge badge = badgeRepo.findById(1);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge.getId(), LocalDateTime.now())));
    }

    @Test
    public void testFindByUserAndBadgeSuccess() {
        assertDoesNotThrow(() -> userRepo.add(new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        Badge badge = badgeRepo.findById(1);
        LocalDateTime dateAwarded =  LocalDateTime.parse("2023-04-08 16:00", Constants.DATE_TIME_FORMATTER);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge.getId(), dateAwarded)));

        AwardedBadge awardedBadge = awardedBadgeRepo.findByUserAndBadge(user.getId(), badge.getId());
        assertEquals(user.getId(), awardedBadge.getUserId());
        assertEquals(badge.getId(), awardedBadge.getBadgeId());
        assertEquals(dateAwarded, awardedBadge.getDateAwarded());
    }

    @Test
    public void testFindByUserAndBadgeFailure() {
        assertNull(awardedBadgeRepo.findByUserAndBadge(1, 2));
    }

    @Test
    public void testGetAllForUser() {
        assertDoesNotThrow(() -> userRepo.add(new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        List<AwardedBadge> badges = (List<AwardedBadge>) awardedBadgeRepo.getAllForUser(user.getId());
        assertEquals(0, badges.size());
        Badge badge1 = badgeRepo.findById(1);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge1.getId(), LocalDateTime.now())));
        Badge badge2 = badgeRepo.findById(2);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge2.getId(), LocalDateTime.now())));
        badges = (List<AwardedBadge>) awardedBadgeRepo.getAllForUser(user.getId());
        assertEquals(2, badges.size());
    }
}
