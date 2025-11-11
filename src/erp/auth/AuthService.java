package erp.auth;

import erp.auth.hash.PasswordHasher;
import erp.auth.store.AuthDAO;

public class AuthService {
    private final AuthDAO dao = new AuthDAO();

    public Session login(String username, String password) throws Exception {
        var row = dao.findByUsername(username);
        if (row == null) throw new AuthException("Incorrect username or password.");
        if (!PasswordHasher.verify(password, row.hash()))
            throw new AuthException("Incorrect username or password.");

        dao.updateLastLogin(row.userId());
        return new Session(row.userId(), row.username(), row.role());
    }

    public static record Session(long userId, String username, String role) {}
    public static class AuthException extends Exception { public AuthException(String m){super(m);} }
}
