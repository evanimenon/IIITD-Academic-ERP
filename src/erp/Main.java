package erp;

import javax.swing.*;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        FontKit.init();

        erp.db.DatabaseConnection.init();

        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
