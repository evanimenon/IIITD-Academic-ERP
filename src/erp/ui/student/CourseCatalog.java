// CourseCatalog.java
package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

public class CourseCatalog extends StudentFrameBase {

    private static final Color BORDER_COLOR   = new Color(230, 233, 236);
    private static final Color BG_LIGHT       = new Color(246, 248, 252);
    private static final Color CARD_SELECTED  = new Color(220, 241, 239);
    private static final String SEARCH_PLACEHOLDER = "Search courses...";

    /**
     * Single course row, plus whether THIS student is registered in it.
     */
    static class CourseRecord {
        final String courseId;
        final String code;
        final String title;
        final String instructors;
        final int credits;
        final int capacity;
        final int enrolled;
        final boolean registered; // true if this student has REGISTERED enrollment for this course

        CourseRecord(String courseId, String code, String title, String instructors,
                     int credits, int capacity, int enrolled, boolean registered) {
            this.courseId    = courseId;
            this.code        = code;
            this.title       = title;
            this.instructors = instructors;
            this.credits     = credits;
            this.capacity    = capacity;
            this.enrolled    = enrolled;
            this.registered  = registered;
        }
    }

    private JTextField searchField;
    private JPanel     cardsContainer;
    private List<CourseRecord> courses;
    private CourseCard selectedCard;

    // Backward compatible constructor (no studentId -> mostly for testing)
    public CourseCatalog(String userDisplayName) {
        this(null, userDisplayName);
    }

    public CourseCatalog(String studentId, String userDisplayName) {
        // IMPORTANT: we don't keep our own studentId; we let StudentFrameBase own it.
        super(studentId, userDisplayName, Page.CATALOG);
        setTitle("IIITD ERP – Course Catalog");
        // Maintenance banner is now handled by StudentFrameBase.buildBody()
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // ── Top bar: title + search ───────────────────────────────────────────
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(8, 4, 8, 4));
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.X_AXIS));

        JLabel currentLbl = new JLabel("Current Courses");
        currentLbl.setFont(FontKit.semibold(20f));
        currentLbl.setBorder(new EmptyBorder(0, 4, 0, 0));

        topBar.add(currentLbl);
        topBar.add(Box.createHorizontalGlue());

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setMaximumSize(new Dimension(360, 36));
        searchPanel.setPreferredSize(new Dimension(320, 36));

        searchField = new JTextField();
        searchField.setFont(FontKit.regular(14f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));

        // placeholder behaviour
        searchField.setForeground(new Color(148, 163, 184));
        searchField.setText(SEARCH_PLACEHOLDER);
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (SEARCH_PLACEHOLDER.equals(searchField.getText())) {
                    searchField.setText("");
                    searchField.setForeground(new Color(15, 23, 42));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().trim().isEmpty()) {
                    searchField.setForeground(new Color(148, 163, 184));
                    searchField.setText(SEARCH_PLACEHOLDER);
                }
            }
        });

        searchPanel.add(searchField, BorderLayout.CENTER);
        topBar.add(searchPanel);

        main.add(topBar, BorderLayout.NORTH);

        // ── Cards container + scroll ─────────────────────────────────────────
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setBackground(BG_LIGHT);
        cardsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scroll.getViewport().setBackground(BG_LIGHT);
        scroll.getVerticalScrollBar().setUnitIncrement(16); // smoother scroll

        main.add(scroll, BorderLayout.CENTER);

        // search behaviour
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { rebuildCards(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { rebuildCards(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { rebuildCards(); }
        });

        loadCourses();
        return main;
    }

    // ── Card component ───────────────────────────────────────────────────────
    private class CourseCard extends RoundedPanel {
        final CourseRecord record;
        boolean selected = false;

        CourseCard(CourseRecord record) {
            super(16);
            this.record = record;
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(10, 10, 10, 10));

            int h = 90;
            setPreferredSize(new Dimension(0, h));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            setMinimumSize(new Dimension(0, h));

            // Left coloured stripe
            JPanel stripe = new JPanel();
            stripe.setPreferredSize(new Dimension(6, 1));
            stripe.setBackground(getDeptColor(record.courseId));
            stripe.setOpaque(true);
            add(stripe, BorderLayout.WEST);

            // Center text
            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(0, 12, 0, 0));

            JLabel titleLbl = new JLabel(record.title);
            titleLbl.setFont(FontKit.semibold(15f));
            titleLbl.setForeground(new Color(15, 23, 42));

            String instName = (record.instructors == null || record.instructors.isBlank())
                    ? "TBA"
                    : record.instructors;
            JLabel instLbl = new JLabel(instName);
            instLbl.setFont(FontKit.regular(13f));
            instLbl.setForeground(new Color(100, 116, 139));

            JLabel idLbl = new JLabel("ID: " + record.courseId + " • Code: " + record.code);
            idLbl.setFont(FontKit.regular(12f));
            idLbl.setForeground(new Color(148, 163, 184));

            center.add(Box.createVerticalStrut(2));
            center.add(titleLbl);
            center.add(Box.createVerticalStrut(3));
            center.add(instLbl);
            center.add(Box.createVerticalStrut(3));
            center.add(idLbl);

            add(center, BorderLayout.CENTER);

            // Right side: enrolled/capacity + “Registered” pill if registered
            JPanel right = new JPanel();
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            right.setOpaque(false);
            right.setBorder(new EmptyBorder(4, 0, 4, 6));

            JLabel capLbl = new JLabel(record.enrolled + "/" + record.capacity);
            capLbl.setFont(FontKit.semibold(13f));
            capLbl.setForeground(new Color(75, 85, 99));
            capLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(capLbl);

            if (record.registered) {
                right.add(Box.createVerticalStrut(6));
                JLabel regLbl = new JLabel("Registered");
                regLbl.setFont(FontKit.semibold(11f));
                regLbl.setForeground(new Color(22, 163, 74));
                regLbl.setOpaque(true);
                regLbl.setBackground(new Color(220, 252, 231));
                regLbl.setBorder(new EmptyBorder(2, 8, 2, 8));
                regLbl.setAlignmentX(Component.RIGHT_ALIGNMENT);
                right.add(regLbl);
            }

            right.add(Box.createVerticalGlue());
            add(right, BorderLayout.EAST);

            // Mouse behaviour
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSelectedCard(CourseCard.this);

                    // Double-click → try add/drop
                    if (e.getClickCount() >= 2) {
                        handleCardAction(record);
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(new Color(250, 250, 250));
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(Color.WHITE);
                        repaint();
                    }
                }
            });

            updateVisualState();
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            updateVisualState();
        }

        private void updateVisualState() {
            if (selected) {
                setBackground(CARD_SELECTED);
            } else {
                setBackground(Color.WHITE);
            }
            repaint();
        }
    }

    private void setSelectedCard(CourseCard card) {
        if (selectedCard == card) return;
        if (selectedCard != null) selectedCard.setSelected(false);
        selectedCard = card;
        if (selectedCard != null) selectedCard.setSelected(true);
    }

    // Double-click: Register or Drop using modern dialogs
    private void handleCardAction(CourseRecord rec) {
        // 🔒 Block in maintenance mode
        if (isReadOnly()) {
            ModernResultDialog.showMessage(
                    this,
                    ModernResultDialog.Type.ERROR,
                    "Maintenance mode",
                    "You cannot add or drop courses while the system is in maintenance mode.");
            return;
        }

        // studentId here should come from StudentFrameBase
        if (studentId == null || studentId.isBlank()) {
            ModernResultDialog.showMessage(
                    this,
                    ModernResultDialog.Type.ERROR,
                    "Missing Student ID",
                    "Student ID is not available on this screen.");
            return;
        }

        boolean alreadyRegistered = rec.registered;
        String action = alreadyRegistered ? "Drop" : "Add";

        // 1) Confirmation dialog
        ModernConfirmDialog confirm = new ModernConfirmDialog(
                this,
                action,
                rec.title,
                rec.courseId,
                rec.code);
        boolean confirmed = confirm.showDialog();
        if (!confirmed) {
            return; // user hit "Close"
        }

        // 2) Perform action
        String resultMsg;
        if (alreadyRegistered) {
            resultMsg = EnrollmentService.dropCourse(studentId, rec.courseId);
        } else {
            resultMsg = EnrollmentService.registerForCourse(studentId, rec.courseId);
        }

        // 3) Classify result & show modern result dialog
        ModernResultDialog.Type type = classifyResult(resultMsg, alreadyRegistered);
        String title;
        if (type == ModernResultDialog.Type.SUCCESS) {
            title = alreadyRegistered ? "Course Dropped" : "Course Added";
        } else if (type == ModernResultDialog.Type.CAPACITY_FULL) {
            title = "Course Capacity Full!";
            resultMsg = "Course capacity is full! You cannot register for this course.";
        } else {
            title = "Action Failed";
        }

        ModernResultDialog.showMessage(
                this,
                type,
                title,
                resultMsg);

        // 4) Reload so registered badges / ordering update
        loadCourses();
    }

    /**
     * Decide how to color the result dialog based on the backend message.
     */
    private ModernResultDialog.Type classifyResult(String msg, boolean wasDrop) {
        if (msg == null)
            return ModernResultDialog.Type.ERROR;
        String lower = msg.toLowerCase();

        // success heuristics
        if (lower.startsWith("successfully")
                || lower.contains("section dropped successfully")
                || lower.contains("you have successfully dropped")) {
            return ModernResultDialog.Type.SUCCESS;
        }

        // explicit capacity full
        if (lower.contains("capacity is full") || lower.contains("course capacity is full")) {
            return ModernResultDialog.Type.CAPACITY_FULL;
        }

        // deadline / date passed
        if (lower.contains("deadline") || lower.contains("no longer drop")
                || lower.contains("time for drop has gone")
                || lower.contains("can no longer")) {
            return ModernResultDialog.Type.ERROR;
        }

        // generic error / warning
        return ModernResultDialog.Type.ERROR;
    }

    // Dept colour based on first 3 letters of course_id
    private Color getDeptColor(String courseId) {
        if (courseId == null || courseId.length() < 3)
            return Color.BLACK;
        String prefix = courseId.substring(0, 3).toUpperCase();

        switch (prefix) {
            case "BIO": return new Color(16, 185, 129);   // green
            case "CSE": return new Color(59, 130, 246);   // blue
            case "DES": return new Color(139, 92, 246);   // purple
            case "ECE": return new Color(234, 179, 8);    // yellow
            case "ECO": return new Color(22, 163, 74);    // light green
            case "MTH": return new Color(248, 113, 113);  // red
            case "ABC": return Color.BLACK;
            default:    return new Color(148, 163, 184);  // neutral grey
        }
    }

    // ── Load courses + registered flags explicitly, then shuffle ─────────────
    private void loadCourses() {
        if (courses == null) {
            courses = new ArrayList<>();
        } else {
            courses.clear();
        }

        // 1) Which course_ids is this student registered in?
        Set<String> registeredCourseIds = new HashSet<>();

        if (studentId != null && !studentId.isBlank()) {
            final String regSql = "SELECT DISTINCT s.course_id " +
                    "FROM erp_db.enrollments e " +
                    "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                    "WHERE e.student_id = ? " +
                    "  AND e.status = 'REGISTERED'";

            try (Connection conn = DatabaseConnection.erp().getConnection();
                 PreparedStatement ps = conn.prepareStatement(regSql)) {

                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        registeredCourseIds.add(rs.getString("course_id"));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // If this fails, we just treat everything as unregistered.
            }
        }

        // 2) Load full course catalog
        final String sql = "SELECT c.course_id, c.code, c.title, c.credits, " +
                "       COALESCE(SUM(s.capacity), 0) AS total_capacity, " +
                "       COALESCE(GROUP_CONCAT(DISTINCT i.instructor_name " +
                "               ORDER BY i.instructor_name SEPARATOR ', '), 'TBA') AS instructors, " +
                "       COALESCE(( " +
                "           SELECT COUNT(*) " +
                "           FROM erp_db.enrollments e " +
                "           JOIN erp_db.sections s2 ON s2.section_id = e.section_id " +
                "           WHERE s2.course_id = c.course_id " +
                "             AND e.status = 'REGISTERED' " +
                "       ), 0) AS enrolled " +
                "FROM   erp_db.courses c " +
                "LEFT JOIN erp_db.sections s    ON s.course_id = c.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "GROUP BY c.course_id, c.code, c.title, c.credits " +
                "ORDER BY c.course_id ASC";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                boolean registeredForThisCourse = registeredCourseIds.contains(courseId);

                CourseRecord rec = new CourseRecord(
                        courseId,
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getString("instructors"),
                        rs.getInt("credits"),
                        rs.getInt("total_capacity"),
                        rs.getInt("enrolled"),
                        registeredForThisCourse);
                courses.add(rec);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        rebuildCards();
    }

    // ── Build UI: registered on top, others below ────────────────────────────
    private void rebuildCards() {
        if (cardsContainer == null)
            return;

        String text = (searchField == null) ? "" : searchField.getText().trim();
        String filter = SEARCH_PLACEHOLDER.equals(text) ? "" : text.toLowerCase();

        cardsContainer.removeAll();
        selectedCard = null;

        List<CourseRecord> top    = new ArrayList<>(); // registered
        List<CourseRecord> bottom = new ArrayList<>(); // not registered

        if (courses != null) {
            for (CourseRecord rec : courses) {
                if (!filter.isEmpty() && !matchesFilter(rec, filter))
                    continue;

                if (rec.registered)
                    top.add(rec);
                else
                    bottom.add(rec);
            }
        }

        top.sort(Comparator.comparing(r -> r.courseId));
        bottom.sort(Comparator.comparing(r -> r.courseId));

        if (!top.isEmpty()) {
            cardsContainer.add(sectionLabel("Registered Courses"));
            cardsContainer.add(Box.createVerticalStrut(6));
            for (CourseRecord rec : top) {
                cardsContainer.add(new CourseCard(rec));
                cardsContainer.add(Box.createVerticalStrut(8));
            }
            cardsContainer.add(Box.createVerticalStrut(12));
        }

        if (!bottom.isEmpty()) {
            cardsContainer.add(sectionLabel("Other Courses"));
            cardsContainer.add(Box.createVerticalStrut(6));
            for (CourseRecord rec : bottom) {
                cardsContainer.add(new CourseCard(rec));
                cardsContainer.add(Box.createVerticalStrut(8));
            }
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    private JComponent sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FontKit.semibold(13f));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBorder(new EmptyBorder(4, 4, 0, 0));
        return lbl;
    }

    private boolean matchesFilter(CourseRecord rec, String filter) {
        if (filter == null || filter.isEmpty())
            return true;

        String haystack = (rec.courseId + " " +
                           rec.code + " " +
                           rec.title + " " +
                           (rec.instructors == null ? "" : rec.instructors))
                           .toLowerCase();

        return haystack.contains(filter);
    }

    // ── Modern confirm + result dialogs (unchanged) ─────────────────────────
    // ... keep ModernConfirmDialog & ModernResultDialog exactly as in your version ...

    /**
     * Modern "Are you sure?" dialog for Add / Drop.
     */
    private static class ModernConfirmDialog extends JDialog {

        private boolean accepted = false;

        ModernConfirmDialog(JFrame parent,
                            String action, // "Add" or "Drop"
                            String courseTitle,
                            String courseId,
                            String courseCode) {

            super(parent, true); // modal
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0)); // transparent around rounded panel

            Color accent = "Drop".equalsIgnoreCase(action)
                    ? new Color(239, 68, 68) // red-ish
                    : new Color(37, 99, 235); // blue-ish

            RoundedPanel card = new RoundedPanel(24);
            card.setLayout(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new EmptyBorder(16, 24, 20, 24));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 0, 12, 0));

            JLabel icon = new JLabel("!");
            icon.setFont(FontKit.semibold(24f));
            icon.setHorizontalAlignment(SwingConstants.CENTER);
            icon.setOpaque(true);
            icon.setBackground(accent);
            icon.setForeground(Color.WHITE);
            icon.setPreferredSize(new Dimension(40, 40));
            icon.setBorder(new EmptyBorder(4, 0, 4, 0));

            JPanel iconWrapper = new JPanel();
            iconWrapper.setOpaque(false);
            iconWrapper.add(icon);

            header.add(iconWrapper, BorderLayout.NORTH);
            card.add(header, BorderLayout.NORTH);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(4, 0, 8, 0));

            JLabel titleLbl = new JLabel(
                    (action.equalsIgnoreCase("Drop") ? "Drop Course?" : "Add Course?"));
            titleLbl.setFont(FontKit.semibold(18f));
            titleLbl.setForeground(new Color(15, 23, 42));
            titleLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel courseLbl = new JLabel(courseTitle);
            courseLbl.setFont(FontKit.regular(14f));
            courseLbl.setForeground(new Color(55, 65, 81));
            courseLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel metaLbl = new JLabel("ID: " + courseId + " • Code: " + courseCode);
            metaLbl.setFont(FontKit.regular(12f));
            metaLbl.setForeground(new Color(148, 163, 184));
            metaLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            center.add(Box.createVerticalStrut(4));
            center.add(titleLbl);
            center.add(Box.createVerticalStrut(6));
            center.add(courseLbl);
            center.add(Box.createVerticalStrut(4));
            center.add(metaLbl);

            card.add(center, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            buttons.setOpaque(false);
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
            buttons.setBorder(new EmptyBorder(10, 0, 0, 0));

            JButton primary = new JButton(action.equalsIgnoreCase("Drop") ? "Drop" : "Add");
            primary.setFont(FontKit.semibold(13f));
            primary.setForeground(Color.WHITE);
            primary.setBackground(accent);
            primary.setFocusPainted(false);
            primary.setBorderPainted(false);
            primary.setOpaque(true);
            primary.setPreferredSize(new Dimension(110, 34));
            primary.setMaximumSize(new Dimension(110, 34));

            primary.addActionListener(e -> {
                accepted = true;
                dispose();
            });

            JButton secondary = new JButton("Close");
            secondary.setFont(FontKit.regular(13f));
            secondary.setForeground(new Color(107, 114, 128));
            secondary.setBackground(new Color(248, 250, 252));
            secondary.setFocusPainted(false);
            secondary.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235)));
            secondary.setPreferredSize(new Dimension(100, 34));
            secondary.setMaximumSize(new Dimension(100, 34));

            secondary.addActionListener(e -> {
                accepted = false;
                dispose();
            });

            buttons.add(Box.createHorizontalGlue());
            buttons.add(primary);
            buttons.add(Box.createHorizontalStrut(12));
            buttons.add(secondary);
            buttons.add(Box.createHorizontalGlue());

            card.add(buttons, BorderLayout.SOUTH);

            setContentPane(card);
            pack();
            setLocationRelativeTo(parent);
        }

        boolean showDialog() {
            setVisible(true);
            return accepted;
        }
    }

    private static class ModernResultDialog extends JDialog {

        enum Type {
            SUCCESS,
            CAPACITY_FULL,
            ERROR
        }

        private ModernResultDialog(JFrame parent,
                                   Type type,
                                   String title,
                                   String message) {
            super(parent, true);
            setUndecorated(true);
            setBackground(new Color(0, 0, 0, 0));

            Color accent;
            switch (type) {
                case SUCCESS:
                    accent = new Color(34, 197, 94); // green
                    break;
                case CAPACITY_FULL:
                case ERROR:
                default:
                    accent = new Color(239, 68, 68); // red
                    break;
            }

            RoundedPanel card = new RoundedPanel(24);
            card.setLayout(new BorderLayout());
            card.setBackground(Color.WHITE);
            card.setBorder(new EmptyBorder(0, 0, 16, 0));

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(accent);
            header.setBorder(new EmptyBorder(10, 20, 14, 20));

            JLabel icon = new JLabel("!");
            icon.setFont(FontKit.semibold(24f));
            icon.setForeground(Color.WHITE);
            icon.setHorizontalAlignment(SwingConstants.LEFT);

            JLabel titleLbl = new JLabel(title);
            titleLbl.setFont(FontKit.semibold(18f));
            titleLbl.setForeground(Color.WHITE);

            JPanel headerText = new JPanel();
            headerText.setOpaque(false);
            headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
            headerText.add(titleLbl);

            header.add(icon, BorderLayout.WEST);
            header.add(headerText, BorderLayout.CENTER);

            card.add(header, BorderLayout.NORTH);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
            center.setBorder(new EmptyBorder(12, 24, 0, 24));

            JLabel msgLbl = new JLabel("<html><body style='text-align:center;width:260px;'>"
                    + escapeHtml(message) + "</body></html>");
            msgLbl.setFont(FontKit.regular(13f));
            msgLbl.setForeground(new Color(75, 85, 99));
            msgLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

            center.add(Box.createVerticalStrut(4));
            center.add(msgLbl);

            card.add(center, BorderLayout.CENTER);

            JPanel buttons = new JPanel();
            buttons.setOpaque(false);
            buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
            buttons.setBorder(new EmptyBorder(12, 24, 12, 24));

            JButton close = new JButton("Close");
            close.setFont(FontKit.semibold(13f));
            close.setForeground(Color.WHITE);
            close.setBackground(accent);
            close.setFocusPainted(false);
            close.setBorderPainted(false);
            close.setOpaque(true);
            close.setPreferredSize(new Dimension(110, 34));
            close.setMaximumSize(new Dimension(110, 34));

            close.addActionListener(e -> dispose());

            buttons.add(Box.createHorizontalGlue());
            buttons.add(close);
            buttons.add(Box.createHorizontalGlue());

            card.add(buttons, BorderLayout.SOUTH);

            setContentPane(card);
            pack();
            setLocationRelativeTo(parent);
        }

        private static String escapeHtml(String s) {
            if (s == null)
                return "";
            return s.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
        }

        static void showMessage(JFrame parent, Type type, String title, String message) {
            ModernResultDialog d = new ModernResultDialog(parent, type, title, message);
            d.setVisible(true);
        }
    }
}
