package erp.ui.admin;

import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.db.Maintenance;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public abstract class AdminFrameBase extends JFrame {

    // === COLOR PALETTE ===
    protected static final Color TEAL_DARK = new Color(39, 96, 92);
    protected static final Color TEAL = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG = new Color(246, 247, 248);
    protected static final Color TEXT_900 = new Color(24, 30, 37);
    protected static final Color TEXT_600 = new Color(100, 116, 139);
    protected static final Color CARD = Color.WHITE;

    protected JPanel mainPanel;     // area for page-specific content
    protected String adminName;
    protected String activePage;    // "AddUser", "Dashboard", etc.

    public AdminFrameBase(String adminName, String activePage) {
        this.adminName = adminName;
        this.activePage = activePage;

        setTitle("IIITD ERP – Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        initUI();
    }

    // ========== COMMON UI FRAMEWORK ==========
    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createTopBanner(), BorderLayout.NORTH);

        // Maintenance mode banner
        if (Maintenance.isOn()) {
            root.add(createMaintenanceBanner(), BorderLayout.NORTH);
        }

        // Page-specific panel (children use this)
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        root.add(mainPanel, BorderLayout.CENTER);
    }

    // ========== SIDEBAR ==========
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        sidebar.add(createSidebarProfile(), BorderLayout.NORTH);
        sidebar.add(createSidebarNav(), BorderLayout.CENTER);

        return sidebar;
    }

    private JPanel createSidebarProfile() {
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(230, 233, 236));
                g.fillOval(0, 0, getWidth(), getHeight());
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        profile.add(avatar);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(adminName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        JLabel meta = new JLabel("Admin");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);

        return profile;
    }

    private JPanel createSidebarNav() {
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));

        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        addNav(nav, "Home", () -> new AdminDashboard(adminName), "Dashboard");
        addNav(nav, "Add User", () -> new AddUser(adminName), "AddUser");
        addNav(nav, "Manage Courses", () -> new ManageCourses(adminName), "Courses");
        addNav(nav, "Assign Instructor", () -> new AssignInstructor(adminName), "Assign");

        nav.add(Box.createVerticalStrut(40));
        addNav(nav, "Settings", () -> {}, "Settings");
        addNav(nav, "Log Out", () -> {}, "Logout");

        return nav;
    }

    private void addNav(JPanel nav, String label, Runnable openPage, String key) {
        boolean isActive = key.equals(activePage);
        NavButton btn = new NavButton(label, isActive);

        btn.addActionListener(e -> {
            openPage.run();
            this.dispose();
        });

        nav.add(btn);
        nav.add(Box.createVerticalStrut(8));
    }

    // ========== TOP BANNER ==========
    private JPanel createTopBanner() {
        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));

        JLabel h1 = new JLabel(getPageTitle());
        h1.setFont(FontKit.bold(26f));
        h1.setForeground(Color.WHITE);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));

        hero.add(h1, BorderLayout.WEST);
        hero.add(adminLabel, BorderLayout.EAST);

        return hero;
    }

    // child classes override
    protected String getPageTitle() {
        return "Admin Panel";
    }

    private JPanel createMaintenanceBanner() {
        JPanel p = new JPanel();
        p.setBackground(new Color(255, 235, 230));
        p.setBorder(new EmptyBorder(12, 18, 12, 18));

        JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes are disabled");
        msg.setFont(FontKit.semibold(14f));
        msg.setForeground(new Color(180, 60, 50));

        p.add(msg);
        return p;
    }
}
