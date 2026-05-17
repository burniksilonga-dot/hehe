package util;

import model.User;
import java.awt.Color;


public class SessionContext {
    private static User currentUser;
    public static void  login(User u)  { currentUser = u; }
    public static void  logout()       { currentUser = null; }
    public static User  getUser()      { return currentUser; }
    public static boolean isLoggedIn() { return currentUser != null; }
}

class AppColors {
    // Pomofocus red (work mode)
    static final Color POMO_RED    = new Color(0xC0, 0x39, 0x2B);
    static final Color POMO_RED2   = new Color(0xA0, 0x2D, 0x20);
    // Short break green
    static final Color SHORT_GREEN = new Color(0x4C, 0x8B, 0x6A);
    // Long break blue
    static final Color LONG_BLUE   = new Color(0x4A, 0x6F, 0xA5);
    // UI neutrals
    static final Color WHITE       = Color.WHITE;
    static final Color CARD_BG     = new Color(255,255,255,200);
    static final Color TEXT_DARK   = new Color(0x33, 0x33, 0x33);
    static final Color TEXT_GRAY   = new Color(0x88, 0x88, 0x88);
    static final Color BORDER      = new Color(0xDD, 0xDD, 0xDD);
}
