package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import dao.TaskDAO;
import model.Task;
import ui.UI;
import util.SessionContext;

public class AccomplishedPanel extends JPanel {
    private final TaskDAO dao = new TaskDAO();
    private JPanel listPanel;
    private JLabel lblCount;
    private JScrollPane scroll;

    public AccomplishedPanel() {
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrap = new JPanel();
        wrap.setBackground(UI.POMO_RED);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 60, 30, 60));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UI.POMO_RED);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 255, 255, 80)));
        hdr.setMaximumSize(new Dimension(500, 36));
        JLabel title = new JLabel("✔  Accomplished Tasks");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        lblCount = new JLabel("0 tasks");
        lblCount.setFont(UI.F_SMALL);
        lblCount.setForeground(new Color(255, 220, 220));
        hdr.add(title,    BorderLayout.WEST);
        hdr.add(lblCount, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setBackground(UI.POMO_RED);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        wrap.add(hdr);
        wrap.add(Box.createVerticalStrut(14));
        wrap.add(listPanel);

        scroll = new JScrollPane(wrap);
        scroll.setBackground(UI.POMO_RED);
        scroll.getViewport().setBackground(UI.POMO_RED);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        if (SessionContext.getUser() == null) return;
        listPanel.removeAll();
        List<Task> done = dao.getByUser(SessionContext.getUser().getId())
            .stream().filter(Task::isCompleted).toList();
        lblCount.setText(done.size() + " task" + (done.size() != 1 ? "s" : ""));

        if (done.isEmpty()) {
            JLabel empty = new JLabel("No completed tasks yet. Keep going! 🎯", SwingConstants.CENTER);
            empty.setFont(UI.F_BODY);
            empty.setForeground(new Color(255, 220, 220));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(50));
            listPanel.add(empty);
        } else {
            for (Task t : done) {
                JPanel card = new JPanel(new BorderLayout(12, 0));
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 4, 0, 0, new Color(76, 139, 106)),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));
                card.setMaximumSize(new Dimension(500, 56));

                JLabel chk = new JLabel("✔");
                chk.setFont(new Font("Segoe UI", Font.BOLD, 16));
                chk.setForeground(new Color(76, 139, 106));

                JPanel col = new JPanel();
                col.setOpaque(false);
                col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));
                JLabel name = new JLabel("<html><strike>" + t.getName() + "</strike></html>");
                name.setFont(UI.F_BODY); name.setForeground(UI.TEXT_GRAY);
                JLabel meta = new JLabel(t.getDonePomos() + "/" + t.getEstPomos() + " pomos"
                    + (t.getSubject().isEmpty() ? "" : " · " + t.getSubject()));
                meta.setFont(UI.F_SMALL); meta.setForeground(UI.TEXT_GRAY);
                col.add(name); col.add(meta);

                card.add(chk, BorderLayout.WEST);
                card.add(col, BorderLayout.CENTER);
                listPanel.add(card);
                listPanel.add(Box.createVerticalStrut(6));
            }
        }
        listPanel.revalidate();
        listPanel.repaint();
    }
}
