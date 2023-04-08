package repository.database;

import controller.PopupMessage;
import domain.Quest;
import domain.QuestStatus;
import domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.QuestRepository;
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

public class QuestDBRepository implements QuestRepository {
    public static final Logger logger = LogManager.getLogger();
    private final JdbcUtils dbUtils;

    public QuestDBRepository(String url) {
        logger.info("Initializing QuestDBRepository with the following url: {}", url);
        this.dbUtils = new JdbcUtils(url);
    }

    private Quest extractQuest(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int giverId = resultSet.getInt("giver_id");
        int playerId = resultSet.getInt("player_id");
        LocalDateTime dateOfPosting = LocalDateTime.parse(resultSet.getString("date_of_posting"), Constants.DATE_TIME_FORMATTER);
        int reward = resultSet.getInt("reward");
        QuestStatus status = QuestStatus.valueOf(resultSet.getString("status"));
        String word = resultSet.getString("word");
        return new Quest(id, giverId, playerId, dateOfPosting, reward, status, word);
    }

    @Override
    public void add(Quest quest) throws RepositoryException {
        logger.traceEntry("Adding quest: {}", quest);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("INSERT INTO Quests (giver_id, date_of_posting, reward, status, word) VALUES (?, ?, ?, ?, ?)")) {
            preStmt.setInt(1, quest.getGiverId());
            preStmt.setString(2, quest.getDateOfPosting().format(Constants.DATE_TIME_FORMATTER));
            preStmt.setInt(3, quest.getReward());
            preStmt.setString(4, quest.getStatus().toString());
            preStmt.setString(5, quest.getWord());
            int result = preStmt.executeUpdate();
            if (result == 0) {
                throw new RepositoryException("Add quest failed!");
            }
            logger.traceExit("Added {} instances", result);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
    }

    @Override
    public void delete(Quest quest) {

    }

    @Override
    public void update(Quest quest, Integer id) throws RepositoryException {
        logger.traceEntry("Updating quest: {}, {}", id, quest);
        Connection conn = dbUtils.getConnection();
        try(PreparedStatement preStmt = conn.prepareStatement("UPDATE Quests SET giver_id=?, player_id=?, date_of_posting=?, reward=?, status=?, word=? WHERE id=?")) {
            preStmt.setInt(1, quest.getGiverId());
            preStmt.setInt(2, quest.getPlayerId());
            preStmt.setString(3, quest.getDateOfPosting().format(Constants.DATE_TIME_FORMATTER));
            preStmt.setInt(4, quest.getReward());
            preStmt.setString(5, quest.getStatus().toString());
            preStmt.setString(6, quest.getWord());
            preStmt.setInt(7, id);
            int result = preStmt.executeUpdate();
            if (result == 0) {
                throw new RepositoryException("Update quest failed!");
            }
            logger.traceExit("Updated {} instances", result);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
    }

    @Override
    public Quest findById(Integer id) {
        return null;
    }

    @Override
    public Iterable<Quest> getAll() {
        logger.traceEntry("Getting all");
        Connection conn = dbUtils.getConnection();
        List<Quest> quests = new ArrayList<>();
        try(PreparedStatement preStmt = conn.prepareStatement("SELECT * FROM Quests")) {
            try(ResultSet result = preStmt.executeQuery()) {
                while (result.next()) {
                    quests.add(extractQuest(result));
                }
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("DB error " + e);
        }
        logger.traceExit();
        return quests;
    }
}
