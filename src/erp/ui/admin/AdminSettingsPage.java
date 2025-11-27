// CourseCatalog.java
package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.ui.common.*;
import erp.ui.auth.LoginPage;
import erp.db.DatabaseConnection;
import erp.ui.auth.ChangePassword;


public class AdminSettingsPage extends AdminFrameBase {

    private static final Color BORDER_COLOR   = new Color(230, 233, 236);

    // Backward compatible constructor (no ID -> mostly for testing)
    public AdminSettingsPage(String userDisplayName) {
        this(null, userDisplayName);
    }

    public AdminSettingsPage(String adminId, String userDisplayName) {
        super(adminId, userDisplayName, Page.SETTINGS);
        setTitle("IIITD ERP – Settings");
    }

    @Override
    protected JComponent buildMainContent() {

        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(20, 20, 20, 20));

        // -------- PROFILE CARD --------
        RoundedPanel profileCard = new RoundedPanel(10);
        profileCard.setLayout(new BorderLayout(20, 0));
        profileCard.setBackground(Color.WHITE);
        profileCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // Instructor info text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel uname = new JLabel(userDisplayName != null ? userDisplayName : "Admin");


        uname.setFont(FontKit.bold(22));
        textPanel.add(uname);
        textPanel.add(Box.createVerticalStrut(5));
        JLabel id = new JLabel("Admin ID: " + (adminId != null ? adminId : "Unknown"));
        id.setFont(FontKit.regular(16));
        textPanel.add(id);

        String usrname = getUsernameforAdminID(adminId);
        System.out.println(usrname);
        textPanel.add(Box.createVerticalStrut(5));
        JLabel username = new JLabel("Username: " + (usrname != null ? usrname : "Unknown"));
        username.setFont(FontKit.regular(16));
        textPanel.add(username);

        profileCard.add(textPanel, BorderLayout.CENTER);
        main.add(profileCard);
        main.add(Box.createVerticalStrut(30));

        // -------- ACTION BUTTONS --------
        RoundedButton changePasswordBtn = new RoundedButton("Change Password");
        changePasswordBtn.setFont(FontKit.semibold(17));
        changePasswordBtn.setPreferredSize(new Dimension(200, 45));
        changePasswordBtn.addActionListener(e -> {
            dispose();
            new ChangePassword(userDisplayName).setVisible(true);;
        });

        RoundedButton logoutBtn = new RoundedButton("Logout");
        logoutBtn.setFont(FontKit.semibold(17));
        logoutBtn.setPreferredSize(new Dimension(200, 45));
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginPage().setVisible(true);
        });

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(changePasswordBtn);
        btnPanel.add(logoutBtn);

        main.add(btnPanel);

        return main;
    }

    private String getUsernameforAdminID(String adminID){
        try (Connection conn = DatabaseConnection.auth().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM users_auth WHERE user_id = ?")) {
            stmt.setString(1, adminID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }  

}
