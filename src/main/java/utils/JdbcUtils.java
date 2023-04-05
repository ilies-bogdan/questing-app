package utils;

import controller.PopupMessage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcUtils {
    // private final Properties jdbcProps;
    private final String url;
    private Connection connection = null;

    public JdbcUtils(String url) {
        this.url = url;
    }

    private Connection getNewConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("Databse connection error: " + e.getMessage());
        }

        return conn;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = getNewConnection();
            }
        } catch (SQLException e) {
            PopupMessage.showErrorMessage("Databse connection error: " + e.getMessage());
        }
        return connection;
    }
}
