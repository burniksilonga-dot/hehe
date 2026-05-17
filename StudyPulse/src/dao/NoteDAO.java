package dao;


import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.Note;
import util.DBConnection;

public class NoteDAO {

    public void saveNote(Note n) {
        String check="SELECT id FROM notes WHERE user_id=? AND note_date=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(check)){
            ps.setInt(1,n.getUserId()); ps.setDate(2, n.getNoteDate()!=null ? Date.valueOf(n.getNoteDate()) : null);
            ResultSet rs=ps.executeQuery();
            if(rs.next()){
                int id=rs.getInt("id");
                String upd="UPDATE notes SET content=? WHERE id=?";
                try(PreparedStatement ps2=c.prepareStatement(upd)){ps2.setString(1,n.getContent()); ps2.setInt(2,id); ps2.executeUpdate();}
            } else {
                String ins="INSERT INTO notes (user_id,note_date,content) VALUES (?,?,?)";
               
                try(PreparedStatement ps2=c.prepareStatement(ins)){
                    ps2.setInt(1,n.getUserId()); ps2.setDate(2, n.getNoteDate()!=null?Date.valueOf(n.getNoteDate()):null); ps2.setString(3,n.getContent()); ps2.executeUpdate();
                }
            }
        } catch(SQLException e){e.printStackTrace();}
    }
    

    public String getNoteForDate(int userId, LocalDate date) {
        String sql="SELECT content FROM notes WHERE user_id=? AND note_date=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId); ps.setDate(2,Date.valueOf(date));
            ResultSet rs=ps.executeQuery(); if(rs.next()) return rs.getString("content");
        } catch(SQLException e){e.printStackTrace();}
        return "";
    }

    public List<Note> getAllByUser(int userId) {
        List<Note> list=new ArrayList<>();
        String sql="SELECT * FROM notes WHERE user_id=? ORDER BY created_at DESC";
        try(Connection c=DBConnection.getConnection(); PreparedStatement ps=c.prepareStatement(sql)){
            ps.setInt(1,userId); ResultSet rs=ps.executeQuery();
            while(rs.next()){Note n=new Note(); n.setId(rs.getInt("id")); n.setUserId(rs.getInt("user_id")); Date d=rs.getDate("note_date"); if(d!=null) n.setNoteDate(d.toLocalDate()); n.setContent(rs.getString("content")); list.add(n);}
        } catch(SQLException e){e.printStackTrace();}
        return list;
    }
}