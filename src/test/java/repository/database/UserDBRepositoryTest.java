package repository.database;

import controller.PopupMessage;
import domain.Rank;
import domain.User;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import repository.RepositoryException;
import utils.JdbcUtils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class UserDBRepositoryTest {
    private static JdbcUtils dbUtils;
    private static UserDBRepository userRepo;

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
    }

    @BeforeEach
    public void clearUsersTable() {
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
    public void testAddUser() {
        User user = new User("username", "email", 1, "salt", Rank.Bronze, 100);
        assertDoesNotThrow(() -> userRepo.add(user));
        for (User u : userRepo.getAll()) {
            assertEquals(user.getUsername(), u.getUsername());
            assertEquals(user.getEmail(), u.getEmail());
            assertEquals(user.getPasswordCode(), u.getPasswordCode());
            assertEquals(user.getSalt(), u.getSalt());
            assertEquals(user.getRank(), u.getRank());
            assertEquals(user.getTokenCount(), u.getTokenCount());
        }
    }

    @Test
    public void testFindUserByUsernameSuccess() {
        User user = new User("username", "email", 1, "salt", Rank.Bronze, 100);
        assertDoesNotThrow(() -> userRepo.add(user));
        assertDoesNotThrow(() -> userRepo.findByUsername("username"));
    }

    @Test
    public void testFindUserByUsernameFailure() {
        assertNull(userRepo.findByUsername("username"));
    }

    @Test
    public void testUpdateUserSuccess() {
        User user = new User("username", "email", 1, "salt", Rank.Bronze, 100);
        assertDoesNotThrow(() -> userRepo.add(user));
        assertDoesNotThrow(() -> userRepo.update(
                new User("username1", "email1", 2, "salt1", Rank.Silver, 1000),
                userRepo.findByUsername("username").getId()));
        assertEquals("username1", userRepo.findByUsername("username1").getUsername());
        assertEquals("email1", userRepo.findByUsername("username1").getEmail());
        assertEquals(2, userRepo.findByUsername("username1").getPasswordCode());
        assertEquals("salt1", userRepo.findByUsername("username1").getSalt());
        assertEquals(Rank.Silver, userRepo.findByUsername("username1").getRank());
        assertEquals(1000, userRepo.findByUsername("username1").getTokenCount());
    }

    @Test
    public void testUpdateUserFailure() {
        assertThrows(RepositoryException.class, () -> userRepo.update(
                new User("username1", "email1", 2, "salt1", Rank.Silver, 1000),
                1));
    }
}
