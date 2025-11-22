package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.TableHeader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyRegistrationsFrame extends StudentFrameBase {

    private final String studentId;
    private DefaultTableModel model;
    private JTable table;

    public MyRegistrationsFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.REGISTRATIONS);
        this.studentId = studentId;
        setTitle("IIITD ERP – My Registrations");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setOpaque(false);

        JLabel title = new JLabel("My Registrations");
        title.setFont(FontKit.bold(20f));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));

        main.add(title, BorderLayout.NORTH);

        // table
        String[] cols = {
                "Enrollment ID",   // hidden
                "Course ID",
                "Code",
                "Title",
                "Credits",
                "Section ID",
                "Instructor",
                "Day / Time",
                "Room",
                "Semester",
                "Year",
                "Status"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(FontKit.regular(14f));
        table.setRowHeight(28);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 233, 236));
        table.setBackground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader hdr = table.getTableHeader();
        hdr.setDefaultRenderer(new TableHeader());

        // hide Enrollment ID column from view
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setMinWidth(0);
        cm.getColumn(0).setMaxWidth(0);
        cm.getColumn(0).setPreferredWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 236), 1));
        main.add(sp, BorderLayout.CENTER);

        // bottom buttons: Drop, View Grades, Transcript CSV/PDF
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.setOpaque(false);

        JButton dropBtn = new JButton("Drop Section");
        JButton gradesBtn = new JButton("View Grades");
        JButton csvBtn = new JButton("Download Transcript (CSV)");
        JButton pdfBtn = new JButton("Download Transcript (PDF)");

        dropBtn.setFont(FontKit.semibold(13f));
        gradesBtn.setFont(FontKit.semibold(13f));
        csvBtn.setFont(FontKit.semibold(13f));
        pdfBtn.setFont(FontKit.semibold(13f));

        bottom.add(dropBtn);
        bottom.add(gradesBtn);
        bottom.add(csvBtn);
        bottom.add(pdfBtn);
        main.add(bottom, BorderLayout.SOUTH);

        // actions
        dropBtn.addActionListener(e -> onDrop());
        gradesBtn.addActionListener(e -> onViewGrades());
        csvBtn.addActionListener(e -> TranscriptExporter.exportTranscriptCsv(this, studentId));
        pdfBtn.addActionListener(e -> TranscriptExporter.exportTranscriptPdf(this, studentId));

        loadRegistrations();
        return main;
    }

    private void loadRegistrations() {
        model.setRowCount(0);

        final String sql =
                "SELECT e.enrollment_id, e.status, " +
                "       c.course_id, c.code, c.title, c.credits, " +
                "       s.section_id, s.day_time, s.room, s.semester, s.year, " +
                "       i.instructor_name " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s   ON e.section_id = s.section_id " +
                "JOIN   erp_db.courses c    ON s.course_id = c.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  e.student_id = ? " +
                "ORDER BY s.year, s.semester, c.course_id";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
                            rs.getInt("enrollment_id"),
                            rs.getString("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getInt("section_id"),
                            rs.getString("instructor_name"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getString("status")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading registrations:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private Integer getSelectedEnrollmentId() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        Object val = model.getValueAt(modelRow, 0);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return Integer.valueOf(val.toString());
    }

    private void onDrop() {
        Integer enrId = getSelectedEnrollmentId();
        if (enrId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a registration first.",
                    "Drop Section",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String confirm = "Are you sure you want to drop this section?";
        int opt = JOptionPane.showConfirmDialog(this, confirm,
                "Confirm Drop", JOptionPane.YES_NO_OPTION);
        if (opt != JOptionPane.YES_OPTION) return;

        String msg = EnrollmentService.dropEnrollment(enrId, studentId);
        JOptionPane.showMessageDialog(this, msg, "Drop Section",
                JOptionPane.INFORMATION_MESSAGE);

        loadRegistrations();
    }

    private void onViewGrades() {
        Integer enrId = getSelectedEnrollmentId();
        if (enrId == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a registration first.",
                    "View Grades",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        GradesDialog.showForEnrollment(this, enrId);
    }
}
