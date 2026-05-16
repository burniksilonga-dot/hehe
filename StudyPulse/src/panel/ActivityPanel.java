package panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
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
        lblHours.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblDays.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        lblStreak.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel cTitle = new JLabel("Focus Hours — This Week");
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cTitle.setForeground(UI.TEXT_DARK);
        cTitle.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        cTitle.setAlignmentX(LEFT_ALIGNMENT);

        // ── Improved chart panel ──────────────────────────────────────────
        chartPanel = new JPanel() {
            private static final int PAD_LEFT   = 42;  // room for Y-axis labels
            private static final int PAD_BOTTOM = 28;  // room for X-axis labels
            private static final int PAD_TOP    = 16;
            private static final int PAD_RIGHT  = 10;
            private static final int NUM_GRID   = 4;   // horizontal grid lines

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth();
                int H = getHeight();
                int chartW = W - PAD_LEFT - PAD_RIGHT;
                int chartH = H - PAD_TOP  - PAD_BOTTOM;
                int bottom = PAD_TOP + chartH;   // y-coordinate of the baseline

                // ── empty state ──
                if (weekData.isEmpty()) {
                    g2.setColor(UI.TEXT_GRAY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String msg = "No sessions recorded yet. Start a Pomodoro to track!";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, PAD_LEFT + (chartW - fm.stringWidth(msg)) / 2, H / 2);
                    return;
                }

                int n   = weekData.size();
                int max = weekData.values().stream().max(Integer::compare).orElse(1);
                // Round max up to a nice grid step
                int step = Math.max(1, (int) Math.ceil((double) max / NUM_GRID));
                int gridMax = step * NUM_GRID;

                Font labelFont = new Font("Segoe UI", Font.PLAIN, 10);
                g2.setFont(labelFont);
                FontMetrics fm = g2.getFontMetrics();

                // ── horizontal grid lines + Y-axis labels ──
                g2.setStroke(new BasicStroke(1f));
                for (int i = 0; i <= NUM_GRID; i++) {
                    int val = i * step;
                    int y   = bottom - (int) ((double) val / gridMax * chartH);
                    // grid line
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawLine(PAD_LEFT, y, PAD_LEFT + chartW, y);
                    // Y label  (e.g. "2 h")
                    g2.setColor(new Color(160, 160, 160));
                    String yLbl = val + "";
                    int lw = fm.stringWidth(yLbl);
                    g2.drawString(yLbl, PAD_LEFT - lw - 4, y + fm.getAscent() / 2 - 1);
                }

                // ── Y-axis title ──
                g2.setColor(new Color(160, 160, 160));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString("pomos", 0, PAD_TOP + 8);

                // ── baseline ──
                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(PAD_LEFT, bottom, PAD_LEFT + chartW, bottom);

                // ── bars ──
                int barGroupW = chartW / n;
                int barW      = (int) (barGroupW * 0.55);
                int bx        = PAD_LEFT + (barGroupW - barW) / 2;
                String today  = LocalDate.now().toString();

                g2.setFont(labelFont);
                fm = g2.getFontMetrics();

                for (Map.Entry<String, Integer> e : weekData.entrySet()) {
                    boolean isToday = e.getKey().equals(today);
                    int barH = (int) ((double) e.getValue() / gridMax * chartH);
                    if (barH < 2 && e.getValue() > 0) barH = 2; // always show a sliver

                    // bar fill
                    g2.setColor(isToday ? UI.POMO_RED : new Color(245, 192, 188));
                    g2.fillRoundRect(bx, bottom - barH, barW, barH, 5, 5);

                    // value label above bar (only if bar has some height)
                    if (e.getValue() > 0) {
                        String vLbl = String.valueOf(e.getValue());
                        int vlx = bx + barW / 2 - fm.stringWidth(vLbl) / 2;
                        g2.setColor(isToday ? UI.POMO_RED : new Color(190, 100, 95));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        g2.drawString(vLbl, vlx, bottom - barH - 3);
                        g2.setFont(labelFont);
                    }

                    // X-axis label (day-of-month)
                    String dayLbl = e.getKey().length() >= 10 ? e.getKey().substring(8) : "";
                    int dlx = bx + barW / 2 - fm.stringWidth(dayLbl) / 2;
                    g2.setColor(isToday ? UI.POMO_RED : new Color(160, 160, 160));
                    g2.setFont(isToday
                            ? new Font("Segoe UI", Font.BOLD, 10)
                            : new Font("Segoe UI", Font.PLAIN, 10));
                    g2.drawString(dayLbl, dlx, bottom + PAD_BOTTOM - 8);

                    bx += barGroupW;
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setAlignmentX(LEFT_ALIGNMENT);
        chartPanel.setPreferredSize(new Dimension(520, 200));
        chartPanel.setMaximumSize(new Dimension(520, 200));

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
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
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
