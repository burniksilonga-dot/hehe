package panel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import dao.SubjectDAO;
import model.Activity;
import model.Subject;
import ui.UI;
import util.SessionContext;

public class SubjectsPanel extends JPanel {
    private final SubjectDAO dao = new SubjectDAO();
    private JPanel listPanel;
    private JScrollPane scroll;

    public SubjectsPanel() {
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrap = new JPanel();
        wrap.setBackground(UI.POMO_RED);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));
        
        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UI.POMO_RED);
        hdr.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255, 255, 255, 80)));
        JLabel title = new JLabel("📚  Subjects & Deadlines");
        title.setFont(new Font("Segoe UI Emojis", Font.PLAIN, 16));
        title.setForeground(Color.WHITE);

        JButton btnAdd = new JButton("+ Add Subject");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setBackground(Color.WHITE);
        btnAdd.setForeground(UI.POMO_RED);
        btnAdd.setOpaque(true);
        btnAdd.setContentAreaFilled(true);
        btnAdd.setBorderPainted(false);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setBorder(BorderFactory.createEmptyBorder(5, 12, 5, 12));
        btnAdd.addActionListener(e -> showAddSubjectDialog());
        btnAdd.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnAdd.setBackground(new Color(240,240,240)); }
            public void mouseExited(MouseEvent e)  { btnAdd.setBackground(Color.WHITE); }
        });

        hdr.add(title,  BorderLayout.WEST);
        hdr.add(btnAdd, BorderLayout.EAST);

        listPanel = new JPanel();
        listPanel.setBackground(UI.POMO_RED);
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));

        JPanel content = new JPanel();
        content.setBackground(UI.POMO_RED);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(hdr);
        content.add(Box.createVerticalStrut(14));
        content.add(listPanel);

        wrap.add(content, BorderLayout.NORTH);

        scroll = new JScrollPane(wrap);
        scroll.setBackground(UI.POMO_RED);
        scroll.getViewport().setBackground(UI.POMO_RED);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(12);

        add(scroll, BorderLayout.CENTER);
    }

    public void refresh() {
        if (SessionContext.getUser() == null) return;
        listPanel.removeAll();

        int uid = SessionContext.getUser().getId();
        List<Subject> subjects = dao.getByUser(uid);
        List<Activity> allActivities = new ArrayList<>();

        // Priority summary: nearest deadlines across all subjects
        List<Activity> allPending = new ArrayList<>();
        for (Subject s : subjects) {
            List<Activity> acts = dao.getActivitiesBySubject(s.getId());

            allActivities.addAll(acts); // collect ALL activities

            allPending.addAll(
                acts.stream().filter(a -> !a.isCompleted()).toList()
            );
        }
        allPending.sort(Comparator.comparing(a -> a.getDeadline() != null ? a.getDeadline() : LocalDate.MAX));

        if (!allPending.isEmpty()) {
            JPanel pCard = sectionCard();
            JLabel pTitle = new JLabel("⚡  Nearest Deadlines");
            pTitle.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
            pTitle.setForeground(UI.POMO_RED);
            pCard.add(pTitle);
            pCard.add(Box.createVerticalStrut(8));

            for (int i = 0; i < Math.min(3, allPending.size()); i++) {
                Activity a = allPending.get(i);
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

                JLabel num = new JLabel("#" + (i + 1));
                num.setFont(new Font("Segoe UI", Font.BOLD, 11));
                num.setForeground(UI.POMO_RED);
                num.setPreferredSize(new Dimension(26, 18));

                JLabel name = new JLabel(a.getName());
                name.setFont(UI.F_BODY);

                long days = a.daysUntil();
                String dlText = days < 0 ? "Overdue" : days == 0 ? "Today" : "In " + days + "d";
                Color dlColor = days <= 0 ? new Color(200,50,50) : days <= 3 ? new Color(200,100,0) : new Color(30,120,60);
                JLabel dl = new JLabel(dlText);
                dl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                dl.setForeground(dlColor);

                row.add(num,  BorderLayout.WEST);
                row.add(name, BorderLayout.CENTER);
                row.add(dl,   BorderLayout.EAST);
                pCard.add(row);
            }
            pCard.setMaximumSize(new Dimension(580, Integer.MAX_VALUE));
            listPanel.add(pCard);
            listPanel.add(Box.createVerticalStrut(12));
        }

        if (subjects.isEmpty()) {
            JLabel empty = new JLabel("No subjects yet. Click '+ Add Subject' to get started!", SwingConstants.CENTER);
            empty.setFont(UI.F_BODY);
            empty.setForeground(new Color(255, 220, 220));
            empty.setAlignmentX(CENTER_ALIGNMENT);
            listPanel.add(Box.createVerticalStrut(40));
            listPanel.add(empty);
        } else {
            for (Subject s : subjects) {
                listPanel.add(buildSubjectCard(s));
                listPanel.add(Box.createVerticalStrut(10));
            }
        }
        checkAllCompleted(allActivities);

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel buildSubjectCard(Subject s) {
        JPanel card = sectionCard();
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        Color subColor;
        try { subColor = Color.decode(s.getColor()); }
        catch (Exception ex) { subColor = UI.POMO_RED; }

        // Subject header row
        JPanel hdr = new JPanel(new BorderLayout(8, 0));
        hdr.setBackground(Color.WHITE);

        JPanel dotName = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        dotName.setBackground(Color.WHITE);
        JPanel dot = new JPanel();
        dot.setBackground(subColor);
        dot.setPreferredSize(new Dimension(12, 12));
        JLabel nameL = new JLabel(s.getName());
        nameL.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dotName.add(dot); dotName.add(nameL);

        // Right side of Header (Add Activity & Delete Subject)
        JPanel rightHdr = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        rightHdr.setBackground(Color.WHITE);

        JButton addAct = new JButton("+ Activity");
        addAct.setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        addAct.setBackground(subColor);
        addAct.setForeground(Color.WHITE);
        addAct.setOpaque(true);
        addAct.setContentAreaFilled(true);
        addAct.setBorderPainted(false);
        addAct.setFocusPainted(false);
        addAct.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addAct.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        addAct.addActionListener(e -> showAddActivityDialog(s));

        JButton delSub = new JButton("🗑"); 
        delSub.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        delSub.setBackground(new Color(220, 50, 50));
        delSub.setForeground(Color.WHITE);
        delSub.setOpaque(true);
        delSub.setContentAreaFilled(true);
        delSub.setBorderPainted(false);
        delSub.setFocusPainted(false);
        delSub.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        delSub.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        delSub.setToolTipText("Delete Subject");
        delSub.addActionListener(e -> deleteSubjectWithPrompt(s));

        rightHdr.add(addAct);
        rightHdr.add(delSub);

        hdr.add(dotName, BorderLayout.WEST);
        hdr.add(rightHdr, BorderLayout.EAST);
        
        card.add(hdr);
        card.add(Box.createVerticalStrut(8));
        card.add(new JSeparator());
        card.add(Box.createVerticalStrut(6));

        List<Activity> acts = dao.getActivitiesBySubject(s.getId());
        if (acts.isEmpty()) {
            JLabel emp = new JLabel("No activities yet — click '+ Activity' to add one.");
            emp.setFont(UI.F_SMALL);
            emp.setForeground(UI.TEXT_GRAY);
            card.add(emp);
        } else {
            for (Activity a : acts) {
                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

                JPanel chk = buildActCheck(a, subColor);

                JLabel aName = new JLabel(a.isCompleted()
                    ? "<html><strike>" + a.getName() + "</strike></html>" : a.getName());
                aName.setFont(UI.F_BODY);
                aName.setForeground(a.isCompleted() ? UI.TEXT_GRAY : UI.TEXT_DARK);

                JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                right.setBackground(Color.WHITE);
                
                if (a.getDeadline() != null) {
                    long days = a.daysUntil();
                    String dt = a.isCompleted() ? "Done"
                        : days < 0 ? "Overdue" : days == 0 ? "Today" : "In " + days + "d";
                    Color dc = a.isCompleted() ? UI.TEXT_GRAY
                        : days <= 0 ? new Color(200,50,50) : days <= 3 ? new Color(200,100,0) : new Color(30,120,60);
                    JLabel dl = new JLabel(a.getDeadline().format(DateTimeFormatter.ofPattern("MMM d")) + " · " + dt);
                    dl.setFont(UI.F_SMALL);
                    dl.setForeground(dc);
                    right.add(dl);
                }

                // Delete Activity Button
                JButton delAct = new JButton("✕");
                delAct.setFont(new Font("Segoe UI", Font.BOLD, 12));
                delAct.setForeground(new Color(200, 80, 80));
                delAct.setContentAreaFilled(false);
                delAct.setBorderPainted(false);
                delAct.setFocusPainted(false);
                delAct.setMargin(new Insets(0, 4, 0, 4));
                delAct.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                delAct.setToolTipText("Delete Activity");
                delAct.addActionListener(e -> deleteActivityWithPrompt(a));
                
                right.add(delAct);

                row.add(chk,   BorderLayout.WEST);
                row.add(aName, BorderLayout.CENTER);
                row.add(right, BorderLayout.EAST);
                card.add(row);
            }
        }
        return card;
    }

    private void deleteSubjectWithPrompt(Subject s) {
        int res = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the subject '" + s.getName() + "' and all of its activities?\nThis cannot be undone.",
            "Confirm Delete Subject",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (res == JOptionPane.YES_OPTION) {
            dao.deleteSubject(s.getId());
            refresh();
        }
    }

    private void deleteActivityWithPrompt(Activity a) {
        int res = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete the activity '" + a.getName() + "'?",
            "Confirm Delete Activity",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (res == JOptionPane.YES_OPTION) {
            dao.deleteActivity(a.getId());
            refresh();
        }
    }

    private JPanel buildActCheck(Activity a, Color color) {
        JPanel p = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (a.isCompleted()) {
                    g2.setColor(new Color(76, 139, 106));
                    g2.fillOval(2, 2, 18, 18);
                    g2.setColor(Color.WHITE);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(5, 11, 9, 15); g2.drawLine(9, 15, 16, 7);
                } else {
                    g2.setColor(color);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawOval(2, 2, 18, 18);
                }
            }
        };
        p.setBackground(Color.WHITE);
        p.setPreferredSize(new Dimension(24, 24));
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        p.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                dao.setActivityDone(a.getId(), !a.isCompleted());
                refresh();
            }
        });
        return p;
    }

    private void showAddSubjectDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        gc.insets = new Insets(4,0,4,0); gc.gridx=0;

        JTextField tfName = UI.textField(20);
        String[] colors = {"#6366F1","#EC4899","#F59E0B","#10B981","#3B82F6","#EF4444","#8B5CF6","#F97316"};
        JComboBox<String> cbColor = new JComboBox<>(colors);
        cbColor.setFont(UI.F_BODY);

        gc.gridy=0; panel.add(new JLabel("Subject Name:"), gc);
        gc.gridy=1; panel.add(tfName, gc);
        gc.gridy=2; panel.add(new JLabel("Color:"), gc);
        gc.gridy=3; panel.add(cbColor, gc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String n = tfName.getText().trim();
            if (n.isEmpty()) { UI.error(this, "Subject name is required."); return; }
            Subject s = new Subject(SessionContext.getUser().getId(), n, (String) cbColor.getSelectedItem());
            dao.addSubject(s);
            refresh();
        }
    }

    private void showAddActivityDialog(Subject s) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1.0;
        gc.insets = new Insets(4,0,4,0); gc.gridx=0;

        JTextField tfName = UI.textField(20);
        JTextField tfDate = UI.textField(12);
        tfDate.setText(LocalDate.now().plusDays(7).toString());
        tfDate.setToolTipText("Format: YYYY-MM-DD");

        gc.gridy=0; panel.add(new JLabel("Activity Name:"), gc);
        gc.gridy=1; panel.add(tfName, gc);
        gc.gridy=2; panel.add(new JLabel("Deadline (YYYY-MM-DD):"), gc);
        gc.gridy=3; panel.add(tfDate, gc);

        int res = JOptionPane.showConfirmDialog(this, panel, "Add Activity to " + s.getName(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String n = tfName.getText().trim();
            if (n.isEmpty()) { UI.error(this, "Activity name is required."); return; }
            LocalDate dl = null;
            try { dl = LocalDate.parse(tfDate.getText().trim()); } catch (Exception ignored) {}
            Activity a = new Activity(s.getId(), SessionContext.getUser().getId(), n, dl);
            dao.addActivity(a);
            refresh();
        }
    }
    
    private void showCelebrationBanner() {
        JLabel banner = new JLabel("🎉 All tasks completed! Great job!", SwingConstants.CENTER);
        banner.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        banner.setForeground(Color.WHITE);
        banner.setBackground(new Color(76,175,80));
        banner.setOpaque(true);
        banner.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        add(banner, BorderLayout.NORTH);
        revalidate();

        new javax.swing.Timer(4000, e -> {
            remove(banner);
            revalidate();
            repaint();
        }).start();
    }
    
    private void checkAllCompleted(List<Activity> acts) {
        boolean allDone = acts.stream().allMatch(Activity::isCompleted);

        if (allDone && !acts.isEmpty()) {
            showCelebrationBanner();
        }
    }

    private JPanel sectionCard() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UI.BORDER_CLR),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        return p;
    }
}