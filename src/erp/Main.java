package erp;

import javax.swing.*;
import erp.ui.LoginPage;

public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Load fonts for UI
        LoginPage.FontKit.init();

        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
