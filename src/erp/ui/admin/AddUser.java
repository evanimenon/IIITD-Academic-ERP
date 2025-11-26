package erp.ui.admin;

import javax.swing.*;

public class AddUser extends AdminFrameBase {

    // convenience constructor if some code only passes displayName
    public AddUser(String displayName) {
        this(null, displayName);
    }

    public AddUser(String adminId, String displayName) {
        super(adminId, displayName, Page.USERS);
        setTitle("IIITD ERP – Manage Users");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    @Override
    protected JComponent buildMainContent() {
        // New single-page Manage Users UI (tables, filters, dialogs)
        return new ManageUsersPanel(this, adminId, userDisplayName);
    }
}
