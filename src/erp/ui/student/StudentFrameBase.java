// StudentFrameBase.java
package erp.ui.student;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public abstract class StudentFrameBase extends JFrame {

    // Shared palette
    protected static final Color TEAL_DARK = new Color(39, 96, 92);
    protected static final Color TEAL = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG = new Color(246, 247, 248);

    public enum Page {
        HOME, CATALOG, REGISTRATIONS, TIMETABLE
    }

    // Persistent student id across all student frames
    protected static String currentStudentId;

    protected final String userDisplayName;
    protected final String studentId;
    protected final JPanel root = new JPanel(new BorderLayout());
    protected JLabel metaLabel;

    protected StudentFrameBase(String studentId, String userDisplayName, Page activePage) {

        // Update global id if non-null provided
        if (studentId != null && !studentId.isBlank()) {
            currentStudentId = studentId;
        }

        // Always use the latest known id
        this.studentId = currentStudentId;
        this.userDisplayName = userDisplayName;

        System.out.println("[DEBUG] StudentFrameBase: using studentId = '" + this.studentId + "'");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        FontKit.init();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("IIITD ERP");
        setSize(1200, 800);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);

        root.setBackground(BG);
        setContentPane(root);

        root.add(buildSidebar(activePage), BorderLayout.WEST);
        root.add(buildBody(), BorderLayout.CENTER);
    }

    private JComponent buildSidebar(Page active) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // profile
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(userDisplayName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        metaLabel = new JLabel("Year, Program");
        metaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaLabel.setForeground(new Color(210, 225, 221));
        metaLabel.setFont(FontKit.regular(14f));
        profile.add(metaLabel);

        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_LIGHT, 1),
                new EmptyBorder(8, 8, 32, 8)));

        sidebar.add(profile, BorderLayout.NORTH);

        // nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton home = new NavButton("  🏠  Home", active == Page.HOME);
        home.addActionListener(e -> {
            if (active != Page.HOME) {
                System.out.println("[DEBUG] NAV → Dashboard (studentId=" + studentId + ")");
                new StudentDashboard(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(home);
        nav.add(Box.createVerticalStrut(8));

        NavButton catalog = new NavButton("  📚  Course Catalogue", active == Page.CATALOG);
        catalog.addActionListener(e -> {
            if (active != Page.CATALOG) {
                System.out.println("[DEBUG] NAV → Catalog (studentId=" + studentId + ")");
                new CourseCatalog(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(catalog);
        nav.add(Box.createVerticalStrut(8));

        NavButton regs = new NavButton("  📜  My Registrations", active == Page.REGISTRATIONS);
        regs.addActionListener(e -> {
            if (active != Page.REGISTRATIONS) {
                System.out.println("[DEBUG] NAV → Registrations (studentId=" + studentId + ")");
                new MyRegistrationsFrame(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(regs);
        nav.add(Box.createVerticalStrut(8));

        NavButton tt = new NavButton("  🗺️  Time Table", active == Page.TIMETABLE);
        tt.addActionListener(e -> {
            if (active != Page.TIMETABLE) {
                System.out.println("[DEBUG] NAV → Timetable (studentId=" + studentId + ")");
                new StudentTimetableFrame(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(tt);
        nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        NavButton logout = new NavButton("  🚪  Log Out", false);
        logout.addActionListener(e -> {
            currentStudentId = null;
            new LoginPage().setVisible(true);
            dispose();
        });
        nav.add(logout);

        sidebar.add(nav, BorderLayout.CENTER);
        return sidebar;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(24, 24, 24, 24));
        body.add(buildMainContent(), BorderLayout.CENTER);
        return body;
    }

    protected abstract JComponent buildMainContent();

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
}
