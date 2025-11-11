package erp.tools;
import erp.db.DatabaseConnection;
import erp.auth.hash.PasswordHasher;
import java.sql.*;

public class BulkRehash {
    public static void main(String[] args) throws Exception {
        DatabaseConnection.init();
        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement select = c.prepareStatement("SELECT user_id, password_hash FROM users_auth");
             ResultSet rs = select.executeQuery()) {

            while (rs.next()) {
                long id = rs.getLong("user_id");
                String plain = rs.getString("password_hash");
                if (plain == null || plain.startsWith("$2")) continue; // already hashed

                String hashed = PasswordHasher.hash(plain);

                try (PreparedStatement upd = c.prepareStatement(
                        "UPDATE users_auth SET password_hash=? WHERE user_id=?")) {
                    upd.setString(1, hashed);
                    upd.setLong(2, id);
                    upd.executeUpdate();
                }
            }
        }
        System.out.println("✅ All plaintext passwords converted to bcrypt.");
    }
}
