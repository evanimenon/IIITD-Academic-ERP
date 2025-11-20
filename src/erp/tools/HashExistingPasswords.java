package erp.tools;

import erp.db.DatabaseConnection;
import erp.auth.PasswordUtil;

import java.sql.*;

public class HashExistingPasswords {

    public static void main(String[] args) {
        System.out.println("Connecting to auth DB and hashing existing passwords...");

        // Force init (not strictly needed, but explicit)
        DatabaseConnection.init();

        try (Connection conn = DatabaseConnection.auth().getConnection()) {

            // --- Debug: which DB are we actually connected to? ---
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT DATABASE()")) {
                if (rs.next()) {
                    System.out.println("Using database: " + rs.getString(1));
                }
            }

            // --- Debug: how many rows are in users_auth? ---
            int totalRows;
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM users_auth")) {
                rs.next();
                totalRows = rs.getInt(1);
            }
            System.out.println("users_auth rows: " + totalRows);

            conn.setAutoCommit(false);

            String selectSql = "SELECT user_id, username, password_hash FROM users_auth";
            String updateSql = "UPDATE users_auth SET password_hash = ? WHERE user_id = ?";

            try (PreparedStatement sel = conn.prepareStatement(selectSql);
                 PreparedStatement upd = conn.prepareStatement(updateSql)) {

                ResultSet rs = sel.executeQuery();
                int updated = 0;
                int alreadyHashed = 0;

                while (rs.next()) {
                    long userId = rs.getLong("user_id");
                    String username = rs.getString("username");
                    String current = rs.getString("password_hash");

                    if (current == null || current.isBlank()) {
                        continue;
                    }

                    // Skip if already looks like bcrypt
                    if (current.startsWith("$2a$")
                            || current.startsWith("$2b$")
                            || current.startsWith("$2y$")) {
                        alreadyHashed++;
                        continue;
                    }

                    // Hash the existing plain text
                    String hashed = PasswordUtil.hashPassword(current);

                    upd.setString(1, hashed);
                    upd.setLong(2, userId);
                    upd.addBatch();
                    updated++;

                    // Debug print for first few users
                    if (updated <= 5) {
                        System.out.println("Hashing user_id=" + userId +
                                           " username=" + username +
                                           " old='" + current + "'");
                    }

                    if (updated % 200 == 0) {
                        upd.executeBatch();
                    }
                }

                upd.executeBatch();
                conn.commit();

                System.out.println("Updated to hashed passwords: " + updated);
                System.out.println("Already hashed (skipped): " + alreadyHashed);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Done.");
    }
}
