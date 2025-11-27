package erp.ui.admin;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;


import erp.auth.hash.PasswordHasher;

public class ManageUsersPanel extends JPanel {

    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color CARD = Color.WHITE;

    private final AdminFrameBase parentFrame;
    private final String adminId;
    private final String adminDisplayName;

    private final StudentsTableModel studentsModel = new StudentsTableModel();
    private final InstructorsTableModel instructorsModel = new InstructorsTableModel();

    private final JPanel studentsSection = new JPanel(new BorderLayout());
    private final JPanel instructorsSection = new JPanel(new BorderLayout());

    // tables + sorting/filtering
    private JTable studentsTable;
    private JTable instructorsTable;
    private TableRowSorter<StudentsTableModel> studentSorter;
    private TableRowSorter<InstructorsTableModel> instructorSorter;
    private JTextField searchField;

    // collapse state
    private boolean studentsCollapsed = false;
    private boolean instructorsCollapsed = false;

    public ManageUsersPanel(AdminFrameBase parentFrame, String adminId, String adminDisplayName) {
        this.parentFrame = parentFrame;
        this.adminId = adminId;
        this.adminDisplayName = adminDisplayName;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        // Load REAL data from DB
        loadFromDatabase();
    }

    // ---------------------------------------------------------------------
    // Header bar
    // ---------------------------------------------------------------------
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("User Management");
        title.setFont(FontKit.bold(26f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Manage all users in one place. Control access and monitor activity.");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(4));
        titleBox.add(subtitle);

        header.add(titleBox, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        RoundedButton addStudentBtn = new RoundedButton("Add Student");
        RoundedButton addInstructorBtn = new RoundedButton("Add Instructor");
        RoundedButton saveBtn = new RoundedButton("Save Changes");
        RoundedButton exportBtn = new RoundedButton("Export");
        RoundedButton importBtn = new RoundedButton("Import");
        RoundedButton backBtn = new RoundedButton("Back");

        for (JButton b : new JButton[] { addStudentBtn, addInstructorBtn, saveBtn, exportBtn, importBtn, backBtn }) {
            b.setFont(FontKit.semibold(13f));
            b.setFocusPainted(false);
            b.setBorder(new EmptyBorder(8, 16, 8, 16));
        }

        addStudentBtn.setBackground(new Color(22, 163, 74));
        addStudentBtn.setForeground(Color.WHITE);
        addInstructorBtn.setBackground(new Color(59, 130, 246));
        addInstructorBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(8, 47, 73));
        saveBtn.setForeground(Color.WHITE);
        exportBtn.setBackground(new Color(148, 163, 184));
        exportBtn.setForeground(Color.WHITE);
        importBtn.setBackground(new Color(148, 163, 184));
        importBtn.setForeground(Color.WHITE);
        backBtn.setBackground(new Color(148, 163, 184));
        backBtn.setForeground(Color.WHITE);

        addStudentBtn.addActionListener(this::onAddStudent);
        addInstructorBtn.addActionListener(this::onAddInstructor);
        saveBtn.addActionListener(e -> onSaveChanges());
        exportBtn.addActionListener(e -> onExportCsv());
        importBtn.addActionListener(e -> onImportCsv());

        backBtn.addActionListener(e -> {
            new AdminDashboard(adminId, adminDisplayName).setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        right.add(addStudentBtn);
        right.add(addInstructorBtn);
        right.add(saveBtn);
        right.add(exportBtn);
        right.add(importBtn);
        right.add(backBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    // ---------------------------------------------------------------------
    // Body: search + tables
    // ---------------------------------------------------------------------
    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BorderLayout(0, 16));

        // Search bar row
        body.add(buildSearchRow(), BorderLayout.NORTH);

        // Students section
        studentsSection.setOpaque(false);
        studentsSection.setBorder(new EmptyBorder(8, 0, 8, 0));
        studentsSection.add(buildStudentsCard(), BorderLayout.CENTER);

        // Instructors section
        instructorsSection.setOpaque(false);
        instructorsSection.setBorder(new EmptyBorder(8, 0, 8, 0));
        instructorsSection.add(buildInstructorsCard(), BorderLayout.CENTER);

        JPanel sections = new JPanel();
        sections.setOpaque(false);
        sections.setLayout(new BoxLayout(sections, BoxLayout.Y_AXIS));
        sections.add(studentsSection);
        sections.add(Box.createVerticalStrut(12));
        sections.add(instructorsSection);

        JScrollPane scroll = new JScrollPane(sections);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        body.add(scroll, BorderLayout.CENTER);
        return body;
    }

    private JComponent buildSearchRow() {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(0, 0, 12, 0));

        // Wrapper that looks like a normal text field
        RoundedPanel inputWrapper = new RoundedPanel(12); // small radius, not a circle
        inputWrapper.setBackground(Color.WHITE);
        inputWrapper.setLayout(new BorderLayout());
        inputWrapper.setBorder(new EmptyBorder(4, 10, 4, 10));
        inputWrapper.setPreferredSize(new Dimension(320, 32)); // normal search box size

        JLabel searchIcon = new JLabel("\uD83D\uDD0D");
        searchIcon.setForeground(TEXT_600);
        searchIcon.setBorder(new EmptyBorder(0, 0, 0, 6));

        searchField = new JTextField();
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setFont(FontKit.regular(14f));
        searchField.setForeground(TEXT_900);
        searchField.setCaretColor(TEXT_900);
        searchField.putClientProperty("JTextField.placeholderText",
                "Search by name, username, roll no, program…");

        inputWrapper.add(searchIcon, BorderLayout.WEST);
        inputWrapper.add(searchField, BorderLayout.CENTER);

        // Put it on the left; it will look like a normal text box
        row.add(inputWrapper, BorderLayout.WEST);

        // Wire up filter logic
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateFilters();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateFilters();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateFilters();
            }
        });

        return row;
    }

    private void updateFilters() {
        String text = searchField != null ? searchField.getText().trim() : "";
        RowFilter<Object, Object> rf = null;

        if (!text.isEmpty()) {
            try {
                rf = RowFilter.regexFilter("(?i)" + Pattern.quote(text));
            } catch (Exception ignored) {
            }
        }

        if (studentSorter != null) {
            studentSorter.setRowFilter(rf);
        }
        if (instructorSorter != null) {
            instructorSorter.setRowFilter(rf);
        }
    }

    private JComponent buildStudentsCard() {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(12, 16, 16, 16));
        card.setLayout(new BorderLayout(0, 8));

        // Header (clickable to collapse)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel title = new JLabel("Students");
        title.setFont(FontKit.semibold(15f));
        title.setForeground(TEXT_900);

        JLabel chevron = new JLabel("▾");
        chevron.setFont(FontKit.bold(16f));
        chevron.setForeground(TEXT_600);

        header.add(title, BorderLayout.WEST);
        header.add(chevron, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // Table
        studentsTable = new JTable(studentsModel);
        studentsTable.setFillsViewportHeight(true);
        studentsTable.setRowHeight(30);
        studentsTable.setShowGrid(true);
        studentsTable.setGridColor(new Color(241, 245, 249));
        studentsTable.setFont(FontKit.regular(13f));
        studentsTable.getTableHeader().setFont(FontKit.semibold(13f));

        studentSorter = new TableRowSorter<>(studentsModel);
        studentsTable.setRowSorter(studentSorter);

        JScrollPane sc = new JScrollPane(studentsTable);
        sc.setBorder(BorderFactory.createLineBorder(BORDER));

        final JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(sc, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        // Collapse behaviour
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                studentsCollapsed = !studentsCollapsed;
                content.setVisible(!studentsCollapsed);
                chevron.setText(studentsCollapsed ? "▸" : "▾");
                card.revalidate();
                card.repaint();
            }
        });

        return card;
    }

    private JComponent buildInstructorsCard() {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(12, 16, 16, 16));
        card.setLayout(new BorderLayout(0, 8));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 4, 0));

        JLabel title = new JLabel("Instructors");
        title.setFont(FontKit.semibold(15f));
        title.setForeground(TEXT_900);

        JLabel chevron = new JLabel("▾");
        chevron.setFont(FontKit.bold(16f));
        chevron.setForeground(TEXT_600);

        header.add(title, BorderLayout.WEST);
        header.add(chevron, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        instructorsTable = new JTable(instructorsModel);
        instructorsTable.setFillsViewportHeight(true);
        instructorsTable.setRowHeight(30);
        instructorsTable.setShowGrid(true);
        instructorsTable.setGridColor(new Color(241, 245, 249));
        instructorsTable.setFont(FontKit.regular(13f));
        instructorsTable.getTableHeader().setFont(FontKit.semibold(13f));

        instructorSorter = new TableRowSorter<>(instructorsModel);
        instructorsTable.setRowSorter(instructorSorter);

        JScrollPane sc = new JScrollPane(instructorsTable);
        sc.setBorder(BorderFactory.createLineBorder(BORDER));

        final JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(sc, BorderLayout.CENTER);

        card.add(content, BorderLayout.CENTER);

        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        header.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                instructorsCollapsed = !instructorsCollapsed;
                content.setVisible(!instructorsCollapsed);
                chevron.setText(instructorsCollapsed ? "▸" : "▾");
                card.revalidate();
                card.repaint();
            }
        });

        return card;
    }

    // ---------------------------------------------------------------------
    // DB LOADING
    // ---------------------------------------------------------------------

    private void loadFromDatabase() {
        studentsModel.clear();
        instructorsModel.clear();

        loadStudentsFromDb();
        loadInstructorsFromDb();

        studentsModel.fireTableDataChanged();
        instructorsModel.fireTableDataChanged();

        updateFilters(); // apply filter after reload
    }

    private void loadStudentsFromDb() {
        String sql = """
                SELECT user_id, username, role, status, last_login
                FROM users_auth
                WHERE role = 'student'
                ORDER BY user_id
                """;

        studentsModel.clear();

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                StudentsTableModel.Row row = new StudentsTableModel.Row();
                row.state = RowState.CLEAN;
                row.userId = rs.getString("user_id");
                row.username = rs.getString("username");
                row.role = rs.getString("role");
                row.status = rs.getString("status");

                Timestamp ts = rs.getTimestamp("last_login");
                row.lastLogin = (ts != null) ? ts.toString() : "-";

                populateStudentDetails(row);

                studentsModel.addLoadedRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load students from database.\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateStudentDetails(StudentsTableModel.Row row) {
        String sql = """
                SELECT full_name, roll_no, program, year
                FROM students
                WHERE student_id = ?
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(sql)) {

            // student_id = username
            ps.setString(1, row.username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    row.fullName = rs.getString("full_name");
                    row.rollNo = rs.getString("roll_no");
                    row.program = rs.getString("program");
                    row.year = rs.getString("year");
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] No student ERP row for username/student_id="
                    + row.username + ": " + e.getMessage());
        }
    }

    private void loadInstructorsFromDb() {
        String sql = """
                SELECT user_id, username, role, status, last_login
                FROM users_auth
                WHERE role = 'instructor'
                ORDER BY user_id
                """;

        instructorsModel.clear();

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                InstructorsTableModel.Row row = new InstructorsTableModel.Row();
                row.state = RowState.CLEAN;
                row.userId = rs.getString("user_id");
                row.username = rs.getString("username");
                row.role = rs.getString("role");
                row.status = rs.getString("status");

                Timestamp ts = rs.getTimestamp("last_login");
                row.lastLogin = (ts != null) ? ts.toString() : "-";

                populateInstructorDetails(row);

                instructorsModel.addLoadedRow(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load instructors from database.\n" + e.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateInstructorDetails(InstructorsTableModel.Row row) {
        String sql = """
                SELECT instructor_name, department
                FROM instructors
                WHERE instructor_id = ?
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(sql)) {

            ps.setString(1, row.userId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    row.fullName = rs.getString("instructor_name");
                    row.department = rs.getString("department");
                }
            }
        } catch (SQLException e) {
            System.err.println("[WARN] No instructor ERP row for user_id="
                    + row.userId + ": " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // Actions
    // ---------------------------------------------------------------------

    private void onAddStudent(ActionEvent e) {
        AddStudentDialog dlg = new AddStudentDialog();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!dlg.approved)
            return;

        StudentsTableModel.Row r = new StudentsTableModel.Row();
        r.state = RowState.NEW;
        r.userId = ""; // DB will assign
        r.username = dlg.username;
        r.role = "STUDENT";
        r.fullName = dlg.fullName;
        r.rollNo = dlg.rollNo;
        r.program = dlg.program;
        r.year = dlg.year;
        r.status = "ACTIVE";
        r.lastLogin = "-";
        r.delete = false;
        r.password = dlg.password;

        studentsModel.addNewRow(r);
    }

    private void onAddInstructor(ActionEvent e) {
        AddInstructorDialog dlg = new AddInstructorDialog();
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);

        if (!dlg.approved)
            return;

        InstructorsTableModel.Row r = new InstructorsTableModel.Row();
        r.state = RowState.NEW;
        r.userId = "";
        r.username = dlg.username;
        r.role = "INSTRUCTOR";
        r.fullName = dlg.fullName;
        r.department = dlg.department;
        r.status = "ACTIVE";
        r.lastLogin = "-";
        r.delete = false;
        r.password = dlg.password;

        instructorsModel.addNewRow(r);
    }

    private void onSaveChanges() {
        int newStudents = studentsModel.countByState(RowState.NEW);
        int newInstructors = instructorsModel.countByState(RowState.NEW);
        int delStudents = studentsModel.countMarkedForDelete();
        int delInstructors = instructorsModel.countMarkedForDelete();

        StringBuilder sb = new StringBuilder();
        sb.append("<html><b>Summary of pending changes</b><br><br>");
        sb.append("New students: ").append(newStudents).append("<br>");
        sb.append("New instructors: ").append(newInstructors).append("<br>");
        sb.append("Marked for delete (students): ").append(delStudents).append("<br>");
        sb.append("Marked for delete (instructors): ").append(delInstructors).append("<br><br>");
        sb.append("Do you want to apply these changes to the database now?<br>");
        sb.append("</html>");

        int choice = JOptionPane.showConfirmDialog(
                this,
                sb.toString(),
                "Save Changes",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            applyChangesToDatabase();
            JOptionPane.showMessageDialog(
                    this,
                    "Changes saved successfully.",
                    "Save Changes",
                    JOptionPane.INFORMATION_MESSAGE);
            loadFromDatabase();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save changes:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

        // ---------------------------------------------------------------------
    // CSV Export / Import
    // ---------------------------------------------------------------------

    private void onExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export users to CSV");
        chooser.setSelectedFile(new File("users_export.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            // Header
            bw.write("type,username,full_name,roll_no,program,year,department,status,password");
            bw.newLine();

            // Students
            for (StudentsTableModel.Row r : studentsModel.getRows()) {
                bw.write(String.join(",",
                        "student",
                        escapeCsv(r.username),
                        escapeCsv(r.fullName),
                        escapeCsv(r.rollNo),
                        escapeCsv(r.program),
                        escapeCsv(r.year),
                        "", // department (not used)
                        escapeCsv(r.status),
                        ""  // password left empty; fill if you want on re-import
                ));
                bw.newLine();
            }

            // Instructors
            for (InstructorsTableModel.Row r : instructorsModel.getRows()) {
                bw.write(String.join(",",
                        "instructor",
                        escapeCsv(r.username),
                        escapeCsv(r.fullName),
                        "", "", "",                     // roll_no, program, year not used
                        escapeCsv(r.department),
                        escapeCsv(r.status),
                        ""  // password
                ));
                bw.newLine();
            }

            JOptionPane.showMessageDialog(this,
                    "Exported users to:\n" + file.getAbsolutePath(),
                    "Export CSV",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onImportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import users from CSV");

        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();

        int addedStudents = 0;
        int addedInstructors = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { // skip header
                    first = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = parseCsvLine(line);
                if (parts.length < 3) continue;

                String type = parts[0].trim().toLowerCase(Locale.ROOT);

                if ("student".equals(type)) {
                    StudentsTableModel.Row r = new StudentsTableModel.Row();
                    r.state = RowState.NEW;
                    r.userId = "";
                    r.username   = safeGet(parts, 1);
                    r.fullName   = safeGet(parts, 2);
                    r.rollNo     = safeGet(parts, 3);
                    r.program    = safeGet(parts, 4);
                    r.year       = safeGet(parts, 5);
                    r.status     = safeGet(parts, 7).isBlank() ? "active" : safeGet(parts, 7);
                    r.password   = safeGet(parts, 8); // plain password; will be hashed on save
                    r.role       = "STUDENT";
                    r.lastLogin  = "-";
                    r.delete     = false;

                    studentsModel.addNewRow(r);
                    addedStudents++;

                } else if ("instructor".equals(type)) {
                    InstructorsTableModel.Row r = new InstructorsTableModel.Row();
                    r.state = RowState.NEW;
                    r.userId = "";
                    r.username   = safeGet(parts, 1);
                    r.fullName   = safeGet(parts, 2);
                    r.department = safeGet(parts, 6);
                    r.status     = safeGet(parts, 7).isBlank() ? "active" : safeGet(parts, 7);
                    r.password   = safeGet(parts, 8); // plain password
                    r.role       = "INSTRUCTOR";
                    r.lastLogin  = "-";
                    r.delete     = false;

                    instructorsModel.addNewRow(r);
                    addedInstructors++;
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Imported " + addedStudents + " students and "
                            + addedInstructors + " instructors.\n"
                            + "Click \"Save Changes\" to write them to the database.",
                    "Import CSV",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to import CSV:\n" + ex.getMessage(),
                    "Import Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // small helpers ------------------------------------------------------

    private String escapeCsv(String s) {
        if (s == null) return "";
        boolean needQuotes = s.contains(",") || s.contains("\"") || s.contains("\n");
        String escaped = s.replace("\"", "\"\"");
        return needQuotes ? "\"" + escaped + "\"" : escaped;
    }

    private String[] parseCsvLine(String line) {
        // very small CSV parser handling quotes
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else {
                if (ch == '"') {
                    inQuotes = true;
                } else if (ch == ',') {
                    cols.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(ch);
                }
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }

    private String safeGet(String[] arr, int idx) {
        return (idx >= 0 && idx < arr.length) ? arr[idx] : "";
    }


    private void applyChangesToDatabase() throws SQLException {

        // --- Students ---
        for (StudentsTableModel.Row r : studentsModel.getRows()) {
            if (r.delete && r.userId != null && !r.userId.isBlank()) {
                deleteStudentUser(r);
            } else if (r.state == RowState.NEW && !r.delete) {
                insertNewStudentUser(r);
            } else if (r.state == RowState.MODIFIED && !r.delete) {
                updateStudentUser(r);
            }
        }

        // --- Instructors ---
        for (InstructorsTableModel.Row r : instructorsModel.getRows()) {
            if (r.delete && r.userId != null && !r.userId.isBlank()) {
                deleteInstructorUser(r);
            } else if (r.state == RowState.NEW && !r.delete) {
                insertNewInstructorUser(r);
            } else if (r.state == RowState.MODIFIED && !r.delete) {
                updateInstructorUser(r);
            }
        }
    }

    private void deleteStudentUser(StudentsTableModel.Row r) throws SQLException {
        // 1) Delete from students by student_id = username
        String erpSql = "DELETE FROM students WHERE student_id = ?";

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {
            ps.setString(1, r.username);
            ps.executeUpdate();
        }

        // 2) Delete from users_auth by user_id
        String authSql = "DELETE FROM users_auth WHERE user_id = ?";

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(authSql)) {
            ps.setString(1, r.userId);
            ps.executeUpdate();
        }
    }

    private void updateStudentUser(StudentsTableModel.Row r) throws SQLException {
        // Update users_auth (username + status)
        String authSql = """
                UPDATE users_auth
                SET username = ?, status = ?
                WHERE user_id = ?
                """;

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(authSql)) {

            ps.setString(1, r.username);
            ps.setString(2, (r.status == null || r.status.isBlank()) ? "active" : r.status);
            ps.setString(3, r.userId);
            ps.executeUpdate();
        }

        // Update students row
        String erpSql = """
                UPDATE students
                SET student_id = ?, roll_no = ?, full_name = ?, program = ?, year = ?
                WHERE student_id = ?
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {

            // if username changed, we move the key too
            ps.setString(1, r.username); // new student_id
            ps.setString(2, r.rollNo);
            ps.setString(3, r.fullName);
            ps.setString(4, r.program);
            ps.setString(5, r.year);
            ps.setString(6, r.username); // old student_id — if you want to support renames,
                                         // store old value separately; else keep as-is.
            ps.executeUpdate();
        }

        r.state = RowState.CLEAN;
    }

    private void deleteInstructorUser(InstructorsTableModel.Row r) throws SQLException {
        String erpSql = "DELETE FROM instructors WHERE instructor_id = ?";

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {
            ps.setString(1, r.userId);
            ps.executeUpdate();
        }

        String authSql = "DELETE FROM users_auth WHERE user_id = ?";

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(authSql)) {
            ps.setString(1, r.userId);
            ps.executeUpdate();
        }
    }

    private void updateInstructorUser(InstructorsTableModel.Row r) throws SQLException {
        // 1) Update users_auth (login side)
        long newId = generateNewUserId();

        String authSql = """
                UPDATE users_auth
                SET username = ?, status = ?
                WHERE user_id = ?
                """;

        try (Connection authConn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = authConn.prepareStatement(authSql)) {

            ps.setString(1, r.username); // e.g. "inst1"
            ps.setString(2, (r.status == null || r.status.isBlank())
                    ? "active" // matches your dump
                    : r.status);
            ps.setString(3, r.userId); // e.g. "1000001"
            ps.executeUpdate();
        }

        r.userId = String.valueOf(newId);

        // 2) Update instructors (ERP side)
        String erpSql = """
                UPDATE instructors
                SET username = ?, full_name = ?, department = ?
                WHERE instructor_id = ?
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {

            ps.setString(1, r.username); // same username as in auth
            ps.setString(2, r.fullName); // display name
            ps.setString(3, r.department); // dept from table
            ps.setString(4, r.userId); // instructor_id = user_id (1000001, etc.)
            ps.executeUpdate();
        }

        r.state = RowState.CLEAN;
    }

    private void insertNewStudentUser(StudentsTableModel.Row r) throws SQLException {

        if (r.username == null || r.username.isBlank()) {
            throw new SQLException("Student username is required.");
        }
        if (r.password == null || r.password.isBlank()) {
            throw new SQLException("Password is required for new student " + r.username);
        }

        String hashed = PasswordHasher.hash(r.password);

        // ---------------------------------------------------------
        // STEP 1: INSERT INTO users_auth (manual user_id)
        // ---------------------------------------------------------
        long newId = generateNewUserId();

        String authSql = """
                INSERT INTO users_auth
                    (user_id, username, role, password_hash, status, last_login, failed_attempts, locked_until)
                VALUES
                    (?, ?, 'student', ?, 'active', NULL, 0, NULL)
                """;

        try (Connection conn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = conn.prepareStatement(authSql)) {

            ps.setLong(1, newId);
            ps.setString(2, r.username);
            ps.setString(3, hashed);
            ps.executeUpdate();
        }

        // Make sure the model row knows the new user_id
        r.userId = String.valueOf(newId);

        // ---------------------------------------------------------
        // STEP 2: INSERT INTO students
        // ---------------------------------------------------------
        String erpSql = """
                INSERT INTO students
                    (student_id, roll_no, full_name, program, year)
                VALUES
                    (?, ?, ?, ?, ?)
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {

            ps.setString(1, r.username); // students.student_id = username
            ps.setString(2, r.rollNo);
            ps.setString(3, r.fullName);
            ps.setString(4, r.program);
            ps.setString(5, r.year);
            ps.executeUpdate();
        }

        r.state = RowState.CLEAN;
        r.password = null;
    }

    private void insertNewInstructorUser(InstructorsTableModel.Row r) throws SQLException {

        if (r.username == null || r.username.isBlank()) {
            throw new SQLException("Instructor username is required.");
        }
        if (r.password == null || r.password.isBlank()) {
            throw new SQLException("Password is required for new instructor " + r.username);
        }

        String hashed = PasswordHasher.hash(r.password);

        // ---------------------------------------------------------
        // STEP 1: GENERATE NEW user_id
        // ---------------------------------------------------------
        long newId = generateNewUserId(); // <-- SAME AS STUDENT

        // ---------------------------------------------------------
        // STEP 2: INSERT INTO users_auth WITH user_id
        // ---------------------------------------------------------
        String authSql = """
                INSERT INTO users_auth
                    (user_id, username, role, password_hash, status, last_login, failed_attempts, locked_until)
                VALUES
                    (?, ?, 'instructor', ?, 'active', NULL, 0, NULL)
                """;

        try (Connection conn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = conn.prepareStatement(authSql)) {

            ps.setLong(1, newId); // user_id
            ps.setString(2, r.username); // username
            ps.setString(3, hashed); // password hash
            ps.executeUpdate();
        }

        r.userId = String.valueOf(newId); // REQUIRED for later updates/deletes

        // ---------------------------------------------------------
        // STEP 3: INSERT INTO instructors TABLE
        // ---------------------------------------------------------
        String erpSql = """
                INSERT INTO instructors
                    (instructor_id, username, full_name, department)
                VALUES
                    (?, ?, ?, ?)
                """;

        try (Connection erpConn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = erpConn.prepareStatement(erpSql)) {

            ps.setLong(1, newId); // instructor_id = user_id
            ps.setString(2, r.username);
            ps.setString(3, r.fullName);
            ps.setString(4, r.department);
            ps.executeUpdate();
        }

        r.state = RowState.CLEAN;
        r.password = null;
    }

    private long generateNewUserId() throws SQLException {
        String sql = "SELECT MAX(user_id) AS max_id FROM users_auth";
        try (Connection conn = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                long max = rs.getLong("max_id");
                return max + 1;
            } else {
                return 1;
            }
        }
    }

    // ---------------------------------------------------------------------
    // Table models + row state
    // ---------------------------------------------------------------------

    private enum RowState {
        CLEAN,
        NEW,
        MODIFIED,
        TO_DELETE
    }

    // ---- Students model ----
    private static class StudentsTableModel extends AbstractTableModel {

        static class Row {
            RowState state = RowState.CLEAN;

            String userId;
            String username;
            String role;
            String fullName;
            String rollNo;
            String program;
            String year;
            String status;
            String lastLogin;
            boolean delete;

            String password; // only used for NEW rows
        }

        private final List<Row> rows = new ArrayList<>();

        private final String[] cols = {
                "User ID",
                "Username",
                "Role",
                "Full Name",
                "Roll No",
                "Program",
                "Year",
                "Status",
                "Last Login",
                "Delete?"
        };

        @Override
        public int getRowCount() {
            return rows.size();
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
            if (columnIndex == 9)
                return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0 || columnIndex == 2 || columnIndex == 8) {
                return false;
            }
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.userId;
                case 1 -> r.username;
                case 2 -> r.role;
                case 3 -> r.fullName;
                case 4 -> r.rollNo;
                case 5 -> r.program;
                case 6 -> r.year;
                case 7 -> r.status;
                case 8 -> r.lastLogin;
                case 9 -> r.delete;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            switch (columnIndex) {
                case 1 -> r.username = (String) aValue;
                case 3 -> r.fullName = (String) aValue;
                case 4 -> r.rollNo = (String) aValue;
                case 5 -> r.program = (String) aValue;
                case 6 -> r.year = (String) aValue;
                case 7 -> r.status = (String) aValue;
                case 9 -> r.delete = (Boolean) aValue;
            }
            if (r.state == RowState.CLEAN) {
                r.state = RowState.MODIFIED;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        void clear() {
            rows.clear();
        }

        void addLoadedRow(Row r) {
            rows.add(r);
        }

        void addNewRow(Row r) {
            rows.add(r);
            int idx = rows.size() - 1;
            fireTableRowsInserted(idx, idx);
        }

        int countByState(RowState state) {
            int c = 0;
            for (Row r : rows) {
                if (r.state == state)
                    c++;
            }
            return c;
        }

        int countMarkedForDelete() {
            int c = 0;
            for (Row r : rows) {
                if (r.delete)
                    c++;
            }
            return c;
        }

        List<Row> getRows() {
            return rows;
        }
    }

    // ---- Instructors model ----
    private static class InstructorsTableModel extends AbstractTableModel {

        static class Row {
            RowState state = RowState.CLEAN;

            String userId;
            String username;
            String role;
            String fullName;
            String department;
            String status;
            String lastLogin;
            boolean delete;

            String password; // NEW rows only
        }

        private final List<Row> rows = new ArrayList<>();

        private final String[] cols = {
                "User ID",
                "Username",
                "Role",
                "Full Name",
                "Department",
                "Status",
                "Last Login",
                "Delete?"
        };

        @Override
        public int getRowCount() {
            return rows.size();
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
            if (columnIndex == 7)
                return Boolean.class;
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex == 0 || columnIndex == 2 || columnIndex == 6) {
                return false;
            }
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.userId;
                case 1 -> r.username;
                case 2 -> r.role;
                case 3 -> r.fullName;
                case 4 -> r.department;
                case 5 -> r.status;
                case 6 -> r.lastLogin;
                case 7 -> r.delete;
                default -> "";
            };
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            Row r = rows.get(rowIndex);
            switch (columnIndex) {
                case 1 -> r.username = (String) aValue;
                case 3 -> r.fullName = (String) aValue;
                case 4 -> r.department = (String) aValue;
                case 5 -> r.status = (String) aValue;
                case 7 -> r.delete = (Boolean) aValue;
            }
            if (r.state == RowState.CLEAN) {
                r.state = RowState.MODIFIED;
            }
            fireTableRowsUpdated(rowIndex, rowIndex);
        }

        void clear() {
            rows.clear();
        }

        void addLoadedRow(Row r) {
            rows.add(r);
        }

        void addNewRow(Row r) {
            rows.add(r);
            int idx = rows.size() - 1;
            fireTableRowsInserted(idx, idx);
        }

        int countByState(RowState state) {
            int c = 0;
            for (Row r : rows) {
                if (r.state == state)
                    c++;
            }
            return c;
        }

        int countMarkedForDelete() {
            int c = 0;
            for (Row r : rows) {
                if (r.delete)
                    c++;
            }
            return c;
        }

        List<Row> getRows() {
            return rows;
        }
    }

    // ---------------------------------------------------------------------
    // Dialogs
    // ---------------------------------------------------------------------

    private static class AddStudentDialog extends JDialog {
        boolean approved = false;

        String fullName;
        String studentId;
        String username;
        String rollNo;
        String program;
        String year;
        String password;

        AddStudentDialog() {
            setModal(true);
            setTitle("Add Student");
            setSize(420, 360);
            setLayout(new BorderLayout());
            setLocationRelativeTo(null);

            JPanel form = new JPanel();
            form.setLayout(new GridBagLayout());
            form.setBorder(new EmptyBorder(16, 16, 16, 16));

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.insets = new Insets(4, 4, 4, 4);
            gc.anchor = GridBagConstraints.WEST;

            JTextField fullNameField = new JTextField(20);
            JTextField studentIdField = new JTextField(20);
            JTextField usernameField = new JTextField(20);
            JTextField rollNoField = new JTextField(20);
            JTextField programField = new JTextField(20);
            JTextField yearField = new JTextField(20);
            JPasswordField passwordField = new JPasswordField(20);

            addRow(form, gc, "Full Name", fullNameField);
            addRow(form, gc, "Student ID", studentIdField);
            addRow(form, gc, "Username", usernameField);
            addRow(form, gc, "Roll No", rollNoField);
            addRow(form, gc, "Program", programField);
            addRow(form, gc, "Year", yearField);
            addRow(form, gc, "Password", passwordField);

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            buttons.add(ok);
            buttons.add(cancel);
            add(buttons, BorderLayout.SOUTH);

            ok.addActionListener(e -> {
                fullName = fullNameField.getText().trim();
                studentId = studentIdField.getText().trim();
                username = usernameField.getText().trim();
                rollNo = rollNoField.getText().trim();
                program = programField.getText().trim();
                year = yearField.getText().trim();
                password = new String(passwordField.getPassword());

                if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Full name, username, and password are required.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                approved = true;
                dispose();
            });

            cancel.addActionListener(e -> dispose());
        }
    }

    private static class AddInstructorDialog extends JDialog {
        boolean approved = false;

        String fullName;
        String instructorId;
        String username;
        String department;
        String password;

        AddInstructorDialog() {
            setModal(true);
            setTitle("Add Instructor");
            setSize(420, 280);
            setLayout(new BorderLayout());
            setLocationRelativeTo(null);

            JPanel form = new JPanel(new GridBagLayout());
            form.setBorder(new EmptyBorder(16, 16, 16, 16));

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.insets = new Insets(4, 4, 4, 4);
            gc.anchor = GridBagConstraints.WEST;

            JTextField fullNameField = new JTextField(20);
            JTextField instrIdField = new JTextField(20);
            JTextField usernameField = new JTextField(20);
            JTextField deptField = new JTextField(20);
            JPasswordField passwordField = new JPasswordField(20);

            addRow(form, gc, "Full Name", fullNameField);
            addRow(form, gc, "Instructor ID", instrIdField);
            addRow(form, gc, "Username", usernameField);
            addRow(form, gc, "Department", deptField);
            addRow(form, gc, "Password", passwordField);

            add(form, BorderLayout.CENTER);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            buttons.add(ok);
            buttons.add(cancel);
            add(buttons, BorderLayout.SOUTH);

            ok.addActionListener(e -> {
                fullName = fullNameField.getText().trim();
                instructorId = instrIdField.getText().trim();
                username = usernameField.getText().trim();
                department = deptField.getText().trim();
                password = new String(passwordField.getPassword());

                if (fullName.isBlank() || username.isBlank() || password.isBlank()) {
                    JOptionPane.showMessageDialog(this,
                            "Full name, username, and password are required.",
                            "Validation",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                approved = true;
                dispose();
            });

            cancel.addActionListener(e -> dispose());
        }
    }

    private static void addRow(JPanel panel, GridBagConstraints gc, String label, JComponent field) {
        gc.gridx = 0;
        gc.weightx = 0;
        panel.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gc);
        gc.gridy++;
    }
}
