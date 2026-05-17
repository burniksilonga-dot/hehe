package model;
import java.time.*;


public class Subject {
    private int    id, userId;
    private String name, color;

    public Subject() {}
    public Subject(int userId, String name, String color) {
        this.userId = userId; this.name = name; this.color = color;
    }
    public int    getId()     { return id; }
    public int    getUserId() { return userId; }
    public String getName()   { return name; }
    public String getColor()  { return color; }

    public void setId(int v)      { id     = v; }
    public void setUserId(int v)  { userId = v; }
    public void setName(String v) { name   = v; }
    public void setColor(String v){ color  = v; }
    @Override public String toString() { return name; }
}