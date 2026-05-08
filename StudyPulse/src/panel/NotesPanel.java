package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import dao.NoteDAO;
import model.Note;
import ui.UI;
import util.SessionContext;

public class NotesPanel extends JPanel {
    private final NoteDAO dao = new NoteDAO();
    private JTextArea noteTA;
    private JPanel savedList;
    private JScrollPane scroll;

    public NotesPanel() {
        setBackground(UI.POMO_RED);
        setLayout(new BorderLayout());
        buildUI();
    }

    private void buildUI() {
        JPanel wrap = new JPanel();
        wrap.setBackground(UI.POMO_RED);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 50, 30, 50));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(UI.POMO_RED);
        hdr.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(255,255,255,80)));
        hdr.setMaximumSize(new Dimension(520, 38));
        JLabel title = new JLabel("📝  Quick Notes");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JButton btnSave = new JButton("💾  Save");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(Color.WHITE);
        btnSave.setForeground(UI.POMO_RED);
        btnSave.setOpaque(true); btnSave.setContentAreaFilled(true);
        btnSave.setBorderPainted(false); btnSave.setFocusPainted(false);
        btnSave.setBorder(BorderFactory.createEmptyBorder(5,12,5,12));
        btnSave.addActionListener(e -> saveNote());
        hdr.add(title, BorderLayout.WEST);
        hdr.add(btnSave, BorderLayout.EAST);

        JScrollPane taScroll = new JScrollPane();
        taScroll.setBackground(new Color(140, 35, 28));
        taScroll.getViewport().setBackground(new Color(140, 35, 28));
        taScroll.setMaximumSize(new Dimension(520, 160));
        taScroll.setAlignmentX(LEFT_ALIGNMENT);
        taScroll.setBorder(BorderFactory.createLineBorder(new Color(255,255,255,50)));

        JLabel savedLbl = new JLabel("Saved Notes");
        savedLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        savedLbl.setForeground(Color.WHITE);
        savedLbl.setBorder(BorderFactory.createEmptyBorder(14, 0, 8, 0));
        savedLbl.setAlignmentX(LEFT_ALIGNMENT);

        savedList = new JPanel();
        savedList.setBackground(UI.POMO_RED);
        savedList.setLayout(new BoxLayout(savedList, BoxLayout.Y_AXIS));

        wrap.add(hdr);
        wrap.add(taScroll);
        
                noteTA = new JTextArea(6, 30);
                wrap.add(noteTA);
                noteTA.setFont(UI.F_BODY);
                noteTA.setLineWrap(true);
                noteTA.setWrapStyleWord(true);
                noteTA.setBackground(new Color(140, 35, 28));
                noteTA.setForeground(Color.WHITE);
                noteTA.setCaretColor(Color.WHITE);
                noteTA.setOpaque(true);
                noteTA.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        wrap.add(savedLbl);
        wrap.add(savedList);

        scroll = new JScrollPane(wrap);
        scroll.setBackground(UI.POMO_RED);
        scroll.getViewport().setBackground(UI.POMO_RED);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scroll, BorderLayout.CENTER);
    }

    private void saveNote() {
        String txt = noteTA.getText().trim();
        if (txt.isEmpty()) { UI.error(this, "Note is empty."); return; }
        Note n = new Note(SessionContext.getUser().getId(), null, txt);
        dao.saveNote(n);
        noteTA.setText("");
        refresh();
        UI.info(this, "Note saved!");
    }

    public void refresh() {
        if (SessionContext.getUser() == null) return;
        savedList.removeAll();
        for (Note n : dao.getAllByUser(SessionContext.getUser().getId())) {
            JPanel card = new JPanel();
            card.setBackground(new Color(160, 45, 36));
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            card.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
            card.setAlignmentX(LEFT_ALIGNMENT);

            String dateStr = n.getNoteDate() != null
                ? n.getNoteDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                : (n.getCreatedAt() != null ? n.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "");
            JLabel dateLbl = new JLabel(dateStr);
            dateLbl.setFont(UI.F_SMALL);
            dateLbl.setForeground(new Color(255, 200, 200));

            JTextArea ta = new JTextArea(n.getContent());
            ta.setEditable(false);
            ta.setFont(UI.F_BODY);
            ta.setBackground(new Color(160, 45, 36));
            ta.setForeground(Color.WHITE);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setOpaque(true);
            ta.setBorder(null);

            card.add(dateLbl);
            card.add(Box.createVerticalStrut(4));
            card.add(ta);

            savedList.add(card);
            savedList.add(Box.createVerticalStrut(8));
        }
        savedList.revalidate();
        savedList.repaint();
    }
}