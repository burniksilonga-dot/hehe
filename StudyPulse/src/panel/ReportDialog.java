package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import dao.StudySessionDAO;
import ui.UI;
import util.SessionContext;

public class ReportDialog extends JDialog {
    private final StudySessionDAO dao = new StudySessionDAO();

    public ReportDialog(Frame parent) {
        super(parent, "Report", true);
        setSize(460, 500);
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(150, 35, 26));
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel title = new JLabel("REPORT");
        title.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        title.setForeground(Color.WHITE);
        JButton cls = new JButton("✕");
        cls.setFont(UI.F_BODY); cls.setForeground(Color.WHITE);
        cls.setBackground(new Color(150, 35, 26));
        cls.setOpaque(true); cls.setContentAreaFilled(true);
        cls.setBorderPainted(false); cls.setFocusPainted(false);
        cls.addActionListener(e -> dispose());
        header.add(title, BorderLayout.WEST);
        header.add(cls,   BorderLayout.EAST);

        // Body
        JPanel body = new JPanel();
        body.setBackground(Color.WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        int uid = SessionContext.getUser().getId();
        Map<String, Integer> week = dao.getWeeklyPomoCount(uid);
        int totalPomos = week.values().stream().mapToInt(Integer::intValue).sum();
        double hrs = totalPomos * 25.0 / 60;

        JLabel aTitle = new JLabel("Activity Summary");
        aTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 16));
        aTitle.setForeground(UI.TEXT_DARK);
        aTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Stats row
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(Color.WHITE);
        stats.setMaximumSize(new Dimension(420, 100));
        stats.setAlignmentX(LEFT_ALIGNMENT);
        stats.add(sCard(String.format("%.1f", hrs), "hours focused", "⏱"));
        stats.add(sCard(String.valueOf(week.size()), "days accessed", "📅"));
        stats.add(sCard(String.valueOf(week.size() > 0 ? 1 : 0), "day streak", "🔥"));

        JLabel cTitle = new JLabel("Focus Hours — This Week");
        cTitle.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        cTitle.setForeground(UI.TEXT_DARK);
        cTitle.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        cTitle.setAlignmentX(LEFT_ALIGNMENT);

        // Bar chart
        JPanel chart = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (week.isEmpty()) {
                    g2.setColor(UI.TEXT_GRAY);
                    g2.setFont(UI.F_BODY);
                    g2.drawString("No data yet — complete a Pomodoro session first!", 10, getHeight() / 2);
                    return;
                }
                int n = week.size();
                int max = week.values().stream().max(Integer::compare).orElse(1);
                int bw = (getWidth() - (n + 1) * 10) / n;
                int bx = 10;
                int bottom = getHeight() - 28;
                g2.setColor(new Color(220, 220, 220));
                g2.drawLine(0, bottom, getWidth(), bottom);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                for (Map.Entry<String, Integer> e : week.entrySet()) {
                    int bh = (int) ((double) e.getValue() / max * (bottom - 10));
                    g2.setColor(new Color(245, 192, 188));
                    g2.fillRoundRect(bx, bottom - bh, bw, bh, 4, 4);
                    // value label on top of bar
                    if (e.getValue() > 0) {
                        g2.setColor(UI.POMO_RED);
                        g2.drawString(String.valueOf(e.getValue()), bx + bw/2 - 4, bottom - bh - 4);
                    }
                    g2.setColor(UI.TEXT_GRAY);
                    String lbl = e.getKey().length() >= 10 ? e.getKey().substring(8) : "";
                    g2.drawString(lbl, bx + bw / 2 - 6, getHeight() - 8);
                    bx += bw + 10;
                }
            }
        };
        chart.setBackground(Color.WHITE);
        chart.setAlignmentX(LEFT_ALIGNMENT);
        chart.setPreferredSize(new Dimension(420, 180));
        chart.setMaximumSize(new Dimension(420, 180));

        body.add(aTitle);
        body.add(Box.createVerticalStrut(12));
        body.add(stats);
        body.add(cTitle);
        body.add(chart);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.setBackground(Color.WHITE);
        scroll.getViewport().setBackground(Color.WHITE);

        root.add(header, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel sCard(String num, String lbl, String ico) {
        JPanel p = new JPanel();
        p.setBackground(new Color(255, 240, 239));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(12, 8, 12, 8));
        JLabel il = new JLabel(ico, SwingConstants.CENTER); il.setFont(new Font("Segoe UI",Font.PLAIN,18)); il.setAlignmentX(CENTER_ALIGNMENT);
        JLabel nl = new JLabel(num, SwingConstants.CENTER); nl.setFont(new Font("Segoe UI",Font.BOLD,22)); nl.setForeground(UI.POMO_RED); nl.setAlignmentX(CENTER_ALIGNMENT);
        JLabel ll = new JLabel(lbl, SwingConstants.CENTER); ll.setFont(UI.F_SMALL); ll.setForeground(new Color(200,100,100)); ll.setAlignmentX(CENTER_ALIGNMENT);
        p.add(il); p.add(nl); p.add(ll);
        return p;
    }
}