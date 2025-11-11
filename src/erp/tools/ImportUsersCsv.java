package erp.tools;

import erp.db.DatabaseConnection;
import erp.auth.hash.PasswordHasher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;

public class ImportUsersCsv {

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java ... erp.tools.ImportUsersCsv <path/to/auth_db.csv>");
            System.exit(1);
        }
        Path csv = Path.of(args[0]);
        if (!Files.exists(csv)) {
            System.err.println("File not found: " + csv.toAbsolutePath());
            System.exit(2);
        }

        DatabaseConnection.init();

        try (Connection c = DatabaseConnection.auth().getConnection()) {
            c.setAutoCommit(false);

            // Detect headers
            try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()))) {
                String header = br.readLine();
                if (header == null) throw new IllegalArgumentException("Empty CSV");
                String[] cols = Arrays.stream(header.split(","))
                        .map(String::trim).map(String::toLowerCase).toArray(String[]::new);

                int iUser = idx(cols, "username", true);
                int iRole = idx(cols, "role", true);
                int iPwd  = idx(cols, "password", false);
                int iHash = idx(cols, "password_hash", false);
                int iStat = idx(cols, "status", false);

                String line;
                int ok = 0, skipped = 0;
                String sql = "INSERT INTO users_auth (username, role, password_hash, status) " +
                             "VALUES (?,?,?,?) " +
                             "ON DUPLICATE KEY UPDATE role=VALUES(role), password_hash=VALUES(password_hash), status=VALUES(status)";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    while ((line = br.readLine()) != null) {
                        if (line.isBlank()) continue;
                        String[] f = splitCsv(line, cols.length);
                        String username = val(f, iUser);
                        String role = normalizeRole(val(f, iRole));
                        String status = (iStat >= 0) ? val(f, iStat) : "active";
                        if (status == null || status.isBlank()) status = "active";

                        String storedHash = (iHash >= 0) ? val(f, iHash) : null;
                        String plain = (iPwd >= 0) ? val(f, iPwd) : null;

                        String finalHash = null;
                        if (storedHash != null && storedHash.startsWith("$2")) {
                            finalHash = storedHash;
                        } else if (plain != null && !plain.isBlank()) {
                            finalHash = PasswordHasher.hash(plain);
                        } else {
                            // default per role (optional fallback)
                            plain = switch (role) {
                                case "admin" -> "admin@123";
                                case "instructor" -> "inst@123";
                                default -> "stud@123";
                            };
                            finalHash = PasswordHasher.hash(plain);
                        }

                        if (username == null || username.isBlank() || role == null) {
                            skipped++;
                            continue;
                        }

                        ps.setString(1, username);
                        ps.setString(2, role);
                        ps.setString(3, finalHash);
                        ps.setString(4, status.toLowerCase());
                        ps.addBatch();
                        ok++;
                    }
                    ps.executeBatch();
                }
                c.commit();
                System.out.println("✅ Imported/updated rows: " + ok + " (skipped: " + skipped + ")");
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static int idx(String[] cols, String name, boolean required) {
        for (int i = 0; i < cols.length; i++) if (cols[i].equals(name)) return i;
        if (required) throw new IllegalArgumentException("CSV missing required column: " + name);
        return -1;
    }

    private static String[] splitCsv(String line, int expectedCols) {
        // Simple split; assumes no embedded commas. For advanced CSV, plug in OpenCSV.
        String[] parts = Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);
        if (parts.length != expectedCols) {
            // pad or truncate to expected length to avoid AIOOB in val()
            parts = Arrays.copyOf(parts, expectedCols);
        }
        return parts;
    }

    private static String val(String[] f, int i) {
        return (i >= 0 && i < f.length) ? (f[i] == null ? null : f[i].trim()) : null;
    }

    private static String normalizeRole(String role) {
        if (role == null) return "student";
        role = role.trim().toLowerCase();
        if (!role.equals("admin") && !role.equals("instructor") && !role.equals("student")) {
            return "student";
        }
        return role;
    }
}
