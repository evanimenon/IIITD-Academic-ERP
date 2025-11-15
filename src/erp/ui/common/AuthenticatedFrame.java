package erp.ui.common;

import erp.auth.AuthContext;
import erp.auth.Role;
import javax.swing.*;

public abstract class AuthenticatedFrame extends JFrame {
    protected AuthenticatedFrame(Role requiredRole) {
        Role actual = AuthContext.getRole();
        if (requiredRole != Role.UNKNOWN && actual != requiredRole) {
            JOptionPane.showMessageDialog(null, "Not authorized for this page.");
            // redirect to login (or appropriate dashboard)
            new erp.ui.auth.LoginPage().setVisible(true);
            // ensure this frame isn't used
            throw new SecurityException("Unauthorized");
        }
    }
}
