package erp.auth.store;

import erp.db.DatabaseConnection;
import java.sql.*;

public class AuthDAO {

    // What we return from the DB lookup
    public static record AuthRow(long userId, String username, String role, String hash) {
    }

    /** Find a user by username (no status filter to avoid silent mismatches). */
    public AuthRow findByUsername(String username) throws SQLException {
        final String sql = "SELECT user_id, username, role, password_hash " +
                "FROM users_auth WHERE username = ?";

        try (Connection c = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return null;
                return new AuthRow(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"));
            }
        }
    }

    /** Touch last_login on success (optional column; ignore if missing). */
    public void updateLastLogin(long userId) throws SQLException {
        try (Connection c = DatabaseConnection.auth().getConnection();
                PreparedStatement ps = c.prepareStatement(
                        "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

}
