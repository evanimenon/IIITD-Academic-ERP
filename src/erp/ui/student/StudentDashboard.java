// StudentDashboard.java
package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class StudentDashboard extends StudentFrameBase {

    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color CARD       = Color.WHITE;

    private String program = "Program";
    private String yearStr = "Year";
    private String rollNo  = "Roll";

    private JToggleButton bellToggle;
    private JPopupMenu notificationsPopup;

    public StudentDashboard(String studentId, String displayName) {
        super(studentId, displayName, Page.HOME);
        setTitle("IIITD ERP – Student Dashboard");
        fetchStudentMeta();
        metaLabel.setText("Year " + yearStr + ", " + program);  // update sidebar
    }

    // fallback for older code: no studentId known
    public StudentDashboard(String displayName) {
        this(null, displayName);
    }

    private void fetchStudentMeta() {
        if (studentId == null || studentId.isBlank()) return;

        final String sql =
                "SELECT roll_no, program, year " +
                "FROM erp_db.students " +
                "WHERE student_id = ? OR roll_no = ?";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    rollNo  = rs.getString("roll_no");
                    program = rs.getString("program");
                    yearStr = String.valueOf(rs.getInt("year"));
                }
            }
        } catch (Exception ignored) {}
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // Hero pinned at the top like in the reference
        main.add(buildHero(), BorderLayout.NORTH);

        // Center content uses a 2-column responsive-ish layout
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;

        // Row 0: metrics row spanning both columns
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0;
        gc.gridwidth = 2;
        grid.add(buildMetricsRow(), gc);

        // Row 1, col 0: Today's schedule (main content)
        gc.gridx = 0; gc.gridy = 1;
        gc.weightx = 1.4; gc.weighty = 1;
        gc.gridwidth = 1;
        grid.add(buildTodayScheduleCard(), gc);

        // Row 1, col 1: Right column (registration + upcoming)
        gc.gridx = 1; gc.gridy = 1;
        gc.weightx = 0.8; gc.weighty = 1;
        grid.add(buildRightColumn(), gc);

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }

    private JComponent buildHero() {
        erp.ui.common.RoundedPanel hero =
                new erp.ui.common.RoundedPanel(24);
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

        String welcomeName = (rollNo != null && !rollNo.isBlank())
                ? rollNo
                : userDisplayName;

        JLabel h1 = new JLabel("Welcome back, " + welcomeName + "!");
        h1.setForeground(Color.WHITE);
        h1.setFont(FontKit.bold(28f));
        heroLeft.add(h1);

        JLabel subtitle = new JLabel("Program: " + program + " • Year " + yearStr);
        subtitle.setForeground(new Color(210, 233, 229));
        subtitle.setFont(FontKit.regular(15f));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(subtitle);
        hero.add(heroLeft, BorderLayout.CENTER);

        // Right side: bell toggle (opens floating notifications panel)
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        heroRight.setOpaque(false);
        bellToggle = new JToggleButton("\uD83D\uDD14"); // bell emoji; visible in most fonts
        bellToggle.setFocusPainted(false);
        bellToggle.setBorderPainted(false);
        bellToggle.setContentAreaFilled(false);
        bellToggle.setForeground(Color.WHITE);
        bellToggle.setFont(FontKit.bold(18f));
        bellToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellToggle.addActionListener(e -> toggleNotifications());
        heroRight.add(bellToggle);
        hero.add(heroRight, BorderLayout.EAST);

        return hero;
    }

    /**
     * Row of 3 compact metrics, more meaningful than the old cards.
     */
    private JComponent buildMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        row.add(metricCard("0", "Registered Courses"));
        row.add(metricCard("0", "Total Credits"));
        row.add(metricCard("-", "Holds / To-dos"));

        return row;
    }

    /**
     * Left column: today's classes card.
     * Later you can wire this up to timetable/enrollments; for now it's UI-only.
     */
    private JComponent buildTodayScheduleCard() {
        erp.ui.common.RoundedPanel card =
                new erp.ui.common.RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Today's Schedule");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Based on your registered courses");
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

        // Placeholder rows – hook to DB later
        list.add(scheduleRow("CSE101 – Advanced Programming",
                "09:00 – 10:30 • C-101"));
        list.add(Box.createVerticalStrut(12));
        list.add(scheduleRow("BIO201 – Molecular Biology",
                "11:00 – 12:30 • B-204"));
        list.add(Box.createVerticalStrut(12));
        list.add(scheduleRow("MTH201 – Linear Algebra",
                "14:00 – 15:30 • A-102"));

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

    /**
     * Right column: registration status + simple "this week" card.
     */
    private JComponent buildRightColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(buildRegistrationCard());
        col.add(Box.createVerticalStrut(16));
        col.add(buildWeekCard());

        return col;
    }

    private JComponent buildRegistrationCard() {
        erp.ui.common.RoundedPanel card =
                new erp.ui.common.RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Registration");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel status = new JLabel("Course registration window status: TBD");
        status.setFont(FontKit.regular(13f));
        status.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(status);

        card.add(header, BorderLayout.NORTH);

        JButton go = new JButton("Go to Registration") {
            {
                setFocusPainted(false);
                setBorderPainted(false);
                setContentAreaFilled(false);
                setForeground(Color.WHITE);
                setFont(FontKit.semibold(14f));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(new EmptyBorder(10, 18, 10, 18));
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                super.paintComponent(g);
                g2.dispose();
            }
        };

        go.addActionListener(e -> {
            System.out.println("[DEBUG] DASH → Registration quick action (studentId=" + studentId + ")");
            new MyRegistrationsFrame(studentId, userDisplayName).setVisible(true);
            SwingUtilities.getWindowAncestor(card).dispose();
        });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrap.setOpaque(false);
        btnWrap.add(go);
        card.add(btnWrap, BorderLayout.SOUTH);

        return card;
    }

    private JComponent buildWeekCard() {
        erp.ui.common.RoundedPanel card =
                new erp.ui.common.RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("This Week");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Upcoming items");
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

        list.add(weekItem("Assignment 2 due – CSE101", "Friday, 11:55 PM"));
        list.add(Box.createVerticalStrut(10));
        list.add(weekItem("Quiz – BIO201", "Saturday, 10:00 AM"));
        list.add(Box.createVerticalStrut(10));
        list.add(weekItem("Add/Drop deadline", "Sunday, 5:00 PM"));

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
            // Show as a floating panel; negative X to roughly right-align with the bell
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

        // Header row (title + "Mark all as read")
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

        // List of notifications
        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        list.add(notifItem("Registration opens for Spring 2026", "Today, 9:00 AM"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Grade publishing window updated", "Yesterday"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Maintenance: ERP downtime 11 PM–1 AM", "2 days ago"));

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
        erp.ui.common.RoundedPanel p = new erp.ui.common.RoundedPanel(14);
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
        return d.getDayOfMonth() + " " + d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + d.getYear();
    }

    private erp.ui.common.RoundedPanel metricCard(String value, String label) {
        erp.ui.common.RoundedPanel p = new erp.ui.common.RoundedPanel(18);
        p.setBackground(CARD);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(20, 22, 20, 22));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.anchor = GridBagConstraints.WEST;
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

}
