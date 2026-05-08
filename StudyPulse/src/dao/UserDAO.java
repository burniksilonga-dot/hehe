package dao;

import model.*;
import util.DBConnection;

import java.sql.*;
import java.time.*;
import java.util.*;

/**
 * LAYER: DAO (Data Access Object)
 * PURPOSE: The ONLY layer that writes SQL. All 5 DAOs are in this file.
 *
 * HOW IT WORKS:
 *   1. Each method gets a Connection from DBConnection.getConnection()
 *   2. It creates a PreparedStatement (pre-compiled SQL with ? placeholders)
 *   3. Sets the ? values (prevents SQL injection)
 *   4. Executes the query and maps results back to model objects
 *   5. try-with-resources auto-closes the PreparedStatement
 *
 * WHY PreparedStatement?
 *   Instead of: "SELECT * WHERE name = '" + name + "'"  ← DANGEROUS (SQL injection)
 *   We use:     "SELECT * WHERE name = ?"               ← SAFE
 */

// ── UserDAO ───────────────────────────────────────────────────────────────────
public class UserDAO {

    public User findByCredentials(String username, String password) {
        String sql = "SELECT * FROM users WHERE username=? AND password=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username); ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean register(User u) {
        String sql = "INSERT INTO users (username, password, full_name) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getFullName());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys();
            if (k.next()) u.setId(k.getInt(1));
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public void saveSettings(User u) {
        String sql = "UPDATE users SET work_minutes=?,short_break=?,long_break=?,sound_enabled=?,dark_mode=?,long_interval=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, u.getWorkMinutes()); ps.setInt(2, u.getShortBreak());
            ps.setInt(3, u.getLongBreak());   ps.setBoolean(4, u.isSoundEnabled());
            ps.setBoolean(5, u.isDarkMode()); ps.setInt(6, u.getLongInterval());
            ps.setInt(7, u.getId());
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("full_name"));
        u.setWorkMinutes(rs.getInt("work_minutes"));
        u.setShortBreak(rs.getInt("short_break"));
        u.setLongBreak(rs.getInt("long_break"));
        u.setLongInterval(rs.getInt("long_interval"));
        u.setSoundEnabled(rs.getBoolean("sound_enabled"));
        u.setDarkMode(rs.getBoolean("dark_mode"));
        return u;
    }
}
