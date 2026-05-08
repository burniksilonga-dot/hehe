package panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import dao.UserDAO;
import model.User;
import service.PomodoroService;
import ui.UI;
import util.SessionContext;

public class SettingDialog extends JDialog {
    private final PomodoroService service;
    private final UserDAO userDAO = new UserDAO();
    private JSpinner spWork, spShort, spLong, spInterval;
    private JCheckBox cbSound;

    public SettingDialog(Frame parent, PomodoroService svc) {
        super(parent, "Setting", true);
        this.service = svc;
        setSize(380, 420);
        setResizable(false);
        setLocationRelativeTo(parent);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        // Header bar
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(150, 35, 26));
        header.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        JLabel title = new JLabel("SETTING");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
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
        JPanel body = new JPanel(new GridBagLayout());
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1.0;
        gc.insets = new Insets(6, 0, 6, 0);
        gc.gridx = 0;
        gc.gridwidth = 3;

        User u = SessionContext.getUser();
        spWork     = sp(u.getWorkMinutes(), 1, 90);
        spShort    = sp(u.getShortBreak(), 1, 30);
        spLong     = sp(u.getLongBreak(), 1, 60);
        spInterval = sp(u.getLongInterval(), 1, 10);
        cbSound    = new JCheckBox("Enable sound notifications", u.isSoundEnabled());
        cbSound.setBackground(Color.WHITE);
        cbSound.setFont(UI.F_BODY);

        // Timer section header
        JLabel timerSec = new JLabel("⏱  TIMER");
        timerSec.setFont(new Font("Segoe UI", Font.BOLD, 11));
        timerSec.setForeground(UI.TEXT_GRAY);
        gc.gridy = 0; body.add(timerSec, gc);

        // Time inputs row
        gc.gridy = 1; gc.gridwidth = 1;
        gc.insets = new Insets(2, 0, 2, 8);
        JPanel p1 = labeledSpinner("Pomodoro",    spWork);
        JPanel p2 = labeledSpinner("Short Break", spShort);
        JPanel p3 = labeledSpinner("Long Break",  spLong);
        body.add(p1, gc); gc.gridx=1; body.add(p2, gc); gc.gridx=2; body.add(p3, gc);

        gc.gridx = 0; gc.gridwidth = 3;
        gc.insets = new Insets(6, 0, 6, 0);

        // Long break interval
        gc.gridy = 2;
        JPanel intRow = new JPanel(new BorderLayout(8, 0));
        intRow.setBackground(Color.WHITE);
        JLabel intLbl = new JLabel("Long Break Interval");
        intLbl.setFont(UI.F_BODY);
        spInterval.setPreferredSize(new Dimension(70, 30));
        intRow.add(intLbl, BorderLayout.WEST);
        intRow.add(spInterval, BorderLayout.EAST);
        body.add(intRow, gc);

        // Divider
        gc.gridy = 3; body.add(new JSeparator(), gc);

        // Sound section
        JLabel sndSec = new JLabel("🔊  SOUND");
        sndSec.setFont(new Font("Segoe UI", Font.BOLD, 11));
        sndSec.setForeground(UI.TEXT_GRAY);
        gc.gridy = 4; body.add(sndSec, gc);
        gc.gridy = 5; body.add(cbSound, gc);

        // Divider
        gc.gridy = 6; body.add(new JSeparator(), gc);

        // OK button
        JButton ok = new JButton("OK");
        ok.setFont(new Font("Segoe UI", Font.BOLD, 14));
        ok.setBackground(UI.POMO_RED);
        ok.setForeground(Color.WHITE);
        ok.setOpaque(true); ok.setContentAreaFilled(true);
        ok.setBorderPainted(false); ok.setFocusPainted(false);
        ok.setPreferredSize(new Dimension(80, 36));
        ok.addActionListener(e -> save());

        JPanel okRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        okRow.setBackground(Color.WHITE);
        okRow.add(ok);
        gc.gridy = 7; gc.insets = new Insets(12, 0, 0, 0);
        body.add(okRow, gc);

        root.add(header, BorderLayout.NORTH);
        root.add(body,   BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel labeledSpinner(String label, JSpinner sp) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Color.WHITE);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(UI.TEXT_GRAY);
        sp.setPreferredSize(new Dimension(70, 30));
        p.add(l,  BorderLayout.NORTH);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private void save() {
        User u = SessionContext.getUser();
        u.setWorkMinutes((int) spWork.getValue());
        u.setShortBreak((int) spShort.getValue());
        u.setLongBreak((int) spLong.getValue());
        u.setLongInterval((int) spInterval.getValue());
        u.setSoundEnabled(cbSound.isSelected());
        userDAO.saveSettings(u);
        service.reloadSettings();
        UI.info(this, "Settings saved!");
        dispose();
    }

    private JSpinner sp(int v, int min, int max) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(v, min, max, 1));
        s.setFont(UI.F_BODY);
        return s;
    }
}
