package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.admin.AddUser;
import erp.ui.admin.AdminDashboard;
import erp.ui.admin.ManageCourses;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

import erp.ui.common.NavButton;
import erp.ui.common.RoundedPanel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class GradeStudents extends JFrame {
    // Palette
    private static final Color TEAL_DARK  = new Color(39, 96, 92);
    private static final Color TEAL       = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG         = new Color(246, 247, 248);
    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;
    private String department = "Computer Science"; // TODO: fetch from DB

    public GradeStudents(String instrID, String displayName) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        FontKit.init();
        DatabaseConnection.init();

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

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton homeBtn = new NavButton("  🏠  Home", false);
        homeBtn.addActionListener(e -> { new InstructorDashboard(instrID, displayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton sectionBtn = new NavButton("  📚  My Sections", false);
        sectionBtn.addActionListener(e -> { new MySections(instrID, displayName).setVisible(true); dispose(); });
        nav.add(sectionBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton gradeBtn = new NavButton("  ✒️  Grade Students", true);
        gradeBtn.addActionListener(e -> { new GradeStudents(instrID, displayName).setVisible(true); dispose(); });
        nav.add(gradeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton classStatBtn= new NavButton("  📊  Class Stats", false);
        classStatBtn.addActionListener(e -> { new ClassStats(instrID, displayName).setVisible(true); dispose(); });
        nav.add(classStatBtn);
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

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("✒️  Grade Students");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + displayName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main Content: List of Sections ---
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        

        // --- THIS HAS ERRORS IM GONNA FIX IT LATER ---
        // class SectionInfo {
        //     String sectionID;
        //     String courseCode;
        //     String courseName;
        //     String semester;
        // }
        // TODO: Replace with DB results
        // List<SectionInfo> sections = Database.getSectionsForInstructor(instrID);

        // for (SectionInfo sec : sections) {
        //     RoundedPanel card = new RoundedPanel(20);
        //     card.setBackground(CARD);
        //     card.setBorder(new EmptyBorder(20, 24, 20, 24));
        //     card.setLayout(new BorderLayout());

        //     // left: course + section info
        //     JPanel left = new JPanel();
        //     left.setOpaque(false);
        //     left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        //     JLabel title = new JLabel(sec.courseCode + " – " + sec.sectionID);
        //     title.setFont(FontKit.bold(20f));
        //     title.setForeground(TEXT_900);

        //     JLabel subtitle = new JLabel(sec.courseName);
        //     subtitle.setFont(FontKit.regular(15f));
        //     subtitle.setForeground(TEXT_600);

        //     JLabel sem = new JLabel("Semester: " + sec.semester);
        //     sem.setFont(FontKit.regular(14f));
        //     sem.setForeground(TEXT_600);

        //     left.add(title);
        //     left.add(Box.createVerticalStrut(6));
        //     left.add(subtitle);
        //     left.add(Box.createVerticalStrut(4));
        //     left.add(sem);

        //     card.add(left, BorderLayout.WEST);

        //     // right: action buttons
        //     JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        //     actions.setOpaque(false);

        //     JButton csvBtn = new JButton("Upload CSV");
        //     csvBtn.setBackground(TEAL);
        //     csvBtn.setForeground(Color.WHITE);
        //     csvBtn.setFont(FontKit.semibold(14f));
        //     csvBtn.setFocusPainted(false);
        //     csvBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        //     csvBtn.addActionListener(e -> { new CSVUploadPage(instrID, displayName).setVisible(true); dispose(); });

        //     JButton manualBtn = new JButton("Manual Grading");
        //     manualBtn.setBackground(TEAL_LIGHT);
        //     manualBtn.setForeground(Color.WHITE);
        //     manualBtn.setFont(FontKit.semibold(14f));
        //     manualBtn.setFocusPainted(false);
        //     manualBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        //     manualBtn.addActionListener(e -> { new ManualGradingPage(instrID, displayName).setVisible(true); dispose(); });

        //     actions.add(csvBtn);
        //     actions.add(manualBtn);

        //     card.add(actions, BorderLayout.EAST);

        //     content.add(card);
        //     content.add(Box.createVerticalStrut(16));
        // }

        // root.add(new JScrollPane(content), BorderLayout.CENTER);


        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.NORTH);
        }
    }

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

}
