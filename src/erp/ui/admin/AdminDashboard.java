package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import erp.db.Maintenance;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedButton;

public class AdminDashboard extends AdminFrameBase {

    // Palette for cards / text
    private static final Color BG        = new Color(246, 247, 248);
    private static final Color TEXT_900  = new Color(24, 30, 37);
    private static final Color TEXT_600  = new Color(100, 116, 139);
    private static final Color CARD      = Color.WHITE;

    // Old usage from LoginPage: new AdminDashboard(display)
    public AdminDashboard(String displayName) {
        this(null, displayName);
    }

    public AdminDashboard(String adminId, String displayName) {
        super(adminId, displayName, Page.HOME);
        setTitle("IIITD ERP – Admin Dashboard");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // header stack: hero + optional maintenance banner
        JPanel headerStack = new JPanel();
        headerStack.setOpaque(false);
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));

        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("Admin Dashboard");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel sub = new JLabel("Manage users, courses, and instructor assignments");
        sub.setFont(FontKit.regular(14f));
        sub.setForeground(new Color(210, 233, 229));
        hero.add(sub, BorderLayout.SOUTH);

        headerStack.add(hero);

        if (Maintenance.isOn()) {
            headerStack.add(Box.createVerticalStrut(12));
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes may be restricted");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            headerStack.add(banner);
        }

        main.add(headerStack, BorderLayout.NORTH);

        // center content – simple 3-card grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;

        gc.gridx = 0; gc.gridy = 0;
        grid.add(buildUsersCard(), gc);

        gc.gridx = 1; gc.gridy = 0;
        grid.add(buildCoursesCard(), gc);

        gc.gridx = 2; gc.gridy = 0;
        grid.add(buildAssignCard(), gc);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(grid, BorderLayout.CENTER);

        JScrollPane sc = new JScrollPane(centerWrapper);
        sc.setBorder(null);
        sc.getViewport().setBackground(BG);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }

    private RoundedPanel baseCard(String title, String desc) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(FontKit.semibold(16f));
        t.setForeground(TEXT_900);

        JLabel d = new JLabel("<html>" + desc + "</html>");
        d.setFont(FontKit.regular(13f));
        d.setForeground(TEXT_600);

        header.add(t);
        header.add(Box.createVerticalStrut(6));
        header.add(d);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    private JComponent buildUsersCard() {
        RoundedPanel card = baseCard(
                "Manage Users",
                "Create, update, and deactivate student, instructor, and admin accounts."
        );

        RoundedButton btn = new RoundedButton("Go to Users");
        btn.setBackground(TEAL);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontKit.semibold(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addActionListener(e -> {
            new AddUser(adminId, userDisplayName).setVisible(true);
            dispose();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(btn);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildCoursesCard() {
        RoundedPanel card = baseCard(
                "Manage Courses",
                "Add or edit courses in the catalog; update credits and meta-data."
        );

        RoundedButton btn = new RoundedButton("Go to Courses");
        btn.setBackground(TEAL);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontKit.semibold(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addActionListener(e -> {
            new ManageCourses(adminId, userDisplayName).setVisible(true);
            dispose();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(btn);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildAssignCard() {
        RoundedPanel card = baseCard(
                "Assign Instructors",
                "Map instructors to offered sections for the current term."
        );

        RoundedButton btn = new RoundedButton("Assign Now");
        btn.setBackground(TEAL);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontKit.semibold(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addActionListener(e -> {
            new AssignInstructor(adminId, userDisplayName).setVisible(true);
            dispose();
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(btn);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    public static void main(String[] args) {
        // For quick manual testing (without auth context it will fail the guard)
        SwingUtilities.invokeLater(() ->
                new AdminDashboard("admin1", "Admin User").setVisible(true)
        );
    }
}
