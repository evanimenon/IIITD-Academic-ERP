package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedTextField;

public class EditSections extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color BORDER = new Color(230, 233, 236);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private final String courseId;
    private final String adminName;
    private JPanel main;

    private RoundedPanel actionCard(String title, String desc, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontKit.bold(18f));
        titleLabel.setForeground(TEXT_900);

        JLabel descLabel = new JLabel("<html><p style='width:240px;'>" + desc + "</p></html>");
        descLabel.setFont(FontKit.regular(14f));
        descLabel.setForeground(TEXT_600);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);

        // hover + click behavior
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            Color normal = CARD;
            Color hover = new Color(238, 241, 245);
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(hover); card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(normal); card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

        return card;
    }

    public EditSections(String adminName, String courseId) {
        this.adminName = adminName;
        this.courseId = courseId;

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
            EditSections.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            EditSections.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            EditSections.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            EditSections.this.dispose();
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

        JLabel h1 = new JLabel("📃 Edit Sections (" + courseId + ")");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main content area ---
        main = new JPanel(new BorderLayout());
        main.setBorder(new EmptyBorder(32, 32, 32, 32));
        main.setOpaque(false);
        root.add(main, BorderLayout.CENTER);

        loadSections();

        
    }

    //load sections for the course from database and display as action cards
    private void loadSections() {
        main.removeAll();
        String query = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year FROM sections WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                boolean hasSections = false;
                JPanel sectionsPanel = new JPanel();
                sectionsPanel.setLayout(new GridLayout(0, 3, 20, 20));

                while (rs.next()) {
                    hasSections = true;
                    String sectionId = rs.getString("section_id");
                    String courseId = rs.getString("course_id");
                    String instructorId = rs.getString("instructor_id");
                    String dayTime = rs.getString("day_time");
                    String room = rs.getString("room");
                    String capacity = rs.getString("capacity");
                    String semester = rs.getString("semester");
                    String year = rs.getString("year");

                    String title = "Section " + sectionId + " (" + courseId + ")";
                    String desc = "Instructor: " + instructorId + "<br>Schedule: " + dayTime + "<br>Room: " + room +
                            "<br>Capacity: " + capacity + "<br>Semester: " + semester + " " + year;

                    RoundedPanel card = actionCard(title, desc, () -> {
                        new EditSectionDetails(adminName, sectionId).setVisible(true);
                        dispose();
                    });

                    sectionsPanel.add(card);
                }

                if (!hasSections) {
                    showEmptyState();
                    return;
                }

                JScrollPane scrollPane = new JScrollPane(sectionsPanel);
                scrollPane.setBorder(null);
                scrollPane.getVerticalScrollBar().setUnitIncrement(16);

                main.removeAll();
                main.add(scrollPane, BorderLayout.CENTER);
                main.revalidate();
                main.repaint();

            }

        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to load sections:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showEmptyState() {
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));

        JLabel msg = new JLabel("No sections in this course yet.");
        msg.setFont(FontKit.bold(20f));
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);
        msg.setForeground(TEXT_900);

        emptyPanel.add(Box.createVerticalStrut(40));
        emptyPanel.add(msg);
        emptyPanel.add(Box.createVerticalStrut(20));

        // Add button
        RoundedButton addBtn = new RoundedButton("Add a Section");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.setPreferredSize(new Dimension(200, 45));
        addBtn.addActionListener(e -> {
            new AddSection(adminName, courseId).setVisible(true);
            dispose();
        });

        emptyPanel.add(addBtn);

        main.add(emptyPanel, BorderLayout.CENTER);
    }

}
