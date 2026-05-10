package ui;

import dao.UserDAO;
import model.User;
import util.SessionContext;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Window.Type;

public class LoginFrame extends JFrame {

    // ── EDIT THESE to change colors & fonts ──────────────────────────────────
    private static final Color  CLR_PRIMARY   = new Color(0xC0392B); // red background
    private static final Color  CLR_WHITE     = Color.WHITE;
    private static final Color  CLR_ERROR     = new Color(180, 30, 30);
    private static final Color  CLR_LINK      = new Color(255, 220, 220);
    private static final Color  CLR_BTN_GREEN = new Color(0x27AE60);
    private static final Font   FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font   FONT_LABEL    = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font   FONT_FIELD    = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font   FONT_BTN      = new Font("Segoe UI", Font.BOLD, 14);
    private static final String LOGO_PATH     = "/resources/logo.png"; // ← your image here
    // ─────────────────────────────────────────────────────────────────────────

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO = new UserDAO();

    // ── top brand panel ───────────────────────────────────────────────────────
    private JPanel  brandPanel;
    private JLabel  lblLogo;

    // ── card switcher ─────────────────────────────────────────────────────────
    private JPanel     cardPanel;
    private CardLayout cardLayout;

    // ── login card ────────────────────────────────────────────────────────────
    private JPanel         loginCard;
    private JPanel         loginForm;
    private JLabel         lblUsernameHdr;
    private JTextField     tfUser;
    private JLabel         lblPasswordHdr;
    private JPasswordField pfPass;
    private JLabel         lblLoginErr;
    private JButton        btnLogin;
    private JButton        btnToRegister;

    // ── register card ─────────────────────────────────────────────────────────
    private JPanel         registerCard;
    private JPanel         registerForm;
    private JLabel         lblRegUserHdr;
    private JTextField     tfRegUser;
    private JLabel         lblRegNameHdr;
    private JTextField     tfRegName;
    private JLabel         lblRegPassHdr;
    private JPasswordField pfRegPass;
    private JLabel         lblRegPass2Hdr;
    private JPasswordField pfRegPass2;
    private JLabel lblRegErr;
    private JButton        btnRegister;
    private JButton        btnToLogin;
    private JCheckBox chckbxNewCheckBox;
    private JCheckBox chckbxNewCheckBox_1;
    private JLabel lblAppName_1;

    // ─────────────────────────────────────────────────────────────────────────

    public LoginFrame() {
    	setForeground(new Color(0, 0, 0));
    	setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 12));
    	setIconImage(Toolkit.getDefaultToolkit().getImage("C:\\Users\\burni\\Downloads\\18d9c5b9-d71b-4099-a92b-3b35d7fa65c6 (2).png"));
        setTitle("StudyPulse — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(380, 530);
        setMinimumSize(new Dimension(350, 560));
        setLocationRelativeTo(null);
        setResizable(true);
        initComponents();
    }

    private void initComponents() {
        getContentPane().setBackground(CLR_PRIMARY);
        getContentPane().setLayout(new BorderLayout());

        // ── Brand / header ────────────────────────────────────────────────────
        brandPanel = new JPanel();
        brandPanel.setBackground(CLR_PRIMARY);
        brandPanel.setLayout(new BoxLayout(brandPanel, BoxLayout.Y_AXIS));
        brandPanel.setBorder(new EmptyBorder(22, 0, 6, 0));

        lblLogo = buildLogoLabel();
        lblLogo.setAlignmentX(CENTER_ALIGNMENT);

        brandPanel.add(lblLogo);

        // ── card panel ────────────────────────────────────────────────────────
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(CLR_PRIMARY);

        buildLoginCard();
        buildRegisterCard();

        cardPanel.add(loginCard,    "login");
        cardPanel.add(registerCard, "register");

        getContentPane().add(brandPanel, BorderLayout.NORTH);
        
        lblAppName_1 = new JLabel("StudyPulse", SwingConstants.CENTER);
        lblAppName_1.setForeground(Color.WHITE);
        lblAppName_1.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 22));
        lblAppName_1.setAlignmentX(0.5f);
        brandPanel.add(lblAppName_1);
        getContentPane().add(cardPanel,  BorderLayout.CENTER);
    }

    // ── login card ────────────────────────────────────────────────────────────

    private void buildLoginCard() {
        loginCard = new JPanel(new BorderLayout());
        loginCard.setBackground(CLR_PRIMARY);
        loginCard.setBorder(new EmptyBorder(8, 36, 16, 36));

        loginForm = new JPanel(new GridBagLayout());
        loginForm.setBackground(CLR_WHITE);
        loginForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(20, 20, 20, 20)
        ));

        lblUsernameHdr = new JLabel("USERNAME");
        lblUsernameHdr.setFont(FONT_LABEL);
        lblUsernameHdr.setForeground(Color.GRAY);

        tfUser = new JTextField(18);
        tfUser.setFont(FONT_FIELD);
        tfUser.setPreferredSize(new Dimension(0, 34));

        lblPasswordHdr = new JLabel("PASSWORD");
        lblPasswordHdr.setFont(FONT_LABEL);
        lblPasswordHdr.setForeground(Color.GRAY);

        pfPass = new JPasswordField(18);
        pfPass.setFont(FONT_FIELD);
        pfPass.setPreferredSize(new Dimension(0, 34));

        lblLoginErr = new JLabel(" ");
        lblLoginErr.setFont(FONT_LABEL);
        lblLoginErr.setForeground(CLR_ERROR);

        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, 14));
        btnLogin.setBackground(new Color(192, 57, 43));
        btnLogin.setForeground(CLR_WHITE);
        btnLogin.setOpaque(true);
        btnLogin.setContentAreaFilled(true);
        btnLogin.setBorderPainted(false);
        btnLogin.setPreferredSize(new Dimension(0, 38));
        btnLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { doLogin(); }
        });
        getRootPane().setDefaultButton(btnLogin);

        // Each add() gets its OWN GridBagConstraints — WindowBuilder requirement
        GridBagConstraints gc0 = new GridBagConstraints();
        gc0.fill = GridBagConstraints.HORIZONTAL; gc0.weightx = 1.0;
        gc0.gridx = 0; gc0.gridy = 0; gc0.insets = new Insets(4, 0, 5, 0);
        loginForm.add(lblUsernameHdr, gc0);

        GridBagConstraints gc1 = new GridBagConstraints();
        gc1.fill = GridBagConstraints.HORIZONTAL; gc1.weightx = 1.0;
        gc1.gridx = 0; gc1.gridy = 1; gc1.insets = new Insets(0, 0, 5, 0);
        loginForm.add(tfUser, gc1);

        GridBagConstraints gc2 = new GridBagConstraints();
        gc2.fill = GridBagConstraints.HORIZONTAL; gc2.weightx = 1.0;
        gc2.gridx = 0; gc2.gridy = 2; gc2.insets = new Insets(4, 0, 5, 0);
        loginForm.add(lblPasswordHdr, gc2);

        GridBagConstraints gc3 = new GridBagConstraints();
        gc3.fill = GridBagConstraints.HORIZONTAL; gc3.weightx = 1.0;
        gc3.gridx = 0; gc3.gridy = 3; gc3.insets = new Insets(0, 0, 5, 0);
        loginForm.add(pfPass, gc3);
        
        chckbxNewCheckBox = new JCheckBox("Show Password");
        GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
        gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 0);
        gbc_chckbxNewCheckBox.gridx = 0;
        gbc_chckbxNewCheckBox.gridy = 4;
        gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
        loginForm.add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
        
        chckbxNewCheckBox.addActionListener(e -> {
            if (chckbxNewCheckBox.isSelected()) {
                pfPass.setEchoChar((char) 0);
            } else {
                pfPass.setEchoChar('●');
            }
        });

        GridBagConstraints gc4 = new GridBagConstraints();
        gc4.fill = GridBagConstraints.HORIZONTAL; gc4.weightx = 1.0;
        gc4.gridx = 0; gc4.gridy = 6; gc4.insets = new Insets(2, 0, 5, 0);
        loginForm.add(lblLoginErr, gc4);

        GridBagConstraints gc5 = new GridBagConstraints();
        gc5.fill = GridBagConstraints.HORIZONTAL; gc5.weightx = 1.0;
        gc5.gridx = 0; gc5.gridy = 7; gc5.insets = new Insets(8, 0, 0, 0);
        loginForm.add(btnLogin, gc5);

        btnToRegister = new JButton("Don't have an account? Register");
        btnToRegister.setFont(FONT_LABEL);
        btnToRegister.setForeground(CLR_LINK);
        btnToRegister.setContentAreaFilled(false);
        btnToRegister.setOpaque(false);
        btnToRegister.setBorderPainted(false);
        btnToRegister.setFocusPainted(false);
        btnToRegister.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToRegister.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnToRegister.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { cardLayout.show(cardPanel, "register"); }
        });

        loginCard.add(loginForm,    BorderLayout.NORTH);
        loginCard.add(btnToRegister, BorderLayout.SOUTH);
        
    }

    // ── register card ─────────────────────────────────────────────────────────

    private void buildRegisterCard() {
        registerCard = new JPanel(new BorderLayout());
        registerCard.setBackground(CLR_PRIMARY);
        registerCard.setBorder(new EmptyBorder(6, 36, 12, 36));
        
        lblRegErr = new JLabel(" ");
        lblRegErr.setFont(FONT_LABEL);
        lblRegErr.setForeground(CLR_ERROR);

        registerForm = new JPanel(new GridBagLayout());
        registerForm.setBackground(CLR_WHITE);
        registerForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(16, 20, 16, 20)
        ));
                                
                                        lblRegUserHdr = new JLabel("USERNAME");
                                        lblRegUserHdr.setFont(FONT_LABEL);
                                        lblRegUserHdr.setForeground(Color.GRAY);
                                        
                                                // Each add() gets its OWN GridBagConstraints — WindowBuilder requirement
                                                GridBagConstraints rg0 = new GridBagConstraints();
                                                rg0.fill = GridBagConstraints.HORIZONTAL; 
                                                rg0.weightx = 1.0;
                                                rg0.gridx = 0; 
                                                rg0.gridy = 0; 
                                                rg0.insets = new Insets(3, 0, 5, 0);
                                                registerForm.add(lblRegUserHdr, rg0);
                                
                                        tfRegUser = new JTextField(18);
                                        tfRegUser.setFont(FONT_FIELD);
                                        tfRegUser.setPreferredSize(new Dimension(0, 34));
                                        
                                                GridBagConstraints rg1 = new GridBagConstraints();
                                                rg1.fill = GridBagConstraints.HORIZONTAL; 
                                                rg1.weightx = 1.0;
                                                rg1.gridx = 0; 
                                                rg1.gridy = 1; 
                                                rg1.insets = new Insets(0, 0, 5, 0);
                                                registerForm.add(tfRegUser, rg1);
                        
                                lblRegNameHdr = new JLabel("FULL NAME");
                                lblRegNameHdr.setFont(FONT_LABEL);
                                lblRegNameHdr.setForeground(Color.GRAY);
                                
                                        GridBagConstraints rg2 = new GridBagConstraints();
                                        rg2.fill = GridBagConstraints.HORIZONTAL; 
                                        rg2.weightx = 1.0;
                                        rg2.gridx = 0; 
                                        rg2.gridy = 2; 
                                        rg2.insets = new Insets(3, 0, 5, 0);
                                        registerForm.add(lblRegNameHdr, rg2);
                                
                                        tfRegName = new JTextField(18);
                                        tfRegName.setFont(FONT_FIELD);
                                        tfRegName.setPreferredSize(new Dimension(0, 34));
                                        
                                                GridBagConstraints rg3 = new GridBagConstraints();
                                                rg3.fill = GridBagConstraints.HORIZONTAL; 
                                                rg3.weightx = 1.0;
                                                rg3.gridx = 0; 
                                                rg3.gridy = 3; 
                                                rg3.insets = new Insets(0, 0, 5, 0);
                                                registerForm.add(tfRegName, rg3);
                        
                                lblRegPassHdr = new JLabel("PASSWORD");
                                lblRegPassHdr.setFont(FONT_LABEL);
                                lblRegPassHdr.setForeground(Color.GRAY);
                                
                                        GridBagConstraints rg4 = new GridBagConstraints();
                                        rg4.fill = GridBagConstraints.HORIZONTAL; 
                                        rg4.weightx = 1.0;
                                        rg4.gridx = 0; 
                                        rg4.gridy = 4; 
                                        rg4.insets = new Insets(3, 0, 5, 0);
                                        registerForm.add(lblRegPassHdr, rg4);
                        
                                pfRegPass = new JPasswordField(18);
                                pfRegPass.setFont(FONT_FIELD);
                                pfRegPass.setPreferredSize(new Dimension(0, 34));
                                
                                        GridBagConstraints rg5 = new GridBagConstraints();
                                        rg5.fill = GridBagConstraints.HORIZONTAL; 
                                        rg5.weightx = 1.0;
                                        rg5.gridx = 0; 
                                        rg5.gridy = 5; 
                                        rg5.insets = new Insets(0, 0, 5, 0);
                                        registerForm.add(pfRegPass, rg5);
                                
                                        lblRegPass2Hdr = new JLabel("CONFIRM PASSWORD");
                                        lblRegPass2Hdr.setFont(FONT_LABEL);
                                        lblRegPass2Hdr.setForeground(Color.GRAY);
                                        
                                                GridBagConstraints rg6 = new GridBagConstraints();
                                                rg6.fill = GridBagConstraints.HORIZONTAL; 
                                                rg6.weightx = 1.0;
                                                rg6.gridx = 0; 
                                                rg6.gridy = 6; 
                                                rg6.insets = new Insets(3, 0, 5, 0);
                                                registerForm.add(lblRegPass2Hdr, rg6);

        JScrollPane scroll = new JScrollPane(registerForm);
                                                                                                        
                                                                                                                pfRegPass2 = new JPasswordField(18);
                                                                                                                pfRegPass2.setFont(FONT_FIELD);
                                                                                                                pfRegPass2.setPreferredSize(new Dimension(0, 34));
                                                                                                                
                                                                                                                        GridBagConstraints rg7 = new GridBagConstraints();
                                                                                                                        rg7.fill = GridBagConstraints.HORIZONTAL; 
                                                                                                                        rg7.weightx = 1.0;
                                                                                                                        rg7.gridx = 0; 
                                                                                                                        rg7.gridy = 7; 
                                                                                                                        rg7.insets = new Insets(0, 0, 5, 0);
                                                                                                                        registerForm.add(pfRegPass2, rg7);
                                                                                                                        
                                                                                                                                btnRegister = new JButton("Create Account");
                                                                                                                                btnRegister.setFont(FONT_BTN);
                                                                                                                                btnRegister.setBackground(CLR_BTN_GREEN);
                                                                                                                                btnRegister.setForeground(CLR_WHITE);
                                                                                                                                btnRegister.setOpaque(true);
                                                                                                                                btnRegister.setContentAreaFilled(true);
                                                                                                                                btnRegister.setBorderPainted(false);
                                                                                                                                btnRegister.setFocusPainted(false);
                                                                                                                                btnRegister.setPreferredSize(new Dimension(0, 38));
                                                                                                                                btnRegister.addActionListener(new ActionListener() {
                                                                                                                                    public void actionPerformed(ActionEvent e) { doRegister(); }
                                                                                                                                });
                                                                                                                                        
                                                                                                                                        chckbxNewCheckBox_1 = new JCheckBox("Show Password");
                                                                                                                                        GridBagConstraints gbc_chckbxNewCheckBox_1 = new GridBagConstraints();
                                                                                                                                        gbc_chckbxNewCheckBox_1.insets = new Insets(0, 0, 5, 0);
                                                                                                                                        gbc_chckbxNewCheckBox_1.gridx = 0;
                                                                                                                                        gbc_chckbxNewCheckBox_1.gridy = 8;
                                                                                                                                        gbc_chckbxNewCheckBox_1.anchor = GridBagConstraints.WEST;
                                                                                                                                        registerForm.add(chckbxNewCheckBox_1, gbc_chckbxNewCheckBox_1);
                                                                                                                                
                                                                                                                                        GridBagConstraints rg9 = new GridBagConstraints();
                                                                                                                                        rg9.fill = GridBagConstraints.HORIZONTAL; 
                                                                                                                                        rg9.weightx = 1.0;
                                                                                                                                        rg9.gridx = 0; 
                                                                                                                                        rg9.gridy = 10; 
                                                                                                                                        rg9.insets = new Insets(8, 0, 0, 0);
                                                                                                                                        registerForm.add(btnRegister, rg9);
                                                                                                                                        
                                                                                                                                        chckbxNewCheckBox_1.addActionListener(e -> {
                                                                                                                                            if (chckbxNewCheckBox_1.isSelected()) {
                                                                                                                                            	 pfRegPass.setEchoChar((char) 0);
                                                                                                                                            	 pfRegPass2.setEchoChar((char) 0);
                                                                                                                                            	 
                                                                                                                                            } else {
                                                                                                                                            	 pfRegPass.setEchoChar('●');
                                                                                                                                                 pfRegPass2.setEchoChar('●');
                                                                                                                                            }
                                                                                                                                        });

        scroll.setBorder(null);
        scroll.setBackground(CLR_PRIMARY);
        scroll.getViewport().setBackground(CLR_PRIMARY);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        btnToLogin = new JButton("Already have an account? Login");
        btnToLogin.setFont(FONT_LABEL);
        btnToLogin.setForeground(CLR_LINK);
        btnToLogin.setContentAreaFilled(false);
        btnToLogin.setOpaque(false);
        btnToLogin.setBorderPainted(false);
        btnToLogin.setFocusPainted(false);
        btnToLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnToLogin.setBorder(new EmptyBorder(8, 0, 0, 0));
        btnToLogin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { cardLayout.show(cardPanel, "login"); }
        });

        registerCard.add(scroll,      BorderLayout.CENTER);
        registerCard.add(btnToLogin,  BorderLayout.SOUTH);
        
        GridBagConstraints rg8 = new GridBagConstraints();
        rg8.fill = GridBagConstraints.HORIZONTAL;
        rg8.weightx = 1.0;
        rg8.gridx = 0;
        rg8.gridy = 9;
        rg8.insets = new Insets(2, 0, 5, 0);
        registerForm.add(lblRegErr, rg8);
    }

    // ── logo helper ───────────────────────────────────────────────────────────


    private JLabel buildLogoLabel() {
        try {
            java.net.URL url = getClass().getResource(LOGO_PATH);
            if (url != null) {
                ImageIcon raw    = new ImageIcon(url);
                Image     scaled = raw.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                return new JLabel(new ImageIcon(scaled), SwingConstants.CENTER);
            }
        } catch (Exception ignored) { /* fall through */ }

        // Fallback: styled text badge
        JLabel fallback = new JLabel("", SwingConstants.CENTER);
        fallback.setBackground(new Color(240, 240, 240));
        fallback.setIcon(new ImageIcon("C:\\Users\\burni\\Downloads\\18d9c5b9-d71b-4099-a92b-3b35d7fa65c6 (2).png"));
        fallback.setFont(new Font("Segoe UI", Font.BOLD, 19));
        fallback.setForeground(new Color(255, 255, 255));
        fallback.setPreferredSize(new Dimension(60, 60));
        return fallback;
    }

    // ── actions ───────────────────────────────────────────────────────────────

    private void doLogin() {
        String u = tfUser.getText().trim();
        String p = new String(pfPass.getPassword());
        if (u.isEmpty() || p.isEmpty()) {
            lblLoginErr.setText("Please fill in all fields.");
            return;
        }
        User found = userDAO.findByCredentials(u, p);
        if (found == null) {
            lblLoginErr.setText("Incorrect username or password.");
            return;
        }
        SessionContext.login(found);
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }

    private void doRegister() {
        String u  = tfRegUser.getText().trim();
        String n  = tfRegName.getText().trim();
        String p  = new String(pfRegPass.getPassword());
        String p2 = new String(pfRegPass2.getPassword());
        if (u.isEmpty() || p.isEmpty())        { lblRegErr.setText("Username and password required.");          return; }
        if (p.length() < 6)                    { lblRegErr.setText("Password must be at least 6 characters."); return; }
        if (!p.equals(p2))                     { lblRegErr.setText("Passwords do not match.");                  return; }
        if (userDAO.findByUsername(u) != null) { lblRegErr.setText("Username already taken.");                  return; }
        User newUser = new User(u, p, n.isEmpty() ? u : n);
        userDAO.register(newUser);
        SessionContext.login(newUser);
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
