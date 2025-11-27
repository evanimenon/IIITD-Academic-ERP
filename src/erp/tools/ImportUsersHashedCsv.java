package erp.tools;

import erp.db.DatabaseConnection;
import erp.auth.PasswordUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImportUsersHashedCsv {

    private static final DateTimeFormatter TS_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java ... erp.tools.ImportUsersHashedCsv <path/to/users_auth.csv>");
            System.exit(1);
        }

        Path csvPath = Paths.get(args[0]);
        if (!Files.exists(csvPath)) {
            System.err.println("CSV not found: " + csvPath.toAbsolutePath());
            System.exit(1);
        }

        System.out.println("Connecting to auth DB and importing from: " + csvPath);

        try (Connection conn = DatabaseConnection.auth().getConnection()) {
            conn.setAutoCommit(false);

            String sql =
                "INSERT INTO users_auth " +
                "(user_id, username, role, password_hash, status, last_login) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "username = VALUES(username), " +
                "role = VALUES(role), " +
                "password_hash = VALUES(password_hash), " +
                "status = VALUES(status), " +
                "last_login = VALUES(last_login)";

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 BufferedReader br = Files.newBufferedReader(csvPath, StandardCharsets.UTF_8)) {

                String line;
                boolean first = true;
                int count = 0;

                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    // skip header row if present
                    if (first && line.toLowerCase().startsWith("user_id")) {
                        first = false;
                        continue;
                    }
                    first = false;

                    String[] parts = line.split(",", -1);
                    if (parts.length < 6) {
                        System.err.println("Skipping malformed line: " + line);
                        continue;
                    }

                    long userId       = Long.parseLong(parts[0].trim());
                    String username   = parts[1].trim();
                    String role       = parts[2].trim();
                    String plainPass  = parts[3].trim(); 
                    String status     = parts[4].trim();
                    String lastLoginS = parts[5].trim();

                    String hashed = PasswordUtil.hashPassword(plainPass);

                    ps.setLong(1, userId);
                    ps.setString(2, username);
                    ps.setString(3, role);
                    ps.setString(4, hashed);
                    ps.setString(5, status.isEmpty() ? null : status);

                    if (lastLoginS.isEmpty() || lastLoginS.equalsIgnoreCase("null")) {
                        ps.setTimestamp(6, null);
                    } else {
                        LocalDateTime ldt = LocalDateTime.parse(lastLoginS, TS_FMT);
                        ps.setTimestamp(6, Timestamp.valueOf(ldt));
                    }

                    ps.addBatch();
                    count++;

                    if (count % 500 == 0) {
                        ps.executeBatch();
                    }
                }

                ps.executeBatch();
                conn.commit();
                System.out.println("Imported/updated " + count + " users with hashed passwords.");
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
