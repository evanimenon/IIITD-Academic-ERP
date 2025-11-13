package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class StudentDashboard extends JFrame {

    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    // DB-derived
    private String program = "Program";
    private String yearStr = "Year";

    // UI refs
    private JPanel rightDock;           // notifications dock
    private JToggleButton bellToggle;   // toggle button

    // Backward compat
    public StudentDashboard(String displayName) {
        this(null, displayName);
    }

    public StudentDashboard(String studentId, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        if (studentId != null && !studentId.isBlank()) fetchStudentMeta(studentId);

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

        JLabel meta = new JLabel(yearStr + ", " + program);
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
        homeBtn.addActionListener(e -> { new StudentDashboard(studentId, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton catalogueBtn = new NavButton("  📚  Course Catalogue", false);
        catalogueBtn.addActionListener(e -> { new CourseCatalogue(displayName).setVisible(true); dispose(); });
        nav.add(catalogueBtn);
        nav.add(Box.createVerticalStrut(8));

        nav.add(new NavButton("  📜  My Registrations", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🗺️  Time Table", false));
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

        JLabel subtitle = new JLabel("Always stay updated in your student portal");
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

        RoundedPanel card1 = metricCard("Text1", "Subtitle");
        RoundedPanel card2 = metricCard("Text2", "Subtitle");
        RoundedPanel card3 = metricCard("Text3", "Subtitle");
        gc.gridx = 0; gc.gridy = 0; grid.add(card1, gc);
        gc.gridx = 1; gc.gridy = 0; grid.add(card2, gc);
        gc.gridx = 2; gc.gridy = 0; grid.add(card3, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 3; gc.weighty = 1;
        grid.add(enrolledCoursesStrip(), gc);

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

    private void fetchStudentMeta(String studentId) {
        final String sql = "SELECT program, year FROM erp_db.students WHERE student_id = ? OR roll_no = ?";
        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    program = rs.getString("program");
                    yearStr = String.valueOf(rs.getInt("year"));
                }
            }
        } catch (Exception ignored) {}
    }

    private static String todayString() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + d.getYear();
    }

    private RoundedPanel metricCard(String value, String label) {
        RoundedPanel p = new RoundedPanel(18);
        p.setBackground(CARD);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 22, 20, 22));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
        JLabel v = new JLabel(value);
        v.setFont(FontKit.bold(18f));
        v.setForeground(TEXT_900);
        p.add(v, g);
        g.gridy = 1;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.regular(13f));
        l.setForeground(TEXT_600);
        p.add(l, g);
        return p;
    }

    private JPanel enrolledCoursesStrip() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new GridLayout(1, 2, 16, 16));

        RoundedPanel c1 = new RoundedPanel(18);
        c1.setBackground(CARD);
        c1.setLayout(new BorderLayout());
        c1.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel t1 = new JLabel("Advanced Programming");
        t1.setFont(FontKit.semibold(16f));
        t1.setForeground(TEXT_900);
        c1.add(t1, BorderLayout.NORTH);
        JButton view1 = pillButton("View");
        c1.add(new JPanel(new FlowLayout(FlowLayout.LEFT)) {{ setOpaque(false); add(view1); }}, BorderLayout.SOUTH);

        RoundedPanel c2 = new RoundedPanel(18);
        c2.setBackground(CARD);
        c2.setLayout(new BorderLayout());
        c2.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel t2 = new JLabel("Operating Systems");
        t2.setFont(FontKit.semibold(16f));
        t2.setForeground(TEXT_900);
        c2.add(t2, BorderLayout.NORTH);
        JButton view2 = pillButton("View");
        c2.add(new JPanel(new FlowLayout(FlowLayout.LEFT)) {{ setOpaque(false); add(view2); }}, BorderLayout.SOUTH);

        row.add(c1);
        row.add(c2);
        return row;
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

    // Rounded panel with soft shadow
    static class RoundedPanel extends JPanel {
        private final int arc;
        RoundedPanel(int arc) { this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            for (int i = 6; i >= 1; i--) {
                float a = 0.035f * (i / 6f);
                g2.setColor(new Color(0, 0, 0, a));
                g2.fill(new RoundRectangle2D.Double(6 - i, 6 - i, w - 12 + 2*i, h - 12 + 2*i, arc + i, arc + i));
            }
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(6, 6, w - 12, h - 12, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Sidebar button
    public static class NavButton extends JButton {
        private final boolean selected;
        public NavButton(String text, boolean selected) {
            super(text);
            this.selected = selected;
            setHorizontalAlignment(LEFT);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(FontKit.semibold(16f));
            setBorder(new EmptyBorder(10, 14, 10, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (getModel().isRollover() || selected) {
                g2.setColor(new Color(255, 255, 255, selected ? 60 : 30));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 14, 14));
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentDashboard("stu1", "Student 123").setVisible(true));
    }
}
