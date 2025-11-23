package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.util.List;
import java.util.ArrayList;

import java.awt.*;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import erp.ui.common.NavButton;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;



public class GradeStudents extends JFrame {

    // ---- Simple local model to avoid DB ----
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
    private String department = "Computer Science"; // TODO: fetch from DB

    public GradeStudents(String instrID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP – Dashboard");
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

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("✒️  Grade Students");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + displayName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);


        // ---------- MAIN CONTENT ----------
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        List<SectionInfo> sections = new ArrayList<>();
        sections = fetchSectionsForInstructor(instrID);

        if(sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(empty);
        }
        else{
            for (SectionInfo sec : sections) {
                RoundedPanel card = new RoundedPanel(20);
                card.setBackground(CARD);
                card.setBorder(new EmptyBorder(20, 24, 20, 24));
                card.setLayout(new BorderLayout());

                // left column
                JPanel left = new JPanel();
                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                JLabel title = new JLabel(sec.courseID + " – " + sec.sectionID);
                title.setFont(FontKit.bold(20f));
                title.setForeground(TEXT_900);

                JLabel subtitle = new JLabel(getCourseName(sec.courseID));
                subtitle.setFont(FontKit.regular(15f));
                subtitle.setForeground(TEXT_600);

                JLabel sem = new JLabel("Semester: " + sec.semester);
                sem.setFont(FontKit.regular(14f));
                sem.setForeground(TEXT_600);

                left.add(title);
                left.add(Box.createVerticalStrut(6));
                left.add(subtitle);
                left.add(Box.createVerticalStrut(4));
                left.add(sem);

                card.add(left, BorderLayout.WEST);

                // right column – action buttons
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
                actions.setOpaque(false);

                RoundedButton csvBtn = new RoundedButton("Upload CSV");
                csvBtn.addActionListener(e -> { new CSVUploadPage(instrID, displayName).setVisible(true); dispose(); });
                csvBtn.setBackground(TEAL);
                csvBtn.setForeground(Color.WHITE);
                csvBtn.setFont(FontKit.semibold(14f));
                csvBtn.setFocusPainted(false);
                csvBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

                RoundedButton manualBtn = new RoundedButton("Manual Grading");
                manualBtn.addActionListener(e -> { new ManualGradingPage(instrID, displayName).setVisible(true); dispose(); });
                manualBtn.setBackground(TEAL_LIGHT);
                manualBtn.setForeground(Color.WHITE);
                manualBtn.setFont(FontKit.semibold(14f));
                manualBtn.setFocusPainted(false);
                manualBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

                actions.add(csvBtn);
                actions.add(manualBtn);

                card.add(actions, BorderLayout.EAST);

                content.add(card);
                content.add(Box.createVerticalStrut(16));
            }
        }
        root.add(new JScrollPane(content), BorderLayout.CENTER);

        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.NORTH);
        }
    }

    private List<SectionInfo> fetchSectionsForInstructor(String instructorId) {
        List<SectionInfo> list = new ArrayList<>();

        String sql = """
            SELECT 
                s.section_id,
                s.course_id,
                s.instructor_id,
                s.day_time,
                s.room,
                s.capacity,
                s.semester,
                s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            WHERE s.instructor_id = ?
        """;

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instructorId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SectionInfo si = new SectionInfo();
                    si.sectionID = rs.getInt("section_id");
                    si.courseID = rs.getString("course_id");
                    si.instructorID = instructorId;
                    si.dayTime = rs.getString("day_time");
                    si.room = rs.getString("room");
                    si.capacity = rs.getInt("capacity");
                    si.semester = rs.getString("semester");
                    si.year = rs.getInt("year");
                    
                    list.add(si);
                }
            }
        }
        catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
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
