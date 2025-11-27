// StudentFrameBase.java
package erp.ui.student;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class StudentFrameBase extends JFrame {

    // Shared palette
    protected static final Color TEAL_DARK = new Color(39, 96, 92);
    protected static final Color TEAL = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG = new Color(246, 247, 248);

    // Maintenance-mode flag (read once per frame)
    protected final boolean maintenanceMode;

    public enum Page {
        HOME, CATALOG, REGISTRATIONS, TIMETABLE, SETTINGS
    }

    // Persistent student id across all student frames
    protected static String currentStudentId;

    protected final String userDisplayName;
    protected final String studentId;
    protected final JPanel root = new JPanel(new BorderLayout());
    protected JLabel metaLabel;

    protected StudentFrameBase(String studentId, String userDisplayName, Page activePage) {

        // Update global id if a non-null one is passed
        if (studentId != null && !studentId.isBlank()) {
            currentStudentId = studentId;
        }

        // Always use the latest known id
        this.studentId = currentStudentId;
        this.userDisplayName = userDisplayName;


        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        FontKit.init();
        // Check maintenance flag once
        maintenanceMode = erp.db.MaintenanceService.isMaintenanceOn();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("IIITD ERP");

        // Base size (for smaller screens), but always start maximized
        setSize(1200, 800);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);

        root.setBackground(BG);
        setContentPane(root);

        root.add(buildSidebar(activePage), BorderLayout.WEST);
        root.add(buildBody(), BorderLayout.CENTER);
    }

    // Sidebar with nav + profile
    private JComponent buildSidebar(Page active) {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(260, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // profile
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 100);
            }
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

        // nav buttons
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // HOME
        NavButton home = new NavButton("Home", active == Page.HOME);
        home.addActionListener(e -> {
            if (active != Page.HOME) {
                new StudentDashboard(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(home);
        nav.add(Box.createVerticalStrut(8));

        // REGISTRATION → CourseCatalog
        NavButton registration = new NavButton("Registration", active == Page.CATALOG);
        registration.addActionListener(e -> {
            if (active != Page.CATALOG) {
                new CourseCatalog(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(registration);
        nav.add(Box.createVerticalStrut(8));

        // MY COURSES → MyRegistrationsFrame
        NavButton myCourses = new NavButton("My Courses", active == Page.REGISTRATIONS);
        myCourses.addActionListener(e -> {
            if (active != Page.REGISTRATIONS) {
                new MyRegistrationsFrame(studentId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(myCourses);
        nav.add(Box.createVerticalStrut(8));

        // TIME TABLE
        NavButton tt = new NavButton("Time Table", active == Page.TIMETABLE);
        tt.addActionListener(e -> {
            if (active != Page.TIMETABLE) {
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

        NavButton logout = new NavButton("Log Out", false);
        logout.addActionListener(e -> {
            currentStudentId = null;
            new LoginPage().setVisible(true);
            dispose();
        });
        nav.add(logout);

        NavButton settings = new NavButton("Settings", false);
        settings.addActionListener(e -> {
            currentStudentId = null;
            new StudentSettingsPage(studentId, userDisplayName).setVisible(true);
            dispose();
        });
        nav.add(settings);

        sidebar.add(nav, BorderLayout.CENTER);

        return sidebar;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(24, 24, 24, 24));

        if (maintenanceMode) {
            body.add(buildMaintenanceBanner(), BorderLayout.NORTH);
        }

        body.add(buildMainContent(), BorderLayout.CENTER);
        return body;
    }

    // ---- Maintenance helpers ----

    protected boolean isReadOnly() {
        return maintenanceMode;
    }

    /**
     * Convenience: give this any buttons that perform writes (add/drop, save, etc.)
     * and they will automatically be disabled when maintenanceMode is ON.
     */
    protected void enforceReadOnlyOnButtons(JButton... buttons) {
        if (!maintenanceMode || buttons == null) return;
        for (JButton b : buttons) {
            if (b == null) continue;
            b.setEnabled(false);
            b.setToolTipText("Disabled: system is in maintenance mode");
        }
    }

    private JComponent buildMaintenanceBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBorder(new EmptyBorder(0, 0, 12, 0));
        banner.setOpaque(false);

        JPanel pill = new JPanel(new BorderLayout());
        pill.setBackground(new Color(254, 243, 199)); // amber-100
        pill.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel text = new JLabel(
                "System is in maintenance mode. Editing is temporarily disabled by an administrator.");
        text.setFont(FontKit.regular(13f));
        text.setForeground(new Color(120, 53, 15)); // amber-900

        pill.add(text, BorderLayout.CENTER);
        banner.add(pill, BorderLayout.CENTER);
        return banner;
    }

    protected abstract JComponent buildMainContent();

    // Shared nav button style
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
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
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
                // pill background on hover/selected
                g2.setColor(new Color(255, 255, 255, selected ? 70 : 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

                // subtle left accent bar
                g2.setColor(new Color(204, 252, 246, 190));
                g2.fillRoundRect(4, 6, 4, getHeight() - 12, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
