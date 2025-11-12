package erp;

import javax.swing.*;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

public class Main {
    public static void main(String[] args) {
        // Native look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Load fonts BEFORE creating any UI
        FontKit.init();

        // Initialize DB pools ONCE before showing UI
        erp.db.DatabaseConnection.init();

        // Show the LoginPage ONCE on the EDT
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
