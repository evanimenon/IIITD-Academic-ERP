package erp.tools;

import erp.db.DatabaseConnection;
import erp.auth.hash.PasswordHasher;

import java.sql.*;

public class BulkRehash {
    public static void main(String[] args) throws Exception {
        DatabaseConnection.init();
        try (Connection c = DatabaseConnection.auth().getConnection()) {
            String select = "SELECT user_id, plain FROM users_auth";
            String update = "UPDATE users_auth SET password_hash=? WHERE user_id=?";

            try (PreparedStatement psSel = c.prepareStatement(select);
                 PreparedStatement psUpd = c.prepareStatement(update);
                 ResultSet rs = psSel.executeQuery()) {

                while (rs.next()) {
                    long id = rs.getLong("user_id");
                    String plain = rs.getString("plain");
                    if (plain == null || plain.isBlank()) continue;

                    String hash = PasswordHasher.hash(plain);
                    psUpd.setString(1, hash);
                    psUpd.setLong(2, id);
                    psUpd.addBatch();
                }

                psUpd.executeBatch();
                System.out.println("Done hashing all users.");
            }
        }
    }
}
