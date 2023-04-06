package service;

import controller.PopupMessage;
import domain.Quest;
import domain.QuestStatus;
import domain.Rank;
import domain.User;
import domain.validation.ValidationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RepositoryException;
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
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class QuestingServiceTest {
    private static JdbcUtils dbUtils;
    private static QuestingService service;

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
        service = new QuestingService(new UserDBRepository(url),
                new QuestDBRepository(url));
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
    public void testAddUserSuccess() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertEquals("username", user.getUsername());
        assertEquals("email@gmail.com", user.getEmail());
        assertEquals(user.getPasswordCode(), Objects.hash("123456" + user.getSalt()));
    }

    @Test
    public void testAddUserValidationFailure() {
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> service.addUser("u", "e", "1"));
        assertEquals("Username must be at least " + Constants.MINIMUM_USERNAME_LENGTH + " characters long!\n" +
                "Invalid email!\n", validationException.getMessage());
        validationException = assertThrows(ValidationException.class,
                () -> service.addUser("username", "email@gmail.com", "1"));
        assertEquals("Password must be at least " + Constants.MINIMUM_PASSWORD_LENGTH + " characters long!\n",
                validationException.getMessage());
    }

    @Test
    public void testAddUserServiceFailure() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> service.addUser("username", "email1@gmail.com", "123456"));
        assertEquals("Username already in use!", serviceException.getMessage());
        serviceException = assertThrows(ServiceException.class,
                () -> service.addUser("username1", "email@gmail.com", "123456"));
        assertEquals("Email already in use!", serviceException.getMessage());
    }

    @Test
    public void testFindUserByUsernameSuccess() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        assertDoesNotThrow(() -> service.findUserByUsername("username"));
    }

    @Test
    public void testFindUserByUsernameFailure() {
        assertDoesNotThrow(() -> service.addUser("username1", "email@gmail.com", "123456"));
        assertNull(service.findUserByUsername("username"));
    }

    @Test
    public void testUpdateUserSuccess() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        assertDoesNotThrow(() -> service.updateUser(service.findUserByUsername("username").getId(),
                "username1", "email1", 2, "salt1", Rank.Silver, 1000));
        assertEquals("username1", service.findUserByUsername("username1").getUsername());
        assertEquals("email1", service.findUserByUsername("username1").getEmail());
        assertEquals(2, service.findUserByUsername("username1").getPasswordCode());
        assertEquals("salt1", service.findUserByUsername("username1").getSalt());
        assertEquals(Rank.Silver, service.findUserByUsername("username1").getRank());
        assertEquals(1000, service.findUserByUsername("username1").getTokenCount());
    }

    @Test
    public void testUpdateUserFailure() {
        assertThrows(RepositoryException.class, () -> service.updateUser(1, "username1", "email1",
                2, "salt1", Rank.Silver, 1000));
    }

    @Test
    public void testAddQuestSuccess() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User found = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addQuest(found.getId(), 100, "words"));
    }

    @Test
    public void testAddQuestFailure() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User found = service.findUserByUsername("username");
        ValidationException thrown = assertThrows(ValidationException.class,
                () -> service.addQuest(found.getId(), 0, "words"));
        assertEquals("Reward must be greater than 0!\n", thrown.getMessage());

        thrown = assertThrows(ValidationException.class,
                () -> service.addQuest(found.getId(), 100, "qwert"));
        assertEquals("Unknown word!\n", thrown.getMessage());
    }

    @Test
    public void testUpdateQuestSuccess() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addUser("username1", "email1@gmail.com", "123456"));
        User newUser = service.findUserByUsername("username1");

        assertDoesNotThrow(() -> service.addQuest(user.getId(), 100, "words"));
        assertDoesNotThrow(() -> service.updateQuest(((List<Quest>) service.getPostedQuests(user)).get(0).getId(),
                user.getId(), newUser.getId(), LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                        10, QuestStatus.accepted, "spice"));

        Quest quest = ((List<Quest>) service.getPostedQuests(user)).get(0);
        assertEquals(user.getId(), quest.getGiverId());
        assertEquals(newUser.getId(), quest.getPlayerId());
        assertEquals(LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER), quest.getDateOfPosting());
        assertEquals(10, quest.getReward());
        assertEquals(QuestStatus.accepted, quest.getStatus());
        assertEquals("spice", quest.getWord());
    }

    @Test
    public void testUpdateQuestFailure() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addQuest(user.getId(), 100, "words"));
        assertThrows(RepositoryException.class, () -> service.updateQuest(0, user.getId(), 0,
                LocalDateTime.parse("2023-06-04 12:10", Constants.DATE_TIME_FORMATTER),
                10, QuestStatus.accepted, "spice"));
    }

    @Test
    public void testGetPostedQuests() {
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addQuest(user.getId(), 100, "words"));
        List<Quest> quests = (List<Quest>) service.getPostedQuests(user);
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
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addQuest(user.getId(), 100, "words"));

        assertDoesNotThrow(() -> service.addUser("username1", "email1@gmail.com", "123456"));
        User other = service.findUserByUsername("username1");
        List<Quest> quests = (List<Quest>) service.getAvailableQuests(other);
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
        assertDoesNotThrow(() -> service.addUser("username", "email@gmail.com", "123456"));
        User user = service.findUserByUsername("username");
        assertDoesNotThrow(() -> service.addQuest(user.getId(), 100, "words"));
        assertDoesNotThrow(() -> service.addUser("username1", "email1@gmail.com", "123456"));
        User other = service.findUserByUsername("username1");

        Quest quest = ((List<Quest>) service.getPostedQuests(user)).get(0);
        assertDoesNotThrow(() -> service.updateQuest(quest.getId(), user.getId(), other.getId(),
                quest.getDateOfPosting(),quest.getReward(), quest.getStatus(), quest.getWord()));

        List<Quest> quests = (List<Quest>) service.getQuestJournal(other);
        assertEquals(1, quests.size());
        for (Quest q : quests) {
            assertEquals(user.getId(), q.getGiverId());
            assertEquals(other.getId(), q.getPlayerId());
            assertEquals(((List<Quest>) service.getPostedQuests(user)).get(0).getDateOfPosting(), q.getDateOfPosting());
            assertEquals(((List<Quest>) service.getPostedQuests(user)).get(0).getReward(), q.getReward());
            assertEquals(((List<Quest>) service.getPostedQuests(user)).get(0).getStatus(), q.getStatus());
            assertEquals(((List<Quest>) service.getPostedQuests(user)).get(0).getWord(), q.getWord());
        }
    }
}
