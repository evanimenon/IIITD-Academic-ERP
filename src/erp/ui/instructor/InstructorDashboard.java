package erp.ui.instructor;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.ScrollPaneConstants;
import java.awt.*;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class InstructorDashboard extends InstructorFrameBase {

    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD     = Color.WHITE;

    private JToggleButton bellToggle;
    private JPopupMenu notificationsPopup;

    public InstructorDashboard(String instructorId, String displayName) {
        super(instructorId, displayName, Page.HOME);
        setTitle("IIITD ERP – Instructor Dashboard");

        String dept = getDepartmentSafe(instructorId);
        if (metaLabel != null) {
            metaLabel.setText("Department: " + dept);
        }
    }

    public InstructorDashboard(String displayName) {
        this(currentInstructorId, displayName);
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        main.add(buildHero(), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 1.0;
        gc.weighty = 0;
        gc.gridwidth = 2;
        grid.add(buildMetricsRow(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 1.4;
        gc.weighty = 1;
        gc.gridwidth = 1;
        grid.add(buildTodayTeachingCard(), gc);

        gc.gridx = 1;
        gc.gridy = 1;
        gc.weightx = 0.8;
        gc.weighty = 1;
        grid.add(buildRightColumn(), gc);

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildHero() {
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout(16, 0));

        JPanel heroLeft = new JPanel();
        heroLeft.setOpaque(false);
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));

        JLabel date = new JLabel(todayString());
        date.setForeground(new Color(196, 234, 229));
        date.setFont(FontKit.semibold(14f));
        heroLeft.add(date);
        heroLeft.add(Box.createVerticalStrut(8));

        JLabel h1 = new JLabel("Welcome back, " + userDisplayName + "!");
        h1.setForeground(Color.WHITE);
        h1.setFont(FontKit.bold(28f));
        heroLeft.add(h1);

        JLabel subtitle = new JLabel("Manage your sections, grading, and class performance in one place.");
        subtitle.setForeground(new Color(210, 233, 229));
        subtitle.setFont(FontKit.regular(15f));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(subtitle);

        hero.add(heroLeft, BorderLayout.CENTER);

        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        heroRight.setOpaque(false);

        bellToggle = new JToggleButton("\uD83D\uDD14");
        bellToggle.setFocusPainted(false);
        bellToggle.setBorderPainted(false);
        bellToggle.setContentAreaFilled(false);
        bellToggle.setOpaque(false);
        bellToggle.setForeground(Color.WHITE);
        bellToggle.setFont(FontKit.bold(18f));
        bellToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellToggle.addActionListener(e -> toggleNotifications());

        heroRight.add(bellToggle);
        hero.add(heroRight, BorderLayout.EAST);

        return hero;
    }

    private JComponent buildMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        row.add(metricCard("0", "Sections this semester"));
        row.add(metricCard("0", "Students across sections"));
        row.add(metricCard("-", "Items to grade"));

        return row;
    }

    private RoundedPanel metricCard(String value, String label) {
        RoundedPanel p = new RoundedPanel(18);
        p.setBackground(CARD);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 22, 20, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;

        JLabel v = new JLabel(value);
        v.setFont(FontKit.bold(20f));
        v.setForeground(TEXT_900);
        p.add(v, g);

        g.gridy = 1;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.regular(13f));
        l.setForeground(TEXT_600);
        p.add(l, g);

        return p;
    }

    private JComponent buildTodayTeachingCard() {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Today's Teaching Schedule");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Based on your assigned sections");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(scheduleRow("CSE101 – Introduction to Programming", "09:00 – 10:30 • C-101"));
        list.add(Box.createVerticalStrut(12));
        list.add(scheduleRow("BIO201 – Foundations of Biology II", "11:00 – 12:30 • B-204"));
        list.add(Box.createVerticalStrut(12));
        list.add(scheduleRow("MTH201 – Linear Algebra", "14:00 – 15:30 • A-102"));

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent scheduleRow(String course, String meta) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel c = new JLabel(course);
        c.setFont(FontKit.semibold(14f));
        c.setForeground(TEXT_900);

        JLabel m = new JLabel(meta);
        m.setFont(FontKit.regular(12f));
        m.setForeground(TEXT_600);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(c);
        text.add(Box.createVerticalStrut(2));
        text.add(m);

        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JComponent buildRightColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(buildQuickActionsCard());
        col.add(Box.createVerticalStrut(16));
        col.add(buildWeekCard());

        return col;
    }

    private JComponent buildQuickActionsCard() {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Quick Actions");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Jump directly to common instructor tools");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 12));
        btnRow.setOpaque(false);

        btnRow.add(primaryPillButton("My Sections", e -> {
            new MySections().setVisible(true);
            SwingUtilities.getWindowAncestor(card).dispose();
        }));

        card.add(btnRow, BorderLayout.CENTER);
        return card;
    }

    private JButton primaryPillButton(String label, ActionListener listener) {
        JButton b = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setForeground(Color.WHITE);
        b.setFont(FontKit.semibold(14f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.addActionListener(listener);
        return b;
    }

    private JComponent buildWeekCard() {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("This Week");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Upcoming teaching & grading items");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        card.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(weekItem("Grade submission deadline – CSE101", "Friday, 11:55 PM"));
        list.add(Box.createVerticalStrut(10));
        list.add(weekItem("Quiz 2 – BIO201", "Saturday, 10:00 AM"));
        list.add(Box.createVerticalStrut(10));
        list.add(weekItem("Publish feedback for Assignment 1", "Sunday, 5:00 PM"));

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    private JComponent weekItem(String header, String when) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel h = new JLabel(header);
        h.setFont(FontKit.semibold(13f));
        h.setForeground(TEXT_900);

        JLabel t = new JLabel(when);
        t.setFont(FontKit.regular(12f));
        t.setForeground(TEXT_600);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(h);
        text.add(Box.createVerticalStrut(2));
        text.add(t);

        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private void toggleNotifications() {
        if (notificationsPopup == null) {
            notificationsPopup = buildNotificationsPopup();
        }

        if (notificationsPopup.isVisible()) {
            notificationsPopup.setVisible(false);
        } else {
            notificationsPopup.show(bellToggle, -320, bellToggle.getHeight() + 8);
        }
    }

    private JPopupMenu buildNotificationsPopup() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        popup.setOpaque(false);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Notifications");
        title.setFont(FontKit.bold(16f));
        title.setForeground(TEXT_900);

        JLabel markAll = new JLabel("Mark all as read");
        markAll.setFont(FontKit.regular(12f));
        markAll.setForeground(TEAL);
        markAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(title, BorderLayout.WEST);
        header.add(markAll, BorderLayout.EAST);

        container.add(header, BorderLayout.NORTH);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(notifItem("Timetable update for CSE101", "Today, 9:00 AM"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Grade submission deadline reminder", "Yesterday"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("ERP maintenance window 11 PM–1 AM", "2 days ago"));

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setPreferredSize(new Dimension(360, 260));
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getViewport().setBackground(Color.WHITE);

        container.add(sp, BorderLayout.CENTER);
        popup.add(container);

        return popup;
    }

    private JComponent notifItem(String header, String when) {
        RoundedPanel p = new RoundedPanel(14);
        p.setBackground(new Color(248, 249, 250));
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel h = new JLabel(header);
        h.setFont(FontKit.semibold(14f));
        h.setForeground(TEXT_900);

        JLabel t = new JLabel(when);
        t.setFont(FontKit.regular(12f));
        t.setForeground(TEXT_600);

        p.add(h, BorderLayout.NORTH);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private static String todayString() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() + " " +
                d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " +
                d.getYear();
    }

    private String getDepartmentSafe(String instructorId) {
        if (instructorId == null || instructorId.isBlank()) {
            return "None";
        }

        String dept = "None";

        String sql = "SELECT department FROM instructors WHERE instructor_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instructorId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dept = rs.getString("department");
                }
            }
        } catch (NumberFormatException | SQLException ex) {
            ex.printStackTrace();
        }

        return dept;
    }

}
