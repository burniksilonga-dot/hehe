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

    // ── Available palette (matches the Pomofocus screenshot) ────────────
    private static final Color[] PALETTE = {
        new Color(186,  73,  73),   // classic red
        new Color( 56, 133, 138),   // teal
        new Color( 57, 112, 151),   // blue-steel
        new Color(194, 130,  72),   // amber/brown
        new Color(111,  95, 170),   // purple
        new Color(176,  58, 140),   // magenta
        new Color( 85, 130,  90),   // forest green
        new Color( 90, 105, 130),   // slate
    };

    // Keys used when saving to User (you can add proper getters/setters on User)
    // Defaults mirror the standard Pomofocus colours
    private Color pomodoroColor  = new Color(186, 73, 73);
    private Color shortBreakColor= new Color( 56,133,138);
    private Color longBreakColor = new Color( 57,112,151);

    private final PomodoroService service;
    private final UserDAO userDAO = new UserDAO();
    private JSpinner spWork, spShort, spLong, spInterval;
    private JCheckBox cbSound;
    private JCheckBox cbAutoPomodoro;
    private JCheckBox cbAutoBreak;

    // Small swatch buttons that show the currently chosen colour
    private SwatchButton swPomodoro, swShort, swLong;

    // The floating colour-picker popup (one shared instance)
    private JDialog pickerPopup;
    private SwatchButton activePickerTarget;

    public SettingDialog(Frame parent, PomodoroService svc) {
        super(parent, "Setting", true);
        this.service = svc;
        setSize(400, 510);
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
        JButton cls = new JButton("✕");
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

        cbAutoPomodoro = new JCheckBox("Auto start Pomodoro");
        cbAutoBreak    = new JCheckBox("Auto start Break");

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

        // ── Divider ───────────────────────────────────────────────────────
        gc.gridy = 3;
        body.add(new JSeparator(), gc);

        // ── 🎨 THEME ──────────────────────────────────────────────────────
        gc.gridy = 4;
        body.add(sectionLabel("🎨  THEME"), gc);

        // Three colour pickers in one row
        gc.gridy = 5;
        body.add(buildColorPickerRow(), gc);

        // ── Divider ───────────────────────────────────────────────────────
        gc.gridy = 6;
        body.add(new JSeparator(), gc);

        // ── 🔊 SOUND ──────────────────────────────────────────────────────
        gc.gridy = 7;
        body.add(sectionLabel("🔊  SOUND"), gc);
        gc.gridy = 8;
        body.add(cbSound, gc);

        // ── Divider ───────────────────────────────────────────────────────
        gc.gridy = 9;
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
        gc.gridy = 10;
        gc.insets = new Insets(12, 0, 0, 0);
        body.add(okRow, gc);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        setContentPane(root);
    }

    // ════════════════════════════════════════════════════════════════════
    //  Colour-picker row  (Pomodoro | Short Break | Long Break)
    // ════════════════════════════════════════════════════════════════════

    private JPanel buildColorPickerRow() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setBackground(Color.WHITE);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets  = new Insets(0, 0, 0, 8);

        swPomodoro = new SwatchButton(pomodoroColor);
        swShort    = new SwatchButton(shortBreakColor);
        swLong     = new SwatchButton(longBreakColor);

        gc.gridx = 0; row.add(timerColorCell("Pomodoro",    swPomodoro), gc);
        gc.gridx = 1; row.add(timerColorCell("Short Break", swShort),    gc);
        gc.gridx = 2; gc.insets = new Insets(0,0,0,0);
                      row.add(timerColorCell("Long Break",  swLong),     gc);

        swPomodoro.addActionListener(e -> showColorPicker(swPomodoro));
        swShort.addActionListener(e    -> showColorPicker(swShort));
        swLong.addActionListener(e     -> showColorPicker(swLong));

        return row;
    }

    /** A small column: label on top, round swatch button below. */
    private JPanel timerColorCell(String label, SwatchButton swatch) {
        JPanel cell = new JPanel();
        cell.setBackground(Color.WHITE);
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setAlignmentX(CENTER_ALIGNMENT);

        swatch.setAlignmentX(CENTER_ALIGNMENT);

        cell.add(lbl);
        cell.add(Box.createVerticalStrut(4));
        cell.add(swatch);

        return cell;
    }
    // ════════════════════════════════════════════════════════════════════
    //  Floating colour-picker popup
    // ════════════════════════════════════════════════════════════════════

    private void showColorPicker(SwatchButton target) {
        if (pickerPopup != null && pickerPopup.isVisible()) {
            pickerPopup.dispose();
            if (activePickerTarget == target) {    // toggle off if same btn
                activePickerTarget = null;
                return;
            }
        }
        activePickerTarget = target;

        pickerPopup = new JDialog(this, false);    // non-modal so dialog stays open
        pickerPopup.setUndecorated(true);
        pickerPopup.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel hint = new JLabel("Pick a color for "
                + labelFor(target), SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.BOLD, 11));
        hint.setForeground(new Color(80, 80, 80));
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        // 4×2 grid of colour swatches
        JPanel grid = new JPanel(new java.awt.GridLayout(2, 4, 6, 6));
        grid.setBackground(Color.WHITE);

        for (Color c : PALETTE) {
            JButton swatch = new JButton() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(c);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    // Check-mark if this colour is currently selected
                    if (c.equals(target.chosenColor)) {
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        g2.drawString("✓", getWidth() / 2 - 5, getHeight() / 2 + 5);
                    }
                }
            };
            swatch.setPreferredSize(new Dimension(34, 34));
            swatch.setOpaque(false);
            swatch.setContentAreaFilled(false);
            swatch.setBorderPainted(false);
            swatch.setFocusPainted(false);
            swatch.setCursor(java.awt.Cursor.getPredefinedCursor(
                    java.awt.Cursor.HAND_CURSOR));
            swatch.addActionListener(e -> {
                applyColor(target, c);
                pickerPopup.dispose();
            });
            grid.add(swatch);
        }

        content.add(hint, BorderLayout.NORTH);
        content.add(grid, BorderLayout.CENTER);
        pickerPopup.setContentPane(content);
        pickerPopup.pack();

        // Position popup just below the swatch button
        java.awt.Point loc = target.getLocationOnScreen();
        pickerPopup.setLocation(loc.x, loc.y + target.getHeight() + 4);
        pickerPopup.setVisible(true);

        // Close popup when user clicks outside
        pickerPopup.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowLostFocus(java.awt.event.WindowEvent e) {
                if (pickerPopup != null) pickerPopup.dispose();
            }
            public void windowGainedFocus(java.awt.event.WindowEvent e) {}
        });
    }

    private String labelFor(SwatchButton sw) {
        if (sw == swPomodoro) return "Pomodoro";
        if (sw == swShort)    return "Short Break";
        return "Long Break";
    }

    private void applyColor(SwatchButton target, Color c) {
        target.setChosenColor(c);
        if (target == swPomodoro) pomodoroColor   = c;
        else if (target == swShort) shortBreakColor = c;
        else                        longBreakColor  = c;
        target.repaint();
    }

    // ════════════════════════════════════════════════════════════════════
    //  Save
    // ════════════════════════════════════════════════════════════════════

    private void save() {
        User u = SessionContext.getUser();
        u.setWorkMinutes((int) spWork.getValue());
        u.setShortBreak((int)  spShort.getValue());
        u.setLongBreak((int)   spLong.getValue());
        u.setLongInterval((int) spInterval.getValue());
        u.setSoundEnabled(cbSound.isSelected());

        // ── Persist chosen colours (add these fields to User + UserDAO) ──
        // u.setPomodoroColor(pomodoroColor.getRGB());
        // u.setShortBreakColor(shortBreakColor.getRGB());
        // u.setLongBreakColor(longBreakColor.getRGB());

        userDAO.saveSettings(u);
        service.reloadSettings();

        // Notify the timer panel / UI to apply new colours
        applyThemeColors();

        UI.info(this, "Settings saved!");
        dispose();
    }

    /**
     * Apply the chosen colours to the running app.
     * Extend this method to push colours into TimerPanel / MainFrame as needed.
     */
    private void applyThemeColors() {
        // Example — if UI holds global theme colors, update them here:
        // UI.setPomodoroColor(pomodoroColor);
        // UI.setShortBreakColor(shortBreakColor);
        // UI.setLongBreakColor(longBreakColor);
        // ((MainFrame) getOwner()).setThemeColor(pomodoroColor);
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
