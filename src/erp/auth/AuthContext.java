package erp.auth;

public final class AuthContext {
    private static volatile AuthService.Session session;

    private AuthContext() {
    }

    public static void setSession(AuthService.Session s) {
        session = s;
    }

    public static AuthService.Session getSession() {
        return session;
    }

    public static void clear() {
        session = null;
    }

    public static Role getRole() {
        var s = session;
        return s == null ? Role.UNKNOWN : Role.from(s.role());
    }

    // --- NEW HELPERS ---

    public static Integer getUserId() {
        var s = session;
        if (s == null)
            return null;

        long raw = s.userId();
        return (int) raw;
    }

    public static String getUsername() {
        var s = session;
        return (s == null) ? null : s.username();
    }

    public static boolean isLoggedIn() {
        return session != null;
    }
}
