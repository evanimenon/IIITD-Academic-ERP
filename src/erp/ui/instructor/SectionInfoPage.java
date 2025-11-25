package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;

import java.util.List;
import java.util.ArrayList;
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


public class SectionInfoPage extends JFrame {

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

    public SectionInfoPage(String instrID, int sectionID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP - Dashboard");
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

        NavButton homeBtn = new NavButton("  🏠  Home", false);
        homeBtn.addActionListener(e -> { new InstructorDashboard(instrID, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionBtn = new NavButton("  📚  My Sections", true);
        sectionBtn.addActionListener(e -> { new MySections(instrID, displayName).setVisible(true); dispose(); });
        nav.add(sectionBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton gradeBtn = new NavButton("  ✒️  Grade Students", false);
        gradeBtn.addActionListener(e -> { new GradeStudents(instrID,displayName).setVisible(true); dispose(); });
        nav.add(gradeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton classStatBtn= new NavButton("  📊  Class Stats", false);
        classStatBtn.addActionListener(e -> { new ClassStats(instrID,displayName).setVisible(true); dispose(); });
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

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("📚  My Sections");
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
        main.setBorder(new EmptyBorder(24, 32, 24, 32));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));

        // Fetch details
        SectionInfo section = getSectionDetails(sectionID);
        String courseName = getCourseName(section.courseID);

        // --- SECTION HEADER (Simple, text only) ---
        JLabel sectionTitleLabel = new JLabel(courseName + " (" + section.courseID + ")");
        sectionTitleLabel.setFont(FontKit.bold(24f));
        sectionTitleLabel.setForeground(TEXT_900);
        main.add(sectionTitleLabel);

        main.add(Box.createVerticalStrut(8));

        JLabel sectionMeta = new JLabel(
                "Semester: " + section.semester + " " + section.year +
                "   |   Schedule: " + section.dayTime +
                "   |   Room: " + section.room +
                "   |   Capacity: " + section.capacity
        );
        sectionMeta.setFont(FontKit.regular(16f));
        sectionMeta.setForeground(TEXT_600);
        main.add(sectionMeta);

        main.add(Box.createVerticalStrut(24));

        // --- DIVIDER ---
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBackground(new Color(210, 214, 218)); // softer
        main.add(divider);

        main.add(Box.createVerticalStrut(24));

        List<String> students = getEnrolledStudents(sectionID);

        RoundedPanel studentsCard = new RoundedPanel(20);
        studentsCard.setBackground(CARD);
        studentsCard.setLayout(new BorderLayout());

        // === Banner Header ===

        RoundedPanel studentbanner = new RoundedPanel(24);
        studentbanner.setBackground(TEAL_DARK);
        studentbanner.setBorder(new EmptyBorder(12, 20, 12, 20));
        studentbanner.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));

        JLabel bannerLabel = new JLabel("Enrolled Students (" + students.size() + ")");
        bannerLabel.setFont(FontKit.semibold(18f));
        bannerLabel.setForeground(Color.WHITE);
        studentbanner.add(bannerLabel);

        studentsCard.add(studentbanner, BorderLayout.NORTH);

        // === Table data (ID only for now) ===
        String[] columnNames = {"Student ID", "Final Grade"};
        String[][] rowData = new String[students.size()][1];

        for (int i = 0; i < students.size(); i++) {
            rowData[i][0] = students.get(i);
        }

        // Table setup
        JTable table = new JTable(rowData, columnNames);
        table.setFont(FontKit.regular(15f));
        table.setForeground(TEXT_900);
        table.setRowHeight(28);
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);

        // Header styling
        JTableHeader tableHeader = table.getTableHeader();
        tableHeader.setBackground(new Color(245, 245, 245));
        tableHeader.setFont(FontKit.semibold(15f));
        tableHeader.setForeground(TEXT_900);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        studentsCard.add(scrollPane, BorderLayout.CENTER);

        // Add to main content
        main.add(studentsCard);
        root.add(main, BorderLayout.CENTER);

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
    }

    //get section details
    SectionInfo getSectionDetails(int sectionID) {
        SectionInfo section = null;
        String sql = "SELECT * FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    section = new SectionInfo();
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
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return section;
    }

    //get students enrolled in the section
    List<String> getEnrolledStudents(int sectionID) {
        List<String> students = new ArrayList<>();
        String sql = "SELECT student_id FROM enrollments WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(rs.getString("student_id"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return students;
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

