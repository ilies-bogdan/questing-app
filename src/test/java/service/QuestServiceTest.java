package service;

import controller.PopupMessage;
import domain.Quest;
import domain.QuestStatus;
import domain.User;
import domain.validation.ValidationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RepositoryException;
import repository.UserRepository;
import repository.database.QuestDBRepository;
import repository.database.UserDBRepository;
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

public class QuestServiceTest {
    private static JdbcUtils dbUtils;
    private static UserService userSrv;
    private static QuestService questSrv;

    @BeforeAll
    public static void init() {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }
        String url = props.getProperty("jdbc.url.test");
        dbUtils = new JdbcUtils(url);
        UserRepository userRepo = new UserDBRepository(url);
        userSrv = new UserService(userRepo);
        questSrv = new QuestService(userRepo, new QuestDBRepository(url));
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

//    @AfterAll
//    public static void clear() {
//        Connection conn = dbUtils.getConnection();
//        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users");
//            PreparedStatement preStmtQuests = conn.prepareStatement("DELETE FROM Quests")) {
//            preStmtUsers.executeUpdate();
//            preStmtQuests.executeUpdate();
//        } catch (SQLException e) {
//            PopupMessage.showErrorMessage("DB error " + e);
//        }
//    }

    @Test
    public void testAddQuestSuccess() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User found = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> questSrv.addQuest(found.getId(), 100, "words"));
    }

    @Test
    public void testAddQuestFailure() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User found = userSrv.findUserByUsername("username");
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> questSrv.addQuest(found.getId(), 0, "words"));
        assertEquals("Reward must be greater than 0!\n", thrown.getMessage());

        thrown = assertThrows(ValidationException.class,
                () -> questSrv.addQuest(found.getId(), 100, "qwert"));
        assertEquals("Unknown word!\n", thrown.getMessage());
    }

    @Test
    public void testUpdateQuestSuccess() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> userSrv.addUser("username1", "email1@gmail.com", "123456"));
        User newUser = userSrv.findUserByUsername("username1");

        assertDoesNotThrow(() -> questSrv.addQuest(user.getId(), 100, "words"));
        assertDoesNotThrow(() -> questSrv.updateQuest(((List<Quest>) questSrv.getPostedQuests(user)).get(0).getId(),
                user.getId(), newUser.getId(), LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                        10, QuestStatus.accepted, "spice"));

        Quest quest = ((List<Quest>) questSrv.getPostedQuests(user)).get(0);
        assertEquals(user.getId(), quest.getGiverId());
        assertEquals(newUser.getId(), quest.getPlayerId());
        assertEquals(LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER), quest.getDateOfPosting());
        assertEquals(10, quest.getReward());
        assertEquals(QuestStatus.accepted, quest.getStatus());
        assertEquals("spice", quest.getWord());
    }

    @Test
    public void testUpdateQuestFailure() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> questSrv.addQuest(user.getId(), 100, "words"));
        assertThrows(RepositoryException.class, () -> questSrv.updateQuest(0, user.getId(), 0,
                LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                10, QuestStatus.accepted, "spice"));
    }

    @Test
    public void testGetPostedQuests() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> questSrv.addQuest(user.getId(), 100, "words"));
        List<Quest> quests = (List<Quest>) questSrv.getPostedQuests(user);
        assertEquals(1, quests.size());
        for (Quest quest : quests) {
            assertEquals(user.getId(), quest.getGiverId());
            assertEquals(100, quest.getReward());
            assertEquals(QuestStatus.posted, quest.getStatus());
            assertEquals("words", quest.getWord());
        }
    }

    @Test
    public void testGetAvailableQuests() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> questSrv.addQuest(user.getId(), 100, "words"));

        assertDoesNotThrow(() -> userSrv.addUser("username1", "email1@gmail.com", "123456"));
        User other = userSrv.findUserByUsername("username1");
        List<Quest> quests = (List<Quest>) questSrv.getAvailableQuests(other);
        assertEquals(1, quests.size());
        for (Quest quest : quests) {
            assertEquals(user.getId(), quest.getGiverId());
            assertEquals(100, quest.getReward());
            assertEquals(QuestStatus.posted, quest.getStatus());
            assertEquals("words", quest.getWord());
        }
    }

    @Test
    public void testGetQuestJournal() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertDoesNotThrow(() -> questSrv.addQuest(user.getId(), 100, "words"));
        assertDoesNotThrow(() -> userSrv.addUser("username1", "email1@gmail.com", "123456"));
        User other = userSrv.findUserByUsername("username1");

        Quest quest = ((List<Quest>) questSrv.getPostedQuests(user)).get(0);
        assertDoesNotThrow(() -> questSrv.updateQuest(quest.getId(), user.getId(), other.getId(),
                quest.getDateOfPosting(),quest.getReward(), quest.getStatus(), quest.getWord()));

        List<Quest> quests = (List<Quest>) questSrv.getQuestJournal(other);
        assertEquals(1, quests.size());
        for (Quest q : quests) {
            assertEquals(user.getId(), q.getGiverId());
            assertEquals(other.getId(), q.getPlayerId());
            assertEquals(((List<Quest>) questSrv.getPostedQuests(user)).get(0).getDateOfPosting(), q.getDateOfPosting());
            assertEquals(((List<Quest>) questSrv.getPostedQuests(user)).get(0).getReward(), q.getReward());
            assertEquals(((List<Quest>) questSrv.getPostedQuests(user)).get(0).getStatus(), q.getStatus());
            assertEquals(((List<Quest>) questSrv.getPostedQuests(user)).get(0).getWord(), q.getWord());
        }
    }
}
