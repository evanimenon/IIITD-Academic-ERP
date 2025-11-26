package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.util.List;
import java.util.ArrayList;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.admin.EditExistingCourses;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import erp.ui.common.NavButton;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedTextField;


public class EditComponents extends JFrame {

    public class SectionInfo {
        public int sectionID;
        public String courseID;
        public String instructorID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    private int sectionID;
    private String instrID;
    private String displayName;
    private final List<RoundedTextField[]> fields = new ArrayList<>();
    private RoundedPanel form;
    private JPanel formRows;

    public EditComponents(String instrID, int SectionID, String displayName) {
        this.instrID = instrID;
        this.sectionID = SectionID;
        this.displayName = displayName;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP - Manage Components (Section " + sectionID + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // Sidebar
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 24, 8));

        JPanel avatarWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        avatarWrap.setOpaque(false);
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(96, 96); }
        };
        avatarWrap.add(avatar);
        profile.add(avatarWrap);
        profile.add(Box.createVerticalStrut(14));

        JLabel name = new JLabel(displayName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        String department = getDepartment(instrID);
        JLabel meta = new JLabel("Department: " + department);
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(Box.createVerticalStrut(6));
        profile.add(meta);

        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_LIGHT, 1),
                new EmptyBorder(12, 10, 28, 10)
        ));
        sidebar.add(profile, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton homeBtn = new NavButton("Home", false);
        homeBtn.addActionListener(e -> { new InstructorDashboard(instrID, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionBtn = new NavButton("My Sections", true);
        sectionBtn.addActionListener(e -> { new MySections().setVisible(true); dispose(); });
        nav.add(sectionBtn);
        nav.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        nav.add(new NavButton("Settings", false));
        nav.add(Box.createVerticalStrut(8));

        NavButton logoutBtn = new NavButton("Log Out", false);
        logoutBtn.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("📚  Manage Components");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + displayName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // ---------- MAIN CONTENT ----------
        JPanel main = new JPanel();
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(32, 40, 32, 40));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        root.add(new JScrollPane(main), BorderLayout.CENTER);

        // --- Section Header (simple text, NOT card) ---
        SectionInfo section = getSectionDetails(sectionID);
        String courseName = getCourseName(section.courseID);

        JLabel title = new JLabel(courseName + " (" + section.courseID + ")");
        title.setFont(FontKit.bold(22f));
        title.setForeground(TEXT_900);
        main.add(title);

        JLabel sectionmeta = new JLabel(
                "Semester: " + section.semester + " " + section.year +
                " | Schedule: " + section.dayTime +
                " | Room: " + section.room +
                " | Capacity: " + section.capacity
        );
        sectionmeta.setFont(FontKit.regular(15f));
        sectionmeta.setForeground(TEXT_600);
        main.add(sectionmeta);

        // Divider

        main.add(Box.createVerticalStrut(16));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(60, 120, 116));
        sep2.setMaximumSize(new Dimension(1500, 1));
        main.add(sep2); 
        main.add(Box.createVerticalStrut(24));

        // ---------- FORM ----------
        RoundedPanel form = new RoundedPanel(20);
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(32, 40, 32, 40));
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Fields
        List<String[]> existing = getComponents(sectionID);
        for(String[] comp : existing) {
            RoundedTextField component   = new RoundedTextField(20, "Enter component name");
            RoundedTextField weight = new RoundedTextField(20, "Enter weight (%)");
            RoundedTextField[] row = new RoundedTextField[]{component, weight};
            fields.add(row);
            gc.gridy++;
            gc.gridx = 0;
            form.add(component, gc);
            gc.gridx = 1;
            form.add(weight, gc);
            //button to remove component
            RoundedButton deleteBtn = new RoundedButton("Remove");
            deleteBtn.addActionListener(e -> {
                fields.remove(row);
                form.remove(component);
                form.remove(weight);
                form.remove(deleteBtn);
                form.revalidate();
                form.repaint();
                deletecomponent(sectionID, component.getText().trim());
            });
            loadcomponent(comp, component, weight);
            gc.gridx = 2;
            form.add(deleteBtn, gc);
        }
        gc.gridy = 0;
        gc.gridx = 2;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.EAST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btns.setOpaque(false);

        RoundedButton cancel = new RoundedButton("Cancel");
        cancel.addActionListener(e -> {
            new ManageComponents(instrID, sectionID, displayName).setVisible(true);
            dispose();
        });
        form.add(btns, gc);
        main.add(form);
        main.add(Box.createVerticalStrut(24));

        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON - Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.NORTH);
        }

    }

    private void loadcomponent(String[] comp, RoundedTextField component, RoundedTextField weight) {
        component.setText(comp[0]);
        weight.setText(comp[1]);
    }

    private void deletecomponent(int sectionId, String componentName) {
        String sql = "DELETE FROM section_components WHERE section_id = ? AND component_name = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.setString(2, componentName);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Component removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Component not found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing component", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAll(List<RoundedTextField[]> fields) {
        String sql = "INSERT INTO section_components (section_id, component_name, weight) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (RoundedTextField[] row : fields) {
                String comp = row[0].getText().trim();
                String wStr = row[1].getText().trim();
                if (comp.isEmpty() || wStr.isEmpty()) continue;

                float weight = Float.parseFloat(wStr);

                stmt.setInt(1, sectionID);
                stmt.setString(2, comp);
                stmt.setFloat(3, weight);
                stmt.addBatch();
            }

            stmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Components added!", "Success", JOptionPane.INFORMATION_MESSAGE);
            new ManageComponents(instrID, sectionID, displayName).setVisible(true);
            dispose();

        } 
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    List<String[]> getComponents(int sectionId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT component_name, weight FROM section_components WHERE section_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("component_name"),
                        String.valueOf(rs.getFloat("weight"))
                    });
                }
            }
        } 
        catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return list;
    }

    SectionInfo getSectionDetails(int sectionId) {
        SectionInfo section = new SectionInfo();
        String sql = "SELECT * FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    section.sectionID = rs.getInt("section_id");
                    section.courseID = rs.getString("course_id");
                    section.instructorID = rs.getString("instructor_id");
                    section.dayTime = rs.getString("day_time");
                    section.semester = rs.getString("semester");
                    section.year = rs.getInt("year");
                    section.room = rs.getString("room");
                    section.capacity = rs.getInt("capacity");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return section;
    }

    String getCourseName(String courseId) {
        String name = "Unknown Course";
        String sql = "SELECT title FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("title");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }

    String getDepartment(String instructorId) {
        String dept = "None"; // default

        String sql = "SELECT department FROM instructors WHERE instructor_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, Long.parseLong(instructorId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dept = rs.getString("department");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dept;
    }
}

