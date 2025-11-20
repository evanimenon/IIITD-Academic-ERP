package erp.auth;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {}

    public static String hashPassword(String plain) {
        if (plain == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return BCrypt.hashpw(plain, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean verifyPassword(String plain, String hash) {
        if (plain == null || hash == null) return false;
        return BCrypt.checkpw(plain, hash);
    }
}
