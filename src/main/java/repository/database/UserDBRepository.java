package repository.database;

import controller.PopupMessage;
import domain.Rank;
import domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.RepositoryException;
import repository.UserRepository;
import utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDBRepository implements UserRepository {
    public static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;

    public UserDBRepository(String url) {
        logger.info("Initializing UserDBRepository with the following url: {}", url);
        this.dbUtils = new JdbcUtils(url);
    }

    private User extractUser(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String username = resultSet.getString("username");
        String email = resultSet.getString("email");
        int passwordCode = resultSet.getInt("password_code");
        String salt = resultSet.getString("salt");
        Rank rank = Rank.valueOf(resultSet.getString("rank"));
        int tokenCount = resultSet.getInt("token_count");
        return new User(id, username, email, passwordCode, salt, rank, tokenCount);
    }

    @Override
    public void add(User user) throws RepositoryException {
        logger.traceEntry("Adding user: {}", user);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("INSERT INTO Users (username, email, password_code, salt, rank, token_count) VALUES (?, ?, ?, ?, ?, ?)")) {
            preStmt.setString(1, user.getUsername());
            preStmt.setString(2, user.getEmail());
            preStmt.setInt(3, user.getPasswordCode());
            preStmt.setString(4, user.getSalt());
            preStmt.setString(5, user.getRank().toString());
            preStmt.setInt(6, user.getTokenCount());
            int result = preStmt.executeUpdate();
            if (result == 0) {
                throw new RepositoryException("Add client failed!");
            }
            logger.traceExit("Added {} instances", result);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
    }

    @Override
    public void delete(User user) {

    }

    @Override
    public void update(User user, Integer id) throws RepositoryException {
        logger.traceEntry("Updating user: {}, {}", id, user);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("UPDATE Users SET username=?, email=?, password_code=?, salt=?, rank=?, token_count=? WHERE id=?")) {
            preStmt.setString(1, user.getUsername());
            preStmt.setString(2, user.getEmail());
            preStmt.setInt(3, user.getPasswordCode());
            preStmt.setString(4, user.getSalt());
            preStmt.setString(5, user.getRank().toString());
            preStmt.setInt(6, user.getTokenCount());
            preStmt.setInt(7, id);
            int result = preStmt.executeUpdate();
            if (result == 0) {
                throw new RepositoryException("Update user failed!");
            }
            logger.traceExit("Updated {} instances", result);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
    }

    @Override
    public User findById(Integer id) {
        logger.traceEntry("Finding by ID: {}", id);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Users WHERE id=?")) {
            preStmt.setInt(1, id);
            try(ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    User user = extractUser(result);
                    logger.traceExit("Found: {}", user);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit("Found: {}", null);
        return null;
    }

    @Override
    public Iterable<User> getAll() {
        logger.traceEntry("Getting all");
        Connection conn = dbUtils.getConnection();
        List<User> users = new ArrayList<>();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Users")) {
            try(ResultSet result = preStmt.executeQuery()) {
                while (result.next()) {
                    users.add(extractUser(result));
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
        return users;
    }

    @Override
    public User findByUsername(String username) {
        logger.traceEntry("Finding by username: {}", username);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Users WHERE username=?")) {
            preStmt.setString(1, username);
            try(ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    User user = extractUser(result);
                    logger.traceExit("Found: {}", user);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit("Found: {}", null);
        return null;
    }

    @Override
    public User findByEmail(String email) {
        logger.traceEntry("Finding by email: {}", email);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Users WHERE email=?")) {
            preStmt.setString(1, email);
            try(ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    User user = extractUser(result);
                    logger.traceExit("Found: {}", user);
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit("Found: {}", null);
        return null;
    }
}
