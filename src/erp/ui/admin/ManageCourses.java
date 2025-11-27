package erp.ui.admin;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Admin view for managing Courses, Sections and Enrollments.
 *
 * Layout:
 * - Left: Course cards (similar to student CourseCatalog, larger grid)
 * - Right top: Section cards (similar to instructor MySections)
 * - Right bottom: Enrolled students table (with Final Grade shown only)
 *
 * Admin can:
 * - Edit course details (id, code/acronym, title, credits)
 * - Edit section details (instructor, time, room, capacity, semester, year)
 * - Add / delete courses
 * - Add / delete sections
 * - Add / remove students from a section
 */
public class ManageCourses extends AdminFrameBase {

    // Palette
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color TEXT_400 = new Color(148, 163, 184);
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_HOVER = new Color(242, 248, 247);
    private static final Color BORDER = new Color(226, 232, 240);

    // --- Models -------------------------------------------------------------

    private static class CourseRecord {
        String originalCourseId; // current DB key
        String courseId; // editable
        String code; // courses.code (acronym)
        String title;
        int credits;
    }

    private static class SectionRecord {
        int sectionId;
        String courseId;
        String instructorId;
        String instructorName;
        String dayTime;
        String room;
        int capacity;
        String semester;
        int year;
    }

    private static class StudentRow {
        String enrollmentId;
        String studentId;
        String rollNo;
        String name;
        String program;
        String year;
        String finalGrade; // from enrollments.final_grade
    }

    // --- State --------------------------------------------------------------

    // Courses
    private List<CourseRecord> courses = new ArrayList<>();
    private JPanel courseCardsContainer;
    private CourseCard selectedCourseCard;
    private CourseRecord selectedCourse;

    // Course detail editor
    private JTextField courseIdField;
    private JTextField acronymField;
    private JTextField titleField;
    private JTextField creditsField;
    private boolean courseDirty = false;

    // Sections
    private List<SectionRecord> sections = new ArrayList<>();
    private JPanel sectionsContainer;
    private SectionCard selectedSectionCard;
    private SectionRecord selectedSection;
    private Set<Integer> dirtySectionIds = new HashSet<>();

    // Students
    private List<StudentRow> students = new ArrayList<>();
    private StudentsTableModel studentsModel;
    private JTable studentsTable;

    // Buttons
    private JButton addCourseBtn;
    private JButton deleteCourseBtn;
    private JButton addSectionBtn;
    private JButton deleteSectionBtn;
    private JButton addStudentBtn;
    private JButton removeStudentBtn;
    private JButton saveChangesBtn;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public ManageCourses(String displayName) {
        this(null, displayName);
    }

    public ManageCourses(String adminId, String displayName) {
        super(adminId, displayName, Page.COURSES);
        setTitle("IIITD ERP – Manage Courses & Sections");
        if (metaLabel != null) {
            metaLabel.setText("System Administrator");
        }
    }

    // ------------------------------------------------------------------------
    // Frame content
    // ------------------------------------------------------------------------

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // ---- Hero header ----
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("Manage Courses & Sections");
        h1.setFont(FontKit.bold(24f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel rightLabel = new JLabel("Logged in as " + userDisplayName);
        rightLabel.setFont(FontKit.regular(13f));
        rightLabel.setForeground(new Color(200, 230, 225));
        hero.add(rightLabel, BorderLayout.EAST);

        main.add(hero, BorderLayout.NORTH);

        // ---- Main split: left courses / right details ----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.35);
        split.setBorder(BorderFactory.createEmptyBorder());
        split.setContinuousLayout(true);

        JPanel left = buildCoursesPanel();
        JPanel right = buildDetailsPanel();

        split.setLeftComponent(left);
        split.setRightComponent(right);

        main.add(split, BorderLayout.CENTER);

        // Initial data
        loadCourses();

        return main;
    }

    // ------------------------------------------------------------------------
    // Left: Courses panel
    // ------------------------------------------------------------------------

    private JPanel buildCoursesPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(16, 16, 16, 8));

        JLabel title = new JLabel("Courses");
        title.setFont(FontKit.semibold(16f));
        title.setForeground(new Color(30, 41, 59));
        root.add(title, BorderLayout.NORTH);

        courseCardsContainer = new JPanel();
        courseCardsContainer.setOpaque(false);
        courseCardsContainer.setLayout(new GridLayout(0, 2, 12, 12));

        JScrollPane sc = new JScrollPane(courseCardsContainer);
        sc.setBorder(BorderFactory.createLineBorder(BORDER));
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(sc, BorderLayout.CENTER);

        // Bottom toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        addCourseBtn = new JButton("Add Course");
        deleteCourseBtn = new JButton("Delete Course");

        for (JButton b : List.of(addCourseBtn, deleteCourseBtn)) {
            b.setFont(FontKit.semibold(12f));
            b.setFocusPainted(false);
            b.setBackground(new Color(248, 250, 252));
            b.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        addCourseBtn.addActionListener(e -> handleAddCourse());
        deleteCourseBtn.addActionListener(e -> handleDeleteCourse());

        toolbar.add(addCourseBtn);
        toolbar.add(deleteCourseBtn);

        root.add(toolbar, BorderLayout.SOUTH);

        return root;
    }

    // ------------------------------------------------------------------------
    // Right: details (course editor + sections + students)
    // ------------------------------------------------------------------------

    private JPanel buildDetailsPanel() {
        JPanel root = new JPanel();
        root.setOpaque(false);
        root.setLayout(new BorderLayout(0, 12));
        root.setBorder(new EmptyBorder(16, 8, 16, 16));

        JPanel courseEditor = buildCourseEditorPanel();
        root.add(courseEditor, BorderLayout.NORTH);

        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        verticalSplit.setResizeWeight(0.45);
        verticalSplit.setBorder(BorderFactory.createEmptyBorder());
        verticalSplit.setContinuousLayout(true);

        JPanel sectionsPanel = buildSectionsPanel();
        JPanel studentsPanel = buildStudentsPanel();

        verticalSplit.setTopComponent(sectionsPanel);
        verticalSplit.setBottomComponent(studentsPanel);

        root.add(verticalSplit, BorderLayout.CENTER);

        return root;
    }

    private JPanel buildCourseEditorPanel() {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 20, 14, 20));
        card.setLayout(new BorderLayout(12, 8));

        JLabel title = new JLabel("Course Details");
        title.setFont(FontKit.semibold(14f));
        title.setForeground(TEXT_900);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        courseIdField = new JTextField(14);
        acronymField = new JTextField(10);
        titleField = new JTextField(22);
        creditsField = new JTextField(4);

        JLabel cidLbl = new JLabel("Course ID");
        cidLbl.setFont(FontKit.regular(12f));
        cidLbl.setForeground(TEXT_600);

        JLabel acrLbl = new JLabel("Acronym");
        acrLbl.setFont(FontKit.regular(12f));
        acrLbl.setForeground(TEXT_600);

        JLabel ttlLbl = new JLabel("Title");
        ttlLbl.setFont(FontKit.regular(12f));
        ttlLbl.setForeground(TEXT_600);

        JLabel crdLbl = new JLabel("Credits");
        crdLbl.setFont(FontKit.regular(12f));
        crdLbl.setForeground(TEXT_600);

        gc.gridx = 0;
        gc.gridy = 0;
        form.add(cidLbl, gc);
        gc.gridx = 1;
        form.add(courseIdField, gc);

        gc.gridx = 2;
        form.add(acrLbl, gc);
        gc.gridx = 3;
        form.add(acronymField, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        form.add(ttlLbl, gc);
        gc.gridx = 1;
        gc.gridwidth = 3;
        gc.fill = GridBagConstraints.HORIZONTAL;
        form.add(titleField, gc);
        gc.gridwidth = 1;
        gc.fill = GridBagConstraints.NONE;

        gc.gridx = 0;
        gc.gridy = 2;
        form.add(crdLbl, gc);
        gc.gridx = 1;
        form.add(creditsField, gc);

        card.add(form, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        saveChangesBtn = new JButton("Save Changes");
        saveChangesBtn.setFont(FontKit.semibold(12f));
        saveChangesBtn.setFocusPainted(false);
        saveChangesBtn.setBackground(new Color(22, 163, 74));
        saveChangesBtn.setForeground(Color.WHITE);
        saveChangesBtn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        saveChangesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveChangesBtn.addActionListener(e -> handleSaveChanges());

        right.add(saveChangesBtn);
        card.add(right, BorderLayout.SOUTH);

        // Track changes
        DocumentListener dirtyListener = new SimpleDocumentListener(() -> {
            if (selectedCourse != null) {
                courseDirty = true;
                updateButtonsEnabled();
            }
        });

        courseIdField.getDocument().addDocumentListener(dirtyListener);
        acronymField.getDocument().addDocumentListener(dirtyListener);
        titleField.getDocument().addDocumentListener(dirtyListener);
        creditsField.getDocument().addDocumentListener(dirtyListener);

        return card;
    }

    private JPanel buildSectionsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);

        JLabel title = new JLabel("Sections");
        title.setFont(FontKit.semibold(14f));
        title.setForeground(TEXT_900);
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        root.add(title, BorderLayout.NORTH);

        sectionsContainer = new JPanel();
        sectionsContainer.setOpaque(false);
        sectionsContainer.setLayout(new BoxLayout(sectionsContainer, BoxLayout.Y_AXIS));

        JScrollPane sc = new JScrollPane(sectionsContainer);
        sc.setBorder(BorderFactory.createLineBorder(BORDER));
        sc.getVerticalScrollBar().setUnitIncrement(16);
        sc.getViewport().setBackground(BG);
        sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        root.add(sc, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        addSectionBtn = new JButton("Add Section");
        deleteSectionBtn = new JButton("Delete Section");

        for (JButton b : List.of(addSectionBtn, deleteSectionBtn)) {
            b.setFont(FontKit.semibold(12f));
            b.setFocusPainted(false);
            b.setBackground(new Color(248, 250, 252));
            b.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        addSectionBtn.addActionListener(e -> handleAddSection());
        deleteSectionBtn.addActionListener(e -> handleDeleteSection());

        toolbar.add(addSectionBtn);
        toolbar.add(deleteSectionBtn);

        root.add(toolbar, BorderLayout.SOUTH);

        return root;
    }

    private JPanel buildStudentsPanel() {
        JPanel root = new JPanel(new BorderLayout(0, 8));
        root.setOpaque(false);

        JLabel title = new JLabel("Enrolled Students");
        title.setFont(FontKit.semibold(14f));
        title.setForeground(TEXT_900);
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        root.add(title, BorderLayout.NORTH);

        studentsModel = new StudentsTableModel();
        studentsTable = new JTable(studentsModel);
        styleTable(studentsTable);

        JScrollPane sc = new JScrollPane(studentsTable);
        sc.setBorder(BorderFactory.createLineBorder(BORDER));
        sc.getViewport().setBackground(Color.WHITE);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        root.add(sc, BorderLayout.CENTER);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbar.setOpaque(false);

        addStudentBtn = new JButton("Add Student");
        removeStudentBtn = new JButton("Remove Student");

        for (JButton b : List.of(addStudentBtn, removeStudentBtn)) {
            b.setFont(FontKit.semibold(12f));
            b.setFocusPainted(false);
            b.setBackground(new Color(248, 250, 252));
            b.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        addStudentBtn.addActionListener(e -> handleAddStudent());
        removeStudentBtn.addActionListener(e -> handleRemoveStudent());

        toolbar.add(addStudentBtn);
        toolbar.add(removeStudentBtn);

        root.add(toolbar, BorderLayout.SOUTH);

        return root;
    }

    // ------------------------------------------------------------------------
    // Data loading
    // ------------------------------------------------------------------------

    private void loadCourses() {
        if (courses == null) {
            courses = new ArrayList<>();
        }
        if (sections == null) {
            sections = new ArrayList<>();
        }
        if (dirtySectionIds == null) {
            dirtySectionIds = new HashSet<>();
        }
        if (students == null) {
            students = new ArrayList<>();
        }

        courses.clear();
        selectedCourse = null;
        selectedCourseCard = null;

        if (courseCardsContainer != null) {
            courseCardsContainer.removeAll();
        }

        String sql = "SELECT course_id, code, title, credits FROM courses ORDER BY course_id";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                CourseRecord cr = new CourseRecord();
                cr.originalCourseId = rs.getString("course_id");
                cr.courseId = cr.originalCourseId;
                cr.code = rs.getString("code");
                cr.title = rs.getString("title");
                cr.credits = rs.getInt("credits");
                courses.add(cr);
            }

        } catch (SQLException ex) {
            showError("Failed to load courses", ex);
        }

        if (courseCardsContainer != null) {
            for (CourseRecord cr : courses) {
                CourseCard card = new CourseCard(cr);
                courseCardsContainer.add(card);
            }
            courseCardsContainer.revalidate();
            courseCardsContainer.repaint();
        }

        // Reset right side
        dirtySectionIds.clear();
        clearCourseEditor();

        sections.clear();
        if (sectionsContainer != null) {
            sectionsContainer.removeAll();
            sectionsContainer.revalidate();
            sectionsContainer.repaint();
        }

        students.clear();
        if (studentsModel != null) {
            studentsModel.fireTableDataChanged();
        }

        updateButtonsEnabled();
    }

    private void loadSectionsForCourse(CourseRecord course) {
        if (sections == null) {
            sections = new ArrayList<>();
        }
        if (dirtySectionIds == null) {
            dirtySectionIds = new HashSet<>();
        }

        sections.clear();
        selectedSection = null;
        selectedSectionCard = null;
        dirtySectionIds.clear();

        if (sectionsContainer != null) {
            sectionsContainer.removeAll();
        }

        if (course == null) {
            if (sectionsContainer != null) {
                sectionsContainer.revalidate();
                sectionsContainer.repaint();
            }
            students.clear();
            if (studentsModel != null) {
                studentsModel.fireTableDataChanged();
            }
            updateButtonsEnabled();
            return;
        }

        String sql = """
                SELECT s.section_id,
                       s.course_id,
                       s.instructor_id,
                       s.day_time,
                       s.room,
                       s.capacity,
                       s.semester,
                       s.year,
                       i.instructor_name
                FROM sections s
                LEFT JOIN instructors i
                  ON i.instructor_id = s.instructor_id
                WHERE s.course_id = ?
                ORDER BY s.year, s.semester, s.section_id
                """;

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, course.originalCourseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SectionRecord sr = new SectionRecord();
                    sr.sectionId = rs.getInt("section_id");
                    sr.courseId = rs.getString("course_id");
                    sr.instructorId = rs.getString("instructor_id");
                    sr.instructorName = rs.getString("instructor_name");
                    sr.dayTime = rs.getString("day_time");
                    sr.room = rs.getString("room");
                    sr.capacity = rs.getInt("capacity");
                    sr.semester = rs.getString("semester");
                    sr.year = rs.getInt("year");
                    sections.add(sr);
                }
            }

        } catch (SQLException ex) {
            showError("Failed to load sections", ex);
        }

        if (sectionsContainer != null) {
            for (SectionRecord sr : sections) {
                SectionCard card = new SectionCard(sr);
                sectionsContainer.add(card);
            }
            sectionsContainer.revalidate();
            sectionsContainer.repaint();
        }

        students.clear();
        if (studentsModel != null) {
            studentsModel.fireTableDataChanged();
        }
        updateButtonsEnabled();
    }

    private void loadStudentsForSection(SectionRecord section) {
        if (students == null) {
            students = new ArrayList<>();
        }
        students.clear();

        if (section == null) {
            if (studentsModel != null) {
                studentsModel.fireTableDataChanged();
            }
            updateButtonsEnabled();
            return;
        }

        String stuSql = """
                SELECT e.enrollment_id,
                       e.student_id,
                       s.roll_no,
                       s.full_name,
                       s.program,
                       s.year,
                       e.final_grade
                FROM enrollments e
                LEFT JOIN students s
                  ON s.student_id = e.student_id
                WHERE e.section_id = ?
                  AND e.status = 'REGISTERED'
                ORDER BY s.roll_no, e.student_id
                """;

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(stuSql)) {

            ps.setInt(1, section.sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StudentRow row = new StudentRow();
                    row.enrollmentId = rs.getString("enrollment_id");
                    row.studentId = rs.getString("student_id");

                    row.rollNo = rs.getString("roll_no");
                    if (row.rollNo == null)
                        row.rollNo = "";

                    row.name = rs.getString("full_name");
                    if (row.name == null)
                        row.name = "";

                    row.program = rs.getString("program");
                    if (row.program == null)
                        row.program = "";

                    row.year = rs.getString("year");
                    if (row.year == null)
                        row.year = "";

                    row.finalGrade = rs.getString("final_grade");
                    if (row.finalGrade == null)
                        row.finalGrade = "";

                    students.add(row);
                }
            }

        } catch (SQLException ex) {
            showError("Failed to load students", ex);
        }

        if (studentsModel != null) {
            studentsModel.fireTableDataChanged();
        }
        updateButtonsEnabled();
    }

    // ------------------------------------------------------------------------
    // Actions
    // ------------------------------------------------------------------------

    private void handleAddCourse() {
        JTextField idField = new JTextField(10);
        JTextField acrField = new JTextField(8);
        JTextField titleField = new JTextField(20);
        JTextField creditsField = new JTextField("4");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Course ID:"));
        form.add(idField);
        form.add(new JLabel("Acronym:"));
        form.add(acrField);
        form.add(new JLabel("Title:"));
        form.add(titleField);
        form.add(new JLabel("Credits:"));
        form.add(creditsField);

        int res = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add New Course",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String cid = idField.getText().trim();
        String acr = acrField.getText().trim();
        String ttl = titleField.getText().trim();
        String crdStr = creditsField.getText().trim();

        if (cid.isEmpty() || acr.isEmpty() || ttl.isEmpty() || crdStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(crdStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Credits must be an integer.",
                    "Invalid Credits",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO courses (course_id, code, title, credits) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cid);
            ps.setString(2, acr);
            ps.setString(3, ttl);
            ps.setInt(4, credits);
            ps.executeUpdate();

            loadCourses();

        } catch (SQLException ex) {
            showError("Failed to add course", ex);
        }
    }

    private void handleDeleteCourse() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a course to delete.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        CourseRecord cr = selectedCourse;

        int sectionCount = 0;
        int enrollmentCount = 0;

        String countSections = "SELECT COUNT(*) FROM sections WHERE course_id = ?";
        String countEnrollments = "SELECT COUNT(*) FROM enrollments " +
                "WHERE section_id IN (SELECT section_id FROM sections WHERE course_id = ?)";

        try (Connection conn = getConn()) {

            try (PreparedStatement ps = conn.prepareStatement(countSections)) {
                ps.setString(1, cr.originalCourseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sectionCount = rs.getInt(1);
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(countEnrollments)) {
                ps.setString(1, cr.originalCourseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        enrollmentCount = rs.getInt(1);
                    }
                }
            }

        } catch (SQLException ex) {
            showError("Failed to check dependent records", ex);
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Delete course ").append(cr.courseId).append("?\n\n");
        if (sectionCount > 0) {
            message.append("Sections: ").append(sectionCount).append("\n");
        }
        if (enrollmentCount > 0) {
            message.append("Enrollments: ").append(enrollmentCount)
                    .append("\n\nAll related sections and enrollments will also be deleted.");
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                message.toString(),
                "Confirm Delete Course",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        String deleteEnrollments = "DELETE FROM enrollments WHERE section_id IN " +
                "(SELECT section_id FROM sections WHERE course_id = ?)";
        String deleteSections = "DELETE FROM sections WHERE course_id = ?";
        String deleteCourse = "DELETE FROM courses WHERE course_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteEnrollments)) {
                    ps.setString(1, cr.originalCourseId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteSections)) {
                    ps.setString(1, cr.originalCourseId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteCourse)) {
                    ps.setString(1, cr.originalCourseId);
                    ps.executeUpdate();
                }

                conn.commit();
                loadCourses();

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError("Failed to delete course", ex);
        }
    }

    private void handleAddSection() {
        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a course first.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField instrField = new JTextField();
        JTextField dayTimeField = new JTextField("Mon 09:00-10:30");
        JTextField roomField = new JTextField("TBA");
        JTextField capacityField = new JTextField("60");
        JTextField semesterField = new JTextField("Monsoon");
        JTextField yearField = new JTextField("2025");

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel("Instructor ID:"));
        form.add(instrField);
        form.add(new JLabel("Day / Time:"));
        form.add(dayTimeField);
        form.add(new JLabel("Room:"));
        form.add(roomField);
        form.add(new JLabel("Capacity:"));
        form.add(capacityField);
        form.add(new JLabel("Semester:"));
        form.add(semesterField);
        form.add(new JLabel("Year:"));
        form.add(yearField);

        int res = JOptionPane.showConfirmDialog(
                this,
                form,
                "Add Section for " + selectedCourse.courseId,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String instr = instrField.getText().trim();
        String dayTime = dayTimeField.getText().trim();
        String room = roomField.getText().trim();
        String capStr = capacityField.getText().trim();
        String sem = semesterField.getText().trim();
        String yearStr = yearField.getText().trim();

        if (dayTime.isEmpty() || room.isEmpty() || capStr.isEmpty() || sem.isEmpty() || yearStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields are required.",
                    "Missing Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cap;
        int yr;
        try {
            cap = Integer.parseInt(capStr);
            yr = Integer.parseInt(yearStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Capacity and Year must be integers.",
                    "Invalid Data",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = """
                INSERT INTO sections
                    (course_id, instructor_id, day_time, room, capacity, semester, year)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, selectedCourse.originalCourseId);
            ps.setString(2, instr.isEmpty() ? null : instr);
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, cap);
            ps.setString(6, sem);
            ps.setInt(7, yr);

            ps.executeUpdate();
            loadSectionsForCourse(selectedCourse);

        } catch (SQLException ex) {
            showError("Failed to add section", ex);
        }
    }

    private void handleDeleteSection() {
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a section first.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SectionRecord sr = selectedSection;

        int enrollmentCount = 0;
        String countSql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(countSql)) {

            ps.setInt(1, sr.sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    enrollmentCount = rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            showError("Failed to check enrollments for section", ex);
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Delete section ").append(sr.sectionId).append("?\n");
        if (enrollmentCount > 0) {
            message.append("Enrolled students: ").append(enrollmentCount)
                    .append("\nAll related enrollments will be deleted.");
        }

        int res = JOptionPane.showConfirmDialog(
                this,
                message.toString(),
                "Confirm Delete Section",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String deleteEnrollments = "DELETE FROM enrollments WHERE section_id = ?";
        String deleteSection = "DELETE FROM sections WHERE section_id = ?";

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(deleteEnrollments)) {
                    ps.setInt(1, sr.sectionId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(deleteSection)) {
                    ps.setInt(1, sr.sectionId);
                    ps.executeUpdate();
                }

                conn.commit();
                loadSectionsForCourse(selectedCourse);

            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException ex) {
            showError("Failed to delete section", ex);
        }
    }

    private void handleAddStudent() {
        if (selectedSection == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a section first.",
                    "No Section Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String input = JOptionPane.showInputDialog(
                this,
                "Enter Student ID or roll number to add to section " + selectedSection.sectionId + ":",
                "Add Student",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) {
            return;
        }
        String val = input.trim();
        if (val.isEmpty()) {
            return;
        }

        String studentId = null;
        String lookupSql = "SELECT student_id FROM students WHERE student_id = ? OR roll_no = ?";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(lookupSql)) {

            ps.setString(1, val);
            ps.setString(2, val);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    studentId = rs.getString("student_id");
                }
            }

        } catch (SQLException ex) {
            showError("Failed to look up student", ex);
            return;
        }

        if (studentId == null) {
            JOptionPane.showMessageDialog(this,
                    "No student found for value: " + val,
                    "Student Not Found",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String insertSql = """
                INSERT INTO enrollments
                    (student_id, section_id, status)
                VALUES
                    (?, ?, 'REGISTERED')
                """;

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(insertSql)) {

            ps.setString(1, studentId);
            ps.setInt(2, selectedSection.sectionId);
            ps.executeUpdate();

            loadStudentsForSection(selectedSection);

        } catch (SQLException ex) {
            showError("Failed to enroll student in section", ex);
        }
    }

    private void handleRemoveStudent() {
        if (studentsTable == null) {
            return;
        }
        int row = studentsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a student row to remove.",
                    "No Student Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedSection == null) {
            return;
        }

        int modelRow = studentsTable.convertRowIndexToModel(row);
        StudentRow sr = students.get(modelRow);

        int res = JOptionPane.showConfirmDialog(
                this,
                "Remove " + sr.name + " from section " + selectedSection.sectionId + "?",
                "Confirm Remove Student",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.OK_OPTION) {
            return;
        }

        String sql = "DELETE FROM enrollments WHERE enrollment_id = ?";

        try (Connection conn = getConn();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sr.enrollmentId);
            ps.executeUpdate();
            loadStudentsForSection(selectedSection);

        } catch (SQLException ex) {
            showError("Failed to remove student from section", ex);
        }
    }

    private void handleSaveChanges() {
        boolean anyDirtySections = (dirtySectionIds != null && !dirtySectionIds.isEmpty());

        if (!courseDirty && !anyDirtySections) {
            JOptionPane.showMessageDialog(this,
                    "No changes to save.",
                    "Nothing to Save",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (selectedCourse == null) {
            JOptionPane.showMessageDialog(this,
                    "Select a course first.",
                    "No Course Selected",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pull current values from fields
        selectedCourse.courseId = courseIdField.getText().trim();
        selectedCourse.code = acronymField.getText().trim();
        selectedCourse.title = titleField.getText().trim();
        String crdStr = creditsField.getText().trim();
        int newCredits;
        try {
            newCredits = Integer.parseInt(crdStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Credits must be an integer.",
                    "Invalid Credits",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        selectedCourse.credits = newCredits;

        try (Connection conn = getConn()) {
            conn.setAutoCommit(false);
            try {
                // 1) Course update
                if (courseDirty) {
                    String oldId = selectedCourse.originalCourseId;
                    String newId = selectedCourse.courseId;

                    if (!newId.equals(oldId)) {
                        String updSections = "UPDATE sections SET course_id = ? WHERE course_id = ?";
                        try (PreparedStatement ps = conn.prepareStatement(updSections)) {
                            ps.setString(1, newId);
                            ps.setString(2, oldId);
                            ps.executeUpdate();
                        }
                    }

                    String updCourse = """
                            UPDATE courses
                               SET course_id = ?, code = ?, title = ?, credits = ?
                             WHERE course_id = ?
                            """;
                    try (PreparedStatement ps = conn.prepareStatement(updCourse)) {
                        ps.setString(1, selectedCourse.courseId);
                        ps.setString(2, selectedCourse.code);
                        ps.setString(3, selectedCourse.title);
                        ps.setInt(4, selectedCourse.credits);
                        ps.setString(5, selectedCourse.originalCourseId);
                        ps.executeUpdate();
                    }

                    selectedCourse.originalCourseId = selectedCourse.courseId;
                    courseDirty = false;
                }

                // 2) Section updates
                if (anyDirtySections) {
                    String sql = """
                            UPDATE sections
                               SET instructor_id = ?, day_time = ?, room = ?,
                                   capacity = ?, semester = ?, year = ?
                             WHERE section_id = ?
                            """;

                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        for (SectionRecord sr : sections) {
                            if (dirtySectionIds == null || !dirtySectionIds.contains(sr.sectionId)) {
                                continue;
                            }

                            int cap = Math.max(sr.capacity, 0);
                            int year = sr.year;

                            ps.setString(1, (sr.instructorId == null || sr.instructorId.isBlank())
                                    ? null
                                    : sr.instructorId);
                            ps.setString(2, sr.dayTime);
                            ps.setString(3, sr.room);
                            ps.setInt(4, cap);
                            ps.setString(5, sr.semester);
                            ps.setInt(6, year);
                            ps.setInt(7, sr.sectionId);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }

                    dirtySectionIds.clear();
                }

                conn.commit();
                JOptionPane.showMessageDialog(this,
                        "Changes saved successfully.",
                        "Saved",
                        JOptionPane.INFORMATION_MESSAGE);

                loadCourses();

            } catch (SQLException ex) {
                conn.rollback();
                showError("Failed to save changes", ex);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            showError("Failed to save changes", ex);
        }
    }

    // ------------------------------------------------------------------------
    // UI helpers
    // ------------------------------------------------------------------------

    private void clearCourseEditor() {
        if (courseIdField != null)
            courseIdField.setText("");
        if (acronymField != null)
            acronymField.setText("");
        if (titleField != null)
            titleField.setText("");
        if (creditsField != null)
            creditsField.setText("");
        courseDirty = false;
    }

    private void populateCourseEditor(CourseRecord cr) {
        if (cr == null) {
            clearCourseEditor();
            return;
        }
        courseIdField.setText(cr.courseId);
        acronymField.setText(cr.code);
        titleField.setText(cr.title);
        creditsField.setText(String.valueOf(cr.credits));
        courseDirty = false;
    }

    private void updateButtonsEnabled() {
        boolean hasCourse = selectedCourse != null;
        boolean hasSection = selectedSection != null;
        boolean anyDirtySections = dirtySectionIds != null && !dirtySectionIds.isEmpty();

        if (deleteCourseBtn != null)
            deleteCourseBtn.setEnabled(hasCourse);
        if (addSectionBtn != null)
            addSectionBtn.setEnabled(hasCourse);
        if (deleteSectionBtn != null)
            deleteSectionBtn.setEnabled(hasSection);
        if (addStudentBtn != null)
            addStudentBtn.setEnabled(hasSection);
        if (removeStudentBtn != null) {
            boolean hasSelection = hasSection && studentsTable != null && studentsTable.getSelectedRow() >= 0;
            removeStudentBtn.setEnabled(hasSelection);
        }
        if (saveChangesBtn != null) {
            saveChangesBtn.setEnabled(courseDirty || anyDirtySections);
        }
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(226, 232, 240));
        table.setRowHeight(28);

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(FontKit.semibold(12f));
        header.setBackground(new Color(248, 250, 252));
        header.setForeground(new Color(71, 85, 105));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        table.setDefaultRenderer(Object.class, centerRenderer);
    }

    private void showError(String message, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                message + ":\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ------------------------------------------------------------------------
    // Inner classes: cards and table model
    // ------------------------------------------------------------------------

    private class CourseCard extends RoundedPanel {
        final CourseRecord record;
        boolean selected = false;

        CourseCard(CourseRecord record) {
            super(18);
            this.record = record;
            setLayout(new BorderLayout(8, 4));
            setOpaque(false);
            setBorder(new EmptyBorder(12, 14, 12, 14));
            setBackground(CARD);

            int h = 96;
            setPreferredSize(new Dimension(0, h));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, h));
            setMinimumSize(new Dimension(0, h));

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

            String codeLine = record.code != null && !record.code.isBlank()
                    ? record.code + " • " + record.courseId
                    : record.courseId;

            JLabel code = new JLabel(codeLine);
            code.setFont(FontKit.semibold(14f));
            code.setForeground(TEXT_900);

            JLabel title = new JLabel(record.title != null ? record.title : "");
            title.setFont(FontKit.regular(13f));
            title.setForeground(TEXT_600);

            JLabel credits = new JLabel(record.credits + " credits");
            credits.setFont(FontKit.regular(12f));
            credits.setForeground(TEXT_400);

            text.add(code);
            text.add(Box.createVerticalStrut(2));
            text.add(title);
            text.add(Box.createVerticalStrut(4));
            text.add(credits);

            add(text, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSelectedCard(CourseCard.this);
                    onCourseSelected(record);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD_HOVER);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD);
                        repaint();
                    }
                }
            });
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(CARD_HOVER);
                setBorder(new EmptyBorder(11, 13, 11, 13));
            } else {
                setBackground(CARD);
                setBorder(new EmptyBorder(12, 14, 12, 14));
            }
            repaint();
        }
    }

    private void setSelectedCard(CourseCard card) {
        if (selectedCourseCard == card)
            return;
        if (selectedCourseCard != null) {
            selectedCourseCard.setSelected(false);
        }
        selectedCourseCard = card;
        if (selectedCourseCard != null) {
            selectedCourseCard.setSelected(true);
        }
    }

    private void onCourseSelected(CourseRecord cr) {
        selectedCourse = cr;
        populateCourseEditor(cr);
        loadSectionsForCourse(cr);
        updateButtonsEnabled();
    }

    private class SectionCard extends RoundedPanel {
        final SectionRecord record;
        boolean selected = false;

        JTextField instructorField;
        JTextField dayTimeField;
        JTextField roomField;
        JTextField capacityField;
        JTextField semesterField;
        JTextField yearField;

        SectionCard(SectionRecord record) {
            super(18);
            this.record = record;
            setBackground(CARD);
            setBorder(new EmptyBorder(14, 16, 14, 16));
            setLayout(new BorderLayout(10, 4));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel badge = new JPanel();
            badge.setPreferredSize(new Dimension(6, 1));
            badge.setMaximumSize(new Dimension(6, Integer.MAX_VALUE));
            badge.setBackground(TEAL);
            add(badge, BorderLayout.WEST);

            JPanel center = new JPanel();
            center.setOpaque(false);
            center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

            JLabel topLine = new JLabel("Section " + record.sectionId);
            topLine.setFont(FontKit.semibold(14f));
            topLine.setForeground(TEXT_900);
            center.add(topLine);

            JPanel instrRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            instrRow.setOpaque(false);
            JLabel instrLabel = new JLabel("Instructor ID:");
            instrLabel.setFont(FontKit.regular(12f));
            instrLabel.setForeground(TEXT_600);

            instructorField = new JTextField(
                    record.instructorId != null ? record.instructorId : "", 10);
            instructorField.setFont(FontKit.regular(12f));

            instrRow.add(instrLabel);
            instrRow.add(instructorField);

            String instrName = record.instructorName != null ? record.instructorName : "";
            if (!instrName.isBlank()) {
                JLabel nameLabel = new JLabel(" (" + instrName + ")");
                nameLabel.setFont(FontKit.regular(12f));
                nameLabel.setForeground(TEXT_400);
                instrRow.add(nameLabel);
            }

            center.add(Box.createVerticalStrut(2));
            center.add(instrRow);

            JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            timeRow.setOpaque(false);

            JLabel timeLbl = new JLabel("Time:");
            timeLbl.setFont(FontKit.regular(12f));
            timeLbl.setForeground(TEXT_600);
            dayTimeField = new JTextField(
                    record.dayTime != null ? record.dayTime : "", 12);
            dayTimeField.setFont(FontKit.regular(12f));

            JLabel roomLbl = new JLabel("Room:");
            roomLbl.setFont(FontKit.regular(12f));
            roomLbl.setForeground(TEXT_600);
            roomField = new JTextField(
                    record.room != null ? record.room : "", 8);
            roomField.setFont(FontKit.regular(12f));

            timeRow.add(timeLbl);
            timeRow.add(dayTimeField);
            timeRow.add(Box.createHorizontalStrut(8));
            timeRow.add(roomLbl);
            timeRow.add(roomField);

            center.add(Box.createVerticalStrut(2));
            center.add(timeRow);

            JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            bottomRow.setOpaque(false);

            JLabel capLbl = new JLabel("Capacity:");
            capLbl.setFont(FontKit.regular(12f));
            capLbl.setForeground(TEXT_600);
            capacityField = new JTextField(String.valueOf(record.capacity), 4);
            capacityField.setFont(FontKit.regular(12f));

            JLabel semLbl = new JLabel("Semester:");
            semLbl.setFont(FontKit.regular(12f));
            semLbl.setForeground(TEXT_600);
            semesterField = new JTextField(
                    record.semester != null ? record.semester : "", 10);
            semesterField.setFont(FontKit.regular(12f));

            JLabel yearLbl = new JLabel("Year:");
            yearLbl.setFont(FontKit.regular(12f));
            yearLbl.setForeground(TEXT_600);
            yearField = new JTextField(
                    record.year == 0 ? "" : String.valueOf(record.year), 6);
            yearField.setFont(FontKit.regular(12f));

            bottomRow.add(capLbl);
            bottomRow.add(capacityField);
            bottomRow.add(Box.createHorizontalStrut(8));
            bottomRow.add(semLbl);
            bottomRow.add(semesterField);
            bottomRow.add(Box.createHorizontalStrut(8));
            bottomRow.add(yearLbl);
            bottomRow.add(yearField);

            center.add(Box.createVerticalStrut(2));
            center.add(bottomRow);

            add(center, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSelectedSectionCard(SectionCard.this);
                    onSectionSelected(record);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD_HOVER);
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBackground(CARD);
                        repaint();
                    }
                }
            });

            SimpleDocumentListener dl = new SimpleDocumentListener(() -> {
                record.instructorId = instructorField.getText().trim();
                record.dayTime = dayTimeField.getText().trim();
                record.room = roomField.getText().trim();
                try {
                    record.capacity = Integer.parseInt(capacityField.getText().trim());
                } catch (NumberFormatException ex) {
                    record.capacity = 0;
                }
                record.semester = semesterField.getText().trim();
                try {
                    record.year = Integer.parseInt(yearField.getText().trim());
                } catch (NumberFormatException ex) {
                    record.year = 0;
                }
                if (dirtySectionIds == null) {
                    dirtySectionIds = new HashSet<>();
                }
                dirtySectionIds.add(record.sectionId);
                updateButtonsEnabled();
            });

            instructorField.getDocument().addDocumentListener(dl);
            dayTimeField.getDocument().addDocumentListener(dl);
            roomField.getDocument().addDocumentListener(dl);
            capacityField.getDocument().addDocumentListener(dl);
            semesterField.getDocument().addDocumentListener(dl);
            yearField.getDocument().addDocumentListener(dl);
        }

        void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(CARD_HOVER);
            } else {
                setBackground(CARD);
            }
            repaint();
        }
    }

    private void setSelectedSectionCard(SectionCard card) {
        if (selectedSectionCard == card)
            return;
        if (selectedSectionCard != null) {
            selectedSectionCard.setSelected(false);
        }
        selectedSectionCard = card;
        if (selectedSectionCard != null) {
            selectedSectionCard.setSelected(true);
        }
    }

    private void onSectionSelected(SectionRecord sr) {
        selectedSection = sr;
        loadStudentsForSection(sr);
        updateButtonsEnabled();
    }

    private class StudentsTableModel extends AbstractTableModel {

        private final String[] cols = {
                "Student ID", "Roll No", "Name", "Program", "Year", "Final Grade"
        };

        @Override
        public int getRowCount() {
            return students.size();
        }

        @Override
        public int getColumnCount() {
            return cols.length;
        }

        @Override
        public String getColumnName(int column) {
            return cols[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Only final grade editable
            return columnIndex == 5;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StudentRow r = students.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.studentId;
                case 1 -> r.rollNo;
                case 2 -> r.name;
                case 3 -> r.program;
                case 4 -> r.year;
                case 5 -> r.finalGrade;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 5)
                return;
            StudentRow r = students.get(rowIndex);
            String newGrade = (aValue == null) ? "" : aValue.toString().trim();
            r.finalGrade = newGrade;

            String sql = "UPDATE enrollments SET final_grade = ? WHERE enrollment_id = ?";
            try (Connection conn = getConn();
                    PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, newGrade.isEmpty() ? null : newGrade);
                ps.setString(2, r.enrollmentId);
                ps.executeUpdate();

            } catch (SQLException ex) {
                showError("Failed to update final grade", ex);
            }

            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    // ------------------------------------------------------------------------
    // DB helper + utilities
    // ------------------------------------------------------------------------

    private Connection getConn() throws SQLException {
        DatabaseConnection.init();
        DataSource ds = DatabaseConnection.erp();
        if (ds == null) {
            throw new SQLException(
                    "ERP DataSource is not configured. Check erp.jdbcUrl / erp.username / erp.password.");
        }
        return ds.getConnection();
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable onChange;

        SimpleDocumentListener(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            onChange.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            onChange.run();
        }
    }
}
