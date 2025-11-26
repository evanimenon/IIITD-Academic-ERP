package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedTextField;
import erp.ui.common.RoundedComboBox;

public class AddNewCourse extends AdminFrameBase {

    private static final Color CARD     = Color.WHITE;
    private static final Color TEXT_900 = new Color(24, 30, 37);

    public AddNewCourse(String displayName) {
        this(null, displayName);
    }

    public AddNewCourse(String adminId, String displayName) {
        super(adminId, displayName, Page.ASSIGN);
        setTitle("IIITD ERP – Add New Course");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("Add New Course");
        h1.setFont(FontKit.bold(24f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel right = new JLabel("Logged in as " + userDisplayName);
        right.setFont(FontKit.regular(13f));
        right.setForeground(new Color(200, 230, 225));
        hero.add(right, BorderLayout.EAST);

        main.add(hero, BorderLayout.NORTH);

        RoundedPanel formCard = new RoundedPanel(20);
        formCard.setBackground(CARD);
        formCard.setBorder(new EmptyBorder(40, 60, 60, 60));
        formCard.setLayout(new GridBagLayout());
        main.add(formCard, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; 
        gbc.gridy = 0;


        // ID
        formCard.add(label("Course ID:"), gbc);

        gbc.gridx = 1;
        RoundedTextField idField = new RoundedTextField(30, "Enter Course ID");
        idField.setFont(FontKit.regular(15f));
        formCard.add(idField, gbc);

        // CODE
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Course Code:"), gbc);

        gbc.gridx = 1;
        RoundedTextField codeField = new RoundedTextField(30, "Enter Course code");
        codeField.setFont(FontKit.regular(15f));
        formCard.add(codeField, gbc);

        // TITLE
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Course Title:"), gbc);

        gbc.gridx = 1;
        RoundedTextField titleField = new RoundedTextField(30, "Enter course title");
        titleField.setFont(FontKit.regular(15f));
        formCard.add(titleField, gbc);

        // CREDITS
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Credits:"), gbc);

        gbc.gridx = 1;
        String[] credits = { "1", "2", "4" };
        RoundedComboBox<String> creditBox = new RoundedComboBox<>(credits);
        creditBox.setFont(FontKit.regular(15f));
        formCard.add(creditBox, gbc);

        // ADD BUTTON
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        RoundedButton submitBtn = new RoundedButton("Add Course");
        submitBtn.setFont(FontKit.bold(16f));
        submitBtn.setBackground(TEAL_DARK);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(new EmptyBorder(10, 30, 10, 30));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        submitBtn.addActionListener(e -> {
            String courseId = idField.getText().trim();
            String code = codeField.getText().trim();
            String title = titleField.getText().trim();
            int creditno = Integer.parseInt((String) creditBox.getSelectedItem());

            if (courseId.isEmpty() || code.isEmpty() || title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String saved = addCourse(courseId, code, title, creditno);
            if (saved==null) {
                JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                idField.setText("");
                codeField.setText("");
                titleField.setText("");
            } 
            else {
                JOptionPane.showMessageDialog(this, saved, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(submitBtn, gbc);
        return main;
    }

    private String addCourse(String courseId, String code, String title, int credits) {
        try (Connection erpConn = DatabaseConnection.erp().getConnection();) {

            // Check ERP duplicates
            try (PreparedStatement ps = erpConn.prepareStatement("SELECT COUNT(*) FROM courses WHERE course_id=? AND code=?")) {
                ps.setString(1, courseId);
                ps.setString(2, code);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    // maintaining count for debugging
                    int count = rs.getInt(1);
                    System.out.println("ERP duplicate count: " + count);
                    if (count > 0){
                        return "Course with same ID and Code already exists in ERP.";
                    }
                }
            }

            // Insert ERP
            try (PreparedStatement ps = erpConn.prepareStatement("INSERT INTO courses(course_id, code, title, credits) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, courseId);
                ps.setString(2, code);
                ps.setString(3, title);
                ps.setInt(4, credits);
                ps.executeUpdate();
            }
            return null;

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            return "Database error: " + ex.getMessage();
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontKit.semibold(16f));
        l.setForeground(TEXT_900);
        return l;
    }
}
