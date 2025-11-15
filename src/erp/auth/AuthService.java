package erp.auth;

import erp.auth.store.AuthDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());

    // Dummy bcrypt hash used to equalize timing when user not found (cost=10)
    private static final String DUMMY_BCRYPT_HASH = "$2a$10$7EqJtq98hPqEX7fNZaFWoOaF8jg1/3g3I7y8wYkq4a2B1K8P6mG9e";

    public static record Session(long userId, String username, String role) {}

    public static class AuthException extends Exception {
        public AuthException(String m) { super(m); }
    }

    /**
     * Attempt to log in a user with username + plaintext password.
     */
    public Session login(String username, String password) throws AuthException {
        if (username == null || username.isBlank()) throw new AuthException("Incorrect username or password.");
        if (password == null) throw new AuthException("Incorrect username or password.");

        try {
            var row = AuthDAO.findByUsername(username.trim());

            if (row == null) {
                // Dummy bcrypt check to make timing similar for missing user vs bad password
                try { BCrypt.checkpw(password, DUMMY_BCRYPT_HASH); } catch (Exception ignored) {}
                throw new AuthException("Incorrect username or password.");
            }

            String storedHash = row.hash();
            if (storedHash == null || storedHash.isBlank()) {
                LOGGER.log(Level.WARNING, "User {0} has empty/NULL password_hash in DB", username);
                try { BCrypt.checkpw(password, DUMMY_BCRYPT_HASH); } catch (Exception ignored) {}
                throw new AuthException("Incorrect username or password.");
            }

            // Log only non-sensitive metadata (FINE)
            try {
                String prefix = storedHash.length() >= 4 ? storedHash.substring(0, 4) : storedHash;
                LOGGER.log(Level.FINE, "Auth verify for user={0}, hashLen={1}, prefix={2}",
                        new Object[]{username, storedHash.length(), prefix});
            } catch (Exception ignored) {}

            boolean ok;
            try {
                ok = BCrypt.checkpw(password, storedHash);
            } catch (IllegalArgumentException ex) {
                // Malformed hash (not bcrypt) — log and treat as failure
                LOGGER.log(Level.WARNING, "Malformed password hash for user=" + username, ex);
                ok = false;
            }

            if (!ok) {
                throw new AuthException("Incorrect username or password.");
            }

            // Successful login — update last_login (best-effort)
            try {
                AuthDAO.updateLastLogin(row.userId());
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Failed to update last_login for userId=" + row.userId(), ex);
            }

            String normalizedRole = row.role() == null ? "" : row.role().trim().toLowerCase(Locale.ROOT);
            return new Session(row.userId(), row.username(), normalizedRole);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Database error during login for user=" + username, ex);
            throw new AuthException("Login error. Please try again later.");
        }
    }
}
