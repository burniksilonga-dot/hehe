package model;
import java.time.*;

import java.time.LocalDateTime;

public class Task {
    private int    id, userId, estPomos, donePomos;
    private String name, subject;
    private boolean completed;
    private LocalDateTime createdAt;

    public Task() {}
    public Task(int userId, String name, String subject, int est) {
        this.userId = userId; this.name = name; this.subject = subject; this.estPomos = est;
    }
    public int     getId()        { return id; }
    public int     getUserId()    { return userId; }
    public String  getName()      { return name; }
    public String  getSubject()   { return subject; }
    public int     getEstPomos()  { return estPomos; }
    public int     getDonePomos() { return donePomos; }
    public boolean isCompleted()  { return completed; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(int v)           { id        = v; }
    public void setUserId(int v)       { userId    = v; }
    public void setName(String v)      { name      = v; }
    public void setSubject(String v)   { subject   = v; }
    public void setEstPomos(int v)     { estPomos  = v; }
    public void setDonePomos(int v)    { donePomos = v; }
    public void setCompleted(boolean v){ completed  = v; }
    public void setCreatedAt(LocalDateTime v){ createdAt = v; }
}