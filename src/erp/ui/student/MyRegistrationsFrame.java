package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MyRegistrationsFrame extends StudentFrameBase {

    private JPanel listPanel;  // holds all course “cards”

    public MyRegistrationsFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.REGISTRATIONS);
        setTitle("IIITD ERP – My Registrations");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Header
        JLabel title = new JLabel("My Registrations");
        title.setFont(FontKit.bold(22f));
        title.setForeground(new Color(24, 30, 37));
        main.add(title, BorderLayout.NORTH);

        // Vertical list of cards
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);

        loadRegistrations();

        return main;
    }

    private void loadRegistrations() {
        System.out.println("[DEBUG] Student ID in MyRegistrationsFrame = '" + this.studentId + "'");

        listPanel.removeAll();

        if (this.studentId == null || this.studentId.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student id available in MyRegistrationsFrame.",
                    "Debug",
                    JOptionPane.WARNING_MESSAGE
            );
            listPanel.revalidate();
            listPanel.repaint();
            return;
        }

        final String sql =
                "SELECT e.enrollment_id, e.status, e.final_grade, " +
                "       c.course_id, c.code, c.title, c.credits, " +
                "       s.section_id, s.day_time, s.room, s.semester, s.year, " +
                "       i.instructor_name " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s ON s.section_id = e.section_id " +
                "JOIN   erp_db.courses c ON c.course_id = s.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  e.student_id = ? " +
                "  AND  e.status = 'REGISTERED' " +
                "ORDER BY s.year, s.semester, c.course_id";

        int count = 0;

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int enrollmentId   = rs.getInt("enrollment_id");
                    String courseId    = rs.getString("course_id");
                    String code        = rs.getString("code");
                    String title       = rs.getString("title");
                    int credits        = rs.getInt("credits");
                    int sectionId      = rs.getInt("section_id");      // kept internal only
                    String instructor  = rs.getString("instructor_name");
                    String dayTime     = rs.getString("day_time");
                    String room        = rs.getString("room");
                    String semester    = rs.getString("semester");
                    int year           = rs.getInt("year");
                    String finalGrade  = rs.getString("final_grade");
                    String status      = rs.getString("status");       // internal only

                    JPanel card = createCourseCard(
                            enrollmentId,
                            courseId,
                            code,
                            title,
                            credits,
                            instructor,
                            dayTime,
                            room,
                            semester,
                            year,
                            finalGrade,
                            status
                    );

                    listPanel.add(card);
                    listPanel.add(Box.createVerticalStrut(12));
                    count++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading registrations:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        if (count == 0) {
            JLabel empty = new JLabel("No registrations found for student: " + this.studentId);
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(new Color(100, 116, 139));
            empty.setBorder(new EmptyBorder(16, 8, 0, 0));
            listPanel.add(empty);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Builds a single “card” for a registered course.
     * We DO NOT show enrollmentId, sectionId, or status on the UI.
     */
    private JPanel createCourseCard(
            int enrollmentId,
            String courseId,
            String code,
            String title,
            int credits,
            String instructor,
            String dayTime,
            String room,
            String semester,
            int year,
            String finalGrade,
            String status
    ) {
        erp.ui.common.RoundedPanel card = new erp.ui.common.RoundedPanel(18);
        card.setLayout(new BorderLayout(12, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // LEFT: course code + title
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(code + " – " + title);
        titleLbl.setFont(FontKit.semibold(16f));
        titleLbl.setForeground(new Color(24, 30, 37));

        String metaLine = "Course ID " + courseId + " • " + credits + " credits";
        JLabel meta = new JLabel(metaLine);
        meta.setFont(FontKit.regular(13f));
        meta.setForeground(new Color(100, 116, 139));

        String instructorLine = (instructor == null || instructor.isBlank())
                ? "Instructor: TBA"
                : "Instructor: " + instructor;
        JLabel instr = new JLabel(instructorLine);
        instr.setFont(FontKit.regular(13f));
        instr.setForeground(new Color(100, 116, 139));

        String scheduleLine = (dayTime == null || dayTime.isBlank() ? "Time TBA" : dayTime)
                + (room == null || room.isBlank() ? "" : " • Room " + room);
        JLabel schedule = new JLabel(scheduleLine);
        schedule.setFont(FontKit.regular(13f));
        schedule.setForeground(new Color(100, 116, 139));

        String termLine = semester + " " + year;
        JLabel term = new JLabel(termLine);
        term.setFont(FontKit.regular(13f));
        term.setForeground(new Color(148, 163, 184));

        left.add(titleLbl);
        left.add(Box.createVerticalStrut(4));
        left.add(meta);
        left.add(Box.createVerticalStrut(2));
        left.add(instr);
        left.add(Box.createVerticalStrut(2));
        left.add(schedule);
        left.add(Box.createVerticalStrut(2));
        left.add(term);

        card.add(left, BorderLayout.CENTER);

        // RIGHT: final grade badge (if any)
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setAlignmentY(Component.TOP_ALIGNMENT);

        if (finalGrade != null && !finalGrade.isBlank()) {
            JLabel gradeBadge = new JLabel("Grade: " + finalGrade);
            gradeBadge.setFont(FontKit.semibold(13f));
            gradeBadge.setForeground(new Color(30, 64, 175));
            gradeBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
            right.add(gradeBadge);
        } else {
            JLabel gradeBadge = new JLabel("Grade: —");
            gradeBadge.setFont(FontKit.regular(13f));
            gradeBadge.setForeground(new Color(148, 163, 184));
            right.add(gradeBadge);
        }

        right.add(Box.createVerticalGlue());
        card.add(right, BorderLayout.EAST);

        // Click behaviour: open Grades dialog for this enrollment
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                GradesDialog.showForEnrollment(MyRegistrationsFrame.this, enrollmentId);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }
}
