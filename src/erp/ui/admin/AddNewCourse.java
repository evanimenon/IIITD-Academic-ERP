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
import erp.ui.common.RoundedComboBox;

public class AddNewCourse extends JFrame {

    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    public AddNewCourse(String adminName) {
        setTitle("IIITD ERP – Add New Course");
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

        JLabel meta = new JLabel("Year");
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
            AddNewCourse.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            AddNewCourse.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            AddNewCourse.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            AddNewCourse.this.dispose();
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

        JLabel h1 = new JLabel("👤 Add New Course");
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

            boolean saved = addCourse(courseId, code, title, creditno);
            if (saved) {
                JOptionPane.showMessageDialog(this, "Student added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                idField.setText("");
                codeField.setText("");
                titleField.setText("");
                creditBox.setSelectedIndex(0);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Student ID or Roll Number already exists!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        formCard.add(submitBtn, gbc);
    }

    private boolean addCourse(String courseId, String code, String title, int credits) {
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
                        return false;
                    }
                }
            }

            // Insert ERP
            try (PreparedStatement ps = erpConn.prepareStatement("INSERT INTO courses(course_id, code, title, credits) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, courseId);
                ps.setString(2, code);
                ps.setString(3, title);
                ps.setInt(5, credits);
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
