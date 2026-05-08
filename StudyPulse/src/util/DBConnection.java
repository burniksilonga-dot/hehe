package util;

import java.sql.*;

/**
 * LAYER: Utility
 * PURPOSE: Provides a single shared MySQL connection for the whole app.
 * HOW IT WORKS:
 *   - Uses the Singleton pattern — only ONE connection object exists at a time.
 *   - getConnection() checks if connection is null or closed, then creates a new one.
 *   - All DAO classes call DBConnection.getConnection() to get the same connection.
 */
public class DBConnection {

    // ── Change these to match your XAMPP setup ────────────────
    private static final String URL  = "jdbc:mysql://localhost:3306/pomodoro_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";   // XAMPP default = blank

    private static Connection conn;

    private DBConnection() {}   // prevent instantiation

    public static Connection getConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(URL, USER, PASS);
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Add mysql-connector-java.jar to classpath.", e);
        }
        return conn;
    }

    public static void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); }
        catch (SQLException ignored) {}
    }
}
