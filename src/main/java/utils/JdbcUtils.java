package utils;

import controller.PopupMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JdbcUtils {
    private static final Logger logger = LogManager.getLogger();
    private final String url;
    private Connection connection = null;

    public JdbcUtils(String url) {
        this.url = url;
    }

    /**
     * Connects to the database.
     * @return the new connection to the database
     */
    private Connection getNewConnection() {
        logger.traceEntry();
        Connection conn = null;
        try {
            logger.info("Connecting to database: {}", url);
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("Databse connection error: " + e.getMessage());
        }
        logger.traceExit();
        return conn;
    }

    /**
     * Gets the connection to the database or creates it first if it doesn't exist.
     * @return the connection to the database
     */
    public Connection getConnection() {
        logger.traceEntry();
        try {
            if (connection == null || connection.isClosed()) {
                connection = getNewConnection();
            }
        } catch (SQLException e) {
            logger.error(e);
            PopupMessage.showErrorMessage("Databse connection error: " + e.getMessage());
        }
        logger.traceExit();
        return connection;
    }
}
