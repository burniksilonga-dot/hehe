package model;

import java.time.*;

/**
 * LAYER: Model
 * PURPOSE: Plain Java classes (POJOs) that mirror the database tables.
 *   No logic here — just fields + getters + setters.
 *   Every field maps to one column in the database.
 */

// ── User ─────────────────────────────────────────────────────────────────────
public class User {
    private int     id;
    private String  username, password, fullName;
    private int     workMinutes = 25, shortBreak = 5, longBreak = 15, longInterval = 4;
    private boolean soundEnabled = true, darkMode = false;
    private int pomodoroColor   = new java.awt.Color(192, 57,  43).getRGB();
    private int shortBrkColor   = new java.awt.Color( 76,139, 106).getRGB();
    private int longBrkColor    = new java.awt.Color( 74,111, 165).getRGB();

    public User() {}
    public User(String username, String password, String fullName) {
        this.username = username; this.password = password; this.fullName = fullName;
    }

    public int     getId()           { return id; }
    public String  getUsername()     { return username; }
    public String  getPassword()     { return password; }
    public String  getFullName()     { return fullName; }
    public int     getWorkMinutes()  { return workMinutes; }
    public int     getShortBreak()   { return shortBreak; }
    public int     getLongBreak()    { return longBreak; }
    public int     getLongInterval() { return longInterval; }
    public int getPomodoroColor()  { return pomodoroColor;  }
    public int getShortBrkColor()  { return shortBrkColor;  }
    public int getLongBrkColor()   { return longBrkColor;   }
    public boolean isSoundEnabled()  { return soundEnabled; }
    public boolean isDarkMode()      { return darkMode; }

    public void setId(int v)              { id           = v; }
    public void setUsername(String v)     { username     = v; }
    public void setPassword(String v)     { password     = v; }
    public void setFullName(String v)     { fullName     = v; }
    public void setWorkMinutes(int v)     { workMinutes  = v; }
    public void setShortBreak(int v)      { shortBreak   = v; }
    public void setLongBreak(int v)       { longBreak    = v; }
    public void setLongInterval(int v)    { longInterval = v; }
    public void setSoundEnabled(boolean v){ soundEnabled  = v; }
    public void setDarkMode(boolean v)    { darkMode      = v; }
    public void setPomodoroColor(int v)  { pomodoroColor  = v; }
    public void setShortBrkColor(int v)  { shortBrkColor  = v; }
    public void setLongBrkColor(int v)   { longBrkColor   = v; }
 // Add these variables to the top of User.java with your other variables
    private boolean autoPomodoro = false;
    private boolean autoBreak = false;

    // Add these getters and setters anywhere inside User.java
    public boolean isAutoPomodoro() {
        return autoPomodoro;
    }

    public void setAutoPomodoro(boolean autoPomodoro) {
        this.autoPomodoro = autoPomodoro;
    }

    public boolean isAutoBreak() {
        return autoBreak;
    }

    public void setAutoBreak(boolean autoBreak) {
        this.autoBreak = autoBreak;
    }
}
