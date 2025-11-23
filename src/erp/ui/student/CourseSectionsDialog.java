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

public class CourseSectionsDialog extends JDialog {

    private final String studentId;
    private final String courseId;
    private DefaultTableModel model;

    public CourseSectionsDialog(Frame owner,
                                String studentId,
                                String courseId,
                                String courseTitle,
                                int totalCapacity,
                                int totalEnrolled) {
        super(owner, "Sections – " + courseTitle, true);
        this.studentId = studentId;
        this.courseId = courseId;

        FontKit.init();
        setSize(720, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JLabel heading = new JLabel(courseTitle);
        heading.setFont(FontKit.semibold(18f));

        JLabel meta = new JLabel("Total capacity: " + totalCapacity +
                " • Registered: " + totalEnrolled);
        meta.setFont(FontKit.regular(13f));
        meta.setForeground(new Color(100, 116, 139));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(heading);
        top.add(Box.createVerticalStrut(4));
        top.add(meta);
        root.add(top, BorderLayout.NORTH);

        String[] cols = {"Section ID", "Instructor", "Day / Time", "Room", "Capacity", "Semester / Year"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(FontKit.regular(13f));
        JScrollPane sp = new JScrollPane(table);
        root.add(sp, BorderLayout.CENTER);

        JButton regBtn = new JButton("Register for this course");
        regBtn.setFont(FontKit.semibold(14f));

        if (studentId == null || studentId.isBlank()) {
            regBtn.setEnabled(false);
            regBtn.setToolTipText("Student ID not available on this screen.");
        }

        regBtn.addActionListener(e -> {
            String msg = EnrollmentService.registerForCourse(studentId, courseId);
            JOptionPane.showMessageDialog(this, msg, "Registration",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(regBtn);
        root.add(bottom, BorderLayout.SOUTH);

        loadSections();
    }

    private void loadSections() {
        final String sql =
                "SELECT s.section_id, s.day_time, s.room, s.capacity, " +
                "       s.semester, s.year, i.instructor_name " +
                "FROM   erp_db.sections s " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  s.course_id = ? " +
                "ORDER BY s.section_id ASC";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
                            rs.getInt("section_id"),
                            rs.getString("instructor_name"),
                            rs.getString("day_time"),
                            rs.getString("room"),
                            rs.getInt("capacity"),
                            rs.getString("semester") + " " + rs.getInt("year")
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading sections:\n" + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
