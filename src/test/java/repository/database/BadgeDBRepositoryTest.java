package repository.database;

import controller.PopupMessage;
import domain.Badge;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.JdbcUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class BadgeDBRepositoryTest {
    private static JdbcUtils dbUtils;
    private static BadgeDBRepository badgeRepo;

    @BeforeAll
    public static void init() {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }
        dbUtils = new JdbcUtils(props.getProperty("jdbc.url.test"));
        badgeRepo = new BadgeDBRepository(props.getProperty("jdbc.url.test"));
    }

    @Test
    public void testFindBadgeSuccess() {
        assertDoesNotThrow(() -> badgeRepo.findById(1));
    }

    @Test
    public void testFindBadgeFailure() {
        assertNull(badgeRepo.findById(0));
    }

    @Test
    public void testGetAllBadges() {
        List<Badge> badges = (List<Badge>) badgeRepo.getAll();
        assertEquals(8, badges.size());
        assertEquals(1, badges.get(0).getId());
    }
}
