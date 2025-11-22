package erp.ui.student;

import erp.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Helper to map authenticated username to student row in erp_db.students.
 *
 * Assumes auth_db.users_auth.username corresponds to students.student_id
 * or students.roll_no (demo: stu1, stu2).
 */
public class StudentLookupService {

    public static class StudentInfo {
        private final String studentId;
        private final String rollNo;
        private final String fullName;
        private final String program;
        private final int year;

        public StudentInfo(String studentId, String rollNo,
                           String fullName, String program, int year) {
            this.studentId = studentId;
            this.rollNo = rollNo;
            this.fullName = fullName;
            this.program = program;
            this.year = year;
        }

        public String getStudentId() { return studentId; }
        public String getRollNo()    { return rollNo; }
        public String getFullName()  { return fullName; }
        public String getProgram()   { return program; }
        public int getYear()         { return year; }
    }

    /**
     * Lookup a student by auth username.
     * Tries match on student_id first, then roll_no.
     */
    public static StudentInfo loadByAuthUsername(String username) {
        final String sql =
                "SELECT student_id, roll_no, full_name, program, year " +
                "FROM erp_db.students " +
                "WHERE student_id = ? OR roll_no = ? " +
                "LIMIT 1";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StudentInfo(
                            rs.getString("student_id"),
                            rs.getString("roll_no"),
                            rs.getString("full_name"),
                            rs.getString("program"),
                            rs.getInt("year")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // not found
    }
}
