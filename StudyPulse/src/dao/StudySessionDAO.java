package dao;



import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
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

    /** Returns a map of date-string → work-pomodoro count for charting */
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
}
