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
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StudentDashboard extends StudentFrameBase {

    private static final Color TEXT_900   = new Color(24, 30, 37);
    private static final Color TEXT_600   = new Color(100, 116, 139);
    private static final Color BORDER     = new Color(226, 232, 240);
    private static final Color CARD       = Color.WHITE;

    private String program = "Program";
    private String yearStr = "Year";
    private String rollNo  = "Roll";

    private JToggleButton bellToggle;
    private JPopupMenu notificationsPopup;
    private JButton profileButton;
    private JPopupMenu profileMenu;

    // Registration summary model
    private static class RegisteredCourse {
        String courseId;
        String code;
        String title;
        int credits;
        String dayTime;
        String room;
    }

    // Lazily initialised; buildMainContent can run before ctor finishes
    private List<RegisteredCourse> registeredCourses;
    private int totalCredits = 0;

    public StudentDashboard(String studentId, String displayName) {
        super(studentId, displayName, Page.HOME);
        setTitle("IIITD ERP – Student Dashboard");

        // After super() returns, we can safely hit DB.
        fetchStudentMeta();
        loadRegistrationSummary();

        if (metaLabel != null) {
            metaLabel.setText("Year " + yearStr + ", " + program);
        }
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

    /**
     * Uses enrollments → sections → courses to compute:
     * - totalCredits (sum of course credits)
     * - registeredCourses list (one per enrollment)
     */
    private void loadRegistrationSummary() {
        if (registeredCourses == null) {
            registeredCourses = new ArrayList<>();
        } else {
            registeredCourses.clear();
        }
        totalCredits = 0;

        if (studentId == null || studentId.isBlank()) return;

        final String sql =
                "SELECT c.course_id, c.code, c.title, c.credits, " +
                "       s.day_time, s.room " +
                "FROM erp_db.enrollments e " +
                "JOIN erp_db.sections s ON e.section_id = s.section_id " +
                "JOIN erp_db.courses  c ON s.course_id = c.course_id " +
                "WHERE e.student_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RegisteredCourse rc = new RegisteredCourse();
                    rc.courseId = rs.getString("course_id");
                    rc.code  = rs.getString("code");
                    rc.title    = rs.getString("title");
                    rc.credits  = rs.getInt("credits");
                    rc.dayTime  = rs.getString("day_time"); // e.g. "Thu 11:00-12:30"
                    rc.room     = rs.getString("room");
                    registeredCourses.add(rc);
                    totalCredits += rc.credits;
                }
            }
        } catch (Exception ex) {
            System.err.println("[StudentDashboard] Failed to load registration summary: " + ex.getMessage());
        }
    }

    @Override
    protected JComponent buildMainContent() {
        if (registeredCourses == null) {
            registeredCourses = new ArrayList<>();
        }

        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        main.add(buildHero(), BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(16, 16, 16, 16);
        gc.fill = GridBagConstraints.BOTH;
        gc.gridy = 0;

        // Left: metrics + registered courses
        gc.gridx = 0;
        gc.weightx = 1.3;
        gc.weighty = 1;
        grid.add(buildLeftColumn(), gc);

        // Right: calendar + timetable
        gc.gridx = 1;
        gc.weightx = 0.9;
        gc.weighty = 1;
        grid.add(buildRightColumn(), gc);

        JScrollPane sc = new JScrollPane(grid);
        sc.setBorder(null);
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }

    // ===== HERO / TOP BAR ====================================================

    private JComponent buildHero() {
        erp.ui.common.RoundedPanel hero =
                new erp.ui.common.RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(20, 24, 20, 24));
        hero.setLayout(new BorderLayout(16, 0));

        // LEFT: greeting + meta
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel date = new JLabel(todayString());
        date.setForeground(new Color(196, 234, 229));
        date.setFont(FontKit.semibold(13f));
        left.add(date);
        left.add(Box.createVerticalStrut(6));

        String welcomeName = (rollNo != null && !rollNo.isBlank())
                ? rollNo
                : userDisplayName;

        JLabel h1 = new JLabel("Welcome, " + welcomeName);
        h1.setForeground(Color.WHITE);
        h1.setFont(FontKit.bold(24f));
        left.add(h1);

        JLabel subtitle = new JLabel("Program: " + program + "  •  Year " + yearStr);
        subtitle.setForeground(new Color(210, 233, 229));
        subtitle.setFont(FontKit.regular(14f));
        left.add(Box.createVerticalStrut(4));
        left.add(subtitle);

        hero.add(left, BorderLayout.CENTER);

        // RIGHT: bell + profile menu
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        bellToggle = new JToggleButton("\uD83D\uDD14");
        bellToggle.setFocusPainted(false);
        bellToggle.setBorderPainted(false);
        bellToggle.setContentAreaFilled(false);
        bellToggle.setForeground(Color.WHITE);
        bellToggle.setFont(FontKit.bold(18f));
        bellToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellToggle.addActionListener(e -> toggleNotifications());
        right.add(bellToggle);

        profileButton = new JButton();
        profileButton.setFocusPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        profileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profileButton.setForeground(Color.WHITE);
        profileButton.setFont(FontKit.regular(13f));

        profileButton.setLayout(new BorderLayout(8, 0));

        // Simple circular avatar with initial
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int size = Math.min(getWidth(), getHeight());
                g2.setColor(new Color(17, 94, 89));
                g2.fillOval(0, 0, size, size);
                g2.setColor(Color.WHITE);
                String initial = userDisplayName != null && !userDisplayName.isBlank()
                        ? userDisplayName.substring(0, 1).toUpperCase(Locale.ENGLISH)
                        : "S";
                Font f = FontKit.semibold(12f);
                g2.setFont(f);
                FontMetrics fm = g2.getFontMetrics();
                int x = (size - fm.stringWidth(initial)) / 2;
                int y = (size - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(initial, x, y);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(26, 26));

        JPanel textWrap = new JPanel();
        textWrap.setOpaque(false);
        textWrap.setLayout(new BoxLayout(textWrap, BoxLayout.Y_AXIS));
        JLabel nameLabel = new JLabel(userDisplayName != null ? userDisplayName : "Student");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(FontKit.semibold(13f));
        JLabel roleLabel = new JLabel("Student");
        roleLabel.setForeground(new Color(186, 230, 253));
        roleLabel.setFont(FontKit.regular(11f));
        textWrap.add(nameLabel);
        textWrap.add(roleLabel);

        JLabel caret = new JLabel("\u25BE"); // ▼
        caret.setForeground(new Color(191, 219, 254));
        caret.setFont(FontKit.regular(10f));

        profileButton.add(avatar, BorderLayout.WEST);
        profileButton.add(textWrap, BorderLayout.CENTER);
        profileButton.add(caret, BorderLayout.EAST);

        profileButton.addActionListener(e -> toggleProfileMenu());

        right.add(profileButton);

        hero.add(right, BorderLayout.EAST);

        return hero;
    }

    private void toggleProfileMenu() {
        if (profileMenu == null) {
            profileMenu = buildProfileMenu();
        }
        if (profileMenu.isVisible()) {
            profileMenu.setVisible(false);
        } else {
            profileMenu.show(profileButton, 0, profileButton.getHeight());
        }
    }

    private JPopupMenu buildProfileMenu() {
        JPopupMenu menu = new JPopupMenu();
        menu.setBorder(BorderFactory.createLineBorder(BORDER));

        JPanel root = new JPanel();
        root.setBorder(new EmptyBorder(10, 12, 10, 12));
        root.setBackground(Color.WHITE);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JLabel header = new JLabel(userDisplayName != null ? userDisplayName : "Student");
        header.setFont(FontKit.semibold(13f));
        header.setForeground(TEXT_900);
        JLabel sub = new JLabel("Student • " + program);
        sub.setFont(FontKit.regular(11f));
        sub.setForeground(TEXT_600);

        root.add(header);
        root.add(sub);
        root.add(Box.createVerticalStrut(8));

        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER);
        root.add(sep);
        root.add(Box.createVerticalStrut(8));

        JButton settingsBtn = new JButton("Open Settings");
        settingsBtn.setFocusPainted(false);
        settingsBtn.setBackground(Color.WHITE);
        settingsBtn.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        settingsBtn.setFont(FontKit.regular(12f));
        settingsBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settingsBtn.setHorizontalAlignment(SwingConstants.LEFT);

        settingsBtn.addActionListener(e -> {
            menu.setVisible(false);
            showSimpleSettingsDialog();
        });

        root.add(settingsBtn);

        menu.add(root);
        return menu;
    }

    private void showSimpleSettingsDialog() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Student Settings", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(16, 18, 16, 18));
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Profile & Settings");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);
        panel.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        center.add(labelPair("Name", userDisplayName));
        center.add(Box.createVerticalStrut(8));
        center.add(labelPair("Program", program));
        center.add(Box.createVerticalStrut(8));
        center.add(labelPair("Year", yearStr));
        center.add(Box.createVerticalStrut(8));
        center.add(labelPair("Role", "Student"));

        panel.add(center, BorderLayout.CENTER);

        JButton close = new JButton("Close");
        close.setFocusPainted(false);
        close.addActionListener(e -> dlg.dispose());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        south.setOpaque(false);
        south.add(close);
        panel.add(south, BorderLayout.SOUTH);

        dlg.setContentPane(panel);
        dlg.setVisible(true);
    }

    private JPanel labelPair(String label, String value) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(FontKit.semibold(12f));
        l.setForeground(TEXT_600);
        JLabel v = new JLabel(value != null ? value : "-");
        v.setFont(FontKit.regular(12f));
        v.setForeground(TEXT_900);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        return p;
    }

    // ===== LEFT COLUMN: METRICS + REGISTERED COURSES =========================

    private JComponent buildLeftColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(buildMetricsRow());
        col.add(Box.createVerticalStrut(16));
        col.add(buildRegisteredCoursesCard());

        return col;
    }

    private JComponent buildMetricsRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);

        int numCourses = (registeredCourses == null) ? 0 : registeredCourses.size();

        row.add(metricCard(
                String.valueOf(numCourses),
                "Registered Courses",
                "Number of enrollments for this term."
        ));
        row.add(metricCard(
                String.valueOf(totalCredits),
                "Total Credits",
                "Sum of course credits you registered for."
        ));

        return row;
    }

    private erp.ui.common.RoundedPanel metricCard(String value, String label, String helper) {
        erp.ui.common.RoundedPanel p = new erp.ui.common.RoundedPanel(18);
        p.setBackground(CARD);
        p.setBorder(new EmptyBorder(16, 18, 16, 18));
        p.setLayout(new BorderLayout());

        JLabel v = new JLabel(value);
        v.setFont(FontKit.bold(22f));
        v.setForeground(TEXT_900);

        JLabel l = new JLabel(label);
        l.setFont(FontKit.semibold(13f));
        l.setForeground(TEXT_600);

        JLabel h = new JLabel(helper);
        h.setFont(FontKit.regular(11f));
        h.setForeground(new Color(148, 163, 184));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(v);
        top.add(Box.createVerticalStrut(2));
        top.add(l);

        p.add(top, BorderLayout.NORTH);
        p.add(h, BorderLayout.SOUTH);

        return p;
    }

    /**
     * Panel: just the courses this student is registered for.
     */
    private JComponent buildRegisteredCoursesCard() {
        erp.ui.common.RoundedPanel card = new erp.ui.common.RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel title = new JLabel("Registered Courses");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Current term enrollments");
        subtitle.setFont(FontKit.regular(12f));
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

        if (registeredCourses == null || registeredCourses.isEmpty()) {
            JLabel empty = new JLabel("No courses registered yet.");
            empty.setForeground(TEXT_600);
            empty.setFont(FontKit.regular(12f));
            list.add(empty);
        } else {
            for (int i = 0; i < registeredCourses.size(); i++) {
                RegisteredCourse rc = registeredCourses.get(i);
                list.add(courseRow(rc));
                if (i < registeredCourses.size() - 1) {
                    list.add(Box.createVerticalStrut(8));
                    JSeparator sep = new JSeparator();
                    sep.setForeground(BORDER);
                    list.add(sep);
                    list.add(Box.createVerticalStrut(8));
                }
            }
        }

        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getViewport().setBackground(CARD);

        card.add(sp, BorderLayout.CENTER);
        return card;
    }

    private JComponent courseRow(RegisteredCourse rc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        String code = rc.code != null && !rc.code.isBlank()
                ? rc.code
                : rc.courseId;

        JLabel line1 = new JLabel(code + "  •  " + rc.title);
        line1.setFont(FontKit.semibold(13f));
        line1.setForeground(TEXT_900);

        JLabel line2 = new JLabel("Credits: " + rc.credits);
        line2.setFont(FontKit.regular(11f));
        line2.setForeground(TEXT_600);

        left.add(line1);
        left.add(Box.createVerticalStrut(2));
        left.add(line2);

        row.add(left, BorderLayout.CENTER);

        return row;
    }

    // ===== RIGHT COLUMN: CALENDAR + TODAY'S TIMETABLE ========================

    private JComponent buildRightColumn() {
        JPanel col = new JPanel();
        col.setOpaque(false);
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(buildCalendarCard());
        col.add(Box.createVerticalStrut(16));
        col.add(buildTodayTimetableCard());

        return col;
    }

    /**
     * Month calendar: lightly teal card background, teal highlight for today.
     */
    private JComponent buildCalendarCard() {
        erp.ui.common.RoundedPanel card = new erp.ui.common.RoundedPanel(20);
        // Light teal background that matches the palette but stays subtle
        card.setBackground(new Color(224, 242, 241)); // very light teal
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        LocalDate today = LocalDate.now();
        LocalDate firstOfMonth = today.withDayOfMonth(1);
        String monthYear = firstOfMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                + " " + firstOfMonth.getYear();

        JLabel title = new JLabel(monthYear);
        title.setFont(FontKit.semibold(15f));
        title.setForeground(new Color(15, 63, 61));

        JLabel subtitle = new JLabel("Calendar");
        subtitle.setFont(FontKit.regular(12f));
        subtitle.setForeground(new Color(45, 106, 79));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(subtitle);
        left.add(title);

        header.add(left, BorderLayout.WEST);
        card.add(header, BorderLayout.NORTH);

        JPanel calendarGrid = new JPanel(new GridLayout(0, 7, 4, 4));
        calendarGrid.setOpaque(false);
        calendarGrid.setBorder(new EmptyBorder(8, 0, 0, 0));

        String[] days = {"M", "T", "W", "T", "F", "S", "S"};
        for (String d : days) {
            JLabel lbl = new JLabel(d, SwingConstants.CENTER);
            lbl.setFont(FontKit.semibold(11f));
            lbl.setForeground(new Color(55, 65, 81));
            calendarGrid.add(lbl);
        }

        int firstDayIndex = dayOfWeekIndex(firstOfMonth.getDayOfWeek()); // 0 = Mon
        int daysInMonth = firstOfMonth.lengthOfMonth();

        for (int i = 0; i < firstDayIndex; i++) {
            calendarGrid.add(new JLabel(""));
        }

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate d = firstOfMonth.withDayOfMonth(day);
            boolean isToday = d.isEqual(today);

            JLabel lbl = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            lbl.setFont(FontKit.regular(11f));

            if (isToday) {
                lbl.setFont(FontKit.semibold(11f));
                lbl.setOpaque(true);
                lbl.setBackground(new Color(22, 101, 88));
                lbl.setForeground(Color.WHITE);
                lbl.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            } else {
                lbl.setForeground(TEXT_900);
            }
            calendarGrid.add(lbl);
        }

        card.add(calendarGrid, BorderLayout.CENTER);
        return card;
    }

    private int dayOfWeekIndex(DayOfWeek dow) {
        // Monday = 0 ... Sunday = 6
        int v = dow.getValue(); // Monday = 1 ... Sunday = 7
        return v - 1;
    }

    /**
     * "Today's Timetable": only courses on today's weekday,
     * using sections.day_time like "Thu 11:00-12:30", sorted by start time.
     */
    private JComponent buildTodayTimetableCard() {
        erp.ui.common.RoundedPanel card = new erp.ui.common.RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Today's Timetable");
        title.setFont(FontKit.semibold(15f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Based on your registered courses");
        subtitle.setFont(FontKit.regular(12f));
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

        List<RegisteredCourse> todayCourses = getCoursesForTodaySorted();

        if (todayCourses.isEmpty()) {
            JLabel empty = new JLabel("No classes found for today.");
            empty.setFont(FontKit.regular(12f));
            empty.setForeground(TEXT_600);
            list.add(empty);
        } else {
            for (RegisteredCourse rc : todayCourses) {
                list.add(timetableRow(rc));
                list.add(Box.createVerticalStrut(8));
            }
        }

        card.add(list, BorderLayout.CENTER);
        return card;
    }

    /**
     * Returns today's courses (by weekday prefix in day_time),
     * sorted by start time parsed from the string (e.g., "Thu 11:00-12:30").
     */
    private List<RegisteredCourse> getCoursesForTodaySorted() {
        List<RegisteredCourse> result = new ArrayList<>();
        if (registeredCourses == null || registeredCourses.isEmpty()) {
            return result;
        }

        String todayAbbrev = LocalDate.now()
                .getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH); // "Mon", "Tue", "Wed", "Thu", ...

        for (RegisteredCourse rc : registeredCourses) {
            if (rc.dayTime == null || rc.dayTime.isBlank()) continue;
            String trimmed = rc.dayTime.trim();        // "Thu 11:00-12:30"
            int spaceIdx = trimmed.indexOf(' ');
            String dayToken = (spaceIdx > 0) ? trimmed.substring(0, spaceIdx) : trimmed;
            if (dayToken.equalsIgnoreCase(todayAbbrev)) {
                result.add(rc);
            }
        }

        // Sort by start time (after the first space)
        result.sort(Comparator.comparing(rc -> {
            LocalTime t = parseStartTime(rc.dayTime);
            return t != null ? t : LocalTime.MAX;
        }));

        return result;
    }

    /**
     * Parses "Thu 11:00-12:30" → LocalTime 11:00.
     */
    private LocalTime parseStartTime(String dayTime) {
        if (dayTime == null) return null;
        String trimmed = dayTime.trim(); // "Thu 11:00-12:30"
        int spaceIdx = trimmed.indexOf(' ');
        if (spaceIdx < 0 || spaceIdx + 1 >= trimmed.length()) return null;
        String timePart = trimmed.substring(spaceIdx + 1); // "11:00-12:30"
        int dashIdx = timePart.indexOf('-');
        String start = (dashIdx > 0) ? timePart.substring(0, dashIdx) : timePart; // "11:00"
        try {
            return LocalTime.parse(start, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception e) {
            return null;
        }
    }

    private JComponent timetableRow(RegisteredCourse rc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        String code = rc.code != null && !rc.code.isBlank()
                ? rc.code
                : rc.courseId;

        JLabel line1 = new JLabel(code + "  •  " + rc.title);
        line1.setFont(FontKit.semibold(13f));
        line1.setForeground(TEXT_900);

        String meta = "";
        if (rc.dayTime != null && !rc.dayTime.isBlank()) {
            meta += rc.dayTime;
        }
        if (rc.room != null && !rc.room.isBlank()) {
            if (!meta.isEmpty()) meta += "  •  ";
            meta += rc.room;
        }
        if (meta.isEmpty()) meta = "Schedule details not available";

        JLabel line2 = new JLabel(meta);
        line2.setFont(FontKit.regular(11f));
        line2.setForeground(TEXT_600);

        left.add(line1);
        left.add(Box.createVerticalStrut(2));
        left.add(line2);

        row.add(left, BorderLayout.CENTER);

        // Simple vertical marker on the left (classy, not loud)
        JPanel markerWrap = new JPanel(new BorderLayout());
        markerWrap.setOpaque(false);
        JComponent marker = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(TEAL_LIGHT);
                g2.fillRoundRect(getWidth() / 2 - 1, 0, 2, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        marker.setPreferredSize(new Dimension(8, 36));
        markerWrap.add(marker, BorderLayout.CENTER);
        row.add(markerWrap, BorderLayout.WEST);

        return row;
    }

    // ===== NOTIFICATIONS =====================================================

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

    // ===== UTIL =============================================================

    private static String todayString() {
        LocalDate d = LocalDate.now();
        return d.getDayOfMonth() + " " +
                d.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) +
                " " + d.getYear();
    }
}
