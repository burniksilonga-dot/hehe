package dao;

import model.*;
import util.DBConnection;

import java.sql.*;
import java.time.*;
import java.util.*;

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
        // FIXED: Added auto_pomodoro=? and auto_break=? to the SQL statement
        String sql = "UPDATE users SET work_minutes=?, short_break=?, long_break=?, " +
                     "sound_enabled=?, dark_mode=?, long_interval=?, " +
                     "pomodoro_color=?, short_brk_color=?, long_brk_color=?, " +
                     "auto_pomodoro=?, auto_break=? WHERE id=?";
                     
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1,     u.getWorkMinutes());
            ps.setInt(2,     u.getShortBreak());
            ps.setInt(3,     u.getLongBreak());
            ps.setBoolean(4, u.isSoundEnabled());
            ps.setBoolean(5, u.isDarkMode());
            ps.setInt(6,     u.getLongInterval());
            ps.setInt(7,     u.getPomodoroColor());  
            ps.setInt(8,     u.getShortBrkColor());   
            ps.setInt(9,     u.getLongBrkColor());
            
            // FIXED: The numbering must match the order of the '?' in the SQL string above
            ps.setBoolean(10, u.isAutoPomodoro()); 
            ps.setBoolean(11, u.isAutoBreak());    
            ps.setInt(12,    u.getId());           // ID is now the 12th question mark
            
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
        u.setPomodoroColor(rs.getInt("pomodoro_color"));
        u.setShortBrkColor(rs.getInt("short_brk_color"));
        u.setLongBrkColor(rs.getInt("long_brk_color"));
        u.setAutoPomodoro(rs.getBoolean("auto_pomodoro"));
        u.setAutoBreak(rs.getBoolean("auto_break"));
        return u;
    }
}