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
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ManualGradingPage extends JFrame {
    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    private int sectionID;
    private JTable table;

    public ManualGradingPage(String instrID, int sectionID, String displayName) {
        this.sectionID = sectionID;
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

        String sql =
                "SELECT s.student_id, sc.component_id, sc.component_name, sc.weight, g.score " +
                "FROM students s " +
                "JOIN enrollments e ON s.student_id = e.student_id " +
                "JOIN section_components sc ON sc.section_id = e.section_id " +
                "LEFT JOIN grades g ON g.enrollment_id = e.enrollment_id AND sc.component_id = g.component_id " +
                "WHERE e.section_id = ? " +
                "ORDER BY s.student_id, sc.component_id";

        Map<String, Map<String, String>> tableMap = new LinkedHashMap<>();
        List<String> componentNames = new ArrayList<>();

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String sid = rs.getString("student_id");
                    String compName = rs.getString("component_name");
                    String score = rs.getString("score");

                    tableMap.putIfAbsent(sid, new LinkedHashMap<>());
                    tableMap.get(sid).put(compName, score == null ? "" : score);

                    if (!componentNames.contains(compName)) {
                        componentNames.add(compName);
                    }
                }
            }

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        String[] cols = getComponents(sectionID);
        String[][] data = new String[tableMap.size()][cols.length];
        int r = 0;

        for (String sid : tableMap.keySet()) {
            data[r][0] = sid;
            Map<String, String> row = tableMap.get(sid);

            for (int c = 1; c < cols.length - 1; c++) {
                String compName = cols[c];
                data[r][c] = row.getOrDefault(compName, "");
            }

            double finalGrade = calculateFinalGrade(sid, sectionID);
            data[r][cols.length - 1] = String.valueOf(finalGrade);

            r++;
        }

        // JTable
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int row, int col) {
                if (Maintenance.isOn()) return false;
                return col != 0 && col != getColumnCount() - 1;
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(FontKit.regular(14f));

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new EmptyBorder(16, 16, 16, 16));

        main.add(sp, BorderLayout.CENTER);


        // Save Button
        RoundedButton saveBtn = new RoundedButton("💾 Save Grades");
        saveBtn.setFont(FontKit.semibold(16f));
        saveBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        saveBtn.setFocusPainted(false);
        saveBtn.setBackground(new Color(230, 245, 230));

        if (Maintenance.isOn()) saveBtn.setEnabled(false);

        saveBtn.addActionListener(e -> {
            saveAll();
            JOptionPane.showMessageDialog(this, "Grades updated.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            new ManualGradingPage(instrID, sectionID, displayName).setVisible(true);
            dispose();
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

    double calculateFinalGrade(String studentId, int sectionId) {
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

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (finalGrade < 0) finalGrade = 0;
        return finalGrade;
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

    private void saveAll() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        // SQL for inserting/updating component grades (grades table)
        String updateGradeSQL =
            "INSERT INTO grades (enrollment_id, component_id, score) VALUES (?, ?, ?) " +
            "ON DUPLICATE KEY UPDATE score = VALUES(score)";

        // SQL for updating final grade (enrollments table)
        String updateFinalGradeSQL = 
            "UPDATE enrollments SET final_grade = ? WHERE student_id = ? AND section_id = ?";

        Connection conn = null;
        
        try {
            conn = DatabaseConnection.erp().getConnection();
            conn.setAutoCommit(false); // Start transaction

            // batch Component Grade Updates
            try (PreparedStatement gradeStmt = conn.prepareStatement(updateGradeSQL)) {
                for (int r = 0; r < rowCount; r++) {
                    String studentId = model.getValueAt(r, 0).toString();
                    String enrollmentId = getEnrollmentID(studentId, sectionID);

                    if (enrollmentId == null) continue;

                    // Loop through editable component columns
                    for (int c = 1; c < colCount - 1; c++) {
                        Object scoreObj = model.getValueAt(r, c);
                        // Only process cells that have a score entered
                        if (scoreObj == null || scoreObj.toString().trim().isEmpty()) continue;

                        try {
                            double score = Double.parseDouble(scoreObj.toString());
                            String componentName = model.getColumnName(c);
                            
                            // Check for valid score range (e.g., 0-100) if necessary
                            if (score < 0 || score > 100) {
                                JOptionPane.showMessageDialog(table, "Invalid value for score (must be between 0-100)", "Error", JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            int componentId = getcomponentID(componentName, sectionID);
                            if (componentId == -1) continue;

                            gradeStmt.setString(1, enrollmentId);
                            gradeStmt.setInt(2, componentId);
                            gradeStmt.setDouble(3, score);
                            gradeStmt.addBatch();
                        } 
                        catch (NumberFormatException nfe) {
                            JOptionPane.showMessageDialog(table, "Invalid format for score.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
                // Execute all component grade updates
                gradeStmt.executeBatch(); 
            }

            // Update Final Grade for Each Student run after the component updates are prepared (but before commit)
            try (PreparedStatement finalGradeStmt = conn.prepareStatement(updateFinalGradeSQL)) {
                for (int r = 0; r < rowCount; r++) {
                    String studentId = model.getValueAt(r, 0).toString();
                    conn.commit(); 
                    
                    double finalGrade = calculateFinalGrade(studentId, sectionID);
                    
                    // Batch final grade updates (optional, but good practice)
                    finalGradeStmt.setDouble(1, finalGrade);
                    finalGradeStmt.setString(2, studentId);
                    finalGradeStmt.setInt(3, sectionID);
                    finalGradeStmt.addBatch();
                    
                    // Update table model for immediate UI refresh (before the page reloads)
                    model.setValueAt(String.valueOf(finalGrade), r, colCount - 1);
                }
                finalGradeStmt.executeBatch();
            }
            
            conn.commit(); // Commit all final grade updates (assuming this is required)

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back due to error.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Error rolling back transaction: " + rollbackEx.getMessage());
                }
            }
            JOptionPane.showMessageDialog(table, "Failed to save grades due to a database error.", "Error",
                        JOptionPane.ERROR_MESSAGE);
        }
        finally {
            // Ensure connection is closed if not closed by the try-with-resources block (if it was created successfully)
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
