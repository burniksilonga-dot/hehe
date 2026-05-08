package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import dao.StudySessionDAO;
import ui.UI;
import util.SessionContext;

public class ActivityPanel extends JPanel {
    private final StudySessionDAO dao = new StudySessionDAO();
    private JPanel chartPanel;
    private JLabel lblHours, lblDays, lblStreak;
    private Map<String, Integer> weekData = new LinkedHashMap<>();
    private JScrollPane scroll;

    public ActivityPanel() {
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrap = new JPanel();
        wrap.setBackground(UI.POMO_RED);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

        // White card container
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI.BORDER_CLR),
            BorderFactory.createEmptyBorder(20, 22, 20, 22)
        ));
        card.setMaximumSize(new Dimension(560, Integer.MAX_VALUE));

        JLabel title = new JLabel("Activity Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(UI.TEXT_DARK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        // 3 stat cards in a row
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(Color.WHITE);
        stats.setMaximumSize(new Dimension(520, 110));
        stats.setAlignmentX(LEFT_ALIGNMENT);

        lblHours  = statNum("0.0");
        lblDays   = statNum("0");
        lblStreak = statNum("0");
        stats.add(statCard(lblHours,  "hours focused", "⏱"));
        stats.add(statCard(lblDays,   "days accessed", "📅"));
        stats.add(statCard(lblStreak, "day streak",    "🔥"));

        JLabel cTitle = new JLabel("Focus Hours — This Week");
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cTitle.setForeground(UI.TEXT_DARK);
        cTitle.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        cTitle.setAlignmentX(LEFT_ALIGNMENT);

        chartPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (weekData.isEmpty()) {
                    g2.setColor(UI.TEXT_GRAY);
                    g2.setFont(UI.F_BODY);
                    g2.drawString("No sessions recorded yet. Start a Pomodoro to track!", 20, getHeight() / 2);
                    return;
                }
                int n = weekData.size();
                int max = weekData.values().stream().max(Integer::compare).orElse(1);
                int bw = (getWidth() - (n + 1) * 10) / n;
                int bx = 10;
                int bottom = getHeight() - 28;
                g2.setColor(new Color(230, 230, 230));
                g2.drawLine(0, bottom, getWidth(), bottom);
                String today = LocalDate.now().toString();
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (Map.Entry<String, Integer> e : weekData.entrySet()) {
                    int bh = (int) ((double) e.getValue() / max * (bottom - 10));
                    g2.setColor(e.getKey().equals(today) ? UI.POMO_RED : new Color(245, 192, 188));
                    g2.fillRoundRect(bx, bottom - bh, bw, bh, 4, 4);
                    g2.setColor(UI.TEXT_GRAY);
                    String lbl = e.getKey().length() >= 10 ? e.getKey().substring(8) : "";
                    int lx = bx + bw / 2 - 6;
                    g2.drawString(lbl, lx, getHeight() - 8);
                    bx += bw + 10;
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setAlignmentX(LEFT_ALIGNMENT);
        chartPanel.setPreferredSize(new Dimension(520, 180));
        chartPanel.setMaximumSize(new Dimension(520, 180));

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(stats);
        card.add(cTitle);
        card.add(chartPanel);

        wrap.add(card);

        scroll = new JScrollPane(wrap);
        scroll.setBackground(UI.POMO_RED);
        scroll.getViewport().setBackground(UI.POMO_RED);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        if (SessionContext.getUser() == null) return;
        int uid = SessionContext.getUser().getId();
        weekData = dao.getWeeklyPomoCount(uid);
        int total = weekData.values().stream().mapToInt(Integer::intValue).sum();
        lblHours.setText(String.format("%.1f", total * 25.0 / 60));
        lblDays.setText(String.valueOf(weekData.size()));
        lblStreak.setText(String.valueOf(calcStreak()));
        chartPanel.repaint();
    }

    private int calcStreak() {
        int s = 0;
        LocalDate d = LocalDate.now();
        while (weekData.containsKey(d.toString())) { s++; d = d.minusDays(1); }
        return s;
    }

    private JPanel statCard(JLabel numLbl, String lbl, String icon) {
        JPanel p = new JPanel();
        p.setBackground(new Color(255, 240, 239));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(12, 10, 12, 10));
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        ico.setAlignmentX(CENTER_ALIGNMENT);
        numLbl.setAlignmentX(CENTER_ALIGNMENT);
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(UI.F_SMALL);
        l.setForeground(new Color(200, 100, 100));
        l.setAlignmentX(CENTER_ALIGNMENT);
        p.add(ico); p.add(numLbl); p.add(l);
        return p;
    }

    private JLabel statNum(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        l.setForeground(UI.POMO_RED);
        return l;
    }
}