package ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * LAYER: UI Utility
 * PURPOSE: Factory methods for all styled Swing components.
 *   Centralizes the Pomofocus visual theme so every panel looks consistent.
 *   Change a color or font here and it updates the whole app.
 */
public class UI {

    // ── Pomofocus palette ─────────────────────────────────────
    public static final Color POMO_RED   = new Color(0xC0, 0x39, 0x2B);
    public static final Color POMO_DARK  = new Color(0xA0, 0x2D, 0x20);
    public static final Color SHORT_GRN  = new Color(0x4C, 0x8B, 0x6A);
    public static final Color LONG_BLUE  = new Color(0x4A, 0x6F, 0xA5);
    public static final Color WHITE      = Color.WHITE;
    public static final Color CARD_WHITE = new Color(255,255,255,220);
    public static final Color TEXT_DARK  = new Color(0x33,0x33,0x33);
    public static final Color TEXT_GRAY  = new Color(0x88,0x88,0x88);
    public static final Color BORDER_CLR = new Color(0xDD,0xDD,0xDD);
    public static final Color ROW_ALT    = new Color(0xF8,0xF9,0xFD);

    // ── Fonts ─────────────────────────────────────────────────
    public static final Font F_TIMER  = new Font("Segoe UI", Font.BOLD, 72);
    public static final Font F_H1     = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font F_H2     = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font F_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_BOLD   = new Font("Segoe UI", Font.BOLD, 13);

    // ── Panels ────────────────────────────────────────────────
    public static JPanel bgPanel(LayoutManager lm, Color bg) {
        JPanel p = new JPanel(lm); p.setBackground(bg); return p;
    }

    public static JPanel card(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR, 1),
            BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return p;
    }

    //-----------------------  labels ------------------------------------
    public static JLabel label(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }
    public static JLabel whiteLabel(String t, Font f) { return label(t,f,WHITE); }
    public static JLabel darkLabel(String t, Font f)   { return label(t,f,TEXT_DARK); }
    public static JLabel grayLabel(String t, Font f)   { return label(t,f,TEXT_GRAY); }

    // ------------------------buttons-------------------
    /** White Pomofocus START button */
    public static JButton startButton(Color accent) {
        JButton b = new JButton("START");
        b.setFont(new Font("Segoe UI", Font.BOLD, 20));
        b.setForeground(accent);
        b.setBackground(WHITE);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(220, 52));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0,0,0,30), 1),
            BorderFactory.createEmptyBorder(10,30,10,30)
        ));
        hover(b, WHITE, new Color(240,240,240));
        return b;
    }

    /** Filled colored button */
    public static JButton filledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(F_BOLD);
        b.setForeground(WHITE);
        b.setBackground(bg);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9,20,9,20));
        hover(b, bg, bg.darker());
        return b;
    }

    /** Outlined ghost button */
    public static JButton outlineBtn(String text, Color color) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setForeground(color);
        b.setBackground(WHITE);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color,1),
            BorderFactory.createEmptyBorder(7,16,7,16)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Transparent button for topbar */
    public static JButton topbarBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setForeground(WHITE);
        b.setBackground(new Color(255,255,255,50));
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6,14,6,14));
        Color h = new Color(255,255,255,80);
        hover(b, new Color(255,255,255,50), h);
        return b;
    }

    // ── Text fields ───────────────────────────────────────────
    public static JTextField textField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(F_BODY);
        tf.setForeground(TEXT_DARK);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR),
            BorderFactory.createEmptyBorder(8,10,8,10)
        ));
        return tf;
    }

    public static JPasswordField passField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setFont(F_BODY);
        pf.setForeground(TEXT_DARK);
        pf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_CLR),
            BorderFactory.createEmptyBorder(8,10,8,10)
        ));
        return pf;
    }

    // ── Table ─────────────────────────────────────────────────
    public static void styleTable(JTable t) {
        t.setFont(F_BODY);
        t.setForeground(TEXT_DARK);
        t.setRowHeight(36);
        t.setGridColor(BORDER_CLR);
        t.setShowVerticalLines(false);
        t.setSelectionBackground(new Color(192,57,43,40));
        t.setSelectionForeground(TEXT_DARK);
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object v,boolean s,boolean f,int r,int c){
                super.getTableCellRendererComponent(t,v,s,f,r,c);
                setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
                setForeground(TEXT_DARK);
                if(!s) setBackground(r%2==0 ? WHITE : ROW_ALT);
                return this;
            }
        });
        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI",Font.BOLD,12));
        h.setBackground(new Color(250,248,248));
        h.setForeground(TEXT_GRAY);
        h.setBorder(BorderFactory.createMatteBorder(0,0,1,0,BORDER_CLR));
        ((DefaultTableCellRenderer)h.getDefaultRenderer()).setBorder(BorderFactory.createEmptyBorder(0,12,0,12));
    }

    public static JScrollPane scroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_CLR));
        sp.getViewport().setBackground(WHITE);
        return sp;
    }

    // ── Dialogs ───────────────────────────────────────────────
    public static void error(Component p, String msg)  { JOptionPane.showMessageDialog(p,msg,"Error",JOptionPane.ERROR_MESSAGE); }
    public static void info(Component p,  String msg)  { JOptionPane.showMessageDialog(p,msg,"Info", JOptionPane.INFORMATION_MESSAGE); }
    public static boolean confirm(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p,msg,"Confirm",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION;
    }

    // ── Separator ─────────────────────────────────────────────
    public static JSeparator sep() {
        JSeparator s = new JSeparator();
        s.setForeground(BORDER_CLR);
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE,1));
        return s;
    }

    // ── Hover helper ─────────────────────────────────────────
    private static void hover(JButton b, Color normal, Color hov) {
        b.addMouseListener(new MouseAdapter(){
            public void mouseEntered(MouseEvent e){ b.setBackground(hov); }
            public void mouseExited(MouseEvent e) { b.setBackground(normal); }
        });
    }

    public static String pad(int n){ return n<10?"0"+n:String.valueOf(n); }
}
