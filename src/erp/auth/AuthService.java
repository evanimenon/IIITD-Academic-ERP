package erp.auth;

import erp.auth.store.AuthDAO;
import erp.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuthService - thin service layer around AuthDAO.
 * - Uses AuthDAO.authenticate(...) to verify plaintext passwords (bcrypt).
 * - Returns a Session containing userId, username and role.
 * - Does not leak whether username exists; returns generic AuthException on failure.
 */

public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    
    public Session login(String username, String password) throws AuthException {
        if (username == null || username.isBlank()) throw new AuthException("Incorrect username or password.");
        if (password == null) throw new AuthException("Incorrect username or password.");
        int wa = getWrongAttempts(username);
        int attempts_left = 5-wa;
        try {
            // Authenticate first (returns boolean). This avoids exposing hash handling.
            boolean ok = AuthDAO.authenticate(username.trim(), password);

            if (!ok) {
                // generic error for wrong creds
                throw new AuthException("Incorrect username or password. " + attempts_left + " attempts left.");
            }

            // Fetch the row to get userId and role for session (row must exist because authenticate succeeded)
            var row = AuthDAO.findByUsername(username.trim());
            if (row == null) {
                // Extremely unlikely (race), but treat as auth failure
                LOGGER.log(Level.WARNING, "Authenticated user not found afterwards: {0}", username);
                throw new AuthException("Incorrect username or password. " + attempts_left + " attempts left.");
            }

            // Best-effort: update last_login; log failures but do not block login
            try {
                AuthDAO.updateLastLogin(row.userId());
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Failed to update last_login for userId=" + row.userId(), ex);
            }

            // Build and return session
            return new Session(row.userId(), row.username(), row.role());

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during login for user=" + username, ex);
            throw new AuthException("Incorrect username or password. " + attempts_left + " attempts left.");
        } 
        catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Unexpected error during login for user=" + username, ex);
            throw new AuthException("Incorrect username or password. " + attempts_left + " attempts left.");
        }
    }

    public static record Session(long userId, String username, String role) {}

    public static class AuthException extends Exception {
        public AuthException(String m) { super(m); }
    }

    private int getWrongAttempts(String usr) {
        String sql = "SELECT failed_attempts FROM users_auth WHERE username = ?";
        try (Connection conn = DatabaseConnection.auth().getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usr);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("failed_attempts");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
