// File: src/tools/DebugAuthCheck.java
package erp.tools;

import erp.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

public class DebugAuthCheck {
    public static void main(String[] args) throws Exception {
        // ensure DB init happened
        DatabaseConnection.init();

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter username to inspect: ");
        String username = sc.nextLine().trim();

        String sql = "SELECT user_id, username, role, password_hash FROM users_auth WHERE username = ?";
        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No row found for username = '" + username + "'.");
                    System.out.println("Possible reasons: different username case, spaces, or wrong DB/schema.");
                    return;
                }
                long id = rs.getLong("user_id");
                String user = rs.getString("username");
                String role = rs.getString("role");
                String hash = rs.getString("password_hash");

                System.out.println("Found row:");
                System.out.println(" user_id = " + id);
                System.out.println(" username = " + user);
                System.out.println(" role = " + role);
                if (hash == null) {
                    System.out.println(" password_hash = NULL");
                } else {
                    System.out.println(" password_hash length = " + hash.length());
                    System.out.println(" password_hash prefix = " + (hash.length() >= 6 ? hash.substring(0, 6) : hash));
                    // optionally show last chars count (never show whole hash)
                }
            }
        } catch (NullPointerException npe) {
            System.err.println("DatabaseConnection.auth() seems to be null — pool not created. Check db.properties / env vars.");
            npe.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Error while querying DB: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
