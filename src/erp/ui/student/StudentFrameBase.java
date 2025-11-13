//template for main menu 
package erp.ui.student;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public abstract class StudentFrameBase extends JFrame {

    // Shared palette
    protected static final Color TEAL_DARK  = new Color(39, 96, 92);
    protected static final Color TEAL       = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG         = new Color(246, 247, 248);

    public enum Page { HOME, CATALOG, REGISTRATIONS, TIMETABLE }

    protected final String userDisplayName;
    protected final JPanel root = new JPanel(new BorderLayout());

    protected StudentFrameBase(String userDisplayName, Page activePage) {
        this.userDisplayName = userDisplayName;

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("IIITD ERP");
        setSize(1200, 800);                          // uniform size for all pages
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);

        root.setBackground(BG);
        setContentPane(root);

        root.add(buildSidebar(activePage), BorderLayout.WEST);
        root.add(buildBody(), BorderLayout.CENTER);
    }

    // Reusable sidebar with active highlight
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

        JLabel meta = new JLabel("Year, Program"); // you can set real data on dashboards that fetch it
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);

        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_LIGHT, 1),
                new EmptyBorder(8, 8, 32, 8)
        ));

        sidebar.add(profile, BorderLayout.NORTH);

        // nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton home = new NavButton("  🏠  Home", active == Page.HOME);
        home.addActionListener(e -> {
            if (active != Page.HOME) {
                new StudentDashboard(userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(home); nav.add(Box.createVerticalStrut(8));

        NavButton catalog = new NavButton("  📚  Course Catalogue", active == Page.CATALOG);
        catalog.addActionListener(e -> {
            if (active != Page.CATALOG) {
                new CourseCatalogue(userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(catalog); nav.add(Box.createVerticalStrut(8));

        nav.add(new NavButton("  📜  My Registrations", active == Page.REGISTRATIONS));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🗺️  Time Table", active == Page.TIMETABLE));
        nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        NavButton logout = new NavButton("  🚪  Log Out", false);
        logout.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });
        nav.add(logout);

        sidebar.add(nav, BorderLayout.CENTER);
        return sidebar;
    }

    // Container for page content
    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBackground(BG);
        body.setBorder(new EmptyBorder(24, 24, 24, 24));
        body.add(buildMainContent(), BorderLayout.CENTER);
        return body;
    }

    // Implement per-page content
    protected abstract JComponent buildMainContent();

    // Shared button used across pages
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
