package erp.ui.admin;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.auth.AuthContext;
import erp.auth.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Shared base frame for all Admin UIs.
 * - Sidebar on the left
 * - CardLayout contentPanel in the center
 * - Pages: HOME, USERS, COURSES, MAINTENANCE
 */
public abstract class AdminFrameBase extends JFrame {

    // Palette
    protected static final Color TEAL_DARK = new Color(39, 96, 92);
    protected static final Color TEAL = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG = new Color(246, 247, 248);

    protected static final Color SIDEBAR_BG = new Color(20, 66, 61);
    protected static final Color SIDEBAR_TEXT = new Color(226, 244, 241);

    public enum Page {
        HOME,
        USERS,
        COURSES,
        ASSIGN,
        MAINTENANCE
    }

    protected final String adminId;
    protected final String userDisplayName;
    protected final Page activePage;

    protected JLabel metaLabel;

    protected final JPanel contentPanel = new JPanel();
    protected final CardLayout cardLayout = new CardLayout();

    private NavItem navHome;
    private NavItem navUsers;
    private NavItem navCourses;
    private NavItem navMaintenance;

    protected AdminFrameBase(String adminId, String displayName, Page activePage) {
        this.adminId = adminId;
        this.userDisplayName = (displayName == null || displayName.isBlank())
                ? "Administrator"
                : displayName;
        this.activePage = (activePage == null) ? Page.HOME : activePage;

        // --- auth guard ---
        if (!AuthContext.isLoggedIn() || AuthContext.getRole() != Role.ADMIN) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Your admin session has expired or is invalid. Please sign in again.",
                        "Session expired",
                        JOptionPane.WARNING_MESSAGE);
                new LoginPage().setVisible(true);
            });
            dispose();
            return;
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        buildFrameShell();
    }

    protected abstract JComponent buildMainContent();

    private void buildFrameShell() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        contentPanel.setLayout(cardLayout);
        contentPanel.setOpaque(false);

        // Wire subclass content to the right card
        switch (activePage) {
            case HOME -> {
                contentPanel.add(buildMainContent(), "DASHBOARD");
                contentPanel.add(buildPlaceholder("Manage Users will appear here."), "MANAGE_USERS");
                contentPanel.add(buildPlaceholder("Manage Courses will appear here."), "MANAGE_COURSES");
                contentPanel.add(buildPlaceholder("Maintenance will appear here."), "MAINTENANCE");
            }
            case USERS -> {
                contentPanel.add(buildPlaceholder("Dashboard is not in this window."), "DASHBOARD");
                contentPanel.add(buildMainContent(), "MANAGE_USERS");
                contentPanel.add(buildPlaceholder("Manage Courses will appear here."), "MANAGE_COURSES");
                contentPanel.add(buildPlaceholder("Maintenance will appear here."), "MAINTENANCE");
            }
            case COURSES -> {
                contentPanel.add(buildPlaceholder("Dashboard is not in this window."), "DASHBOARD");
                contentPanel.add(buildPlaceholder("Manage Users will appear here."), "MANAGE_USERS");
                contentPanel.add(buildMainContent(), "MANAGE_COURSES");
                contentPanel.add(buildPlaceholder("Maintenance will appear here."), "MAINTENANCE");
            }
            case ASSIGN -> {
                contentPanel.add(buildPlaceholder("Dashboard is not in this window."), "DASHBOARD");
                contentPanel.add(buildPlaceholder("Manage Users will appear here."), "MANAGE_USERS");
                contentPanel.add(buildMainContent(), "MANAGE_COURSES"); // reuse courses card
                contentPanel.add(buildPlaceholder("Maintenance will appear here."), "MAINTENANCE");
            }
            case MAINTENANCE -> {
                contentPanel.add(buildPlaceholder("Dashboard is not in this window."), "DASHBOARD");
                contentPanel.add(buildPlaceholder("Manage Users will appear here."), "MANAGE_USERS");
                contentPanel.add(buildPlaceholder("Manage Courses will appear here."), "MANAGE_COURSES");
                contentPanel.add(buildMainContent(), "MAINTENANCE");
            }
        }

        root.add(contentPanel, BorderLayout.CENTER);
        showPage(activePage);
    }

    private JComponent buildPlaceholder(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel label = new JLabel("<html><div style='text-align:center;'>" + text + "</div></html>",
                SwingConstants.CENTER);
        label.setFont(FontKit.regular(15f));
        label.setForeground(new Color(148, 163, 184));
        p.add(label, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR_BG);
        side.setPreferredSize(new Dimension(260, 0));

        // --- profile ---
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(24, 24, 16, 24));

        JPanel avatarPanel = new JPanel(new GridBagLayout());
        avatarPanel.setOpaque(false);
        JComponent avatar = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                Shape circle = new RoundRectangle2D.Double(0, 0, size, size, size, size);
                g2.setClip(circle);
                g2.setColor(new Color(15, 118, 110));
                g2.fillRect(0, 0, size, size);
                g2.setClip(null);
                g2.setColor(new Color(208, 250, 242));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(circle);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(72, 72);
            }
        };
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(12));

        JLabel name = new JLabel(userDisplayName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setFont(FontKit.semibold(16f));
        name.setForeground(Color.WHITE);

        metaLabel = new JLabel("Administrator");
        metaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaLabel.setFont(FontKit.regular(13f));
        metaLabel.setForeground(new Color(186, 230, 213));

        profile.add(name);
        profile.add(Box.createVerticalStrut(4));
        profile.add(metaLabel);

        side.add(profile, BorderLayout.NORTH);

        // --- nav ---
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(12, 16, 12, 16));

        navHome = new NavItem("Dashboard", Page.HOME);
        navUsers = new NavItem("Manage Users", Page.USERS);
        navCourses = new NavItem("Manage Courses", Page.COURSES);
        navMaintenance = new NavItem("Maintenance & Backup", Page.MAINTENANCE);

        nav.add(navHome);
        nav.add(Box.createVerticalStrut(4));
        nav.add(navUsers);
        nav.add(Box.createVerticalStrut(4));
        nav.add(navCourses);
        nav.add(Box.createVerticalStrut(4));
        nav.add(navMaintenance);
        nav.add(Box.createVerticalGlue());

        side.add(nav, BorderLayout.CENTER);

        // --- bottom: logout ---
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(12, 16, 20, 16));

        JButton logout = new JButton("Sign out");
        logout.setFocusPainted(false);
        logout.setForeground(new Color(248, 250, 252));
        logout.setBackground(new Color(15, 23, 42));
        logout.setFont(FontKit.semibold(13f));
        logout.setBorder(new EmptyBorder(8, 14, 8, 14));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.addActionListener(e -> {
            AuthContext.clear();
            SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
            dispose();
        });

        bottom.add(logout, BorderLayout.CENTER);
        side.add(bottom, BorderLayout.SOUTH);

        setNavSelected(activePage);
        return side;
    }

    private String pageToCard(Page page) {
        return switch (page) {
            case HOME -> "DASHBOARD";
            case USERS -> "MANAGE_USERS";
            case COURSES,
                    ASSIGN ->
                "MANAGE_COURSES";
            case MAINTENANCE -> "MAINTENANCE";
        };
    }

    protected void showPage(Page page) {
        cardLayout.show(contentPanel, pageToCard(page));
        setNavSelected(page);
    }

    private void setNavSelected(Page page) {
        if (navHome != null)
            navHome.setSelected(page == Page.HOME);
        if (navUsers != null)
            navUsers.setSelected(page == Page.USERS);
        if (navCourses != null)
            navCourses.setSelected(page == Page.COURSES || page == Page.ASSIGN);
        if (navMaintenance != null)
            navMaintenance.setSelected(page == Page.MAINTENANCE);
    }

    // --- inner nav item class ---
    private class NavItem extends JPanel {
        private boolean selected = false;
        private final Page page;

        NavItem(String text, Page page) {
            this.page = page;
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(8, 10, 8, 10));

            JLabel label = new JLabel(text);
            label.setFont(FontKit.semibold(13f));
            label.setForeground(SIDEBAR_TEXT);
            add(label, BorderLayout.WEST);

            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Behave like StudentFrameBase: open the right frame,
                    // so sidebar and dashboard tiles are consistent.

                    switch (page) {
                        case HOME -> {
                            new AdminDashboard(adminId, userDisplayName).setVisible(true);
                            AdminFrameBase.this.dispose();
                        }
                        case USERS -> {
                            new AddUser(adminId, userDisplayName).setVisible(true);
                            AdminFrameBase.this.dispose();
                        }
                        case COURSES, ASSIGN -> {
                            try {
                                new ManageCourses(adminId, userDisplayName).setVisible(true);
                                AdminFrameBase.this.dispose();
                            } catch (Throwable t) {
                                t.printStackTrace();
                                JOptionPane.showMessageDialog(
                                        AdminFrameBase.this,
                                        "Failed to open Manage Courses:\n" + t.getClass().getSimpleName() + ": "
                                                + t.getMessage(),
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }

                        case MAINTENANCE -> {
                            new AdminMaintenanceFrame(adminId, userDisplayName).setVisible(true);
                            AdminFrameBase.this.dispose();
                        }

                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(new Color(15, 118, 110, 80));
                        setOpaque(true);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setOpaque(false);
                        repaint();
                    }
                }
            });
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setOpaque(true);
                setBackground(new Color(15, 118, 110, 150));
            } else {
                setOpaque(false);
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!isOpaque()) {
                super.paintComponent(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
