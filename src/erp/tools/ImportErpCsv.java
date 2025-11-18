package erp.tools;

import erp.db.DatabaseConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Arrays;

public class ImportErpCsv {

    public static void main(String[] args) throws Exception {
        String baseDir = (args.length > 0) ? args[0] : "data";

        DatabaseConnection.init();

        try (Connection conn = DatabaseConnection.erp().getConnection()) {
            conn.setAutoCommit(false);
            System.out.println("Connected to erp_db, starting import from: " + baseDir);

            importStudents(conn, Path.of(baseDir, "students.csv"));
            importInstructors(conn, Path.of(baseDir, "instructors.csv"));
            importCourses(conn, Path.of(baseDir, "courses.csv"));
            importSections(conn, Path.of(baseDir, "sections.csv"));
            importEnrollments(conn, Path.of(baseDir, "enrollments.csv"));
            importGrades(conn, Path.of(baseDir, "grades.csv"));
            importSettings(conn, Path.of(baseDir, "settings.csv"));

            conn.commit();
            System.out.println("✅ All ERP CSVs imported successfully.");
        }
    }

    // ---------- helpers ----------

    private static String[] splitCsv(String line, int expected) {
        String[] parts = Arrays.stream(line.split(",", -1))
                .map(String::trim)
                .toArray(String[]::new);
        if (parts.length != expected) {
            parts = Arrays.copyOf(parts, expected);
        }
        return parts;
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // ---------- per-table importers ----------

    private static void importStudents(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ students.csv not found, skipping.");
            return;
        }
        System.out.println("Importing students from " + csv);

        String sql = "INSERT INTO students (student_id, roll_no, full_name, program, year) VALUES (?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip header
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 5);
                ps.setString(1, f[0]);
                ps.setString(2, f[1]);
                ps.setString(3, nullIfBlank(f[2]));
                ps.setString(4, f[3]);
                ps.setInt(5, Integer.parseInt(f[4]));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → students: " + count + " rows");
        }
    }

    private static void importInstructors(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ instructors.csv not found, skipping.");
            return;
        }
        System.out.println("Importing instructors from " + csv);

        String sql = "INSERT INTO instructors (instructor_id, department, instructor_name) VALUES (?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 3);
                ps.setLong(1, Long.parseLong(f[0])); // instructor_id must match users_auth.user_id
                ps.setString(2, f[1]);
                ps.setString(3, f[2]);
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → instructors: " + count + " rows");
        }
    }

    private static void importCourses(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ courses.csv not found, skipping.");
            return;
        }
        System.out.println("Importing courses from " + csv);

        String sql = "INSERT INTO courses (course_id, code, title, credits) VALUES (?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 4);
                ps.setString(1, f[0]);
                ps.setString(2, f[1]);
                ps.setString(3, f[2]);
                // credits as INT
                if (f[3] == null || f[3].isBlank()) {
                    ps.setNull(4, Types.INTEGER);
                } else {
                    ps.setInt(4, Integer.parseInt(f[3]));
                }
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → courses: " + count + " rows");
        }
    }

    private static void importSections(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ sections.csv not found, skipping.");
            return;
        }
        System.out.println("Importing sections from " + csv);

        String sql = "INSERT INTO sections " +
                "(section_id, course_id, instructor_id, day_time, room, capacity, semester, year) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 8);
                String sid = nullIfBlank(f[0]);
                if (sid == null) ps.setNull(1, Types.INTEGER);
                else ps.setInt(1, Integer.parseInt(sid));

                ps.setString(2, f[1]); // course_id
                ps.setLong(3, Long.parseLong(f[2])); // instructor_id
                ps.setString(4, f[3]); // day_time
                ps.setString(5, nullIfBlank(f[4])); // room
                ps.setInt(6, Integer.parseInt(f[5])); // capacity
                ps.setString(7, f[6]); // semester
                ps.setInt(8, Integer.parseInt(f[7])); // year

                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → sections: " + count + " rows");
        }
    }

    private static void importEnrollments(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ enrollments.csv not found, skipping.");
            return;
        }
        System.out.println("Importing enrollments from " + csv);

        String sql = "INSERT INTO enrollments " +
                "(enrollment_id, student_id, section_id, status) VALUES (?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 4);
                String eid = nullIfBlank(f[0]);
                if (eid == null) ps.setNull(1, Types.INTEGER);
                else ps.setInt(1, Integer.parseInt(eid));

                ps.setString(2, f[1]); // student_id
                ps.setInt(3, Integer.parseInt(f[2])); // section_id

                String status = nullIfBlank(f[3]);
                if (status == null) status = "REGISTERED";
                ps.setString(4, status);

                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → enrollments: " + count + " rows");
        }
    }

    private static void importGrades(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ grades.csv not found, skipping.");
            return;
        }
        System.out.println("Importing grades from " + csv);

        String sql = "INSERT INTO grades (enrollment_id, component, score, final_grade) VALUES (?,?,?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 4);
                ps.setInt(1, Integer.parseInt(f[0]));
                ps.setString(2, f[1]);

                String score = nullIfBlank(f[2]);
                if (score == null) ps.setNull(3, Types.DECIMAL);
                else ps.setBigDecimal(3, new java.math.BigDecimal(score));

                ps.setString(4, nullIfBlank(f[3]));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → grades: " + count + " rows");
        }
    }

    private static void importSettings(Connection conn, Path csv) throws Exception {
        if (!Files.exists(csv)) {
            System.out.println("⚠️ settings.csv not found, skipping.");
            return;
        }
        System.out.println("Importing settings from " + csv);

        String sql = "INSERT INTO settings (`key`,`value`) VALUES (?,?)";
        try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()));
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String header = br.readLine(); // skip
            if (header == null) return;

            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] f = splitCsv(line, 2);
                ps.setString(1, f[0]);
                ps.setString(2, nullIfBlank(f[1]));
                ps.addBatch();
                count++;
            }
            ps.executeBatch();
            System.out.println("  → settings: " + count + " rows");
        }
    }
}
