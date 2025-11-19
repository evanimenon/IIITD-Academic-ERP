package erp.ui.admin;

import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPasswordField;
import erp.ui.common.RoundedTextField;
import erp.db.DatabaseConnection;
import erp.ui.common.NavButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;
import erp.db.Maintenance;

public class AddStudent extends JFrame {

    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    public AddStudent(String adminName) {
        setTitle("IIITD ERP – Add User");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile block (Top part of sidebar)
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        EmptyBorder br = new EmptyBorder(8, 8, 32, 8);
        profile.setBorder(br);

        // Circular Avatar with rounded corner panel
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                // Draw a circle
                g2.fillOval(0, 0, getWidth(), getHeight()); 
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(adminName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        JLabel meta = new JLabel("Year, Program");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);
        
        // Rounded corners for the entire profile block (visual style enhancement)
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_LIGHT, 1),
            new EmptyBorder(8, 8, 32, 8)
        ));

        sidebar.add(profile, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Navigation Links
        NavButton dashboardBtn = new NavButton("🏠 Home", false);
        dashboardBtn.addActionListener(e -> {
            new AdminDashboard(adminName).setVisible(true);
            AddStudent.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", true);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            AddStudent.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", false);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            AddStudent.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            AddStudent.this.dispose();
        });
        nav.add(assignInstBtn);
        nav.add(Box.createVerticalStrut(8));

        
        // Separator
        nav.add(new JSeparator() {{ 
            setForeground(new Color(60, 120, 116)); 
            setBackground(new Color(60, 120, 116));
            setMaximumSize(new Dimension(240, 1));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }});
        nav.add(Box.createVerticalStrut(40));
        nav.add(new NavButton("  ⚙️  Settings", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🚪  Log Out", false)); // Used door emoji for log out
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("👤 Add Student");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.NORTH);
        }


        // --- Main form ---
        RoundedPanel formCard = new RoundedPanel(20);
        formCard.setBackground(CARD);
        formCard.setBorder(new EmptyBorder(40, 60, 60, 60));
        formCard.setLayout(new GridBagLayout());
        root.add(formCard, BorderLayout.CENTER);

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

        // STUDENT ID
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Student ID:"), gbc);

        gbc.gridx = 1;
        RoundedTextField idField = new RoundedTextField(30, "Enter student ID");
        idField.setFont(FontKit.regular(15f));
        formCard.add(idField, gbc);

        // ROLL NUMBER
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Roll Number:"), gbc);

        gbc.gridx = 1;
        RoundedTextField rollField = new RoundedTextField(30, "Enter roll number");
        rollField.setFont(FontKit.regular(15f));
        formCard.add(rollField, gbc);

        // PROGRAM
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Program:"), gbc);

        gbc.gridx = 1;
        String[] programs = { "CSE","CSD", "CSB", "ECE","EVE", "CSAM", "CSSS", "CSAI" };
        JComboBox<String> programBox = new JComboBox<>(programs);
        programBox.setFont(FontKit.regular(15f));
        formCard.add(programBox, gbc);

        // YEAR
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Year:"), gbc);

        gbc.gridx = 1;
        String[] years = { "1", "2", "3", "4" };
        JComboBox<String> yearBox = new JComboBox<>(years);
        yearBox.setFont(FontKit.regular(15f));
        formCard.add(yearBox, gbc);

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

        RoundedButton submitBtn = new RoundedButton("Add Student");
        submitBtn.setFont(FontKit.bold(16f));
        submitBtn.setBackground(TEAL_DARK);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(new EmptyBorder(10, 30, 10, 30));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        submitBtn.addActionListener(e -> {
            String fullname = nameField.getText().trim();
            String studentId = idField.getText().trim();
            String roll = rollField.getText().trim();
            String program = (String) programBox.getSelectedItem();
            int year = Integer.parseInt((String) yearBox.getSelectedItem());
            String pass = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (fullname.isEmpty() || studentId.isEmpty() || roll.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!pass.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean saved = saveStudent(studentId, roll, fullname, program, year, pass);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                nameField.setText("");
                idField.setText("");
                rollField.setText("");
                passwordField.setText("");
                confirmField.setText("");
                programBox.setSelectedIndex(0);
                yearBox.setSelectedIndex(0);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Student ID or Roll Number already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(submitBtn, gbc);
    }

    private boolean saveStudent(String studentId, String rollNo, String fullName, String program, int year, String password) {
        try (Connection erpConn = DatabaseConnection.erp().getConnection();
             Connection authConn = DatabaseConnection.auth().getConnection();) {

            // Check ERP duplicates
            try (PreparedStatement ps = erpConn.prepareStatement("SELECT COUNT(*) FROM students WHERE student_id=? OR roll_no=?")) {
                ps.setString(1, studentId);
                ps.setString(2, rollNo);
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
            try (PreparedStatement ps = erpConn.prepareStatement("INSERT INTO students(student_id, roll_no, full_name, program, year) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, studentId);
                ps.setString(2, rollNo);
                ps.setString(3, fullName);
                ps.setString(4, program);
                ps.setInt(5, year);
                ps.executeUpdate();
            }

            // Check Auth duplicate
            try (PreparedStatement ps = authConn.prepareStatement("SELECT COUNT(*) FROM users_auth WHERE username=?")) {
                ps.setString(1, studentId);
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
            try (PreparedStatement ps = authConn.prepareStatement("INSERT INTO users_auth(username, role, password_hash, status) VALUES (?, 'student', ?, 'active')")) {
                ps.setString(1, studentId);
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

    public static class RoundedComboBox<E> extends JComboBox<E> {
        private final Color bg = new Color(0xD9, 0xD9, 0xD9);
        private final Color text = new Color(24, 30, 37);
        private final Color placeholderColor = new Color(140, 148, 160);
        private String placeholder = "";

        public RoundedComboBox() {
            super();
            setup();
        }

        public RoundedComboBox(E[] items) {
            super(items);
            setup();
        }

        private void setup() {
            setOpaque(false);
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setFont(FontKit.regular(16f));
            setForeground(text);
            setBackground(bg);

            setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    JButton arrow = new JButton("▼");
                    arrow.setBorder(null);
                    arrow.setFont(FontKit.regular(12f));
                    arrow.setOpaque(false);
                    arrow.setContentAreaFilled(false);
                    arrow.setFocusPainted(false);
                    arrow.setForeground(new Color(100, 116, 139));
                    return arrow;
                }

                @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                    // no grey highlight
                }
            });
            setBorder(new EmptyBorder(12, 16, 12, 16));
        }

        public void setPlaceholder(String text) {
            this.placeholder = text;
            repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Rounded background
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

            g2.dispose();
            super.paintComponent(g);

            // Placeholder
            if (getSelectedIndex() == -1 || getSelectedItem() == null || getSelectedItem().toString().trim().isEmpty()) {
                Graphics2D g3 = (Graphics2D) g.create();
                g3.setColor(placeholderColor);
                g3.setFont(getFont());
                Insets ins = getInsets();
                g3.drawString(placeholder, ins.left, getHeight() / 2 + getFont().getSize() / 2 - 3);
                g3.dispose();
            }
        }
    }

    // Rounded panel class (reuse from Dashboard)
    static class RoundedPanel extends JPanel {
        private final int arc;
        RoundedPanel(int arc) { this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            for (int i = 6; i >= 1; i--) {
                float a = 0.035f * (i / 6f);
                g2.setColor(new Color(0, 0, 0, a));
                g2.fill(new RoundRectangle2D.Double(6 - i, 6 - i, w - 12 + 2*i, h - 12 + 2*i, arc + i, arc + i));
            }
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(6, 6, w - 12, h - 12, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // For manual test
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        SwingUtilities.invokeLater(() -> new AddStudent("Admin 123").setVisible(true));
    }
    
}
