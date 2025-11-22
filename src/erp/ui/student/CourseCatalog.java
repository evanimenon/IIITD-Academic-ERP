// CourseCatalog.java
package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;
import erp.ui.common.TableHeader;

public class CourseCatalog extends StudentFrameBase {

    private static final Color BORDER_COLOR = new Color(230, 233, 236);

    static class TitleCell {
        final String title;
        final String instructors;

        TitleCell(String title, String instructors) {
            this.title = title;
            this.instructors = instructors;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    private final String studentId; // NEW
    private TableRowSorter<TableModel> sorter;
    private DefaultTableModel model;
    private JTable table;

    // backward compatible
    public CourseCatalog(String userDisplayName) {
        this(null, userDisplayName);
    }

    public CourseCatalog(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.CATALOG);
        this.studentId = studentId;
        setTitle("IIITD ERP – Course Catalog");
        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            root.add(banner, BorderLayout.NORTH);
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // search
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField();
        searchField.setFont(FontKit.regular(15f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)));
        searchField.setToolTipText("Search by course ID, code, title, or instructor");
        searchPanel.add(new JLabel("🔍"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        main.add(searchPanel, BorderLayout.NORTH);

        // table
        String[] columns = { "Course ID", "Code", "Title", "Credits", "Capacity", "Enrolled" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int idx) {
                return idx == 2 ? TitleCell.class : super.getColumnClass(idx);
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row))
                    c.setBackground(new Color(220, 241, 239));
                else
                    c.setBackground(Color.WHITE);
                return c;
            }
        };
        table.setFont(FontKit.regular(15f));
        table.setRowHeight(64);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader hdr = table.getTableHeader();
        hdr.setDefaultRenderer(new TableHeader());

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(110);
        cm.getColumn(1).setPreferredWidth(90);
        cm.getColumn(2).setPreferredWidth(500);
        cm.getColumn(3).setPreferredWidth(80);
        cm.getColumn(4).setPreferredWidth(80);
        cm.getColumn(5).setPreferredWidth(80);

        // title+instructor renderer
        TableCellRenderer titleR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row,
                    int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                TitleCell cell = (TitleCell) val;
                l.setOpaque(false);
                l.setBorder(new EmptyBorder(10, 10, 10, 10));
                l.setFont(FontKit.regular(14f));
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                String title = cell.title == null ? "" : cell.title;
                String inst = (cell.instructors == null || cell.instructors.isBlank()) ? "TBA" : cell.instructors;

                l.setText("<html><body style='width:95%;'>"
                        + "<div style='font-weight:600; color:#0066cc; text-decoration:underline; font-size:14px;'>"
                        + esc(title) + "</div>"
                        + "<div style='margin-top:3px; font-size:12px; color:#64748b;'>Instructor: "
                        + esc(inst) + "</div></body></html>");

                int needed = l.getPreferredSize().height + 16;
                if (tbl.getRowHeight(row) != needed)
                    tbl.setRowHeight(row, needed);
                return l;
            }
        };
        cm.getColumn(2).setCellRenderer(titleR);

        // click: open sections + registration dialog
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0)
                    return;

                if (table.isRowSelected(row)) {
                    table.clearSelection(); // deselect
                } else {
                    table.setRowSelectionInterval(row, row);
                }

                if (col == 2) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String courseId = String.valueOf(model.getValueAt(modelRow, 0));
                    TitleCell cell = (TitleCell) model.getValueAt(modelRow, 2);

                    // ⬇️ REPLACE THIS OLD JOptionPane with:
                    if (studentId == null || studentId.isBlank()) {
                        JOptionPane.showMessageDialog(
                                CourseCatalog.this,
                                "Student ID is not available on this screen.",
                                "Registration",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    String msg = EnrollmentService.registerForCourse(studentId, courseId);
                    JOptionPane.showMessageDialog(
                            CourseCatalog.this,
                            msg,
                            "Registration",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Optional: reload course counts after registration
                    model.setRowCount(0);
                    loadCourses();
                }

                // if (col == 2) {
                // int modelRow = table.convertRowIndexToModel(row);
                // String courseId = String.valueOf(model.getValueAt(modelRow, 0));
                // TitleCell cell = (TitleCell) model.getValueAt(modelRow, 2);
                // int capacity = (int) model.getValueAt(modelRow, 4);
                // int enrolled = (int) model.getValueAt(modelRow, 5);

                // new CourseSectionsDialog(
                // CourseCatalog.this,
                // studentId,
                // courseId,
                // cell.title,
                // capacity,
                // enrolled
                // ).setVisible(true);

                // // reload counts after registration
                // model.setRowCount(0);
                // loadCourses();
                // }

            }
        });

        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        main.add(sc, BorderLayout.CENTER);

        // search filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filter();
            }

            private void filter() {
                String t = searchField.getText();
                if (t.isBlank())
                    sorter.setRowFilter(null);
                else {
                    try {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + t));
                    } catch (Exception ex) {
                        sorter.setRowFilter(null);
                    }
                }
            }
        });

        loadCourses();
        return main;
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadCourses() {
        final String sql = "SELECT c.course_id, c.code, c.title, c.credits, " +
                "       COALESCE(SUM(s.capacity), 0) AS total_capacity, " +
                "       COALESCE(GROUP_CONCAT(DISTINCT i.instructor_name ORDER BY i.instructor_name SEPARATOR ', '), 'TBA') AS instructors, "
                +
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
                int capacity = rs.getInt("total_capacity");
                int enrolled = rs.getInt("enrolled");

                model.addRow(new Object[] {
                        rs.getString("course_id"),
                        rs.getString("code"),
                        new TitleCell(rs.getString("title"), rs.getString("instructors")),
                        rs.getInt("credits"),
                        capacity,
                        enrolled
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        erp.db.DatabaseConnection.init();
        SwingUtilities.invokeLater(() -> new CourseCatalog("Student 123").setVisible(true));
    }
}
