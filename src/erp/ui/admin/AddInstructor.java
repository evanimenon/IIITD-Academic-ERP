package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.mindrot.jbcrypt.BCrypt;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedPasswordField;
import erp.ui.common.RoundedTextField;
import erp.ui.common.RoundedComboBox;

public class AddInstructor extends AdminFrameBase {

    private static final Color CARD     = Color.WHITE;
    private static final Color TEXT_900 = new Color(24, 30, 37);

    public AddInstructor(String displayName) {
        this(null, displayName);
    }

    public AddInstructor(String adminId, String displayName) {
        super(adminId, displayName, Page.USERS);
        setTitle("IIITD ERP – Add Instructors");
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

        JLabel h1 = new JLabel("Add Instructor");
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

        // FULL NAME
        formCard.add(label("Full Name:"), gbc);

        gbc.gridx = 1;
        RoundedTextField nameField = new RoundedTextField(30, "Enter full name");
        nameField.setFont(FontKit.regular(15f));
        formCard.add(nameField, gbc);

        // Instrutctor ID
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Instructor ID:"), gbc);

        gbc.gridx = 1;
        RoundedTextField idField = new RoundedTextField(30, "Enter instructor ID");
        idField.setFont(FontKit.regular(15f));
        formCard.add(idField, gbc);

        // DEPARTMENT
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Department:"), gbc);

        gbc.gridx = 1;
        String[] depts = { "CB","ECE", "CSE", "HCD","Mathematics", "SSH" };
        RoundedComboBox<String> deptbBox = new RoundedComboBox<>(depts);
        deptbBox.setFont(FontKit.regular(15f));
        formCard.add(deptbBox, gbc);

        // PASSWORD
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Password:"), gbc);

        gbc.gridx = 1;
        RoundedPasswordField passwordField = new RoundedPasswordField(30, "Enter password");
        passwordField.setFont(FontKit.regular(15f));
        formCard.add(passwordField, gbc);

        // CONFIRM PASSWORD
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Confirm Password:"), gbc);

        gbc.gridx = 1;
        RoundedPasswordField confirmField = new RoundedPasswordField(30, "Confirm password");
        confirmField.setFont(FontKit.regular(15f));
        formCard.add(confirmField, gbc);

        // SUBMIT BUTTON
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        RoundedButton submitBtn = new RoundedButton("Add Instructor");
        submitBtn.setFont(FontKit.bold(16f));
        submitBtn.setBackground(TEAL_DARK);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(new EmptyBorder(10, 30, 10, 30));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        submitBtn.addActionListener(e -> {
            String fullname = nameField.getText().trim();
            String instID = idField.getText().trim();
            String dept = (String) deptbBox.getSelectedItem();
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (fullname.isEmpty() || instID.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean saved = saveInstructor(instID,fullname,dept,pass);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Instructor added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                idField.setText("");
                passwordField.setText("");
                confirmField.setText("");
                deptbBox.setSelectedIndex(0);
            } 
            else {
                JOptionPane.showMessageDialog(this, "This instructor ID already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(submitBtn, gbc);
        return main;
    }

    private boolean saveInstructor(String ID, String fullName, String department,String password) {
        try (Connection erpConn = DatabaseConnection.erp().getConnection();
             Connection authConn = DatabaseConnection.auth().getConnection();) {

            // Check ERP duplicates
            try (PreparedStatement ps = erpConn.prepareStatement("SELECT COUNT(*) FROM instructors WHERE instructor_id=?")) {
                ps.setString(1, ID);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    // maintaining count for debugging
                    int count = rs.getInt(1);
                    System.out.println("ERP duplicate count: " + count);
                    if (count > 0){
                        return false;
                    }
                }
            }

            // Insert ERP
            try (PreparedStatement ps = erpConn.prepareStatement("INSERT INTO instructors(instructor_id, department, instructor_name) VALUES (?, ?, ?)")) {
                ps.setString(1, ID);
                ps.setString(2, department);
                ps.setString(3, fullName);
                ps.executeUpdate();
            }

            // Check Auth duplicate
            try (PreparedStatement ps = authConn.prepareStatement("SELECT COUNT(*) FROM users_auth WHERE username=?")) {
                ps.setString(1, ID);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    int count = rs.getInt(1);
                    System.out.println("Auth duplicate count: " + count);
                    if (count > 0){
                        return false;
                    }
                }
            }

            // Hash password
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));

            // Insert Auth
            try (PreparedStatement ps = authConn.prepareStatement("INSERT INTO users_auth(username, role, password_hash, status) VALUES (?, 'instructor', ?, 'active')")) {
                ps.setString(1, ID);
                ps.setString(2, hash);
                ps.executeUpdate();
            }
            return true;

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontKit.semibold(16f));
        l.setForeground(TEXT_900);
        return l;
    }
}
