package erp.ui.student;

import erp.ui.auth.*;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class Dashboard extends JFrame {

    // Palette
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248); // light app bg
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    public Dashboard(String userDisplayName) {
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
        NavButton homeBtn = new NavButton("  🏠  Home", true);
        nav.add(homeBtn);
        homeBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new Dashboard(userDisplayName).setVisible(true);
                dispose();
            });
        });
        nav.add(Box.createVerticalStrut(8));
        
        // Highlighted button for current page
        NavButton courseCatalogueBtn = new NavButton("  📚  Course Catalogue", false);
        nav.add(courseCatalogueBtn);
        courseCatalogueBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new CourseCatalogue(userDisplayName).setVisible(true);
                dispose();
            });
        });
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  📜  My Registrations", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🗺️  Time Table", false));
        nav.add(Box.createVerticalStrut(40)); // Increased vertical space
        
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

        JLabel subtitle = new JLabel("Always stay updated in your student portal");
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

        // Center content area (scroll)
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 0, 0, 0));

        // "Your Courses" section
        JLabel secTitle = new JLabel("Your Courses");
        secTitle.setFont(FontKit.bold(22f));
        secTitle.setForeground(TEXT_900);
        content.add(secTitle);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(210, 213, 218));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setBorder(new EmptyBorder(10, 0, 10, 0));
        content.add(sep);

        RoundedPanel coursesCard = new RoundedPanel(16);
        coursesCard.setBackground(CARD);
        coursesCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        coursesCard.setLayout(new BorderLayout());
        JLabel empty = new JLabel(
                "<html><b>You haven’t enrolled in any courses yet.</b> Enroll in some courses to get started…</html>");
        empty.setForeground(TEXT_600);
        empty.setFont(FontKit.semibold(14f));
        coursesCard.add(empty, BorderLayout.CENTER);
        content.add(coursesCard);

        main.add(new JScrollPane(content) {{
            setBorder(null);
            getVerticalScrollBar().setUnitIncrement(16);
            setBackground(BG);
        }}, BorderLayout.CENTER);
    }

    private static String todayString() {
        LocalDate d = LocalDate.now();
        String day = String.valueOf(d.getDayOfMonth());
        String month = d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String year = String.valueOf(d.getYear());
        return day + " " + month + " " + year;
    }

    // Rounded panel with soft shadow
    static class RoundedPanel extends JPanel {
        private final int arc;
        RoundedPanel(int arc) { this.arc = arc; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            // soft shadow
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

    // Quick manual launch
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        SwingUtilities.invokeLater(() -> new Dashboard("Student 123").setVisible(true));
    }
}
