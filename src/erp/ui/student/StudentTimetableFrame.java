package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.TableHeader;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class StudentTimetableFrame extends StudentFrameBase {

    private final String studentId;
    private DefaultTableModel model;

    public StudentTimetableFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.TIMETABLE);
        this.studentId = studentId;
        setTitle("IIITD ERP – Time Table");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setOpaque(false);

        JLabel title = new JLabel("My Timetable (Registered Sections)");
        title.setFont(FontKit.bold(20f));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        main.add(title, BorderLayout.NORTH);

        String[] cols = {
                "Course ID",
                "Code",
                "Title",
                "Section ID",
                "Day / Time",
                "Room",
                "Semester",
                "Year"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setFont(FontKit.regular(14f));
        table.setRowHeight(26);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(230, 233, 236));
        table.setBackground(Color.WHITE);

        JTableHeader hdr = table.getTableHeader();
        hdr.setDefaultRenderer(new TableHeader());

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 236), 1));
        main.add(sp, BorderLayout.CENTER);

        loadTimetable();
        return main;
    }

    private void loadTimetable() {
        model.setRowCount(0);

        final String sql =
                "SELECT c.course_id, c.code, c.title, " +
                "       s.section_id, s.day_time, s.room, s.semester, s.year " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s ON e.section_id = s.section_id " +
                "JOIN   erp_db.courses c  ON s.course_id = c.course_id " +
                "WHERE  e.student_id = ? " +
                "  AND  e.status = 'REGISTERED' " +
                "ORDER BY s.day_time, c.course_id";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
                            rs.getString("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("section_id"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getString("semester"),
                            rs.getInt("year")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading timetable:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
