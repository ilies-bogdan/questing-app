package service;

import controller.PopupMessage;
import domain.UserRank;
import domain.User;
import domain.validation.ValidationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RepositoryException;
import repository.database.UserDBRepository;
import utils.Constants;
import utils.JdbcUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private static JdbcUtils dbUtils;
    private static UserService userSrv;

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
        userSrv = new UserService(new UserDBRepository(url));
    }

    @BeforeEach
    public void setUpTest() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmtUsers = conn.prepareStatement("DELETE FROM Users")) {
            preStmtUsers.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @Test
    public void testAddUserSuccess() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        User user = userSrv.findUserByUsername("username");
        assertEquals("username", user.getUsername());
        assertEquals("email@gmail.com", user.getEmail());
        assertEquals(user.getPasswordCode(), Objects.hash("123456" + user.getSalt()));
    }

    @Test
    public void testAddUserValidationFailure() {
        ValidationException validationException = assertThrows(ValidationException.class,
                () -> userSrv.addUser("u", "e", "1"));
        assertEquals("Username must be at least " + Constants.MINIMUM_USERNAME_LENGTH + " characters long!\n" +
                "Invalid email!\n", validationException.getMessage());
        validationException = assertThrows(ValidationException.class,
                () -> userSrv.addUser("username", "email@gmail.com", "1"));
        assertEquals("Password must be at least " + Constants.MINIMUM_PASSWORD_LENGTH + " characters long!\n",
                validationException.getMessage());
    }

    @Test
    public void testAddUserServiceFailure() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> userSrv.addUser("username", "email1@gmail.com", "123456"));
        assertEquals("Username already in use!", serviceException.getMessage());
        serviceException = assertThrows(ServiceException.class,
                () -> userSrv.addUser("username1", "email@gmail.com", "123456"));
        assertEquals("Email already in use!", serviceException.getMessage());
    }

    @Test
    public void testFindUserByUsernameSuccess() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        assertDoesNotThrow(() -> userSrv.findUserByUsername("username"));
    }

    @Test
    public void testFindUserByUsernameFailure() {
        assertDoesNotThrow(() -> userSrv.addUser("username1", "email@gmail.com", "123456"));
        assertNull(userSrv.findUserByUsername("username"));
    }

    @Test
    public void testUpdateUserSuccess() {
        assertDoesNotThrow(() -> userSrv.addUser("username", "email@gmail.com", "123456"));
        assertDoesNotThrow(() -> userSrv.updateUser(userSrv.findUserByUsername("username").getId(),
                "username1", "email1", 2, "salt1", UserRank.Silver, 1000));
        assertEquals("username1", userSrv.findUserByUsername("username1").getUsername());
        assertEquals("email1", userSrv.findUserByUsername("username1").getEmail());
        assertEquals(2, userSrv.findUserByUsername("username1").getPasswordCode());
        assertEquals("salt1", userSrv.findUserByUsername("username1").getSalt());
        assertEquals(UserRank.Silver, userSrv.findUserByUsername("username1").getRank());
        assertEquals(1000, userSrv.findUserByUsername("username1").getTokenCount());
    }

    @Test
    public void testUpdateUserFailure() {
        assertThrows(RepositoryException.class, () -> userSrv.updateUser(1, "username1", "email1",
                2, "salt1", UserRank.Silver, 1000));
    }

}
