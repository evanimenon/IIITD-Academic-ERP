package erp.ui.instructor;

import erp.db.DatabaseConnection;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedButton;

// Auth helpers
import erp.auth.AuthContext;
import erp.auth.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InstructorDashboard extends JFrame {

    // Palette (match student UI)
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    private JToggleButton bellToggle;
    private JPopupMenu notificationsPopup;

    public static class SectionInfo {
        public int sectionID;
        public String courseID;
        public String instructorID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    // Backward compat
    public InstructorDashboard(String displayName) {
        this(null, displayName);
    }

    public InstructorDashboard(String instrID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setTitle("IIITD ERP – Instructor Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Full-screen, like student UI
        setMinimumSize(new Dimension(1200, 800));
        setSize(1200, 800);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        // Authorization guard: only allow INSTRUCTOR role to proceed
        Role actual = AuthContext.getRole();
        if (actual != Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access the Instructor Dashboard."
            );
            AuthContext.clear();
            new LoginPage().setVisible(true);
            dispose();
            return;
        }

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // ---- Sidebar (match StudentFrameBase look) ----
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile block
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

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

        // Nav (no emojis, hover pill style)
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Only this page exists here, so "Home" = selected
        NavButton homeBtn = new NavButton("Home", true);
        homeBtn.addActionListener(e -> {
            // no-op / self – or reload
        });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionsBtn = new NavButton("My Sections", false);
        sectionsBtn.addActionListener(e -> {
            new MySections(instrID, displayName).setVisible(true);
            dispose();
        });
        nav.add(sectionsBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton gradeBtn = new NavButton("Grade Students", false);
        gradeBtn.addActionListener(e -> {
            new GradeStudents(instrID, displayName).setVisible(true);
            dispose();
        });
        nav.add(gradeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton statsBtn = new NavButton("Class Stats", false);
        statsBtn.addActionListener(e -> {
            new ClassStats(instrID, displayName).setVisible(true);
            dispose();
        });
        nav.add(statsBtn);
        nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(32));

        NavButton settingsBtn = new NavButton("Settings", false);
        nav.add(settingsBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton logoutBtn = new NavButton("Log Out", false);
        logoutBtn.addActionListener(e -> {
            AuthContext.clear();
            new LoginPage().setVisible(true);
            dispose();
        });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // ---- Main area ----
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
        h1.setFont(FontKit.bold(28f));
        heroLeft.add(h1);

        JLabel subtitleLbl = new JLabel("Manage your sections, grades, and class performance in one place.");
        subtitleLbl.setForeground(new Color(210, 233, 229));
        subtitleLbl.setFont(FontKit.regular(15f));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(subtitleLbl);
        hero.add(heroLeft, BorderLayout.CENTER);

        // Right side of hero: custom bell icon (no emoji)
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        heroRight.setOpaque(false);

        bellToggle = new JToggleButton() {
            {
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setOpaque(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setPreferredSize(new Dimension(40, 40));
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // hover/selected background pill
                if (getModel().isRollover() || isSelected()) {
                    g2.setColor(new Color(255, 255, 255, 40));
                    g2.fillRoundRect(0, 0, w, h, 20, 20);
                }

                // bell icon
                int size = Math.min(w, h) - 14;
                int cx = (w - size) / 2;
                int cy = (h - size) / 2;

                g2.setColor(Color.WHITE);
                int bodyH = (int) (size * 0.65);
                g2.fillRoundRect(cx + 4, cy + 2, size - 8, bodyH, size / 2, size / 2);
                g2.fillRoundRect(cx + 2, cy + bodyH - 2, size - 4, 6, 6, 6);  // rim
                g2.fillOval(cx + size / 2 - 4, cy + bodyH + 2, 8, 8);        // clapper

                g2.dispose();
            }
        };
        bellToggle.addActionListener(e -> toggleNotifications());
        heroRight.add(bellToggle);
        hero.add(heroRight, BorderLayout.EAST);

        // Center stack (hero + metrics + sections), scrollable
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.add(Box.createVerticalStrut(8));
        centerStack.add(hero);
        centerStack.add(Box.createVerticalStrut(16));
        centerStack.add(buildMetricsRow());
        centerStack.add(Box.createVerticalStrut(16));
        centerStack.add(buildSectionsCard(instrID, displayName));

        JScrollPane sc = new JScrollPane(centerStack);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);
        main.add(sc, BorderLayout.CENTER);
    }

    // ---- Metrics row (similar to student dashboard) ----
    private JComponent buildMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        row.add(metricCard("0", "Sections this term"));
        row.add(metricCard("0", "Unique students"));
        row.add(metricCard("-", "Pending grading"));

        return row;
    }

    private RoundedPanel metricCard(String value, String label) {
        RoundedPanel p = new RoundedPanel(18);
        p.setBackground(CARD);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 22, 20, 22));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        JLabel v = new JLabel(value);
        v.setFont(FontKit.bold(20f));
        v.setForeground(TEXT_900);
        p.add(v, g);
        g.gridy = 1;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.regular(13f));
        l.setForeground(TEXT_600);
        p.add(l, g);
        return p;
    }

    // ---- Sections card ----
    private JComponent buildSectionsCard(String instructorID, String displayName) {
        List<SectionInfo> sections = fetchSectionsForInstructor(instructorID);

        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("My Sections");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("All sections assigned to you this academic year");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);
        card.add(Box.createVerticalStrut(10), BorderLayout.CENTER);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            list.add(Box.createVerticalStrut(12));
            list.add(empty);
        } else {
            for (SectionInfo sec : sections) {
                RoundedPanel secCard = new RoundedPanel(18);
                secCard.setBackground(new Color(248, 249, 250));
                secCard.setBorder(new EmptyBorder(16, 18, 16, 18));
                secCard.setLayout(new BorderLayout());

                JPanel left = new JPanel();
                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                String titleText = sec.courseID + " – Section " +
                        String.format("%02d", sec.sectionID) + ": " + getCourseName(sec.courseID);
                JLabel secTitle = new JLabel(titleText);
                secTitle.setFont(FontKit.bold(15f));
                secTitle.setForeground(TEXT_900);

                String metaLine = "Room: " + sec.room + "  •  Time: " + sec.dayTime +
                        "  •  " + sec.semester + " " + sec.year;
                JLabel secMeta = new JLabel(metaLine);
                secMeta.setFont(FontKit.regular(13f));
                secMeta.setForeground(TEXT_600);

                left.add(secTitle);
                left.add(Box.createVerticalStrut(4));
                left.add(secMeta);

                secCard.add(left, BorderLayout.CENTER);

                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
                actions.setOpaque(false);

                RoundedButton viewBtn = new RoundedButton("View Section");
                viewBtn.addActionListener(e ->
                        new SectionInfoPage(instructorID, sec.sectionID, displayName).setVisible(true)
                );
                viewBtn.setBackground(TEAL);
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(FontKit.semibold(13f));
                viewBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                actions.add(viewBtn);

                secCard.add(actions, BorderLayout.EAST);

                secCard.setAlignmentX(Component.LEFT_ALIGNMENT);
                list.add(secCard);
                list.add(Box.createVerticalStrut(12));
            }
        }

        card.add(list, BorderLayout.CENTER);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    // ---- Notifications (floating popup like student) ----
    private void toggleNotifications() {
        if (notificationsPopup == null) {
            notificationsPopup = buildNotificationsPopup();
        }

        if (notificationsPopup.isVisible()) {
            notificationsPopup.setVisible(false);
        } else {
            notificationsPopup.show(bellToggle, -320, bellToggle.getHeight() + 8);
        }
    }

    private JPopupMenu buildNotificationsPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.setOpaque(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Notifications");
        title.setFont(FontKit.bold(16f));
        title.setForeground(TEXT_900);

        JLabel markAll = new JLabel("Mark all as read");
        markAll.setFont(FontKit.regular(12f));
        markAll.setForeground(TEAL);
        markAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(title, BorderLayout.WEST);
        header.add(markAll, BorderLayout.EAST);
        container.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(notifItem("Timetable update for CSE101", "Today, 9:00 AM"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Grade submission deadline reminder", "Yesterday"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("ERP maintenance window 11 PM–1 AM", "2 days ago"));

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(360, 260));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getViewport().setBackground(Color.WHITE);

        container.add(sp, BorderLayout.CENTER);
        popup.add(container);

        return popup;
    }

    private JComponent notifItem(String header, String when) {
        RoundedPanel p = new RoundedPanel(14);
        p.setBackground(new Color(248, 249, 250));
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

    // ---- Data helpers ----
    private static String todayString() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() + " " +
                d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " +
                d.getYear();
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
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
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }

    // Shared nav button style (clone of StudentFrameBase.NavButton)
    public static class NavButton extends JButton {
        private final boolean selected;
        private boolean hover;

        public NavButton(String text, boolean selected) {
            super(text);
            this.selected = selected;
            setHorizontalAlignment(LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(FontKit.semibold(15f));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }
                @Override public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (hover || selected) {
                g2.setColor(new Color(255, 255, 255, selected ? 70 : 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

                g2.setColor(new Color(204, 252, 246, 190));
                g2.fillRoundRect(4, 6, 4, getHeight() - 12, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new InstructorDashboard("1000001", "Instructor 123").setVisible(true));
    }
}
