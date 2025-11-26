package erp.ui.instructor;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

/**
 * MySections
 *
 * - Shows only sections taught by the current instructor.
 * - Compact card layout.
 * - Clicking a card navigates (in-place) to a "Section Gradebook" view within
 * the same frame using CardLayout.
 * - Gradebook view shows:
 * • Summary stats + charts
 * • Students (rows) x section components (cols) with editable scores,
 * search bar, and CSV import/export.
 */
public class MySections extends InstructorFrameBase {

    // --- Simple local models -------------------------------------------------

    public static class SectionInfo {
        public int sectionID;
        public String courseID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    private static class StudentRow {
        String studentId;
        String enrollId;
        String rollNo;
        String name;
    }

    private static class ComponentInfo {
        int componentId;
        String name;
        int weight;
    }

    // Palette for this screen
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_HOVER = new Color(242, 248, 247);

    // CardLayout keys
    private static final String VIEW_SECTIONS = "sections-list";
    private static final String VIEW_GRADEBOOK = "gradebook";

    private CardLayout cardLayout;
    private JPanel cardPanel;

    // Gradebook state
    private SectionInfo currentSection;
    private final List<StudentRow> gradebookStudents = new ArrayList<>();
    private final List<ComponentInfo> gradebookComponents = new ArrayList<>();
    private final Map<String, String> gradesByKey = new HashMap<>(); // key = enrollId + ":" + componentId
    private final Set<String> dirtyKeys = new HashSet<>(); // keys that were edited

    private GradebookTableModel gradebookModel;
    private JTable gradebookTable;
    private TableRowSorter<GradebookTableModel> sorter;

    public MySections() {
        super(null, null, Page.SECTIONS);
        setTitle("IIITD ERP – My Sections");

        if (metaLabel != null) {
            metaLabel.setText("Department: " + getDepartmentSafe(this.instructorId));
        }
    }

    @Override
    protected JComponent buildMainContent() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(buildSectionsListView(), VIEW_SECTIONS);
        return cardPanel;
    }

    // -------------------------------------------------------------------------
    // Sections list
    // -------------------------------------------------------------------------

    private JComponent buildSectionsListView() {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        // Hero header
        RoundedPanel hero = new RoundedPanel(22);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(22, 26, 22, 26));
        hero.setLayout(new BorderLayout());

        JPanel heroLeft = new JPanel();
        heroLeft.setOpaque(false);
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("📚  My Sections");
        h1.setFont(FontKit.bold(26f));
        h1.setForeground(Color.WHITE);
        heroLeft.add(h1);

        JLabel sub = new JLabel("View and manage only the sections you teach.");
        sub.setFont(FontKit.regular(14f));
        sub.setForeground(new Color(210, 233, 229));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(sub);

        hero.add(heroLeft, BorderLayout.WEST);

        JLabel rightLabel = new JLabel("Logged in as " + userDisplayName);
        rightLabel.setFont(FontKit.regular(13f));
        rightLabel.setForeground(new Color(200, 230, 225));
        hero.add(rightLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // Sections as cards
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 8, 24, 8));

        List<SectionInfo> sections = fetchSectionsForInstructor(this.instructorId);

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            JPanel wrap = new JPanel();
            wrap.setOpaque(false);
            wrap.add(empty);
            content.add(wrap, BorderLayout.CENTER);
        } else {
            JPanel grid = new JPanel();
            grid.setOpaque(false);
            grid.setLayout(new GridLayout(0, 2, 16, 16));

            for (SectionInfo s : sections) {
                grid.add(createSectionCard(s));
            }

            JScrollPane sc = new JScrollPane(grid);
            sc.setBorder(null);
            sc.getVerticalScrollBar().setUnitIncrement(16);
            sc.getViewport().setBackground(BG);
            sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            content.add(sc, BorderLayout.CENTER);
        }

        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JComponent createSectionCard(SectionInfo s) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setLayout(new BorderLayout(8, 4));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel badge = new JPanel();
        badge.setPreferredSize(new Dimension(6, 1));
        badge.setMaximumSize(new Dimension(6, Integer.MAX_VALUE));
        badge.setBackground(TEAL);
        card.add(badge, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel course = new JLabel((s.courseID != null ? s.courseID : "Course"));
        course.setFont(FontKit.semibold(16f));
        course.setForeground(TEXT_900);
        text.add(course);

        String line2 = "Section " + s.sectionID;
        if (s.dayTime != null && !s.dayTime.isBlank()) {
            line2 += " • " + s.dayTime;
        }
        JLabel secLabel = new JLabel(line2);
        secLabel.setFont(FontKit.regular(13f));
        secLabel.setForeground(TEXT_600);
        text.add(Box.createVerticalStrut(2));
        text.add(secLabel);

        String meta = "";
        if (s.room != null && !s.room.isBlank()) {
            meta += "Room " + s.room;
        }
        if (s.semester != null && !s.semester.isBlank()) {
            if (!meta.isEmpty())
                meta += " • ";
            meta += s.semester + " " + (s.year != 0 ? s.year : "");
        }

        if (!meta.isEmpty()) {
            JLabel metaLabel = new JLabel(meta);
            metaLabel.setFont(FontKit.regular(12f));
            metaLabel.setForeground(TEXT_600);
            text.add(Box.createVerticalStrut(2));
            text.add(metaLabel);
        }

        card.add(text, BorderLayout.CENTER);

        if (s.capacity > 0) {
            JLabel cap = new JLabel("Capacity: " + s.capacity);
            cap.setFont(FontKit.regular(12f));
            cap.setForeground(TEXT_600);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottom.setOpaque(false);
            bottom.add(cap);
            card.add(bottom, BorderLayout.SOUTH);
        }

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD);
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                openGradebookView(s);
            }
        });

        return card;
    }

    private List<SectionInfo> fetchSectionsForInstructor(String instrId) {
        List<SectionInfo> list = new ArrayList<>();
        if (instrId == null || instrId.isBlank())
            return list;

        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                "FROM sections " +
                "WHERE instructor_id = ? " +
                "ORDER BY course_id, section_id";

        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instrId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SectionInfo s = new SectionInfo();
                    s.sectionID = rs.getInt("section_id");
                    s.courseID = rs.getString("course_id");
                    s.dayTime = rs.getString("day_time");
                    s.room = rs.getString("room");
                    s.capacity = rs.getInt("capacity");
                    s.semester = rs.getString("semester");
                    s.year = rs.getInt("year");
                    list.add(s);
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // -------------------------------------------------------------------------
    // Gradebook view
    // -------------------------------------------------------------------------

    private void openGradebookView(SectionInfo section) {
        this.currentSection = section;
        loadGradebookData(section.sectionID);

        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(8, 0, 0, 0));

        // Top bar: back + CSV controls
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(new EmptyBorder(0, 0, 12, 0));

        JButton backBtn = new JButton("← Back to My Sections");
        backBtn.setFocusPainted(false);
        backBtn.setBorderPainted(false);
        backBtn.setContentAreaFilled(false);
        backBtn.setFont(FontKit.semibold(13f));
        backBtn.setForeground(TEAL);
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> cardLayout.show(cardPanel, VIEW_SECTIONS));
        topBar.add(backBtn, BorderLayout.WEST);

        JPanel exportImportPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        exportImportPanel.setOpaque(false);

        // Primary SAVE button
        JButton saveBtn = new JButton("Save Changes") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        saveBtn.setFocusPainted(false);
        saveBtn.setBorderPainted(false);
        saveBtn.setContentAreaFilled(false);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(FontKit.semibold(13f));
        saveBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        saveBtn.addActionListener(e -> saveGrades());
        exportImportPanel.add(saveBtn);

        // Export / Import (secondary buttons)
        JButton exportBtn = new JButton("Export CSV");
        styleSecondaryButton(exportBtn);
        exportBtn.addActionListener(e -> exportGradesToCsv());
        exportImportPanel.add(exportBtn);

        JButton importBtn = new JButton("Import CSV");
        styleSecondaryButton(importBtn);
        importBtn.addActionListener(e -> importGradesFromCsv());
        exportImportPanel.add(importBtn);

        topBar.add(exportImportPanel, BorderLayout.EAST);

        root.add(topBar, BorderLayout.NORTH);

        // Center: header + stats + search + table in a card
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.setLayout(new BorderLayout(0, 12));

        // Header area
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        String titleText = (section.courseID != null ? section.courseID : "Course")
                + " — Section " + section.sectionID;
        JLabel title = new JLabel(titleText);
        title.setFont(FontKit.bold(18f));
        title.setForeground(TEXT_900);

        JLabel meta = new JLabel(buildSectionMeta(section));
        meta.setFont(FontKit.regular(13f));
        meta.setForeground(TEXT_600);

        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(meta);

        header.add(titleBox, BorderLayout.WEST);

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(18);
        searchField.putClientProperty("JComponent.roundRect", Boolean.TRUE);
        searchField.setFont(FontKit.regular(13f));
        searchField.setToolTipText("Search by name or roll number");
        searchPanel.add(searchField);

        header.add(searchPanel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // ------------ Stats + Table center area -------------
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        // Stats block (summary + charts)
        JComponent statsPanel = buildStatsPanel();
        center.add(statsPanel, BorderLayout.NORTH);

        // JTable for gradebook
        gradebookModel = new GradebookTableModel();
        gradebookTable = new JTable(gradebookModel);
        gradebookTable.setFillsViewportHeight(true);
        gradebookTable.setRowHeight(26);
        gradebookTable.setFont(FontKit.regular(13f));
        gradebookTable.getTableHeader().setFont(FontKit.semibold(13f));
        gradebookTable.getTableHeader().setReorderingAllowed(false);

        sorter = new TableRowSorter<>(gradebookModel);
        gradebookTable.setRowSorter(sorter);

        // Filter logic
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }

            private void applyFilter() {
                String text = searchField.getText();
                if (text == null || text.isBlank()) {
                    sorter.setRowFilter(null);
                } else {
                    String lc = text.toLowerCase();
                    sorter.setRowFilter(new RowFilter<GradebookTableModel, Integer>() {
                        @Override
                        public boolean include(Entry<? extends GradebookTableModel, ? extends Integer> entry) {
                            GradebookTableModel m = entry.getModel();
                            int row = entry.getIdentifier();
                            String roll = String.valueOf(m.getValueAt(row, 0)).toLowerCase();
                            String name = String.valueOf(m.getValueAt(row, 1)).toLowerCase();
                            return roll.contains(lc) || name.contains(lc);
                        }
                    });
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(gradebookTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder());
        tableScroll.getViewport().setBackground(Color.WHITE);
        tableScroll.getVerticalScrollBar().setUnitIncrement(16);

        center.add(tableScroll, BorderLayout.CENTER);

        card.add(center, BorderLayout.CENTER);

        root.add(card, BorderLayout.CENTER);

        cardPanel.add(root, VIEW_GRADEBOOK);
        cardLayout.show(cardPanel, VIEW_GRADEBOOK);
    }

    private String buildSectionMeta(SectionInfo s) {
        StringBuilder sb = new StringBuilder();
        if (s.semester != null && !s.semester.isBlank()) {
            sb.append("Semester: ").append(s.semester).append(" ").append(s.year);
        }
        if (s.dayTime != null && !s.dayTime.isBlank()) {
            if (sb.length() > 0)
                sb.append("  •  ");
            sb.append("Schedule: ").append(s.dayTime);
        }
        if (s.room != null && !s.room.isBlank()) {
            if (sb.length() > 0)
                sb.append("  •  ");
            sb.append("Room: ").append(s.room);
        }
        return sb.toString();
    }

    private void styleSecondaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(6, 12, 6, 12));
        b.setContentAreaFilled(false);
        b.setFont(FontKit.semibold(13f));
        b.setForeground(TEAL);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // -------------------------------------------------------------------------
    // Gradebook data loading
    // -------------------------------------------------------------------------

    private void loadGradebookData(int sectionId) {
        gradebookStudents.clear();
        gradebookComponents.clear();
        gradesByKey.clear();
        dirtyKeys.clear();

        // 1) components for the section (via section_components)
        String cmpSql = "SELECT id, section_id, component_name, weight " +
                "FROM section_components " +
                "WHERE section_id = ? " +
                "ORDER BY id";

        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement stmt = conn.prepareStatement(cmpSql)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ComponentInfo c = new ComponentInfo();
                    c.componentId = rs.getInt("id");
                    c.name = rs.getString("component_name");
                    c.weight = rs.getInt("weight");
                    gradebookComponents.add(c);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 2) students enrolled in this section
        String stuSql = "SELECT e.enrollment_id, s.student_id, s.roll_no, s.full_name " +
                "FROM enrollments e " +
                "JOIN students s " +
                "  ON (s.student_id = e.student_id OR CAST(s.roll_no AS CHAR) = e.student_id) " +
                "WHERE e.section_id = ? " +
                "ORDER BY s.roll_no";

        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement stmt = conn.prepareStatement(stuSql)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    StudentRow row = new StudentRow();
                    row.enrollId = rs.getString("enrollment_id");
                    row.studentId = rs.getString("student_id");
                    row.rollNo = String.valueOf(rs.getInt("roll_no"));
                    row.name = rs.getString("full_name");
                    gradebookStudents.add(row);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 3) grades for section
        String grdSql = "SELECT g.enrollment_id, g.component_id, g.score " +
                "FROM grades g " +
                "JOIN enrollments e ON e.enrollment_id = g.enrollment_id " +
                "WHERE e.section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement stmt = conn.prepareStatement(grdSql)) {

            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String enrollId = rs.getString("enrollment_id");
                    int compId = rs.getInt("component_id");
                    String score = rs.getString("score");
                    String key = enrollId + ":" + compId;
                    gradesByKey.put(key, score == null ? "" : score);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Gradebook table model
    // -------------------------------------------------------------------------

    private class GradebookTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return gradebookStudents.size();
        }

        @Override
        public int getColumnCount() {
            // Roll No, Full Name, components..., Final Grade
            return 2 + gradebookComponents.size() + 1;
        }

        @Override
        public String getColumnName(int column) {
            if (column == 0)
                return "Roll No";
            if (column == 1)
                return "Full Name";
            if (column == 2 + gradebookComponents.size())
                return "Final Grade";
            return gradebookComponents.get(column - 2).name;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Only component columns editable
            return columnIndex >= 2 && columnIndex < 2 + gradebookComponents.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            StudentRow s = gradebookStudents.get(rowIndex);

            if (columnIndex == 0)
                return s.rollNo;
            if (columnIndex == 1)
                return s.name;

            // Final Grade column (last one)
            if (columnIndex == 2 + gradebookComponents.size()) {
                double pct = computeFinalPercentage(s);
                if (Double.isNaN(pct))
                    return "-";
                return String.format("%.1f", pct);
            }

            // Component columns
            ComponentInfo c = gradebookComponents.get(columnIndex - 2);
            String key = s.enrollId + ":" + c.componentId;
            return gradesByKey.getOrDefault(key, "");
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex < 2 || columnIndex >= 2 + gradebookComponents.size())
                return;

            StudentRow s = gradebookStudents.get(rowIndex);
            ComponentInfo c = gradebookComponents.get(columnIndex - 2);

            String value = (aValue == null) ? "" : aValue.toString().trim();
            String key = s.enrollId + ":" + c.componentId;

            // Basic numeric validation (optional: allow blank to clear)
            if (!value.isBlank()) {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException ex) {
                    // Reject invalid input; keep old value
                    return;
                }
            }

            gradesByKey.put(key, value);
            dirtyKeys.add(key);

            // Refresh this cell + final grade column
            fireTableCellUpdated(rowIndex, columnIndex);
            fireTableCellUpdated(rowIndex, 2 + gradebookComponents.size());
        }
    }

    private void upsertGrade(String enrollId, int componentId, String value) {
        if (enrollId == null || enrollId.isBlank())
            return;

        if (value == null || value.isBlank()) {
            // Delete grade if user cleared the cell
            String delSql = "DELETE FROM grades WHERE enrollment_id = ? AND component_id = ?";
            try (Connection conn = DatabaseConnection.erp().getConnection();
                    PreparedStatement stmt = conn.prepareStatement(delSql)) {
                stmt.setString(1, enrollId);
                stmt.setInt(2, componentId);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return;
        }

        Double numeric;
        try {
            numeric = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            // Ignore invalid non-numeric values
            return;
        }

        String updateSql = "UPDATE grades SET score = ? WHERE enrollment_id = ? AND component_id = ?";
        String insertSql = "INSERT INTO grades (enrollment_id, component_id, score) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.erp().getConnection()) {
            try (PreparedStatement up = conn.prepareStatement(updateSql)) {
                up.setDouble(1, numeric);
                up.setString(2, enrollId);
                up.setInt(3, componentId);
                int rows = up.executeUpdate();
                if (rows == 0) {
                    try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                        ins.setString(1, enrollId);
                        ins.setInt(2, componentId);
                        ins.setDouble(3, numeric);
                        ins.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // CSV Export / Import
    // -------------------------------------------------------------------------

    private void exportGradesToCsv() {
        if (gradebookStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students to export.", "Export CSV",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export grades as CSV");
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            // Header
            StringBuilder header = new StringBuilder();
            header.append("Roll No,Full Name");
            for (ComponentInfo c : gradebookComponents) {
                header.append(",").append(escapeCsv(c.name));
            }
            header.append(",Final Grade");
            w.write(header.toString());
            w.newLine();

            // Rows
            for (StudentRow s : gradebookStudents) {
                StringBuilder line = new StringBuilder();
                line.append(escapeCsv(s.rollNo)).append(",")
                        .append(escapeCsv(s.name));

                for (ComponentInfo c : gradebookComponents) {
                    String key = s.enrollId + ":" + c.componentId;
                    String val = gradesByKey.getOrDefault(key, "");
                    line.append(",").append(escapeCsv(val));
                }

                double pct = computeFinalPercentage(s);
                String finalStr = Double.isNaN(pct) ? "" : String.format("%.1f", pct);
                line.append(",").append(escapeCsv(finalStr));

                w.write(line.toString());
                w.newLine();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(),
                    "Export CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void importGradesFromCsv() {
        if (gradebookStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No students to import for.", "Import CSV",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import grades from CSV");
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File file = chooser.getSelectedFile();
        Map<String, Integer> rollToRow = new HashMap<>();
        for (int i = 0; i < gradebookStudents.size(); i++) {
            rollToRow.put(gradebookStudents.get(i).rollNo, i);
        }

        Map<String, Integer> compNameToId = new HashMap<>();
        for (ComponentInfo c : gradebookComponents) {
            compNameToId.put(c.name, c.componentId);
        }

        try (BufferedReader r = new BufferedReader(new FileReader(file))) {
            String header = r.readLine();
            if (header == null) {
                JOptionPane.showMessageDialog(this, "Empty CSV file.", "Import CSV",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] cols = header.split(",", -1);
            if (cols.length < 2) {
                JOptionPane.showMessageDialog(this, "Invalid CSV header.", "Import CSV",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Map CSV column index -> componentId (ignore Final/Final Grade)
            Map<Integer, Integer> colToComponentId = new HashMap<>();
            for (int i = 2; i < cols.length; i++) {
                String name = unescapeCsv(cols[i].trim());
                if (name.equalsIgnoreCase("Final") || name.equalsIgnoreCase("Final Grade")) {
                    continue;
                }
                if (compNameToId.containsKey(name)) {
                    colToComponentId.put(i, compNameToId.get(name));
                }
            }

            String line;
            while ((line = r.readLine()) != null) {
                if (line.isBlank())
                    continue;
                String[] parts = line.split(",", -1);
                if (parts.length < 2)
                    continue;

                String roll = unescapeCsv(parts[0].trim());
                Integer rowIdx = rollToRow.get(roll);
                if (rowIdx == null)
                    continue;

                StudentRow s = gradebookStudents.get(rowIdx);

                for (int i = 2; i < parts.length; i++) {
                    Integer compId = colToComponentId.get(i);
                    if (compId == null)
                        continue;
                    String value = unescapeCsv(parts[i].trim());
                    String key = s.enrollId + ":" + compId;
                    gradesByKey.put(key, value);
                    dirtyKeys.add(key); // mark as changed
                }
            }

            // Persist to DB
            saveGrades();
            if (gradebookModel != null) {
                gradebookModel.fireTableDataChanged();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to import CSV: " + ex.getMessage(),
                    "Import CSV", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String val) {
        if (val == null)
            return "";
        if (val.contains(",") || val.contains("\"")) {
            return "\"" + val.replace("\"", "\"\"") + "\"";
        }
        return val;
    }

    private String unescapeCsv(String val) {
        if (val == null)
            return "";
        val = val.trim();
        if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
            val = val.substring(1, val.length() - 1).replace("\"\"", "\"");
        }
        return val;
    }

    // -------------------------------------------------------------------------
    // Stats panel (shown above the table)
    // -------------------------------------------------------------------------

    private JComponent buildStatsPanel() {
        JPanel outer = new JPanel();
        outer.setOpaque(false);
        outer.setLayout(new BoxLayout(outer, BoxLayout.Y_AXIS));
        outer.setBorder(new EmptyBorder(8, 0, 16, 0));

        int total = gradebookStudents.size();
        int withGrades = 0;
        double sumFinal = 0.0;

        for (StudentRow s : gradebookStudents) {
            double pct = computeFinalPercentage(s);
            if (!Double.isNaN(pct)) {
                withGrades++;
                sumFinal += pct;
            }
        }

        double mean = (withGrades == 0) ? Double.NaN : (sumFinal / withGrades);
        String meanStr = Double.isNaN(mean) ? "-" : String.format(Locale.US, "%.1f %%", mean);

        // Summary row
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 16, 0));
        summaryRow.setOpaque(false);
        summaryRow.add(metricCard(String.valueOf(total), "Students in section"));
        summaryRow.add(metricCard(meanStr, "Mean final percentage"));
        summaryRow.add(metricCard(String.valueOf(withGrades), "Students with grades"));

        outer.add(summaryRow);
        outer.add(Box.createVerticalStrut(18));

        // Component averages
        List<String> compLabels = new ArrayList<>();
        List<Double> compValues = new ArrayList<>();
        for (ComponentInfo c : gradebookComponents) {
            double sum = 0.0;
            int count = 0;
            for (StudentRow s : gradebookStudents) {
                String key = s.enrollId + ":" + c.componentId;
                String val = gradesByKey.get(key);
                if (val == null || val.isBlank())
                    continue;
                try {
                    double score = Double.parseDouble(val);
                    sum += score;
                    count++;
                } catch (NumberFormatException ignored) {
                }
            }
            compLabels.add(c.name);
            compValues.add(count == 0 ? 0.0 : (sum / count));
        }

        // Final grade brackets
        Map<String, Integer> brackets = new LinkedHashMap<>();
        brackets.put("≥ 80%", 0);
        brackets.put("70–79%", 0);
        brackets.put("60–69%", 0);
        brackets.put("50–59%", 0);
        brackets.put("< 50%", 0);

        for (StudentRow s : gradebookStudents) {
            double pct = computeFinalPercentage(s);
            if (Double.isNaN(pct))
                continue;
            if (pct >= 80) {
                brackets.put("≥ 80%", brackets.get("≥ 80%") + 1);
            } else if (pct >= 70) {
                brackets.put("70–79%", brackets.get("70–79%") + 1);
            } else if (pct >= 60) {
                brackets.put("60–69%", brackets.get("60–69%") + 1);
            } else if (pct >= 50) {
                brackets.put("50–59%", brackets.get("50–59%") + 1);
            } else {
                brackets.put("< 50%", brackets.get("< 50%") + 1);
            }
        }

        List<String> bracketLabels = new ArrayList<>();
        List<Double> bracketValues = new ArrayList<>();
        for (Map.Entry<String, Integer> e : brackets.entrySet()) {
            bracketLabels.add(e.getKey());
            bracketValues.add(e.getValue().doubleValue());
        }

        JPanel chartsRow = new JPanel();
        chartsRow.setOpaque(false);
        chartsRow.setLayout(new GridLayout(1, 2, 16, 0));
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
        chartsRow.setPreferredSize(new Dimension(Integer.MAX_VALUE, 250));
        chartsRow.add(buildChartCard("Average score per component",
                compLabels, compValues, "Average score"));
        chartsRow.add(buildChartCard("Final grade distribution",
                bracketLabels, bracketValues, "Number of students"));

        outer.add(chartsRow);

        return outer;
    }

    private RoundedPanel metricCard(String value, String label) {
        RoundedPanel p = new RoundedPanel(18);
        p.setBackground(Color.WHITE);
        p.setLayout(new GridBagLayout());
        p.setBorder(new EmptyBorder(18, 22, 18, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.anchor = GridBagConstraints.WEST;

        JLabel v = new JLabel(value);
        v.setFont(FontKit.bold(20f));
        v.setForeground(new Color(24, 30, 37));
        p.add(v, g);

        g.gridy = 1;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.regular(13f));
        l.setForeground(new Color(100, 116, 139));
        p.add(l, g);

        return p;
    }

    private JComponent buildChartCard(String title,
            List<String> labels,
            List<Double> values,
            String yCaption) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(18, 18, 18, 18));
        card.setLayout(new BorderLayout());

        JLabel t = new JLabel(title);
        t.setFont(FontKit.semibold(15f));
        t.setForeground(new Color(24, 30, 37));
        card.add(t, BorderLayout.NORTH);

        BarChartPanel chart = new BarChartPanel(labels, values, yCaption);
        card.add(chart, BorderLayout.CENTER);

        return card;
    }

    // Simple reusable bar chart
    private static class BarChartPanel extends JPanel {
        private final List<String> labels;
        private final List<Double> values;
        private final String yCaption;

        BarChartPanel(List<String> labels, List<Double> values, String yCaption) {
            this.labels = labels;
            this.values = values;
            this.yCaption = yCaption;
            setOpaque(false);

            setPreferredSize(new Dimension(200, 180));
            setMinimumSize(new Dimension(200, 150));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int left = 48, right = 16, top = 24, bottom = 48;
            int chartW = w - left - right;
            int chartH = h - top - bottom;

            g2.setColor(new Color(148, 163, 184));
            g2.drawLine(left, top, left, top + chartH);
            g2.drawLine(left, top + chartH, left + chartW, top + chartH);

            if (labels == null || labels.isEmpty()
                    || values == null || values.isEmpty()) {
                String msg = "No data";
                FontMetrics fm = g2.getFontMetrics();
                int sw = fm.stringWidth(msg);
                g2.drawString(msg, (w - sw) / 2, h / 2);
                g2.dispose();
                return;
            }

            double maxVal = 0.0;
            for (Double v : values) {
                if (v != null && v > maxVal)
                    maxVal = v;
            }
            if (maxVal <= 0)
                maxVal = 1.0;

            int n = labels.size();
            int gap = 12;
            int barSpace = chartW / Math.max(n, 1);
            int barW = Math.max(10, barSpace - gap);

            g2.setFont(FontKit.regular(11f));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < n; i++) {
                double val = (i < values.size() && values.get(i) != null)
                        ? values.get(i)
                        : 0.0;
                int barH = (int) Math.round((val / maxVal) * chartH);
                int x = left + i * barSpace + (barSpace - barW) / 2;
                int y = top + chartH - barH;

                // bar body
                g2.setColor(new Color(59, 130, 246));
                g2.fillRoundRect(x, y, barW, barH, 8, 8);

                g2.setColor(new Color(30, 64, 175));
                g2.drawRoundRect(x, y, barW, barH, 8, 8);

                // value label
                String vs = (maxVal <= 10)
                        ? String.format(Locale.US, "%.1f", val)
                        : String.format(Locale.US, "%.0f", val);
                int vsw = fm.stringWidth(vs);
                g2.setColor(new Color(55, 65, 81));
                g2.drawString(vs, x + (barW - vsw) / 2, y - 4);

                // x-axis label
                String lbl = labels.get(i);
                int lx = x + (barW - fm.stringWidth(lbl)) / 2;
                int ly = top + chartH + fm.getAscent() + 4;
                g2.setColor(new Color(100, 116, 139));
                g2.drawString(lbl, lx, ly);
            }

            // y caption
            g2.setColor(new Color(148, 163, 184));
            g2.drawString(yCaption, left, top - 6);

            g2.dispose();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getDepartmentSafe(String instrId) {
        if (instrId == null || instrId.isBlank())
            return "None";

        String dept = "None";
        String sql = "SELECT department FROM instructors WHERE instructor_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instrId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dept = rs.getString("department");
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
        }

        return dept;
    }

    private double computeFinalPercentage(StudentRow s) {
        double sumScores = 0.0;
        double sumWeights = 0.0;

        for (ComponentInfo c : gradebookComponents) {
            String key = s.enrollId + ":" + c.componentId;
            String val = gradesByKey.get(key);
            if (val == null || val.isBlank())
                continue;

            try {
                double score = Double.parseDouble(val);
                sumScores += score;
                sumWeights += (c.weight > 0 ? c.weight : 0);
            } catch (NumberFormatException ignored) {
            }
        }

        if (sumWeights <= 0)
            return Double.NaN;
        return (sumScores / sumWeights) * 100.0; // Final percent
    }

    private void saveGrades() {
        if (dirtyKeys.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No changes to save.",
                    "Save Grades",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "You are about to save " + dirtyKeys.size() + " updated grade value(s).\n" +
                        "Are you sure you want to continue?",
                "Confirm Save",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        for (String key : dirtyKeys) {
            String[] parts = key.split(":");
            if (parts.length != 2)
                continue;
            String enrollId = parts[0];
            int compId;
            try {
                compId = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ex) {
                continue;
            }
            String value = gradesByKey.getOrDefault(key, "");
            upsertGrade(enrollId, compId, value);
        }

        dirtyKeys.clear();
        JOptionPane.showMessageDialog(this,
                "Grades saved successfully.",
                "Save Grades",
                JOptionPane.INFORMATION_MESSAGE);
    }

}
