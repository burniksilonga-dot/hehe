package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import dao.UserDAO;
import model.User;
import service.PomodoroService;
import ui.UI;
import util.SessionContext;

/**
 * Settings dialog — enhanced with per-timer color theme pickers
 * matching the Pomofocus style (Pomodoro / Short Break / Long Break).
 */
public class SettingDialog extends JDialog {

    private final PomodoroService service;
    private final UserDAO userDAO = new UserDAO();
    private JSpinner spWork, spShort, spLong, spInterval;
    private JCheckBox cbSound;
    private JCheckBox cbAutoPomodoro;
    private JCheckBox cbAutoBreak;


    public SettingDialog(Frame parent, PomodoroService svc) {
        super(parent, "Setting", true);
        this.service = svc;
        setSize(400, 580); // Slightly increased height to fit the new section
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
    }

    // ════════════════════════════════════════════════════════════════════
    //  UI construction
    // ════════════════════════════════════════════════════════════════════

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // ── Header ───────────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(150, 35, 26));
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel titleLbl = new JLabel("SETTING");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLbl.setForeground(Color.WHITE);
        JButton cls = new JButton("❎");
        cls.setFont(new Font("Segoe UI Emoji", Font.BOLD, 10));
        cls.setFont(UI.F_BODY);
        cls.setForeground(Color.WHITE);
        cls.setBackground(new Color(150, 35, 26));
        cls.setOpaque(true);
        cls.setContentAreaFilled(true);
        cls.setBorderPainted(false);
        cls.setFocusPainted(false);
        cls.addActionListener(e -> dispose());
        header.add(titleLbl, BorderLayout.WEST);
        header.add(cls,      BorderLayout.EAST);

        // ── Body ─────────────────────────────────────────────────────────
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill      = GridBagConstraints.HORIZONTAL;
        gc.weightx   = 1.0;
        gc.insets    = new Insets(6, 0, 6, 0);
        gc.gridx     = 0;
        gc.gridwidth = 3;

        User u     = SessionContext.getUser();
        spWork     = sp(u.getWorkMinutes(), 25, 90);
        spShort    = sp(u.getShortBreak(),   1, 10);
        spLong     = sp(u.getLongBreak(),   15, 60);
        spInterval = sp(u.getLongInterval(),  1, 10);
        
        cbSound    = new JCheckBox("Enable sound notifications", u.isSoundEnabled());
        cbSound.setBackground(Color.WHITE);
        cbSound.setFont(UI.F_BODY);

        // Set up the Auto Start checkboxes
        cbAutoPomodoro = new JCheckBox("Auto-start Pomodoros");
        cbAutoPomodoro.setBackground(Color.WHITE);
        cbAutoPomodoro.setFont(UI.F_BODY);
        
        cbAutoBreak = new JCheckBox("Auto-start Breaks");
        cbAutoBreak.setBackground(Color.WHITE);
        cbAutoBreak.setFont(UI.F_BODY);
        
        // IF your User model has these, you can load their saved states like this:
        // cbAutoPomodoro.setSelected(u.isAutoPomodoro());
        // cbAutoBreak.setSelected(u.isAutoBreak());

        // ── ⏱ TIMER ──────────────────────────────────────────────────────
        JLabel timerSec = sectionLabel("⏱  TIMER");
        gc.gridy = 0;
        body.add(timerSec, gc);

        // Spinner row
        gc.gridy = 1; gc.gridwidth = 1;
        gc.insets = new Insets(2, 0, 2, 8);
        body.add(labeledSpinner("Pomodoro",    spWork),  gc);
        gc.gridx = 1; body.add(labeledSpinner("Short Break", spShort), gc);
        gc.gridx = 2; body.add(labeledSpinner("Long Break",  spLong),  gc);

        gc.gridx = 0; gc.gridwidth = 3;
        gc.insets = new Insets(6, 0, 6, 0);

        // Long break interval
        gc.gridy = 2;
        JPanel intRow = new JPanel(new BorderLayout(8, 0));
        intRow.setBackground(Color.WHITE);
        JLabel intLbl = new JLabel("Long Break Interval");
        intLbl.setFont(UI.F_BODY);
        spInterval.setPreferredSize(new Dimension(70, 30));
        intRow.add(intLbl,     BorderLayout.WEST);
        intRow.add(spInterval, BorderLayout.EAST);
        body.add(intRow, gc);

        // ── 🔄 AUTOMATION ─────────────────────────────────────────────────
        gc.gridy = 3;
        body.add(sectionLabel("🔄  AUTOMATION"), gc);
        
        gc.gridy = 4;
        gc.insets = new Insets(0, 0, 0, 0); // Tighter spacing for checkboxes
        body.add(cbAutoBreak, gc);
        
        gc.gridy = 5;
        body.add(cbAutoPomodoro, gc);

        // ── 🔊 SOUND ──────────────────────────────────────────────────────
        gc.gridy = 6;
        gc.insets = new Insets(12, 0, 6, 0); // Restore spacing before new section
        body.add(sectionLabel("🔊  SOUND"), gc);
        
        gc.gridy = 7;
        gc.insets = new Insets(0, 0, 6, 0);
        body.add(cbSound, gc);

        // ── Divider ───────────────────────────────────────────────────────
        gc.gridy = 8;
        gc.insets = new Insets(6, 0, 6, 0);
        body.add(new JSeparator(), gc);

        // ── OK button ─────────────────────────────────────────────────────
        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setBackground(UI.POMO_RED);
        ok.setForeground(Color.WHITE);
        ok.setOpaque(true);
        ok.setContentAreaFilled(true);
        ok.setBorderPainted(false);
        ok.setFocusPainted(false);
        ok.setPreferredSize(new Dimension(80, 36));
        ok.addActionListener(e -> save());

        JPanel okRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        okRow.setBackground(Color.WHITE);
        okRow.add(ok);
        gc.gridy = 9;
        gc.insets = new Insets(12, 0, 0, 0);
        body.add(okRow, gc);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        setContentPane(root);
    }

    // ════════════════════════════════════════════════════════════════════
    //  Save Logic
    // ════════════════════════════════════════════════════════════════════

    private void save() {
        User u = SessionContext.getUser();
        u.setWorkMinutes((int) spWork.getValue());
        u.setShortBreak((int)  spShort.getValue());
        u.setLongBreak((int)   spLong.getValue());
        u.setLongInterval((int) spInterval.getValue());
        u.setSoundEnabled(cbSound.isSelected());
        
        // ── Save Auto-Start settings (Uncomment if your User model has these) ──
        u.setAutoPomodoro(cbAutoPomodoro.isSelected());
        u.setAutoBreak(cbAutoBreak.isSelected());

        // ── Persist chosen colours (add these fields to User + UserDAO) ──
        // u.setPomodoroColor(pomodoroColor.getRGB());
        // u.setShortBreakColor(shortBreakColor.getRGB());
        // u.setLongBreakColor(longBreakColor.getRGB());

        userDAO.saveSettings(u);
        service.reloadSettings();

        UI.info(this, "Settings saved!");
        dispose();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════

    private JPanel labeledSpinner(String label, JSpinner sp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(UI.TEXT_GRAY);
        sp.setPreferredSize(new Dimension(70, 30));
        p.add(l,  BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        l.setForeground(UI.TEXT_GRAY);
        return l;
    }

    private JSpinner sp(int v, int min, int max) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, min, max, 1));
        s.setFont(UI.F_BODY);
        return s;
    }

    // ════════════════════════════════════════════════════════════════════
    //  SwatchButton  — a round pill showing the current chosen colour
    // ════════════════════════════════════════════════════════════════════

    private static class SwatchButton extends JButton {
        Color chosenColor;

        SwatchButton(Color initial) {
            this.chosenColor = initial;
            setPreferredSize(new Dimension(32, 32));
            setMaximumSize(new Dimension(32, 32));
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        }

        void setChosenColor(Color c) { this.chosenColor = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(chosenColor);
            g2.fillOval(0, 0, getWidth(), getHeight());
            // Small dropdown arrow to hint it's clickable
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
            g2.drawString("▼", getWidth() / 2 - 4, getHeight() / 2 + 4);
        }
    }
}