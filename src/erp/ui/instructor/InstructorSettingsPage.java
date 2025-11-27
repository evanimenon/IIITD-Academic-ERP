// CourseCatalog.java
package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.ui.common.*;
import erp.ui.common.RoundedButton;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.ui.auth.ChangePassword;


public class InstructorSettingsPage extends InstructorFrameBase {

    private static final Color BORDER_COLOR   = new Color(230, 233, 236);

    public static class InstructorInfo {
        public String instrID;
        public String dept;
        public String fullName;
    }

    // Backward compatible constructor (no ID -> mostly for testing)
    public InstructorSettingsPage(String userDisplayName) {
        this(null, userDisplayName);
    }

    public InstructorSettingsPage(String instructorId, String userDisplayName) {
        super(instructorId, userDisplayName, Page.SETTINGS);
        setTitle("IIITD ERP – Settings");
    }

    @Override
    protected JComponent buildMainContent() {
        InstructorInfo info = getInstrInfo();

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

        JLabel name = new JLabel(info.fullName != null ? info.fullName : "Unknown");
        name.setFont(FontKit.bold(22));
        textPanel.add(name);

        textPanel.add(Box.createVerticalStrut(5));
        JLabel id = new JLabel("Instructor ID: " + info.instrID );
        id.setFont(FontKit.regular(16));
        textPanel.add(id);

        JLabel dept = new JLabel("Department: " + (info.dept != null ? info.dept : "N/A"));
        dept.setFont(FontKit.regular(16));
        textPanel.add(dept);

        profileCard.add(textPanel, BorderLayout.CENTER);
        main.add(profileCard);
        main.add(Box.createVerticalStrut(30));

        // -------- ACTION BUTTONS --------
        RoundedButton changePasswordBtn = new RoundedButton("Change Password");
        changePasswordBtn.setFont(FontKit.semibold(17));
        changePasswordBtn.setPreferredSize(new Dimension(200, 45));
        String username = getUsernameforInstrID(instructorId);
        changePasswordBtn.addActionListener(e -> {
            dispose();
            new ChangePassword(username).setVisible(true);;
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

    private String getUsernameforInstrID(String instrID){
        try (Connection conn = DatabaseConnection.auth().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM users_auth WHERE user_id = ?")) {
            stmt.setString(1, instrID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
            return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }   
    

    private InstructorInfo getInstrInfo() {
        InstructorInfo info = new InstructorInfo();
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT instructor_id, department, instructor_name FROM instructors WHERE instructor_id = ?")) {
            stmt.setString(1, instructorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                info.instrID = rs.getString("instructor_id");
                info.dept = rs.getString("department");
                info.fullName = rs.getString("instructor_name");
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

}
