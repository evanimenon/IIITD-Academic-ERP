package erp.ui.admin;

import javax.swing.*;

public class AdminMaintenanceFrame extends AdminFrameBase {

    public AdminMaintenanceFrame(String adminId, String displayName) {
        super(adminId, displayName, Page.MAINTENANCE);
        setTitle("IIITD ERP – Maintenance & Backup");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    @Override
    protected JComponent buildMainContent() {
        return new MaintenancePanel(this, adminId, userDisplayName);
    }
}
