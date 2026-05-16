package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import dao.NoteDAO;
import model.Note;
import ui.UI;
import util.SessionContext;

public class CalendarPanel extends JPanel {
    private final NoteDAO dao = new NoteDAO();
    private int year, month;
    private JPanel    gridPanel;
    private JLabel    lblMonthYear;
    private JPanel    noteArea;
    private JPanel    pastNoteArea;      // read-only view for past dates
    private JTextArea noteTA;
    private JTextArea pastNoteTA;        // read-only text area
    private JLabel    noteDateLabel;
    private JLabel    pastNoteDateLabel;
    private LocalDate selectedDate;
    private JScrollPane mainScroll;
    private JPanel    mainWrap;

    public CalendarPanel() {
        LocalDate now = LocalDate.now();
        year  = now.getYear();
        month = now.getMonthValue();
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        mainWrap = new JPanel();
        mainWrap.setBackground(UI.POMO_RED);
        mainWrap.setLayout(new BoxLayout(mainWrap, BoxLayout.Y_AXIS));
        mainWrap.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        // ── Month navigation header ───────────────────────────────────────
        JPanel calHdr = new JPanel(new BorderLayout());
        calHdr.setBackground(UI.POMO_RED);
        calHdr.setMaximumSize(new Dimension(520, 40));

        JButton prev = navBtn("‹");
        JButton next = navBtn("›");
        prev.addActionListener(e -> { adjustMonth(-1); buildGrid(); });
        next.addActionListener(e -> { adjustMonth(1);  buildGrid(); });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 17));
        lblMonthYear.setForeground(Color.WHITE);

        calHdr.add(prev,         BorderLayout.WEST);
        calHdr.add(lblMonthYear, BorderLayout.CENTER);
        calHdr.add(next,         BorderLayout.EAST);

        // ── Day-of-week row ───────────────────────────────────────────────
        JPanel dayNames = new JPanel(new GridLayout(1, 7, 4, 0));
        dayNames.setBackground(UI.POMO_RED);
        dayNames.setMaximumSize(new Dimension(520, 24));
        for (String d : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"}) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(new Font("Segoe UI", Font.BOLD, 11));
            l.setForeground(new Color(255, 210, 210));
            dayNames.add(l);
        }

        // ── Calendar grid ─────────────────────────────────────────────────
        gridPanel = new JPanel(new GridLayout(0, 7, 4, 4));
        gridPanel.setBackground(UI.POMO_RED);
        gridPanel.setMaximumSize(new Dimension(520, 280));

        // ── Editable note area (today / future) ───────────────────────────
        noteArea = new JPanel();
        noteArea.setBackground(new Color(160, 45, 36));
        noteArea.setLayout(new BoxLayout(noteArea, BoxLayout.Y_AXIS));
        noteArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        noteArea.setMaximumSize(new Dimension(520, 220));
        noteArea.setVisible(false);

        noteDateLabel = new JLabel("Notes for ...");
        noteDateLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        noteDateLabel.setForeground(Color.WHITE);
        noteDateLabel.setAlignmentX(LEFT_ALIGNMENT);

        noteTA = new JTextArea(4, 30);
        noteTA.setFont(UI.F_BODY);
        noteTA.setLineWrap(true);
        noteTA.setWrapStyleWord(true);
        noteTA.setBackground(new Color(140, 35, 28));
        noteTA.setForeground(Color.WHITE);
        noteTA.setCaretColor(Color.WHITE);
        noteTA.setOpaque(true);
        noteTA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JScrollPane taScroll = new JScrollPane(noteTA);
        taScroll.setMaximumSize(new Dimension(520, 110));
        taScroll.setAlignmentX(LEFT_ALIGNMENT);
        taScroll.setBackground(new Color(140, 35, 28));
        taScroll.getViewport().setBackground(new Color(140, 35, 28));
        taScroll.setBorder(null);

        JButton saveNote = new JButton("Save Note");
        saveNote.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        saveNote.setBackground(new Color(80, 80, 80));
        saveNote.setForeground(Color.WHITE);
        saveNote.setOpaque(true);
        saveNote.setContentAreaFilled(true);
        saveNote.setBorderPainted(false);
        saveNote.setFocusPainted(false);
        saveNote.setAlignmentX(LEFT_ALIGNMENT);
        saveNote.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { saveNote.setBackground(new Color(60, 60, 60)); }
            public void mouseExited(MouseEvent e)  { saveNote.setBackground(new Color(80, 80, 80)); }
        });
        saveNote.addActionListener(e -> saveCalNote());

        noteArea.add(noteDateLabel);
        noteArea.add(Box.createVerticalStrut(8));
        noteArea.add(taScroll);
        noteArea.add(Box.createVerticalStrut(8));
        noteArea.add(saveNote);

        // ── Read-only past note area ───────────────────────────────────────
        pastNoteArea = new JPanel();
        pastNoteArea.setBackground(new Color(120, 35, 28));   // darker = archived feel
        pastNoteArea.setLayout(new BoxLayout(pastNoteArea, BoxLayout.Y_AXIS));
        pastNoteArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        pastNoteArea.setMaximumSize(new Dimension(520, 220));
        pastNoteArea.setVisible(false);

        pastNoteDateLabel = new JLabel("📁  Archived — ...");
        pastNoteDateLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        pastNoteDateLabel.setForeground(new Color(255, 200, 195));
        pastNoteDateLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Hint label
        JLabel archiveHint = new JLabel("Past dates are read-only");
        archiveHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        archiveHint.setForeground(new Color(220, 160, 155));
        archiveHint.setAlignmentX(LEFT_ALIGNMENT);

        pastNoteTA = new JTextArea(4, 30);
        pastNoteTA.setFont(UI.F_BODY);
        pastNoteTA.setLineWrap(true);
        pastNoteTA.setWrapStyleWord(true);
        pastNoteTA.setEditable(false);                         // READ-ONLY
        pastNoteTA.setBackground(new Color(105, 28, 22));
        pastNoteTA.setForeground(new Color(240, 210, 208));
        pastNoteTA.setDisabledTextColor(new Color(240, 210, 208));
        pastNoteTA.setOpaque(true);
        pastNoteTA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 40)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JScrollPane pastScroll = new JScrollPane(pastNoteTA);
        pastScroll.setMaximumSize(new Dimension(520, 110));
        pastScroll.setAlignmentX(LEFT_ALIGNMENT);
        pastScroll.setBackground(new Color(105, 28, 22));
        pastScroll.getViewport().setBackground(new Color(105, 28, 22));
        pastScroll.setBorder(null);

        // "No note" placeholder shown inside the read-only area
        JLabel noNoteHint = new JLabel("No note was saved for this day.");
        noNoteHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        noNoteHint.setForeground(new Color(200, 150, 148));
        noNoteHint.setAlignmentX(LEFT_ALIGNMENT);

        pastNoteArea.add(pastNoteDateLabel);
        pastNoteArea.add(Box.createVerticalStrut(4));
        pastNoteArea.add(archiveHint);
        pastNoteArea.add(Box.createVerticalStrut(8));
        pastNoteArea.add(pastScroll);

        // ── Assemble ──────────────────────────────────────────────────────
        mainWrap.add(calHdr);
        mainWrap.add(Box.createVerticalStrut(8));
        mainWrap.add(dayNames);
        mainWrap.add(Box.createVerticalStrut(4));
        mainWrap.add(gridPanel);
        mainWrap.add(Box.createVerticalStrut(14));
        mainWrap.add(noteArea);
        mainWrap.add(pastNoteArea);

        mainScroll = new JScrollPane(mainWrap);
        mainScroll.setBackground(UI.POMO_RED);
        mainScroll.getViewport().setBackground(UI.POMO_RED);
        mainScroll.setBorder(null);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScroll, BorderLayout.CENTER);
        buildGrid();
    }

    private void buildGrid() {
        gridPanel.removeAll();
        YearMonth ym = YearMonth.of(year, month);
        lblMonthYear.setText(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + year);

        int       firstDay    = LocalDate.of(year, month, 1).getDayOfWeek().getValue() % 7;
        int       daysInMonth = ym.lengthOfMonth();
        LocalDate today       = LocalDate.now();

        for (int i = 0; i < firstDay; i++) {
            JPanel empty = new JPanel();
            empty.setBackground(new Color(160, 48, 38));
            gridPanel.add(empty);
        }

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date   = LocalDate.of(year, month, d);
            boolean isToday  = date.equals(today);
            boolean isSel    = date.equals(selectedDate);
            boolean isPast   = date.isBefore(today);   // ← past-date flag

            JButton btn = new JButton(String.valueOf(d));
            btn.setPreferredSize(new Dimension(60, 40));

            if (isPast) {
                // ── past: muted / greyed-out appearance ──
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btn.setBackground(new Color(140, 42, 33));       // darker, desaturated
                btn.setForeground(new Color(210, 165, 162));      // dimmed text
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                // Hover: slightly lighter but still muted
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(150, 46, 36)); }
                    public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(140, 42, 33)); }
                });
            } else if (isToday || isSel) {
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setBackground(Color.WHITE);
                btn.setForeground(UI.POMO_RED);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(240, 240, 240)); }
                    public void mouseExited(MouseEvent e)  { btn.setBackground(Color.WHITE); }
                });
            } else {
                // future
                btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                btn.setBackground(new Color(175, 50, 40));
                btn.setForeground(Color.WHITE);
                btn.setOpaque(true);
                btn.setContentAreaFilled(true);
                btn.setBorderPainted(false);
                btn.setFocusPainted(false);
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(155, 38, 30)); }
                    public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(175, 50, 40)); }
                });
            }

            final LocalDate fd      = date;
            final boolean   fIsPast = isPast;
            btn.addActionListener(e -> {
                if (fIsPast) {
                    selectPastDay(fd);   // read-only view
                } else {
                    selectDay(fd);       // editable
                }
            });
            gridPanel.add(btn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    /** Show editable note area for today / future dates. */
    private void selectDay(LocalDate date) {
        selectedDate = date;
        noteDateLabel.setText("Notes for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        if (SessionContext.getUser() != null) {
            String existing = dao.getNoteForDate(SessionContext.getUser().getId(), date);
            noteTA.setText(existing != null ? existing : "");
        }
        noteArea.setVisible(true);
        pastNoteArea.setVisible(false);
        buildGrid();
        mainWrap.revalidate();
        mainWrap.repaint();
    }

    /** Show read-only archived note view for past dates. */
    private void selectPastDay(LocalDate date) {
        selectedDate = date;
        pastNoteDateLabel.setText("📁  Archived — "
                + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        String existing = null;
        if (SessionContext.getUser() != null) {
            existing = dao.getNoteForDate(SessionContext.getUser().getId(), date);
        }
        if (existing != null && !existing.isBlank()) {
            pastNoteTA.setText(existing);
        } else {
            pastNoteTA.setText("No note was saved for this day.");
        }
        noteArea.setVisible(false);
        pastNoteArea.setVisible(true);
        buildGrid();
        mainWrap.revalidate();
        mainWrap.repaint();
    }

    private void saveCalNote() {
        if (selectedDate == null || SessionContext.getUser() == null) return;
        Note n = new Note(SessionContext.getUser().getId(), selectedDate, noteTA.getText().trim());
        dao.saveNote(n);
        UI.info(this, "Note saved for " + selectedDate.format(DateTimeFormatter.ofPattern("MMMM d")));
        buildGrid();
    }

    private void adjustMonth(int delta) {
        month += delta;
        if (month < 1)  { month = 12; year--; }
        if (month > 12) { month = 1;  year++; }
    }

    private JButton navBtn(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(175, 50, 40));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(36, 32));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(150, 35, 26)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(new Color(175, 50, 40)); }
        });
        return b;
    }

    public void refresh() { buildGrid(); }
}
