package erp.ui.admin;

import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class AdminDashboard extends AdminFrameBase {

    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_MUTED = new Color(110, 119, 132);

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

        // Header
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(Color.WHITE);
        hero.setBorder(new EmptyBorder(18, 20, 18, 20));
        hero.setLayout(new BorderLayout(16, 0));

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Admin Dashboard");
        title.setFont(FontKit.bold(22f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel(
                "<html>Manage users, courses, and system maintenance for the IIITD ERP.</html>"
        );
        subtitle.setFont(FontKit.regular(14f));
        subtitle.setForeground(TEXT_MUTED);

        heroText.add(title);
        heroText.add(Box.createVerticalStrut(6));
        heroText.add(subtitle);
        hero.add(heroText, BorderLayout.CENTER);

        JPanel headerStack = new JPanel();
        headerStack.setOpaque(false);
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));
        headerStack.add(hero);

        main.add(headerStack, BorderLayout.NORTH);

        // Cards
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1.0;
        gc.weighty = 1.0;

        gc.gridx = 0;
        grid.add(buildUsersCard(), gc);

        gc.gridx = 1;
        grid.add(buildCoursesCard(), gc);

        gc.gridx = 2;
        grid.add(buildMaintenanceCard(), gc);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(grid, BorderLayout.NORTH);

        main.add(centerWrapper, BorderLayout.CENTER);
        return main;
    }

    private RoundedPanel baseCard(String heading, String description) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 16, 16, 16));

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel title = new JLabel(heading);
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel desc = new JLabel("<html>" + description + "</html>");
        desc.setFont(FontKit.regular(13f));
        desc.setForeground(TEXT_MUTED);

        text.add(title);
        text.add(Box.createVerticalStrut(8));
        text.add(desc);

        card.add(text, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildUsersCard() {
        RoundedPanel card = baseCard(
                "Manage Users",
                "View, add, or deactivate student and instructor accounts."
        );
        RoundedButton btn = new RoundedButton("Manage Users");
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
                "Review all courses and sections; edit details and assignments."
        );
        RoundedButton btn = new RoundedButton("Manage Courses");
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

    private JComponent buildMaintenanceCard() {
        RoundedPanel card = baseCard(
                "Maintenance & Backup",
                "Toggle maintenance mode and manage database backups."
        );
        RoundedButton btn = new RoundedButton("Open Maintenance");
        btn.setBackground(TEAL);
        btn.setForeground(Color.WHITE);
        btn.setFont(FontKit.semibold(14f));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Maintenance tools will be implemented in a later phase.",
                        "Maintenance",
                        JOptionPane.INFORMATION_MESSAGE
                )
        );

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        actions.add(btn);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }
}
