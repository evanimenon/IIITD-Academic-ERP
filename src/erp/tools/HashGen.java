package erp.tools;
import erp.auth.hash.PasswordHasher;

public class HashGen {
    public static void main(String[] args) {
        System.out.println("admin@123 -> " + PasswordHasher.hash("admin@123"));
        System.out.println("inst@123  -> " + PasswordHasher.hash("inst@123"));
        System.out.println("stud@123  -> " + PasswordHasher.hash("stud@123"));
    }
}
