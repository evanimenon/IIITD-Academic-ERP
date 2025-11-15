package erp.auth.store;

import erp.db.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data-access helpers for authentication-related queries.
 *
 * NOTE: This class intentionally uses PreparedStatement + try-with-resources everywhere to
 * prevent SQL injection and to ensure connections are always closed.
 */
public final class AuthDAO {
    private static final Logger LOGGER = Logger.getLogger(AuthDAO.class.getName());

    // Query timeouts (seconds) to fail fast if DB is unresponsive.
    private static final int QUERY_TIMEOUT_SECONDS = 10;

    // What we return from the DB lookup
    public static record AuthRow(long userId, String username, String role, String hash) {
    }

    private AuthDAO() { /* no instances */ }

    /**
     * Find a user by username (no status filter to avoid silent mismatches).
     *
     * @param username non-null, non-blank username (trimmed)
     * @return AuthRow or null if not found
     * @throws SQLException on DB errors
     * @see #authenticate(String, String)
     */
    public static AuthRow findByUsername(String username) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }
        final String sql = "SELECT user_id, username, role, password_hash " +
                "FROM users_auth WHERE username = ?";

        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setQueryTimeout(QUERY_TIMEOUT_SECONDS); // set timeout to avoid long-hanging queries
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new AuthRow(
                        rs.getLong("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("password_hash"));
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "DB error while finding user by username", ex);
            throw ex;
        }
    }

    /**
     * Touch last_login on success (optional column; ignore if missing).
     *
     * @param userId database user id
     * @throws SQLException on DB errors
     */
    public static void updateLastLogin(long userId) throws SQLException {
        final String sql = "UPDATE users_auth SET last_login = NOW() WHERE user_id = ?";
        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Failed to update last_login for userId=" + userId, ex);
            throw ex;
        }
    }

    /**
     * Authenticate a username + plaintext password.
     * This method hides the password-hash handling from callers and uses bcrypt check.
     *
     * @param username username to authenticate (non-null, non-blank)
     * @param passwordPlain plaintext password provided by user (non-null)
     * @return true if authenticated, false otherwise
     * @throws SQLException on DB errors
     */
    public static boolean authenticate(String username, String passwordPlain) throws SQLException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be null or blank");
        }
        if (passwordPlain == null) {
            throw new IllegalArgumentException("password must not be null");
        }

        AuthRow row = findByUsername(username);
        if (row == null) return false;

        String storedHash = row.hash();
        if (storedHash == null || storedHash.isBlank()) {
            // Unexpected: user exists but no password hash
            LOGGER.log(Level.WARNING, "User {0} has no password hash stored", username);
            return false;
        }

        try {
            // BCrypt.checkpw is safe — it returns false for non-matching or malformed hashes
            return BCrypt.checkpw(passwordPlain, storedHash);
        } catch (IllegalArgumentException ex) {
            // Malformed stored hash; treat as authentication failure but log for investigation
            LOGGER.log(Level.WARNING, "Malformed bcrypt hash for user " + username, ex);
            return false;
        }
    }

    /**
     * Register a new user with bcrypt-hashed password.
     * Use cost factor appropriate for your environment (12 is a reasonable default).
     *
     * Note: callers must ensure username uniqueness at application or DB level.
     */
    public static void createUser(String username, String passwordPlain, String role) throws SQLException {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("username required");
        if (passwordPlain == null) throw new IllegalArgumentException("password required");
        if (role == null || role.isBlank()) role = "student";

        final String insertSql = "INSERT INTO users_auth (username, password_hash, role, created_at) VALUES (?, ?, ?, NOW())";
        String hashed = BCrypt.hashpw(passwordPlain, BCrypt.gensalt(12));

        try (Connection c = DatabaseConnection.auth().getConnection();
             PreparedStatement ps = c.prepareStatement(insertSql)) {
            ps.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
            ps.setString(1, username.trim());
            ps.setString(2, hashed);
            ps.setString(3, role.trim());
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Failed to create user " + username, ex);
            throw ex;
        }
    }
}
