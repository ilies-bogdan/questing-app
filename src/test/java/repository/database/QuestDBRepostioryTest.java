package repository.database;

import controller.PopupMessage;
import domain.Quest;
import domain.QuestStatus;
import domain.Rank;
import domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RepositoryException;
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

public class QuestDBRepostioryTest {
    private static JdbcUtils dbUtils;
    private static UserDBRepository userRepo;
    private static QuestDBRepository questRepo;

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
        questRepo = new QuestDBRepository(props.getProperty("jdbc.url.test"));
    }

    @BeforeEach
    public void clearTables() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users");
            PreparedStatement preStmtQuests = conn.prepareStatement("DELETE FROM Quests")) {
            preStmtUsers.executeUpdate();
            preStmtQuests.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @AfterAll
    public static void clear() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users");
            PreparedStatement preStmtQuests = conn.prepareStatement("DELETE FROM Quests")) {
            preStmtUsers.executeUpdate();
            preStmtQuests.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @Test
    public void testAddQuestSuccess() {
        User user = new User("username", "email", 1, "salt", Rank.Bronze, 100);
        assertDoesNotThrow(() -> userRepo.add(user));
        User found = userRepo.findByUsername("username");

        Quest quest = new Quest(found.getId(), found.getId(), LocalDateTime.MAX, 100, QuestStatus.posted, "words");
        assertDoesNotThrow(() -> questRepo.add(quest));
        for (Quest q : questRepo.getAll()) {
            assertEquals(found.getId(), quest.getGiverId());
            assertEquals(LocalDateTime.MAX, quest.getDateOfPosting());
            assertEquals(100, quest.getReward());
            assertEquals(QuestStatus.posted, quest.getStatus());
            assertEquals("words", quest.getWord());
        }
    }

    @Test
    public void testUpdateQuestSuccess() {
        assertDoesNotThrow(() -> userRepo.add(new User("username", "email", 1, "salt", Rank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        assertDoesNotThrow(() -> userRepo.add(new User("username1", "email1", 1, "salt", Rank.Bronze, 100)));
        User newUser = userRepo.findByUsername("username1");

        assertDoesNotThrow(() -> questRepo.add(new Quest(user.getId(), user.getId(),
                LocalDateTime.parse("2023-04-06 18:30", Constants.DATE_TIME_FORMATTER),
                100, QuestStatus.posted, "words")));
        assertDoesNotThrow(() -> questRepo.update(
                new Quest(user.getId(), newUser.getId(), LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                        10, QuestStatus.accepted, "spice"),
                ((List<Quest>) questRepo.getAll()).get(0).getId()
        ));

        Quest quest = ((List<Quest>) questRepo.getAll()).get(0);
        assertEquals(user.getId(), quest.getGiverId());
        assertEquals(newUser.getId(), quest.getPlayerId());
        assertEquals(LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER), quest.getDateOfPosting());
        assertEquals(10, quest.getReward());
        assertEquals(QuestStatus.accepted, quest.getStatus());
        assertEquals("spice", quest.getWord());
    }

    @Test
    public void testUpdateQuestFailure() {
        assertDoesNotThrow(() -> userRepo.add(new User("username", "email", 1, "salt", Rank.Bronze, 100)));
        User user = userRepo.findByUsername("username");
        assertDoesNotThrow(() -> questRepo.add(new Quest(user.getId(), user.getId(),
                LocalDateTime.parse("2023-04-06 18:30", Constants.DATE_TIME_FORMATTER),
                100, QuestStatus.posted, "words")));
        assertThrows(RepositoryException.class, () -> questRepo.update(
                new Quest(user.getId(), 0, LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                        10, QuestStatus.accepted, "spice"), 0));
    }
}
