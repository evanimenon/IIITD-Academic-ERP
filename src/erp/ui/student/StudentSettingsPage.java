// CourseCatalog.java
package erp.ui.student;

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


public class StudentSettingsPage extends StudentFrameBase {

    private static final Color BORDER_COLOR   = new Color(230, 233, 236);

    public static class StudentInfo {
        public String studentID;
        public String rollNo;
        public String fullName;
        public String program;
        public int year;
    }

    // Backward compatible constructor (no studentId -> mostly for testing)
    public StudentSettingsPage(String userDisplayName) {
        this(null, userDisplayName);
    }

    public StudentSettingsPage(String studentId, String userDisplayName) {
        // IMPORTANT: we don't keep our own studentId; we let StudentFrameBase own it.
        super(studentId, userDisplayName, Page.SETTINGS);
        setTitle("IIITD ERP – Settings");
    }

    @Override
    protected JComponent buildMainContent() {
        StudentInfo info = getStudentInfo();

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

        // Student info text
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel name = new JLabel(info.fullName != null ? info.fullName : "Unknown");
        name.setFont(FontKit.bold(22));
        textPanel.add(name);

        textPanel.add(Box.createVerticalStrut(5));
        JLabel roll = new JLabel("Roll No: " + (info.rollNo != null ? info.rollNo : "N/A"));
        roll.setFont(FontKit.regular(16));
        textPanel.add(roll);

        JLabel program = new JLabel("Program: " + (info.program != null ? info.program : "N/A"));
        program.setFont(FontKit.regular(16));
        textPanel.add(program);

        JLabel year = new JLabel("Year: " + (info.year > 0 ? info.year : 0));
        year.setFont(FontKit.regular(16));
        textPanel.add(year);

        profileCard.add(textPanel, BorderLayout.CENTER);
        main.add(profileCard);
        main.add(Box.createVerticalStrut(30));

        // -------- ACTION BUTTONS --------
        RoundedButton changePasswordBtn = new RoundedButton("Change Password");
        changePasswordBtn.setFont(FontKit.semibold(17));
        changePasswordBtn.setPreferredSize(new Dimension(200, 45));
        changePasswordBtn.addActionListener(e -> {
            dispose();
            new ChangePassword(studentId).setVisible(true);;
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

    private StudentInfo getStudentInfo() {
        StudentInfo info = new StudentInfo();
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT student_id, roll_no, full_name, program, year FROM students WHERE student_id = ?")) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                info.studentID = rs.getString("student_id");
                info.rollNo = rs.getString("roll_no");
                info.fullName = rs.getString("full_name");
                info.program = rs.getString("program");
                info.year = rs.getInt("year");
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return info;
    }

}
