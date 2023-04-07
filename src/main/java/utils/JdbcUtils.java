package utils;

import controller.PopupMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JdbcUtils {
    // private final Properties jdbcProps;
    private static final Logger logger = LogManager.getLogger();
    private final String url;
    private Connection connection = null;

    public JdbcUtils(String url) {
        this.url = url;
    }

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
