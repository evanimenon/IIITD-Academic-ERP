package erp.auth;

public final class AuthContext {
    private static volatile AuthService.Session session;
    private AuthContext() {}

    public static void setSession(AuthService.Session s){ session = s; }
    public static AuthService.Session getSession(){ return session; }
    public static void clear(){ session = null; }
    public static Role getRole() {
        var s = session;
        return s == null ? Role.UNKNOWN : Role.from(s.role());
    }
}
