package erp.ui.instructor;

import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.auth.AuthContext;
import erp.auth.Role;
import erp.db.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public abstract class InstructorFrameBase extends JFrame {

    // Shared palette
    protected static final Color TEAL_DARK  = new Color(39, 96, 92);
    protected static final Color TEAL       = new Color(28, 122, 120);
    protected static final Color TEAL_LIGHT = new Color(55, 115, 110);
    protected static final Color BG         = new Color(246, 247, 248);

    public enum Page {
        HOME, SECTIONS, GRADES
    }

    // Keep for compatibility, but we now *resolve* from AuthContext
    protected static String currentInstructorId;

    protected final String instructorId;
    protected final String userDisplayName;
    protected final JPanel root = new JPanel(new BorderLayout());
    protected JLabel metaLabel; // "Department: X"

    protected InstructorFrameBase(String instrID, String displayName, Page activePage) {
        // --- Auth guard – only instructors ---
        Role actual = AuthContext.getRole();
        if (actual != Role.INSTRUCTOR) {
            JOptionPane.showMessageDialog(
                    null,
                    "You are not authorized to access the Instructor area."
            );
            AuthContext.clear();
            new LoginPage().setVisible(true);
            dispose();
            throw new IllegalStateException("Unauthorized access to InstructorFrameBase");
        }

        // --- Resolve instructor id from args OR session ---
        String resolvedId = instrID;
        if (resolvedId == null || resolvedId.isBlank()) {
            Integer uid = AuthContext.getUserId();  // helper you added in AuthContext
            if (uid != null) {
                resolvedId = String.valueOf(uid);
            }
        }
        currentInstructorId = resolvedId;   // keep static in sync
        this.instructorId = resolvedId;

        // --- Resolve display name from args OR session ---
        String resolvedName = displayName;
        if (resolvedName == null || resolvedName.isBlank()) {
            String fromSession = AuthContext.getUsername(); // another helper in AuthContext
            if (fromSession != null && !fromSession.isBlank()) {
                resolvedName = fromSession;
            }
        }
        if (resolvedName == null || resolvedName.isBlank()) {
            resolvedName = "Instructor";
        }
        this.userDisplayName = resolvedName;

        // --- Look & feel / shared infra ---
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("IIITD ERP – Instructor");
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
                return new Dimension(96, 96);
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

        metaLabel = new JLabel("Department");
        metaLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        metaLabel.setForeground(new Color(210, 225, 221));
        metaLabel.setFont(FontKit.regular(14f));
        profile.add(Box.createVerticalStrut(6));
        profile.add(metaLabel);

        profile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TEAL_LIGHT, 1),
                new EmptyBorder(12, 10, 28, 10)
        ));
        sidebar.add(profile, BorderLayout.NORTH);

        // nav buttons with hover pill style
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // HOME
        NavButton homeBtn = new NavButton("Home", active == Page.HOME);
        homeBtn.addActionListener(e -> {
            if (active != Page.HOME) {
                new InstructorDashboard(instructorId, userDisplayName).setVisible(true);
                dispose();
            }
        });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        // MY SECTIONS
        NavButton sectionsBtn = new NavButton("My Sections", active == Page.SECTIONS);
        sectionsBtn.addActionListener(e -> {
            if (active != Page.SECTIONS) {
                new MySections().setVisible(true);
                dispose();
            }
        });
        nav.add(sectionsBtn);
        nav.add(Box.createVerticalStrut(8));

        // If you re-enable GradeStudents later, it’ll still work:
        // NavButton gradeBtn = new NavButton("Grade Students", active == Page.GRADES);
        // gradeBtn.addActionListener(e -> {
        //     if (active != Page.GRADES) {
        //         new GradeStudents(instructorId, userDisplayName).setVisible(true);
        //         dispose();
        //     }
        // });
        // nav.add(gradeBtn);
        // nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        NavButton settingsBtn = new NavButton("Settings", false);
        nav.add(settingsBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton logout = new NavButton("Log Out", false);
        logout.addActionListener(e -> {
            AuthContext.clear();
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

    // --- Hover nav button ---
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
                g2.setColor(new Color(255, 255, 255, selected ? 70 : 40));
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));

                g2.setColor(new Color(204, 252, 246, 190));
                g2.fillRoundRect(4, 6, 4, getHeight() - 12, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
