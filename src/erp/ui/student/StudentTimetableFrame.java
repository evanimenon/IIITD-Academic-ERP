// StudentTimetableFrame.java
package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentTimetableFrame extends StudentFrameBase {

    private JTable table;
    private DefaultTableModel model;

    public StudentTimetableFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.TIMETABLE);
        setTitle("IIITD ERP – Time Table");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel title = new JLabel("Time Table");
        title.setFont(FontKit.bold(22f));
        title.setForeground(new Color(24, 30, 37));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {
                "Section ID",
                "Course ID",
                "Code",
                "Title",
                "Day / Time",
                "Room",
                "Semester",
                "Year",
                "Instructor"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panel.add(sp, BorderLayout.CENTER);

        loadTimetable();

        return panel;
    }

    private void loadTimetable() {
        System.out.println("[DEBUG] Student ID in TimetableFrame = '" + this.studentId + "'");

        model.setRowCount(0);

        if (this.studentId == null || this.studentId.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student id available in TimetableFrame.",
                    "Debug",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        final String sql =
                "SELECT s.section_id, c.course_id, c.code, c.title, " +
                "       s.day_time, s.room, s.semester, s.year, " +
                "       i.instructor_name " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s ON s.section_id = e.section_id " +
                "JOIN   erp_db.courses c ON c.course_id = s.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  e.student_id = ? " +
                "  AND  e.status = 'REGISTERED' " +
                "ORDER BY s.year, s.semester, s.day_time";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("section_id"),
                            rs.getString("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getString("instructor_name")
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading timetable:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "No timetable entries found for student: " + this.studentId,
                    "Time Table",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
}
