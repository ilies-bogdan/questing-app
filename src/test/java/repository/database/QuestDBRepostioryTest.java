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
import utils.JdbcUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
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
        try(PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Users; DELETE FROM Quests")) {
            preStmt.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @AfterAll
    public static void clear() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Users; DELETE FROM Quests")) {
            preStmt.executeUpdate();
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
}
