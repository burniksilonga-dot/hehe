package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Task;
import util.DBConnection;

public class TaskDAO {

    public void add(Task t) {
        String sql = "INSERT INTO tasks (user_id,name,subject,est_pomos) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,t.getUserId()); ps.setString(2,t.getName());
            ps.setString(3,t.getSubject()); ps.setInt(4,t.getEstPomos());
            ps.executeUpdate();
            ResultSet k = ps.getGeneratedKeys(); if(k.next()) t.setId(k.getInt(1));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<Task> getByUser(int userId) {
        List<Task> list = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE user_id=? ORDER BY completed, created_at DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapTask(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void incrementDone(int taskId) {
        String sql = "UPDATE tasks SET done_pomos=done_pomos+1 WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void setCompleted(int taskId, boolean done) {
        String sql = "UPDATE tasks SET completed=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1,done); ps.setInt(2,taskId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void delete(int taskId) {
        String sql = "DELETE FROM tasks WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void updateEst(int taskId, int est) {
        String sql = "UPDATE tasks SET est_pomos=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1,est); ps.setInt(2,taskId); ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private Task mapTask(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id")); t.setUserId(rs.getInt("user_id"));
        t.setName(rs.getString("name")); t.setSubject(rs.getString("subject"));
        t.setEstPomos(rs.getInt("est_pomos")); t.setDonePomos(rs.getInt("done_pomos"));
        t.setCompleted(rs.getBoolean("completed"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts!=null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}