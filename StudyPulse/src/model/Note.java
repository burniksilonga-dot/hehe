package model;
import java.time.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Note {
    private int       id, userId;
    private LocalDate noteDate;
    private String    content;
    private LocalDateTime createdAt;

    public Note() {}
    public Note(int userId, LocalDate date, String content) {
        this.userId = userId; this.noteDate = date; this.content = content;
    }
    public int       getId()        { return id; }
    public int       getUserId()    { return userId; }
    public LocalDate getNoteDate()  { return noteDate; }
    public String    getContent()   { return content; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public void setId(int v)          { id        = v; }
    public void setUserId(int v)      { userId    = v; }
    public void setNoteDate(LocalDate v){ noteDate = v; }
    public void setContent(String v)  { content   = v; }
    public void setCreatedAt(LocalDateTime v){ createdAt = v; }
}
