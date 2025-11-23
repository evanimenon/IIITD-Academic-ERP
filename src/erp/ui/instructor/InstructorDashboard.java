package erp.ui.instructor;

import erp.db.DatabaseConnection;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Locale;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedPanel;

// Auth helpers
import erp.auth.AuthContext;
import erp.auth.Role;
import erp.ui.common.RoundedButton;
import java.util.List;

public class InstructorDashboard extends JFrame {

    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    // UI refs
    private JPanel rightDock;           // notifications dock
    private JToggleButton bellToggle;   // toggle button

    // Backward compat
    public InstructorDashboard(String displayName) {
        this(null, displayName);
    }

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

    public InstructorDashboard(String instrID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP – Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        // Authorization guard: only allow INSTRUCTOR role to proceed
        Role actual = AuthContext.getRole();
        if (actual != Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(null, "You are not authorized to access the Instructor Dashboard.");
            AuthContext.clear();
            new LoginPage().setVisible(true);
            dispose();
            return;
        }

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

        NavButton homeBtn = new NavButton("  🏠  Home", true);
        homeBtn.addActionListener(e -> { new InstructorDashboard(instrID, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionBtn = new NavButton("  📚  My Sections", false);
        sectionBtn.addActionListener(e -> { new MySections(instrID,displayName).setVisible(true); dispose(); });
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
        logoutBtn.addActionListener(e -> { AuthContext.clear(); new LoginPage().setVisible(true); dispose(); });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // Main stack
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.add(main, BorderLayout.CENTER);

        // Hero
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout(16, 0));

        JPanel heroLeft = new JPanel();
        heroLeft.setOpaque(false);
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));

        JLabel date = new JLabel(todayString());
        date.setForeground(new Color(196, 234, 229));
        date.setFont(FontKit.semibold(14f));
        heroLeft.add(date);
        heroLeft.add(Box.createVerticalStrut(8));

        JLabel h1 = new JLabel("Welcome back, " + displayName + "!");
        h1.setForeground(Color.WHITE);
        h1.setFont(FontKit.bold(30f));
        heroLeft.add(h1);

        JLabel subtitle = new JLabel("Your space for managing courses with ease and precision");
        subtitle.setForeground(new Color(210, 233, 229));
        subtitle.setFont(FontKit.regular(16f));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(subtitle);
        hero.add(heroLeft, BorderLayout.CENTER);

        // Right side of hero: bell toggle
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        heroRight.setOpaque(false);
        bellToggle = new JToggleButton("🔔");
        bellToggle.setFocusPainted(false);
        bellToggle.setBorderPainted(false);
        bellToggle.setContentAreaFilled(false);
        bellToggle.setForeground(Color.WHITE);
        bellToggle.setFont(FontKit.bold(18f));
        bellToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellToggle.addActionListener(e -> toggleNotifications());
        heroRight.add(bellToggle);
        hero.add(heroRight, BorderLayout.EAST);

        // Grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 0;

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 3; gc.weighty = 1;
        grid.add(loadInstrSections(instrID), gc);

        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.add(Box.createVerticalStrut(16));
        centerStack.add(hero);
        centerStack.add(Box.createVerticalStrut(16));
        centerStack.add(grid);

        JScrollPane sc = new JScrollPane(centerStack);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);
        main.add(sc, BorderLayout.CENTER);

        // Right dock (hidden by default)
        rightDock = buildNotificationsDock();
        rightDock.setVisible(false);
        root.add(rightDock, BorderLayout.EAST);
    }

    private void toggleNotifications() {
        boolean show = bellToggle.isSelected();
        rightDock.setVisible(show);
        // keep window size; just re-layout
        rightDock.getParent().revalidate();
    }

    private JPanel buildNotificationsDock() {
        JPanel dock = new JPanel(new BorderLayout());
        dock.setPreferredSize(new Dimension(320, 0));
        dock.setBackground(new Color(250, 251, 252));
        dock.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Notifications");
        title.setFont(FontKit.bold(18f));
        title.setForeground(TEXT_900);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(notifItem("Registration opens for Spring 2026", "Today, 9:00 AM"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Grade publishing window updated", "Yesterday"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Maintenance: ERP downtime 11 PM–1 AM", "2 days ago"));

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(new Color(235, 239, 242)));
        sp.getViewport().setBackground(new Color(250, 251, 252));

        dock.add(title, BorderLayout.NORTH);
        dock.add(sp, BorderLayout.CENTER);
        return dock;
    }

    private JComponent notifItem(String header, String when) {
        RoundedPanel p = new RoundedPanel(14);
        p.setBackground(Color.WHITE);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel h = new JLabel(header);
        h.setFont(FontKit.semibold(14f));
        h.setForeground(TEXT_900);
        JLabel t = new JLabel(when);
        t.setFont(FontKit.regular(12f));
        t.setForeground(TEXT_600);

        p.add(h, BorderLayout.NORTH);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    //TODO: db -> get department from instructor table for display
    private JScrollPane loadInstrSections(String instructorID) {
        List<SectionInfo> sections = fetchSectionsForInstructor(instructorID);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(empty);
        } 
        
        else {
            for (SectionInfo sec : sections) {
                RoundedPanel card = new RoundedPanel(20);
                card.setBackground(CARD);
                card.setBorder(new EmptyBorder(20, 24, 20, 24));
                card.setLayout(new BorderLayout());

                JPanel left = new JPanel();
                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                JLabel title = new JLabel(sec.courseID + " - Section " + String.format("%02d", sec.sectionID) + ": " + getCourseName(sec.courseID));
                title.setFont(FontKit.bold(20f));
                title.setForeground(TEXT_900);

                JLabel subtitle = new JLabel("Room: " + sec.room + " | Time: " + sec.dayTime);
                subtitle.setFont(FontKit.regular(15f));
                subtitle.setForeground(TEXT_600);

                left.add(title);
                left.add(Box.createVerticalStrut(6));
                left.add(subtitle);

                card.add(left, BorderLayout.WEST);

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
                actions.setOpaque(false);

                RoundedButton viewBtn = new RoundedButton("View Section");
                viewBtn.addActionListener(e -> new ViewStats(sec.sectionID).setVisible(true));
                viewBtn.setBackground(TEAL);
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(FontKit.semibold(14f));
                viewBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                actions.add(viewBtn);

                card.add(actions, BorderLayout.EAST);

                content.add(card);
                content.add(Box.createVerticalStrut(16));
            }
        }

        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(null);
        sc.getViewport().setBackground(BG);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        return sc;
    }


    private static String todayString() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + d.getYear();
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

    private JButton pillButton(String text) {
        return new JButton(text) {
            { setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
              setForeground(Color.WHITE); setFont(FontKit.semibold(14f));
              setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
              setBorder(new EmptyBorder(10, 18, 10, 18)); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                super.paintComponent(g);
                g2.dispose();
            }
        };
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
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new InstructorDashboard("stu1", "Instructor 123").setVisible(true));
    }
}
