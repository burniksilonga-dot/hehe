package pomo;

import ui.LoginFrame;
import util.DBConnection;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // verify DB connection before showing any UI
        try {
            DBConnection.getConnection();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to MySQL.\n\n" + e.getMessage()
                + "\n\nMake sure:\n"
                + "  • XAMPP MySQL is running\n"
                + "  • Database 'studypulse' exists (run schema.sql)\n"
                + "  • Password in DBConnection.java is correct (default: blank)",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        //  launch ui on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new LoginFrame().setVisible(true);
        });
    }
}
