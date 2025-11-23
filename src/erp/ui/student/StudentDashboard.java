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

    private JPanel rightDock;
    private JToggleButton bellToggle;

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

        // --- center stack: hero + metrics + quick strip ---
        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.add(Box.createVerticalStrut(8));
        centerStack.add(buildHero());
        centerStack.add(Box.createVerticalStrut(16));
        centerStack.add(buildMetricsAndQuickStrip());

        JScrollPane sc = new JScrollPane(centerStack);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);

        // right dock for notifications
        rightDock = buildNotificationsDock();
        rightDock.setVisible(false);

        main.add(sc, BorderLayout.CENTER);
        main.add(rightDock, BorderLayout.EAST);
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

        // Right side: bell toggle
        JPanel heroRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        heroRight.setOpaque(false);
        bellToggle = new JToggleButton("🔔");
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

    private JComponent buildMetricsAndQuickStrip() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 0;

        erp.ui.common.RoundedPanel card1 = metricCard("0", "Courses this term");
        erp.ui.common.RoundedPanel card2 = metricCard("0", "Credits registered");
        erp.ui.common.RoundedPanel card3 = metricCard("0", "GPA (ERP)");

        gc.gridx = 0; gc.gridy = 0; grid.add(card1, gc);
        gc.gridx = 1; gc.gridy = 0; grid.add(card2, gc);
        gc.gridx = 2; gc.gridy = 0; grid.add(card3, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 3; gc.weighty = 1;
        grid.add(enrolledCoursesStrip(), gc);

        wrapper.add(grid, BorderLayout.CENTER);
        return wrapper;
    }

    private void toggleNotifications() {
        boolean show = bellToggle.isSelected();
        rightDock.setVisible(show);
        rightDock.getParent().revalidate();
    }

    private JPanel buildNotificationsDock() {
        JPanel dock = new JPanel(new BorderLayout());
        dock.setPreferredSize(new Dimension(320, 0));
        dock.setBackground(new Color(250, 251, 252));
        dock.setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel title = new JLabel("Notifications");
        title.setFont(FontKit.bold(18f));
        title.setForeground(TEXT_900);

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        // TODO: later replace with DB-driven notifications table
        list.add(notifItem("Registration opens for Spring 2026", "Today, 9:00 AM"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Grade publishing window updated", "Yesterday"));
        list.add(Box.createVerticalStrut(12));
        list.add(notifItem("Maintenance: ERP downtime 11 PM–1 AM", "2 days ago"));

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(new Color(235, 239, 242)));
        sp.getViewport().setBackground(new Color(250, 251, 252));

        dock.add(title, BorderLayout.NORTH);
        dock.add(sp, BorderLayout.CENTER);
        return dock;
    }

    private JComponent notifItem(String header, String when) {
        erp.ui.common.RoundedPanel p = new erp.ui.common.RoundedPanel(14);
        p.setBackground(Color.WHITE);
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
        v.setFont(FontKit.bold(18f));
        v.setForeground(TEXT_900);
        p.add(v, g);
        g.gridy = 1;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.regular(13f));
        l.setForeground(TEXT_600);
        p.add(l, g);
        return p;
    }

    private JPanel enrolledCoursesStrip() {
        // For now static; later you can query current enrollments and list top 2
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new GridLayout(1, 2, 16, 16));

        row.add(enrolledCard("Advanced Programming"));
        row.add(enrolledCard("Operating Systems"));
        return row;
    }

    private JComponent enrolledCard(String courseTitle) {
        erp.ui.common.RoundedPanel c = new erp.ui.common.RoundedPanel(18);
        c.setBackground(CARD);
        c.setLayout(new BorderLayout());
        c.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel t = new JLabel(courseTitle);
        t.setFont(FontKit.semibold(16f));
        t.setForeground(TEXT_900);
        c.add(t, BorderLayout.NORTH);

        JButton view = new JButton("View") {{
            setFocusPainted(false); setBorderPainted(false); setContentAreaFilled(false);
            setForeground(Color.WHITE); setFont(FontKit.semibold(14f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));
        }};
        view = new JButton("View") {
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

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrap.setOpaque(false);
        btnWrap.add(view);
        c.add(btnWrap, BorderLayout.SOUTH);

        return c;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
                new StudentDashboard("stu1", "Student 123").setVisible(true));
    }
}
