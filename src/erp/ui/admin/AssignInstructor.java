package erp.ui.admin;

import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import erp.ui.admin.AddUser.RoundedComboBox;
import erp.ui.common.RoundedPanel;
import erp.ui.common.NavButton;

import erp.ui.common.RoundedButton;

public class AssignInstructor extends JFrame {

    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    public AssignInstructor(String adminName) {
        setTitle("IIITD ERP – Assign Instructor");
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
            AssignInstructor.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            AssignInstructor.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", false);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            AssignInstructor.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", true);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            AssignInstructor.this.dispose();
        });
        nav.add(assignInstBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton maintenanceModeBtn = new NavButton("🔧 Maintenance Mode", false);
        maintenanceModeBtn.addActionListener(e -> {
            new MaintenanceMode(adminName).setVisible(true);
            AssignInstructor.this.dispose();
        });
        nav.add(maintenanceModeBtn);
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

        JLabel h1 = new JLabel("👨 Assign Instructor");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main form ---
        RoundedPanel formCard = new RoundedPanel(20);
        formCard.setBackground(CARD);
        formCard.setBorder(new EmptyBorder(40, 60, 60, 60));
        formCard.setLayout(new GridBagLayout());
        root.add(formCard, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel courseLabel = new JLabel("Course:");
        courseLabel.setFont(FontKit.semibold(16f));
        courseLabel.setForeground(TEXT_900);
        formCard.add(courseLabel, gbc);

        gbc.gridx = 1;
        RoundedComboBox<String> courseDropdown = new RoundedComboBox<>();
        courseDropdown.setFont(FontKit.regular(15f));
        // TODO: DB → Load course list
        courseDropdown.addItem("Choose Course");
        formCard.add(courseDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        JLabel sectionLabel = new JLabel("Section:");
        sectionLabel.setFont(FontKit.semibold(16f));
        sectionLabel.setForeground(TEXT_900);
        formCard.add(sectionLabel, gbc);

        gbc.gridx = 1;
        RoundedComboBox<String> sectionDropdown = new RoundedComboBox<>();
        sectionDropdown.setFont(FontKit.regular(15f));

        // TODO: DB → Load sections based on selected course
        sectionDropdown.addItem("Choose Section");
        formCard.add(sectionDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy++;

        JLabel instructorLabel = new JLabel("Instructor:");
        instructorLabel.setFont(FontKit.semibold(16f));
        instructorLabel.setForeground(TEXT_900);
        formCard.add(instructorLabel, gbc);

        gbc.gridx = 1;
        RoundedComboBox<String> instructorDropdown = new RoundedComboBox<>();
        instructorDropdown.setFont(FontKit.regular(15f));
        // TODO: DB → Load instructors list
        instructorDropdown.addItem("Choose Instructor");
        formCard.add(instructorDropdown, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        RoundedButton assignBtn = new RoundedButton("Assign Instructor");
        assignBtn.setFont(FontKit.bold(16f));
        assignBtn.setForeground(Color.WHITE);
        assignBtn.setBackground(TEAL);
        assignBtn.setBorder(new EmptyBorder(12, 28, 12, 28));
        assignBtn.setFocusPainted(false);
        assignBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        assignBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); 
        assignBtn.setAlignmentX(Component.LEFT_ALIGNMENT); 
        // Click handler 
        assignBtn.addActionListener(e -> { 
            String course = (String) courseDropdown.getSelectedItem(); 
            String section = (String) sectionDropdown.getSelectedItem(); 
            String instructor = (String) instructorDropdown.getSelectedItem(); 
            // Simple validation 
            if (course.equals("Choose Course") || instructor.equals("Choose Instructor")) { 
                JOptionPane.showMessageDialog(this, "Please select all required fields."); return; } 
                // TODO: DB → Insert/Update instructor assignment 
                // - Check existing assignment 
                // - Insert new assignment 
                // - Refresh table below 
                
                JOptionPane.showMessageDialog(this, "Instructor assigned successfully!"); });

        formCard.add(assignBtn, gbc);


    }
}
