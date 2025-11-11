package erp.auth.hash;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {
    // Hash at account creation / change-password time
    public static String hash(String plain) {
        // Work factor 10–12 is typical for desktop; adjust to your machine
        return BCrypt.hashpw(plain, BCrypt.gensalt(12));
    }

    // Verify at login
    public static boolean verify(String plain, String hash) {
        return BCrypt.checkpw(plain, hash);
    }
}
