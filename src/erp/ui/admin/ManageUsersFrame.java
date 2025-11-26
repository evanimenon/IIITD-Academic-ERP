package erp.ui.admin;

import javax.swing.JComponent;

public class ManageUsersFrame extends AdminFrameBase {

    public ManageUsersFrame(String adminId, String displayName) {
        super(adminId, displayName, Page.USERS);
        setTitle("IIITD ERP – Manage Users");
    }

    @Override
    protected JComponent buildMainContent() {
        return new ManageUsersPanel(this, adminId, userDisplayName);
    }
}
