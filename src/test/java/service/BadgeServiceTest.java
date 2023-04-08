package service;

import controller.PopupMessage;
import domain.AwardedBadge;
import domain.Badge;
import domain.User;
import domain.UserRank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.database.AwardedBadgeDBRepository;
import repository.database.BadgeDBRepository;
import repository.database.UserDBRepository;
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

public class BadgeServiceTest {
    private static JdbcUtils dbUtils;
    private static UserDBRepository userRepo;
    private static BadgeDBRepository badgeRepo;
    private static AwardedBadgeDBRepository awardedBadgeRepo;
    private static BadgeService badgeSrv;

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
        badgeSrv = new BadgeService(badgeRepo, awardedBadgeRepo);
    }

    @BeforeEach
    public void clearTables() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users");
            PreparedStatement preStmtAwardedBadges= conn.prepareStatement("DELETE FROM AwardedBadges")) {
            preStmtUsers.executeUpdate();
            preStmtAwardedBadges.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @Test
    public void testGetAllBadgesForUser() {
        assertDoesNotThrow(() -> userRepo.add( new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        Badge badge1 = badgeRepo.findById(1);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge1.getId(), LocalDateTime.now())));
        Badge badge2 = badgeRepo.findById(2);
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge2.getId(), LocalDateTime.now())));
        List<Badge> badges = (List<Badge>) badgeSrv.getAllBadgesForUser(user);
        assertEquals(2, badges.size());
    }

    @Test
    public void testUserHasBadge() {
        assertDoesNotThrow(() -> userRepo.add( new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        Badge badge = badgeRepo.findById(1);
        assertFalse(badgeSrv.userHasBadge(user, badge));
        assertDoesNotThrow(() -> awardedBadgeRepo.add(new AwardedBadge(user.getId(), badge.getId(), LocalDateTime.now())));
        assertTrue(badgeSrv.userHasBadge(user, badge));
    }

    @Test
    public void testAddBadgeToUser() {
        assertDoesNotThrow(() -> userRepo.add( new User("username", "email", 1, "salt", UserRank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        Badge badge = badgeRepo.findById(1);
        assertDoesNotThrow(() -> badgeSrv.addBadgeToUser(user, badge));
    }
}
