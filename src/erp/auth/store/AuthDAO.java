package erp.auth.store;

import erp.db.DatabaseConnection;
import java.sql.*;

public class AuthDAO {
    public static record AuthRow(long userId, String username, String role, String hash) {}

    public AuthRow findByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, role, password_hash FROM users_auth WHERE username=? AND status='ACTIVE'";
        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthRow(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash")
                    );
                }
                return null;
            }
        }
    }

    public void updateLastLogin(long userId) throws SQLException {
        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(
               "UPDATE users_auth SET last_login = CURRENT_TIMESTAMP WHERE user_id=?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }
}
