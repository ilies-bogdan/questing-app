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
}
