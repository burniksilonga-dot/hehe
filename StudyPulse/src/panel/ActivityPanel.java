package panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
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
    private JLabel lblHours, lblDays, lblStreak, cTitle;
    private JComboBox<String> timeFilter;
    private Map<String, Integer> chartData = new LinkedHashMap<>();
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
        card.setAlignmentX(Component.CENTER_ALIGNMENT); 

        JLabel title = new JLabel("Activity Summary");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(UI.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 3 stat cards in a row
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(Color.WHITE);
        stats.setMaximumSize(new Dimension(520, 140));
        stats.setPreferredSize(new Dimension(520, 140));
        stats.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblHours  = statNum("0.0");
        lblDays   = statNum("0");
        lblStreak = statNum("0");
        
        stats.add(statCard(lblHours,  "hours focused", "⏱"));
        stats.add(statCard(lblDays,   "days accessed", "📅"));
        stats.add(statCard(lblStreak, "day streak",    "🔥"));
        
        // ── Chart Header (Title + Dropdown) ──
        JPanel chartHdr = new JPanel(new BorderLayout());
        chartHdr.setBackground(Color.WHITE);
        chartHdr.setMaximumSize(new Dimension(520, 40));
        chartHdr.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        
        cTitle = new JLabel("Focus Hours");
        cTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cTitle.setForeground(UI.TEXT_DARK);
        
        timeFilter = new JComboBox<>(new String[]{"This Week", "This Month", "This Year"});
        timeFilter.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        timeFilter.setBackground(Color.WHITE);
        timeFilter.setFocusable(false);
        timeFilter.addActionListener(e -> refresh());
        
        chartHdr.add(cTitle, BorderLayout.WEST);
        chartHdr.add(timeFilter, BorderLayout.EAST);

        // ── Improved chart panel ──────────────────────────────────────────
        chartPanel = new JPanel() {
            private static final int PAD_LEFT   = 42;  
            private static final int PAD_BOTTOM = 28;  
            private static final int PAD_TOP    = 16;
            private static final int PAD_RIGHT  = 10;
            private static final int NUM_GRID   = 4;   

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
                int bottom = PAD_TOP + chartH;   

                if (chartData.isEmpty()) {
                    g2.setColor(UI.TEXT_GRAY);
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    String msg = "No sessions recorded yet. Start a Pomodoro to track!";
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(msg, PAD_LEFT + (chartW - fm.stringWidth(msg)) / 2, H / 2);
                    return;
                }

                int n   = chartData.size();
                int max = chartData.values().stream().max(Integer::compare).orElse(1);
                int step = Math.max(1, (int) Math.ceil((double) max / NUM_GRID));
                int gridMax = step * NUM_GRID;

                Font labelFont = new Font("Segoe UI", Font.PLAIN, 10);
                g2.setFont(labelFont);
                FontMetrics fm = g2.getFontMetrics();

                g2.setStroke(new BasicStroke(1f));
                for (int i = 0; i <= NUM_GRID; i++) {
                    int val = i * step;
                    int y   = bottom - (int) ((double) val / gridMax * chartH);
                    g2.setColor(new Color(230, 230, 230));
                    g2.drawLine(PAD_LEFT, y, PAD_LEFT + chartW, y);
                    g2.setColor(new Color(160, 160, 160));
                    String yLbl = val + "";
                    int lw = fm.stringWidth(yLbl);
                    g2.drawString(yLbl, PAD_LEFT - lw - 4, y + fm.getAscent() / 2 - 1);
                }

                g2.setColor(new Color(160, 160, 160));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
                g2.drawString("pomos", 0, PAD_TOP + 8);

                g2.setColor(new Color(200, 200, 200));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(PAD_LEFT, bottom, PAD_LEFT + chartW, bottom);

                int barGroupW = chartW / n;
                int barW      = Math.max(4, (int) (barGroupW * 0.65)); // Ensure bars don't disappear
                int bx        = PAD_LEFT + (barGroupW - barW) / 2;
                String today  = LocalDate.now().toString();
                String thisMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

                g2.setFont(labelFont);
                fm = g2.getFontMetrics();
                
                String filter = (String) timeFilter.getSelectedItem();
                boolean isMonthView = "This Month".equals(filter);
                boolean isYearView = "This Year".equals(filter);

                int i = 0;
                for (Map.Entry<String, Integer> e : chartData.entrySet()) {
                    boolean isCurrent = isYearView ? e.getKey().equals(thisMonth) : e.getKey().equals(today);
                    int barH = (int) ((double) e.getValue() / gridMax * chartH);
                    if (barH < 2 && e.getValue() > 0) barH = 2; 

                    Color primaryColor = new Color(164, 63, 63); // Soft dark red theme
                    g2.setColor(isCurrent ? primaryColor : new Color(245, 192, 188));
                    g2.fillRoundRect(bx, bottom - barH, barW, barH, 4, 4);

                    // Draw numbers on top of bars (skip if crowding on Month view)
                    if (e.getValue() > 0 && (!isMonthView || n <= 15 || e.getValue() > max/3)) {
                        String vLbl = String.valueOf(e.getValue());
                        int vlx = bx + barW / 2 - fm.stringWidth(vLbl) / 2;
                        g2.setColor(isCurrent ? primaryColor : new Color(190, 100, 95));
                        g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                        g2.drawString(vLbl, vlx, bottom - barH - 3);
                    }

                    // Format X-Axis Labels
                    String label = "";
                    if (isYearView) {
                        // Convert YYYY-MM to Jan, Feb, etc.
                        if (e.getKey().length() >= 7) {
                            String m = e.getKey().substring(5, 7);
                            label = switch (m) {
                                case "01"->"Jan"; case "02"->"Feb"; case "03"->"Mar"; case "04"->"Apr";
                                case "05"->"May"; case "06"->"Jun"; case "07"->"Jul"; case "08"->"Aug";
                                case "09"->"Sep"; case "10"->"Oct"; case "11"->"Nov"; case "12"->"Dec";
                                default->m;
                            };
                        }
                    } else {
                        // Extract Day DD
                        label = e.getKey().length() >= 10 ? e.getKey().substring(8) : "";
                    }

                    // Draw X-axis label (space them out on Month view so they don't overlap)
                    if (!isMonthView || i % 3 == 0 || isCurrent || i == n - 1) {
                        int dlx = bx + barW / 2 - fm.stringWidth(label) / 2;
                        g2.setColor(isCurrent ? primaryColor : new Color(160, 160, 160));
                        g2.setFont(isCurrent ? new Font("Segoe UI", Font.BOLD, 10) : new Font("Segoe UI", Font.PLAIN, 10));
                        g2.drawString(label, dlx, bottom + PAD_BOTTOM - 8);
                    }

                    bx += barGroupW;
                    i++;
                }
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chartPanel.setPreferredSize(new Dimension(520, 200));
        chartPanel.setMaximumSize(new Dimension(520, 200));

        card.add(title);
        card.add(Box.createVerticalStrut(12));
        card.add(stats);
        card.add(chartHdr);
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
        
        String filter = (String) timeFilter.getSelectedItem();
        
        // Fetch different data based on dropdown selection
        if ("This Year".equals(filter)) {
            chartData = dao.getYearlyPomoCount(uid);
            cTitle.setText("Focus Hours — This Year");
        } else if ("This Month".equals(filter)) {
            chartData = dao.getMonthlyPomoCount(uid);
            cTitle.setText("Focus Hours — This Month");
        } else {
            chartData = dao.getWeeklyPomoCount(uid);
            cTitle.setText("Focus Hours — This Week");
        }

        // Calculate total Pomodoros
        int totalPomos = chartData.values().stream().mapToInt(Integer::intValue).sum();
        
        // Calculate ACTIVE days/months (only count items where value is greater than 0)
        int activeCount = (int) chartData.values().stream().filter(v -> v > 0).count();

        // Update the top UI labels
        lblHours.setText(String.format("%.1f", totalPomos * 25.0 / 60)); // Assumes 25min per pomo
        lblDays.setText(String.valueOf(activeCount)); // FIXED: Only counts days with work done
        lblStreak.setText(String.valueOf(calcStreak())); // FIXED: Checks > 0
        
        chartPanel.repaint();
    }

    private int calcStreak() {
        // Always calculate streak using daily data, not monthly/yearly groupings
        Map<String, Integer> recent = dao.getWeeklyPomoCount(SessionContext.getUser().getId());
        int s = 0;
        LocalDate d = LocalDate.now();
        
        // FIXED: Make sure the day actually exists AND has more than 0 pomodoros
        while (recent.containsKey(d.toString()) && recent.get(d.toString()) > 0) { 
            s++; 
            d = d.minusDays(1); 
        }
        return s;
    }

    private JPanel statCard(JLabel numLbl, String lbl, String icon) {
        JPanel p = new JPanel();
        p.setBackground(new Color(255, 240, 239));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24)); 
        ico.setAlignmentX(Component.CENTER_ALIGNMENT);
        ico.setBorder(BorderFactory.createEmptyBorder(12, 0, 5, 0));
        
        numLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel l = new JLabel(lbl, SwingConstants.CENTER);
        l.setFont(UI.F_SMALL);
        l.setForeground(new Color(200, 100, 100));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        p.add(Box.createVerticalGlue()); 
        p.add(ico); 
        p.add(numLbl); 
        p.add(l);
        p.add(Box.createVerticalGlue());
        
        return p;
    }

    private JLabel statNum(String t) {
        JLabel l = new JLabel(t, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 28));
        l.setForeground(new Color(164, 63, 63));
        return l;
    }
}
