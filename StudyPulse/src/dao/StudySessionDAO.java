package dao;



import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import model.StudySession;
import util.DBConnection;

public class StudySessionDAO {

    public int startSession(StudySession s) {
        String sql = "INSERT INTO study_sessions (user_id,session_date,start_time,session_type,task_id) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,s.getUserId());
            ps.setDate(2, Date.valueOf(s.getSessionDate()));
            ps.setTimestamp(3, Timestamp.valueOf(s.getStartTime()));
            ps.setString(4,s.getSessionType());
            if(s.getTaskId()!=null) ps.setInt(5,s.getTaskId()); else ps.setNull(5,Types.INTEGER);
            ps.executeUpdate();
            ResultSet k=ps.getGeneratedKeys(); if(k.next()){s.setId(k.getInt(1)); return s.getId();}
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void completeSession(int id, LocalDateTime end, int durSec) {
        String sql = "UPDATE study_sessions SET end_time=?,duration_sec=? WHERE id=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1,Timestamp.valueOf(end)); ps.setInt(2,durSec); ps.setInt(3,id);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public Map<String,Integer> getWeeklyPomoCount(int userId) {
        Map<String,Integer> map = new LinkedHashMap<>();
        String sql = "SELECT session_date, COUNT(*) as cnt FROM study_sessions " +
                     "WHERE user_id=? AND session_type='WORK' AND session_date >= DATE_SUB(CURDATE(),INTERVAL 7 DAY) " +
                     "GROUP BY session_date ORDER BY session_date";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1,userId);
            ResultSet rs=ps.executeQuery();
            while(rs.next()) map.put(rs.getString("session_date"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    public int getTodayWorkSec(int userId) {
        String sql="SELECT COALESCE(SUM(duration_sec),0) FROM study_sessions WHERE user_id=? AND session_date=CURDATE() AND session_type='WORK'";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId); ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch(SQLException e){e.printStackTrace();}
        return 0;
    }

    public int getTodayPomoCount(int userId) {
        String sql="SELECT COUNT(*) FROM study_sessions WHERE user_id=? AND session_date=CURDATE() AND session_type='WORK'";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId); ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getInt(1);
        } catch(SQLException e){e.printStackTrace();}
        return 0;
    }
 // ── Gets data for the last 30 days (Month View) ──
    public Map<String, Integer> getMonthlyPomoCount(int userId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        // Pre-fill the last 30 days with 0
        LocalDate d = LocalDate.now().minusDays(29);
        for (int i = 0; i < 30; i++) {
            map.put(d.toString(), 0);
            d = d.plusDays(1);
        }

        String sql = "SELECT DATE(session_date) as dt, COUNT(*) as cnt FROM study_sessions " +
                     "WHERE user_id=? AND session_date >= ? GROUP BY dt ORDER BY dt ASC";
        try (java.sql.Connection c = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(LocalDate.now().minusDays(29)));
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("dt"), rs.getInt("cnt"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // ── Gets data grouped by Month for the current year (Year View) ──
    public Map<String, Integer> getYearlyPomoCount(int userId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        int currentYear = LocalDate.now().getYear();
        
        // Pre-fill all 12 months with 0 (Format: YYYY-MM)
        for (int i = 1; i <= 12; i++) {
            String monthKey = currentYear + "-" + String.format("%02d", i);
            map.put(monthKey, 0);
        }

        String sql = "SELECT DATE_FORMAT(session_date, '%Y-%m') as mo, COUNT(*) as cnt FROM study_sessions " +
                     "WHERE user_id=? AND YEAR(session_date)=? GROUP BY mo ORDER BY mo ASC";
        try (java.sql.Connection c = util.DBConnection.getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, currentYear);
            java.sql.ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("mo"), rs.getInt("cnt"));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }
}
