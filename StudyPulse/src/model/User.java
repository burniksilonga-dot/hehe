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
}
