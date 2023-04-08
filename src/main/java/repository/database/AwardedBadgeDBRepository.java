package repository.database;

import controller.PopupMessage;
import domain.AwardedBadge;
import domain.Badge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.AwardedBadgeRepository;
import repository.RepositoryException;
import utils.Constants;
import utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AwardedBadgeDBRepository implements AwardedBadgeRepository {
    private static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;

    public AwardedBadgeDBRepository(String url) {
        logger.info("Initializing AwardedBadgeDBRepository with the following url: {}", url);
        this.dbUtils = new JdbcUtils(url);
    }

    private AwardedBadge extractAwardedBadge(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int userId = resultSet.getInt("user_id");
        int badgeId = resultSet.getInt("badge_id");
        LocalDateTime dateAwarded = LocalDateTime.parse(resultSet.getString("date_awarded"),
                Constants.DATE_TIME_FORMATTER);
        return new AwardedBadge(id, userId, badgeId, dateAwarded);
    }

    @Override
    public AwardedBadge findByUserAndBadge(int userId, int badgeId) {
        logger.traceEntry("Finding by user id and badge id: {} {}", userId, badgeId);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM AwardedBadges WHERE user_id=? AND badge_id=?")) {
            preStmt.setInt(1, userId);
            preStmt.setInt(2, badgeId);
            try(ResultSet result = preStmt.executeQuery()) {
                if (result.next()) {
                    AwardedBadge awardedBadge = extractAwardedBadge(result);
                    logger.traceExit("Found: {}", awardedBadge);
                    return awardedBadge;
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
    public Iterable<AwardedBadge> getAllForUser(int userId) {
        logger.traceEntry("Getting all for user: {}", userId);
        Connection conn = dbUtils.getConnection();
        List<AwardedBadge> awardedBadges = new ArrayList<>();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM AwardedBadges WHERE user_id=?")) {
            preStmt.setInt(1, userId);
            try(ResultSet result = preStmt.executeQuery()) {
                while (result.next()) {
                    awardedBadges.add(extractAwardedBadge(result));
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
        return awardedBadges;
    }

    @Override
    public void add(AwardedBadge awardedBadge) throws RepositoryException {
        logger.traceEntry("Adding awarded badge: {}", awardedBadge);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("INSERT INTO AwardedBadges (user_id, badge_id, date_awarded) VALUES (?, ?, ?)")) {
            preStmt.setInt(1, awardedBadge.getUserId());
            preStmt.setInt(2, awardedBadge.getBadgeId());
            preStmt.setString(3, awardedBadge.getDateAwarded().format(Constants.DATE_TIME_FORMATTER));
            int result = preStmt.executeUpdate();
            if (result == 0) {
                throw new RepositoryException("Add awarded badge failed!");
            }
            logger.traceExit("Added {} instances", result);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
    }

    @Override
    public void delete(AwardedBadge awardedBadge) {

    }

    @Override
    public void update(AwardedBadge awardedBadge, Integer integer) throws RepositoryException {

    }

    @Override
    public AwardedBadge findById(Integer integer) {
        return null;
    }

    @Override
    public Iterable<AwardedBadge> getAll() {
        return null;
    }
}
