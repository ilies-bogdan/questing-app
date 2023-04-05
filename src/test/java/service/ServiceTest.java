package service;

import controller.PopupMessage;
import domain.Rank;
import domain.User;
import domain.validation.ValidationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

public class ServiceTest {
    private static JdbcUtils dbUtils;
    private static UserDBRepository userRepo;
    private static Service service;

    @BeforeAll
    public static void init() {
        Properties props = new Properties();
        try {
            props.load(new FileReader("db.config"));
        } catch (IOException e) {
            PopupMessage.showErrorMessage("Can not find database config file: " + e.getMessage());
        }
        dbUtils = new JdbcUtils(props.getProperty("jdbc.url.test"));
        service = new Service(new UserDBRepository(props.getProperty("jdbc.url.test")));
    }

    @BeforeEach
    public void clearTables() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Users")) {
            preStmt.executeUpdate();
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("DB error " + e);
        }
    }

    @AfterAll
    public static void clear() {
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("DELETE FROM Users")) {
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
}
