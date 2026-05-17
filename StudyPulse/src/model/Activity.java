package model;
import java.time.*;
import java.time.LocalDate;

public class Activity {
    private int       id, subjectId, userId;
    private String    name;
    private LocalDate deadline;
    private boolean   completed;

    public Activity() {}
    public Activity(int subjectId, int userId, String name, LocalDate deadline) {
        this.subjectId = subjectId; this.userId = userId;
        this.name = name; this.deadline = deadline;
    }
    public int       getId()        { return id; }
    public int       getSubjectId() { return subjectId; }
    public int       getUserId()    { return userId; }
    public String    getName()      { return name; }
    public LocalDate getDeadline()  { return deadline; }
    public boolean   isCompleted()  { return completed; }

    public void setId(int v)          { id        = v; }
    public void setSubjectId(int v)   { subjectId = v; }
    public void setUserId(int v)      { userId    = v; }
    public void setName(String v)     { name      = v; }
    public void setDeadline(LocalDate v){ deadline = v; }
    public void setCompleted(boolean v){ completed  = v; }

    /** Days until deadline. Negative = overdue. */
    public long daysUntil() {
        if (deadline == null) return 999;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }
}
