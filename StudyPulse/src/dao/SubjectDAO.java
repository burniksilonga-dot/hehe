package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import model.Activity;
import model.Subject;
import util.DBConnection;

public class SubjectDAO {

    public void addSubject(Subject s) {
        String sql="INSERT INTO subjects (user_id,name,color) VALUES (?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,s.getUserId()); ps.setString(2,s.getName()); ps.setString(3,s.getColor());
            ps.executeUpdate(); ResultSet k=ps.getGeneratedKeys(); if(k.next()) s.setId(k.getInt(1));
        } catch(SQLException e){e.printStackTrace();}
    }

    public List<Subject> getByUser(int userId) {
        List<Subject> list=new ArrayList<>();
        String sql="SELECT * FROM subjects WHERE user_id=? ORDER BY name";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId); ResultSet rs=ps.executeQuery();
            while(rs.next()){Subject s=new Subject(); s.setId(rs.getInt("id")); s.setUserId(rs.getInt("user_id")); s.setName(rs.getString("name")); s.setColor(rs.getString("color")); list.add(s);}
        } catch(SQLException e){e.printStackTrace();}
        return list;
    }

    public void addActivity(Activity a) {
        String sql="INSERT INTO activities (subject_id,user_id,name,deadline) VALUES (?,?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            ps.setInt(1,a.getSubjectId()); ps.setInt(2,a.getUserId()); ps.setString(3,a.getName());
            if(a.getDeadline()!=null) ps.setDate(4,Date.valueOf(a.getDeadline())); else ps.setNull(4,Types.DATE);
            ps.executeUpdate(); ResultSet k=ps.getGeneratedKeys(); if(k.next()) a.setId(k.getInt(1));
        } catch(SQLException e){e.printStackTrace();}
    }

    public List<Activity> getActivitiesBySubject(int subjectId) {
        List<Activity> list=new ArrayList<>();
        String sql="SELECT * FROM activities WHERE subject_id=? ORDER BY deadline, name";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,subjectId); ResultSet rs=ps.executeQuery();
            while(rs.next()){Activity a=new Activity(); a.setId(rs.getInt("id")); a.setSubjectId(rs.getInt("subject_id")); a.setUserId(rs.getInt("user_id")); a.setName(rs.getString("name")); Date dl=rs.getDate("deadline"); if(dl!=null) a.setDeadline(dl.toLocalDate()); a.setCompleted(rs.getBoolean("completed")); list.add(a);}
        } catch(SQLException e){e.printStackTrace();}
        return list;
    }

    public void setActivityDone(int actId, boolean done) {
        String sql="UPDATE activities SET completed=? WHERE id=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setBoolean(1,done); ps.setInt(2,actId); ps.executeUpdate();
        } catch(SQLException e){e.printStackTrace();}
    }
}
