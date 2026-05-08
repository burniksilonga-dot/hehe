package ui;

import service.PomodoroService;
import util.SessionContext;

import javax.swing.*;

import panel.AccomplishedPanel;
import panel.ActivityPanel;
import panel.CalendarPanel;
import panel.NotesPanel;
import panel.ReportDialog;
import panel.SettingDialog;
import panel.SubjectsPanel;

import java.awt.*;

/**
 * Main application window — topbar + CardLayout content.
 * FIX: All tab buttons use setContentAreaFilled(false)/setOpaque(false)
 *      so they NEVER show a white hover flash.
 */
public class MainFrame extends JFrame {

    private static final String LOGO_PATH = null;

	private final PomodoroService timerService = new PomodoroService();

    private Color      currentBg = UI.POMO_RED;
    private JPanel     contentPanel;
    private CardLayout cardLayout;

    private JButton btnTimer, btnAccomplished, btnCalendar,
                    btnSubjects, btnNotes, btnActivity;
    private JButton activeTab;

    private TimerPanel        timerPanel;
    private AccomplishedPanel accomplishedPanel;
    private CalendarPanel     calendarPanel;
    private SubjectsPanel     subjectsPanel;
    private NotesPanel        notesPanel;
    private ActivityPanel     activityPanel;

    public MainFrame() {
        setTitle("StudyPulse");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(700, 820);
        setMinimumSize(new Dimension(620, 680));
        setLocationRelativeTo(null);
        buildUI();
        showTab("timer", btnTimer);
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(currentBg);
        getContentPane().add(buildTopbar(),  BorderLayout.NORTH);
        getContentPane().add(buildContent(), BorderLayout.CENTER);
    }
    private JLabel buildLogoLabel() {
        try {
            java.net.URL url = getClass().getResource(LOGO_PATH);
            if (url != null) {
                ImageIcon raw    = new ImageIcon(url);
                Image     scaled = raw.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
            }
        } catch (Exception ignored) { /* fall through */ }

        // Fallback: styled text badge
        JLabel fallback = new JLabel("", SwingConstants.CENTER);
        fallback.setBackground(new Color(240, 240, 240));
        fallback.setIcon(new ImageIcon("C:\\Users\\burni\\Downloads\\18d9c5b9-d71b-4099-a92b-3b35d7fa65c6 (2).png"));
        fallback.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fallback.setForeground(new Color(255, 255, 255));
        fallback.setPreferredSize(new Dimension(60, 60));
        return fallback;
    }

    private JPanel buildTopbar() {
        // Solid dark red — no transparent/alpha colors
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(150, 35, 26));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

        JLabel logo = UI.whiteLabel("🍅 StudyPulse", new Font("Segoe UI", Font.BOLD, 16));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        tabs.setOpaque(false);

        btnTimer        = tabBtn("Timer");
        btnAccomplished = tabBtn("Accomplished");
        btnCalendar     = tabBtn("Calendar");
        btnSubjects     = tabBtn("Subjects");
        btnNotes        = tabBtn("Notes");
        btnActivity     = tabBtn("Activity");

        btnTimer.addActionListener(e        -> { showTab("timer",        btnTimer);        timerPanel.refresh(); });
        btnAccomplished.addActionListener(e -> { showTab("accomplished",  btnAccomplished); accomplishedPanel.refresh(); });
        btnCalendar.addActionListener(e     -> { showTab("calendar",      btnCalendar);     calendarPanel.refresh(); });
        btnSubjects.addActionListener(e     -> { showTab("subjects",      btnSubjects);     subjectsPanel.refresh(); });
        btnNotes.addActionListener(e        -> { showTab("notes",         btnNotes);        notesPanel.refresh(); });
        btnActivity.addActionListener(e     -> { showTab("activity",      btnActivity);     activityPanel.refresh(); });

        for (JButton b : new JButton[]{btnTimer, btnAccomplished, btnCalendar, btnSubjects, btnNotes, btnActivity})
            tabs.add(b);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        right.setOpaque(false);
        JButton btnReport  = topbarActionBtn("Report");
        JButton btnSetting = topbarActionBtn("Setting");
        JButton btnLogout  = topbarActionBtn("Exit");
        btnReport.addActionListener(e  -> new ReportDialog(this).setVisible(true));
        btnSetting.addActionListener(e -> new SettingDialog(this, timerService).setVisible(true));
        btnLogout.addActionListener(e  -> doLogout());
        right.add(btnReport); right.add(btnSetting); right.add(btnLogout);

        bar.add(logo,  BorderLayout.WEST);
        bar.add(tabs,  BorderLayout.CENTER);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }



    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(currentBg);

        timerPanel        = new TimerPanel(timerService, this);
        accomplishedPanel = new AccomplishedPanel();
        calendarPanel     = new CalendarPanel();
        subjectsPanel     = new SubjectsPanel();
        notesPanel        = new NotesPanel();
        activityPanel     = new ActivityPanel();

        contentPanel.add(timerPanel,        "timer");
        contentPanel.add(accomplishedPanel, "accomplished");
        contentPanel.add(calendarPanel,     "calendar");
        contentPanel.add(subjectsPanel,     "subjects");
        contentPanel.add(notesPanel,        "notes");
        contentPanel.add(activityPanel,     "activity");

        return contentPanel;
    }

    public void showTab(String name, JButton btn) {
        cardLayout.show(contentPanel, name);
        if (activeTab != null) {
            // inactive: transparent look, no fill
            activeTab.setContentAreaFilled(false);
            activeTab.setOpaque(false);
            activeTab.setForeground(new Color(255, 220, 220));
            activeTab.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        // active: solid dark pill
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setBackground(new Color(120, 25, 18));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        activeTab = btn;
    }

    public void setThemeColor(Color bg) {
        currentBg = bg;
        getContentPane().setBackground(bg);
        contentPanel.setBackground(bg);
        repaint();
    }

    private void doLogout() {
        if (UI.confirm(this, "Logout?")) {
            timerService.pause();
            SessionContext.logout();
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }

    // Tab button — NO opaque, NO content fill by default → never white flash
    private JButton tabBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(new Color(255, 220, 220));
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return b;
    }

    // Right-side action buttons (Report, Setting, Exit)
    private JButton topbarActionBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(180, 50, 40));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(160, 38, 28)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(new Color(180, 50, 40)); }
        });
        return b;
    }

    public PomodoroService getTimerService() { return timerService; }
}