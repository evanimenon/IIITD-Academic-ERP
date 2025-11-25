package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import erp.auth.AuthContext;
import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedPanel;

public class CSVUploadPage extends JFrame {

    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    private int sectionID;

    public CSVUploadPage(String instrID, int sectionID, String displayName) {
        this.sectionID = sectionID;
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP – Upload Grades (CSV)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // --- Sidebar ---
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
        logoutBtn.addActionListener(e -> { AuthContext.clear(); new LoginPage().setVisible(true); dispose(); });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("📁 Upload CSV - Bulk Grading");
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

            JLabel msg = new JLabel("⚠️ Maintenance Mode is ON - Changes are Disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.SOUTH);
        }

        // --- MAIN CONTENT ---
        JPanel main = new JPanel(new GridBagLayout());
        main.setBackground(BG);

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(30, 40, 40, 40));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Upload Grades CSV");
        title.setFont(FontKit.bold(22f));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);

        card.add(Box.createVerticalStrut(16));

        JLabel note = new JLabel("<html>Expected format:<br>"
                + "<b>student_id, midsem, quiz1, quiz2, …, endsem (your defined components)</b></html>");
        note.setFont(FontKit.regular(15f));
        note.setForeground(Color.GRAY);
        note.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(note);

        card.add(Box.createVerticalStrut(30));

        JButton uploadBtn = new JButton("📁 Choose CSV File");
        uploadBtn.setFont(FontKit.semibold(16f));
        uploadBtn.setFocusPainted(false);
        uploadBtn.setBackground(new Color(230, 240, 250));
        uploadBtn.setBorder(new EmptyBorder(12, 20, 12, 20));
        uploadBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        if (Maintenance.isOn()) {
            uploadBtn.setEnabled(false);
        }

        uploadBtn.addActionListener(e -> handleUpload(instrID));
        card.add(uploadBtn);

        main.add(card);
        root.add(main, BorderLayout.CENTER);
    }

    private void handleUpload(String instrID) {
        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();

        try (Connection conn = DatabaseConnection.erp().getConnection()) {
            conn.setAutoCommit(false);

            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) throw new RuntimeException("File empty");

            String[] columns = lines.get(0).split(","); // header
            int componentCount = columns.length - 1;

            String sql = "INSERT INTO grades (enrollment_id, component_id, score) " +
                        "VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE score = VALUES(score)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (int i = 1; i < lines.size(); i++) {
                    String[] values = lines.get(i).split(",");
                    String studentID = values[0].trim();

                    String enrollID = getEnrollmentID(studentID, sectionID);
                    if (enrollID == null) {
                        throw new Exception("Student not enrolled: " + studentID);
                    }

                    for (int j = 1; j < columns.length; j++) {
                        String markStr = values[j].trim();
                        if (markStr.isEmpty()) continue;

                        double mark = Double.parseDouble(markStr);

                        int componentID = getcomponentID(columns[j].trim(), sectionID);
                        if (componentID == -1) {
                            throw new Exception("Unknown component: " + columns[j]);
                        }

                        stmt.setString(1, enrollID);
                        stmt.setInt(2, componentID);
                        stmt.setDouble(3, mark);
                        stmt.addBatch();
                    }
                }

                stmt.executeBatch();
                conn.commit();
            }
            
            for (int i = 1; i < lines.size(); i++) {
                String[] values = lines.get(i).split(",");
                String studentID = values[0].trim();
                int finalGrade = calculateFinalGrade(studentID, sectionID);
                insertfinalGrade(studentID, sectionID, finalGrade);
            }

            JOptionPane.showMessageDialog(this,
                    "Grades uploaded and updated successfully.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                
            

        } 
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Upload Failed", JOptionPane.ERROR_MESSAGE);
        }
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

    String getDepartment(String instructorId) {
        String dept = "None";
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
                        continue;
                    }

                    finalGrade += (score * weightage) / 100.0;
                }
            }

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        //fixing negative values
        if (finalGrade < 0) finalGrade = 0;
        return (int) Math.round(finalGrade);
    }

    private void insertfinalGrade(String studentId, int sectionId, double finalGrade) {
        String sql = "UPDATE enrollments SET final_grade = ? WHERE student_id = ? AND section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, finalGrade);
            stmt.setString(2, studentId);
            stmt.setInt(3, sectionId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                System.out.println("No final grade uploaded for " + studentId + " section " + sectionId);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
  
}
