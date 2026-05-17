package model;
import java.time.*;


import java.time.LocalDate;
import java.time.LocalDateTime;

public class StudySession {
    private int           id, userId, durationSec;
    private LocalDate     sessionDate;
    private LocalDateTime startTime, endTime;
    private String        sessionType;
    private Integer       taskId;

    public int           getId()          { return id; }
    public int           getUserId()      { return userId; }
    public LocalDate     getSessionDate() { return sessionDate; }
    public LocalDateTime getStartTime()   { return startTime; }
    public LocalDateTime getEndTime()     { return endTime; }
    public int           getDurationSec() { return durationSec; }
    public String        getSessionType() { return sessionType; }
    public Integer       getTaskId()      { return taskId; }

    public void setId(int v)                  { id          = v; }
    public void setUserId(int v)              { userId      = v; }
    public void setSessionDate(LocalDate v)   { sessionDate = v; }
    public void setStartTime(LocalDateTime v) { startTime   = v; }
    public void setEndTime(LocalDateTime v)   { endTime     = v; }
    public void setDurationSec(int v)         { durationSec = v; }
    public void setSessionType(String v)      { sessionType = v; }
    public void setTaskId(Integer v)          { taskId      = v; }
}
