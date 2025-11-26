package erp.ui.student;

import erp.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EnrollmentService {

    // Universal deadline for DROPS (and optionally for registration testing)
    public static final LocalDate DROP_DEADLINE = LocalDate.of(2025, 11, 30);

    /**
     * Register a student for a course at course level.
     * - Checks duplicate registration (any section of this course).
     * - Checks total course capacity (sum of section capacities).
     * - Picks the section with least load that still has free seats.
     */
    public static String registerForCourse(String studentId, String courseId) {
        if (studentId == null || studentId.isBlank()) {
            return "Student ID not available.";
        }

        try (Connection c = DatabaseConnection.erp().getConnection()) {
            c.setAutoCommit(false);

            // 1) Check duplicate registration in this course
            final String dupSql = "SELECT COUNT(*) " +
                    "FROM erp_db.enrollments e " +
                    "JOIN erp_db.sections s ON e.section_id = s.section_id " +
                    "WHERE e.student_id = ? " +
                    "  AND e.status = 'REGISTERED' " +
                    "  AND s.course_id = ?";

            try (PreparedStatement ps = c.prepareStatement(dupSql)) {
                ps.setString(1, studentId);
                ps.setString(2, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        c.rollback();
                        return "You are already registered in this course.";
                    }
                }
            }

            // 2) Total capacity for this course = SUM(sections.capacity)
            int totalCapacity = 0;
            final String capSql = "SELECT COALESCE(SUM(capacity), 0) AS total_capacity " +
                    "FROM erp_db.sections " +
                    "WHERE course_id = ?";
            try (PreparedStatement ps = c.prepareStatement(capSql)) {
                ps.setString(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalCapacity = rs.getInt("total_capacity");
                    }
                }
            }

            // 3) Total enrolled in this course (REGISTERED only)
            int totalEnrolled = 0;
            final String enrSql = "SELECT COUNT(*) AS enrolled " +
                    "FROM erp_db.enrollments e " +
                    "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                    "WHERE s.course_id = ? " +
                    "  AND e.status = 'REGISTERED'";
            try (PreparedStatement ps = c.prepareStatement(enrSql)) {
                ps.setString(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        totalEnrolled = rs.getInt("enrolled");
                    }
                }
            }

            if (totalCapacity > 0 && totalEnrolled >= totalCapacity) {
                c.rollback();
                return "Course capacity is full. You cannot register.";
            }

            // 4) Pick section with least load that still has free seats
            Long sectionId = null;
            final String pickSql = "SELECT s.section_id, s.capacity, " +
                    "       (SELECT COUNT(*) FROM erp_db.enrollments e " +
                    "        WHERE e.section_id = s.section_id " +
                    "          AND e.status = 'REGISTERED') AS enrolled_here " +
                    "FROM   erp_db.sections s " +
                    "WHERE  s.course_id = ? " +
                    "ORDER BY enrolled_here ASC, s.section_id ASC";

            try (PreparedStatement ps = c.prepareStatement(pickSql)) {
                ps.setString(1, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int cap = rs.getInt("capacity");
                        int here = rs.getInt("enrolled_here");
                        if (here < cap) {
                            sectionId = rs.getLong("section_id");
                            break;
                        }
                    }
                }
            }

            if (sectionId == null) {
                c.rollback();
                return "All sections are full. You cannot register.";
            }

            // 5) Insert enrollment as REGISTERED
            final String insSql = "INSERT INTO erp_db.enrollments (student_id, section_id, status) " +
                    "VALUES (?, ?, 'REGISTERED')";
            try (PreparedStatement ps = c.prepareStatement(insSql)) {
                ps.setString(1, studentId);
                ps.setLong(2, sectionId);
                ps.executeUpdate();
            }

            c.commit();
            return "Successfully registered for the course. Section allocation done by backend.";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Registration failed: " + ex.getMessage();
        }
    }

    /** Can we drop today? (before universal deadline). */
    public static boolean canDrop() {
        // strictly before; change to !isAfter if you want inclusive deadline
        return LocalDate.now().isBefore(DROP_DEADLINE);
    }

    /** Drop a specific enrollment for this student, if before deadline. */
    public static String dropEnrollment(long enrollmentId, String studentId) {
        if (!canDrop()) {
            return "Drop deadline has passed. You cannot drop this section.";
        }

        final String sql = "UPDATE erp_db.enrollments " +
                "SET status = 'DROPPED' " +
                "WHERE enrollment_id = ? " +
                "  AND student_id = ? " +
                "  AND status = 'REGISTERED'";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, enrollmentId);
            ps.setString(2, studentId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                return "No active enrollment found to drop.";
            }
            return "Section dropped successfully.";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Failed to drop: " + ex.getMessage();
        }
    }

    /**
     * A course is "registered" for a student if there exists a row in
     * erp_db.enrollments with this student, some section for this course,
     * AND status = 'REGISTERED'.
     */
    public static boolean isRegisteredForCourse(String studentId, String courseId) {
        final String sql = "SELECT COUNT(*) " +
                "FROM erp_db.enrollments e " +
                "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                "WHERE e.student_id = ? " +
                "  AND s.course_id = ? " +
                "  AND e.status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentId);
            ps.setString(2, courseId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * ✅ NEW:
     * A course can be dropped iff:
     *  - The global/settings drop deadline has NOT passed, and
     *  - There is at least one REGISTERED enrollment for this student+course.
     */
    public static boolean canDropCourse(String studentId, String courseId) {
        try (Connection conn = DatabaseConnection.erp().getConnection()) {

            // 1) Check deadline (same rule as dropCourse)
            LocalDate deadline = fetchDropDeadline(conn);
            LocalDate today = LocalDate.now();
            if (deadline != null && today.isAfter(deadline)) {
                return false;
            }

            // 2) Check if there is a REGISTERED enrollment for this course
            final String sql = "SELECT COUNT(*) " +
                    "FROM erp_db.enrollments e " +
                    "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                    "WHERE e.student_id = ? " +
                    "  AND s.course_id = ? " +
                    "  AND e.status = 'REGISTERED'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                ps.setString(2, courseId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * Drops a course for a student if we are before the configured drop deadline.
     * Returns a human-readable message for the UI.
     */
    public static String dropCourse(String studentId, String courseId) {
        try (Connection conn = DatabaseConnection.erp().getConnection()) {

            // 1) Read global drop deadline from settings (optional but recommended)
            LocalDate deadline = fetchDropDeadline(conn);
            LocalDate today = LocalDate.now();

            if (deadline != null && today.isAfter(deadline)) {
                return "The drop deadline (" + deadline + ") has passed. "
                        + "You can no longer drop this course.";
            }

            // 2) Actually drop: delete enrollment rows for this student+course
            final String sql = "DELETE e FROM erp_db.enrollments e " +
                    "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                    "WHERE e.student_id = ? " +
                    "  AND s.course_id = ? " +
                    "  AND e.status = 'REGISTERED'";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                ps.setString(2, courseId);

                int deleted = ps.executeUpdate();
                if (deleted > 0) {
                    return "You have successfully dropped this course.";
                } else {
                    return "You are not currently enrolled in this course.";
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            return "Error while dropping course: " + ex.getMessage();
        }
    }

    /**
     * Reads COURSE_DROP_DEADLINE from erp_db.settings as YYYY-MM-DD.
     * Returns null if not set or invalid – in that case we allow dropping anytime.
     */
    private static LocalDate fetchDropDeadline(Connection conn) throws SQLException {
        final String sql = "SELECT setting_value " +
                "FROM erp_db.settings " +
                "WHERE setting_key = 'COURSE_DROP_DEADLINE'";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String value = rs.getString("setting_value");
                if (value != null && !value.isBlank()) {
                    try {
                        return LocalDate.parse(value.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
                    } catch (DateTimeParseException ex) {
                        // bad date in DB – treat as no deadline
                        ex.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

}
