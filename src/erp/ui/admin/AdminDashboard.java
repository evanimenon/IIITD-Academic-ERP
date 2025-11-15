package erp.ui.admin;

import erp.ui.common.FontKit;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import erp.ui.common.RoundedPanel;
import erp.ui.common.NavButton;

public class AdminDashboard extends JFrame {

    // Palette
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private static boolean MAINTENANCE_MODE = false;

    private RoundedPanel actionCard(String title, String desc, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontKit.bold(18f));
        titleLabel.setForeground(TEXT_900);

        JLabel descLabel = new JLabel("<html><p style='width:240px;'>" + desc + "</p></html>");
        descLabel.setFont(FontKit.regular(14f));
        descLabel.setForeground(TEXT_600);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);

        // hover + click behavior
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            Color normal = CARD;
            Color hover = new Color(238, 241, 245);
            @Override public void mouseEntered(MouseEvent e) { 
                card.setBackground(hover); card.repaint(); 
            }
            @Override public void mouseExited(MouseEvent e) { 
                card.setBackground(normal); card.repaint(); 
            }
            @Override public void mouseClicked(MouseEvent e) {
                onClick.run(); 
            }
        });

        return card;
    }

    private RoundedPanel maintenanceCard() {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel label = new JLabel("🔧 Maintenance Mode");
        label.setFont(FontKit.bold(18f));
        label.setForeground(TEXT_900);

        JLabel status = new JLabel(MAINTENANCE_MODE ? "ON" : "OFF", SwingConstants.RIGHT);
        status.setFont(FontKit.semibold(16f));
        status.setForeground(MAINTENANCE_MODE ? new Color(34, 197, 94) : new Color(229, 72, 77));

        card.add(label, BorderLayout.WEST);
        card.add(status, BorderLayout.EAST);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            Color normal = CARD;
            Color hover = new Color(238, 241, 245);
            @Override public void mouseEntered(MouseEvent e) { card.setBackground(hover); card.repaint(); }
            @Override public void mouseExited(MouseEvent e) { card.setBackground(normal); card.repaint(); }
            @Override public void mouseClicked(MouseEvent e) {
                MAINTENANCE_MODE = !MAINTENANCE_MODE;
                status.setText(MAINTENANCE_MODE ? "ON" : "OFF");
                status.setForeground(MAINTENANCE_MODE ? new Color(34, 197, 94) : new Color(229, 72, 77));
                JOptionPane.showMessageDialog(card, "Maintenance mode " +
                        (MAINTENANCE_MODE ? "enabled" : "disabled"));
            }
        });

        return card;
    }


    public AdminDashboard(String userDisplayName) {
        setTitle("IIITD ERP – Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);

        // app bg
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile block (Top part of sidebar)
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        EmptyBorder br = new EmptyBorder(8, 8, 32, 8);
        profile.setBorder(br);

        // Circular Avatar with rounded corner panel
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                // Draw a circle
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

        JLabel meta = new JLabel("Year, Program");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);
        
        // Rounded corners for the entire profile block (visual style enhancement)
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_LIGHT, 1),
            new EmptyBorder(8, 8, 32, 8)
        ));

        sidebar.add(profile, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Navigation Links

        NavButton dashboardBtn = new NavButton("🏠 Home", true);
        dashboardBtn.addActionListener(e -> {
            new AdminDashboard(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", false);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        });
        nav.add(assignInstBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton maintenanceModeBtn = new NavButton("🔧 Maintenance Mode", false);
        maintenanceModeBtn.addActionListener(e -> {
            new MaintenanceMode(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        });
        nav.add(maintenanceModeBtn);
        nav.add(Box.createVerticalStrut(8));

        
        // Separator
        nav.add(new JSeparator() {{ 
            setForeground(new Color(60, 120, 116)); 
            setBackground(new Color(60, 120, 116));
            setMaximumSize(new Dimension(240, 1));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }});
        nav.add(Box.createVerticalStrut(40));
        nav.add(new NavButton("  ⚙️  Settings", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🚪  Log Out", false)); // Used door emoji for log out
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // ---- Main area ----
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BorderLayout());
        main.setBorder(new EmptyBorder(24, 24, 24, 24));
        root.add(main, BorderLayout.CENTER);

        // Hero banner
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        main.add(hero, BorderLayout.NORTH);
        hero.setLayout(new BorderLayout(16, 0));

        // left stack
        JPanel heroLeft = new JPanel();
        heroLeft.setOpaque(false);
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));

        JLabel date = new JLabel(todayString());
        date.setForeground(new Color(196, 234, 229));
        date.setFont(FontKit.semibold(14f));
        heroLeft.add(date);
        heroLeft.add(Box.createVerticalStrut(8));

        JLabel h1 = new JLabel("Welcome Back " + userDisplayName + "!");
        h1.setForeground(Color.WHITE);
        h1.setFont(FontKit.bold(34f));
        heroLeft.add(h1);

        JLabel subtitle = new JLabel("Manage users, courses, and system maintenance");
        subtitle.setForeground(new Color(210, 233, 229));
        subtitle.setFont(FontKit.regular(16f));
        heroLeft.add(subtitle);


        hero.add(heroLeft, BorderLayout.CENTER);

        // right graphic placeholder circle
        JLabel heroCircle = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(228, 234, 236));
                int d = Math.min(getWidth(), getHeight());
                g2.fillOval(getWidth()/2 - d/2, getHeight()/2 - d/2, d, d);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(120, 120); }
        };
        heroCircle.setOpaque(false);
        hero.add(heroCircle, BorderLayout.EAST);

        // ---- Content (Quick Actions) ----
        JPanel quick = new JPanel(new GridLayout(2, 2, 20, 20));
        quick.setOpaque(false);
        quick.setBorder(new EmptyBorder(30, 0, 0, 0));

        quick.add(actionCard("👤 Add New User", "Create student/instructor/admin accounts", () -> {
            new AddUser(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        }));

        quick.add(actionCard("📘 Manage Courses", "Create or edit courses and sections", () -> {
            new ManageCourses(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        }));

        quick.add(actionCard("👨 Assign Instructor", "Link instructors to sections", () -> {
            new AssignInstructor(userDisplayName).setVisible(true);
            AdminDashboard.this.dispose();
        }));

        quick.add(maintenanceCard());

        main.add(quick, BorderLayout.CENTER);
    }

    private static String todayString() {
        LocalDate d = LocalDate.now();
        String day = String.valueOf(d.getDayOfMonth());
        String month = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String year = String.valueOf(d.getYear());
        return day + " " + month + " " + year;
    }

    // Quick manual launch
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        SwingUtilities.invokeLater(() -> new AdminDashboard("Admin 123").setVisible(true));
    }
}
