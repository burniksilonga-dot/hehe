package panel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
    
    // Layout variables to prevent jumping
    private JPanel     bottomPanel;
    private CardLayout bottomLayout;
    
    private JPanel    noteArea;
    private JPanel    pastNoteArea;      
    private JTextArea noteTA;
    private JTextArea pastNoteTA;        
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
        noteArea.setBackground(new Color(164, 63, 63)); // Matched to soft red accent
        noteArea.setLayout(new BoxLayout(noteArea, BoxLayout.Y_AXIS));
        noteArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        noteDateLabel = new JLabel("Notes for ...");
        noteDateLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        noteDateLabel.setForeground(Color.WHITE);
        noteDateLabel.setAlignmentX(LEFT_ALIGNMENT);

        noteTA = new JTextArea(4, 30);
        noteTA.setFont(UI.F_BODY);
        noteTA.setLineWrap(true);
        noteTA.setWrapStyleWord(true);
        noteTA.setBackground(new Color(145, 50, 50)); // Deep soft red
        noteTA.setForeground(Color.WHITE);
        noteTA.setCaretColor(Color.WHITE);
        noteTA.setOpaque(true);
        noteTA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 60)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JScrollPane taScroll = new JScrollPane(noteTA);
        taScroll.setAlignmentX(LEFT_ALIGNMENT);
        taScroll.setBackground(new Color(145, 50, 50));
        taScroll.getViewport().setBackground(new Color(145, 50, 50));
        taScroll.setBorder(null);

        JButton saveNote = new JButton("Save Note");
        saveNote.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        saveNote.setBackground(new Color(120, 40, 40));
        saveNote.setForeground(Color.WHITE);
        saveNote.setOpaque(true);
        saveNote.setContentAreaFilled(true);
        saveNote.setBorderPainted(false);
        saveNote.setFocusPainted(false);
        saveNote.setAlignmentX(LEFT_ALIGNMENT);
        saveNote.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { saveNote.setBackground(new Color(100, 30, 30)); }
            public void mouseExited(MouseEvent e)  { saveNote.setBackground(new Color(120, 40, 40)); }
        });
        saveNote.addActionListener(e -> saveCalNote());

        noteArea.add(noteDateLabel);
        noteArea.add(Box.createVerticalStrut(8));
        noteArea.add(taScroll);
        noteArea.add(Box.createVerticalStrut(8));
        noteArea.add(saveNote);

        // ── Read-only past note area ───────────────────────────────────────
        pastNoteArea = new JPanel();
        pastNoteArea.setBackground(new Color(150, 55, 55)); // Muted soft red  
        pastNoteArea.setLayout(new BoxLayout(pastNoteArea, BoxLayout.Y_AXIS));
        pastNoteArea.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));

        pastNoteDateLabel = new JLabel("📁  Archived — ...");
        pastNoteDateLabel.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 13));
        pastNoteDateLabel.setForeground(new Color(255, 200, 195));
        pastNoteDateLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel archiveHint = new JLabel("Past dates are read-only");
        archiveHint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        archiveHint.setForeground(new Color(220, 160, 155));
        archiveHint.setAlignmentX(LEFT_ALIGNMENT);

        pastNoteTA = new JTextArea(4, 30);
        pastNoteTA.setFont(UI.F_BODY);
        pastNoteTA.setLineWrap(true);
        pastNoteTA.setWrapStyleWord(true);
        pastNoteTA.setEditable(false);                         
        pastNoteTA.setBackground(new Color(140, 45, 45)); // Deep muted red
        pastNoteTA.setForeground(new Color(240, 210, 208));
        pastNoteTA.setDisabledTextColor(new Color(240, 210, 208));
        pastNoteTA.setOpaque(true);
        pastNoteTA.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 40)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        JScrollPane pastScroll = new JScrollPane(pastNoteTA);
        pastScroll.setAlignmentX(LEFT_ALIGNMENT);
        pastScroll.setBackground(new Color(140, 45, 45));
        pastScroll.getViewport().setBackground(new Color(140, 45, 45));
        pastScroll.setBorder(null);

        pastNoteArea.add(pastNoteDateLabel);
        pastNoteArea.add(Box.createVerticalStrut(4));
        pastNoteArea.add(archiveHint);
        pastNoteArea.add(Box.createVerticalStrut(8));
        pastNoteArea.add(pastScroll);

        // ── CardLayout Wrapper to prevent jumping ──────────────────────────
        bottomLayout = new CardLayout();
        bottomPanel = new JPanel(bottomLayout);
        bottomPanel.setBackground(UI.POMO_RED);
        bottomPanel.setMaximumSize(new Dimension(520, 220)); 
        
        // Create an empty placeholder card
        JPanel emptyCard = new JPanel();
        emptyCard.setBackground(UI.POMO_RED);
        
        bottomPanel.add(emptyCard, "EMPTY");
        bottomPanel.add(noteArea, "EDIT");
        bottomPanel.add(pastNoteArea, "READ");

        // ── Assemble ──────────────────────────────────────────────────────
        mainWrap.add(calHdr);
        mainWrap.add(Box.createVerticalStrut(8));
        mainWrap.add(dayNames);
        mainWrap.add(Box.createVerticalStrut(4));
        mainWrap.add(gridPanel);
        mainWrap.add(Box.createVerticalStrut(14));
        mainWrap.add(bottomPanel); // Add the fixed-size CardLayout panel here

        mainScroll = new JScrollPane(mainWrap);
        mainScroll.setBackground(UI.POMO_RED);
        mainScroll.getViewport().setBackground(UI.POMO_RED);
        mainScroll.setBorder(null);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(mainScroll, BorderLayout.CENTER);
        buildGrid();
        
        // Show the empty space by default until a day is clicked
        bottomLayout.show(bottomPanel, "EMPTY");
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
            empty.setBackground(new Color(164, 63, 63)); // Match accent color
            gridPanel.add(empty);
        }

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date   = LocalDate.of(year, month, d);
            boolean isToday  = date.equals(today);
            boolean isSel    = date.equals(selectedDate);
            boolean isPast   = date.isBefore(today);   

            JButton btn = new JButton(String.valueOf(d));
            btn.setPreferredSize(new Dimension(60, 40));

            Color bg, fg, hoverBg;
            Font font;

            if (isSel) {
                bg = Color.WHITE;
                fg = UI.POMO_RED;
                hoverBg = new Color(240, 240, 240);
                font = new Font("Segoe UI", Font.BOLD, 13);
            } else if (isToday) {
                bg = new Color(240, 160, 160); // Soft visible pink for today
                fg = UI.POMO_RED;
                hoverBg = new Color(220, 140, 140);
                font = new Font("Segoe UI", Font.BOLD, 12);
            } else if (isPast) {
                bg = new Color(150, 55, 55); // Muted red
                fg = new Color(220, 175, 175);
                hoverBg = new Color(140, 45, 45);
                font = new Font("Segoe UI", Font.PLAIN, 12);
            } else {
                bg = new Color(164, 63, 63); // Standard dark accent
                fg = Color.WHITE;
                hoverBg = new Color(145, 50, 50);
                font = new Font("Segoe UI", Font.PLAIN, 12);
            }

            btn.setFont(font);
            btn.setBackground(bg);
            btn.setForeground(fg);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { btn.setBackground(hoverBg); }
                public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
            });

            final LocalDate fd      = date;
            final boolean   fIsPast = isPast;
            btn.addActionListener(e -> {
                if (fIsPast) {
                    selectPastDay(fd);   
                } else {
                    selectDay(fd);       
                }
            });
            gridPanel.add(btn);
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void selectDay(LocalDate date) {
        selectedDate = date;
        noteDateLabel.setText("Notes for " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
        if (SessionContext.getUser() != null) {
            String existing = dao.getNoteForDate(SessionContext.getUser().getId(), date);
            noteTA.setText(existing != null ? existing : "");
        }
        
        // Show the EDIT card instead of setVisible
        bottomLayout.show(bottomPanel, "EDIT");
        buildGrid();
    }

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
        
        // Show the READ-ONLY card instead of setVisible
        bottomLayout.show(bottomPanel, "READ");
        buildGrid();
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
        
        // Optional: clear selection and hide the note area when changing months
        selectedDate = null;
        bottomLayout.show(bottomPanel, "EMPTY");
    }

    private JButton navBtn(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 20));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(164, 63, 63)); // Matched accent
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(36, 32));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(145, 50, 50)); }
            public void mouseExited(MouseEvent e)  { b.setBackground(new Color(164, 63, 63)); }
        });
        return b;
    }

    public void refresh() { buildGrid(); }
}