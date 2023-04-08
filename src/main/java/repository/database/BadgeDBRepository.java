package repository.database;

import controller.PopupMessage;
import domain.Badge;
import domain.BadgeType;
import domain.Quest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.BadgeRepository;
import repository.RepositoryException;
import utils.JdbcUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BadgeDBRepository implements BadgeRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;

    public BadgeDBRepository(String url) {
        logger.info("Initializing BadgeDBRepository with the following url: {}", url);
        this.dbUtils = new JdbcUtils(url);
    }

    private Badge extractBadge(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        BadgeType type = BadgeType.valueOf(resultSet.getString("type"));
        int requirement = resultSet.getInt("requirement");
        return new Badge(id, title, description, type, requirement);
    }

    @Override
    public void add(Badge badge) throws RepositoryException {

    }

    @Override
    public void delete(Badge badge) {

    }

    @Override
    public void update(Badge badge, Integer integer) throws RepositoryException {

    }

    @Override
    public Badge findById(Integer id) {
        logger.traceEntry("Finding by id: {}", id);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Badges WHERE id=?")) {
            preStmt.setInt(1, id);
            try(ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    Badge badge = extractBadge(result);
                    logger.traceExit("Found: {}", badge);
                    return badge;
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
    public Iterable<Badge> getAll() {
        logger.traceEntry("Getting all");
        Connection conn = dbUtils.getConnection();
        List<Badge> badges = new ArrayList<>();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Badges")) {
            try(ResultSet result = preStmt.executeQuery()) {
                while (result.next()) {
                    badges.add(extractBadge(result));
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
        return badges;
    }
}
