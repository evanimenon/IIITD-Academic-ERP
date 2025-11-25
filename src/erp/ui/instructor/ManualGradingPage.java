package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedPanel;
import java.util.ArrayList;
import java.util.List;

public class ManualGradingPage extends JFrame {
    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    public ManualGradingPage(String instrID, int sectionID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP - Manual Grading");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
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

        NavButton homeBtn = new NavButton("  🏠  Home", false);
        homeBtn.addActionListener(e -> { new InstructorDashboard(instrID, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionBtn = new NavButton("  📚  My Sections", false);
        sectionBtn.addActionListener(e -> { new MySections(instrID, displayName).setVisible(true); dispose(); });
        nav.add(sectionBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton gradeBtn = new NavButton("  ✒️  Grade Students", true);
        gradeBtn.addActionListener(e -> { new GradeStudents(instrID, displayName).setVisible(true); dispose(); });
        nav.add(gradeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton classStatBtn= new NavButton("  📊  Class Stats", false);
        classStatBtn.addActionListener(e -> { new ClassStats(instrID, displayName).setVisible(true); dispose(); });
        nav.add(classStatBtn);
        nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        nav.add(new NavButton("  ⚙️  Settings", false));
        nav.add(Box.createVerticalStrut(8));

        NavButton logoutBtn = new NavButton("  🚪  Log Out", false);
        logoutBtn.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // Banner
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("✒️ Manual Grading");
        h1.setFont(FontKit.bold(26f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + displayName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // Maintenance banner
        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230));
            banner.setBorder(new EmptyBorder(12, 16, 12, 16));
            JLabel msg = new JLabel("⚠️ Maintenance Mode is ON — Editing Disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);
            root.add(banner, BorderLayout.SOUTH);
        }

        // MAIN TABLE
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));
        // load components for selected section
        String[] cols = getComponents(sectionID);

        // load student data - if students are graded, show grades and allow grade editing. If students are ungraded, allow grading.
        String sql = "SELECT s.student_id " +
                     "FROM students s " +
                     "JOIN enrollments e ON s.student_id = e.student_id " +
                     "WHERE e.section_id = ? " +
                     "ORDER BY s.student_id ASC";
        List<String[]> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    List<String> row = new ArrayList<>();
                    row.add(studentId);
                    // for each component, get grade if exists
                    for (int i = 1; i < cols.length - 1; i++) {
                        String compName = cols[i];
                        String gradeSql = "SELECT g.score " +
                                          "FROM grades g " +
                                          "JOIN enrollments e ON g.enrollment_id = e.enrollment_id " +
                                          "JOIN section_components sc ON g.component_id = sc.component_id " +
                                          "WHERE e.student_id = ? AND e.section_id = ? AND sc.component_name = ?";
                        try (PreparedStatement gradeStmt = conn.prepareStatement(gradeSql)) {
                            gradeStmt.setString(1, studentId);
                            gradeStmt.setInt(2, sectionID);
                            gradeStmt.setString(3, compName);
                            try (ResultSet gradeRs = gradeStmt.executeQuery()) {
                                if (gradeRs.next()) {
                                    row.add(String.valueOf(gradeRs.getDouble("score")));
                                } else {
                                    row.add("");
                                }
                            }
                        }
                    }
                    // calculate final grade
                    int finalGrade = calculateFinalGrade(studentId, sectionID);
                    row.add(String.valueOf(finalGrade));
                    rows.add(row.toArray(new String[0]));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        String[][] data = rows.toArray(new String[0][]);

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int row, int col) {
                if (Maintenance.isOn()) return false;
                return col != 0 && col != 5;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(FontKit.regular(14f));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(16, 16, 16, 16));

        main.add(sp, BorderLayout.CENTER);

        // Save button
        JButton saveBtn = new JButton("💾 Save Grades");
        saveBtn.setFont(FontKit.semibold(16f));
        saveBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        saveBtn.setFocusPainted(false);
        saveBtn.setBackground(new Color(230, 245, 230));

        if (Maintenance.isOn()) saveBtn.setEnabled(false);

        saveBtn.addActionListener(e -> {
            
            JOptionPane.showMessageDialog(this, "Grades updated.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        main.add(saveBtn, BorderLayout.SOUTH);
        root.add(main, BorderLayout.CENTER);
    }

    private String getDepartment(String instructorId) {
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

    String getEnrollmentID(String studentId, int sectionId) {
        String enrollId = null;

        String sql = "SELECT enrollment_id FROM enrollments WHERE student_id = ? AND section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    enrollId = rs.getString("enrollment_id");
                }
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return enrollId;
    }

    int getcomponentID(String componentName, int sectionId) {
        int compId = -1;
        String sql = "SELECT component_id FROM section_components WHERE component_name = ? AND section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, componentName);
            stmt.setInt(2, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    compId = rs.getInt("component_id");
                }
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return compId;
    }

    int calculateFinalGrade(String studentId, int sectionId) {
        double finalGrade = 0.0;

        String sql = "SELECT sc.weight, g.score " +
                    "FROM section_components sc " +
                    "JOIN enrollments e ON sc.section_id = e.section_id " +
                    "LEFT JOIN grades g ON e.enrollment_id = g.enrollment_id " +
                    "AND sc.component_id = g.component_id " +
                    "WHERE e.student_id = ? AND e.section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, studentId);
            stmt.setInt(2, sectionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double weightage = rs.getDouble("weight");

                    Double score = rs.getObject("score", Double.class);
                    if (score == null) {
                        // Skip component if no grade exists yet
                        continue;
                    }

                    finalGrade += (score * weightage) / 100.0;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (finalGrade < 0) finalGrade = 0;
        return (int) Math.round(finalGrade);
    }

    private void insertfinalGrade(String studentId, int sectionId, int finalGrade) {
        String sql = "INSERT INTO grades (final_grade) " +
                     "VALUES (?) "+
                     "ON DUPLICATE KEY UPDATE final_grade = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, finalGrade);
            stmt.setDouble(2, finalGrade);
            stmt.executeUpdate();
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
    }  

    private String[] getComponents(int sectionId) {
        String sql = "SELECT component_name FROM section_components WHERE section_id = ?";
        List<String> components = new ArrayList<>();
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    components.add(rs.getString("component_name"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        // Prepare columns array
        String[] cols = new String[components.size() + 2];
        cols[0] = "Student ID";
        for (int i = 0; i < components.size(); i++) {
            cols[i + 1] = components.get(i);
        }
        cols[cols.length - 1] = "Final Grade";
        return cols;
    }
    
}
