package erp.auth.hash;
import java.nio.file.*;
import java.util.*;

public class GenerateHashedCSV {
    public static void main(String[] args) throws Exception {

        System.out.println("Reading input CSV...");

        String input = "C:\\Users\\mroutray\\Documents\\IIITD\\CSE201-AP\\Project\\IIITD-Academic-ERP\\data\\users_auth.csv";
        String output = "C:\\Users\\mroutray\\Documents\\IIITD\\CSE201-AP\\Project\\IIITD-Academic-ERP\\data\\users_auth_hashed.csv";

        List<String> lines = Files.readAllLines(Paths.get(input));
        System.out.println("Total rows read: " + lines.size());

        List<String> out = new ArrayList<>();
        out.add(lines.get(0)); // Header

        System.out.println("Hashing passwords...");

        for (int i = 1; i < lines.size(); i++) {
            String row = lines.get(i);
            String[] parts = row.split(",", -1);

            if (parts.length < 6) {
                System.out.println("Bad row detected: " + row);
                throw new RuntimeException("Bad row: " + row);
            }

            String id = parts[0];
            String username = parts[1];
            String role = parts[2];
            String plainPassword = parts[3];
            String status = parts[4];
            String lastLogin = parts[5];

            System.out.println("Hashing password for user: " + username);

            String hashed = PasswordHasher.hash(plainPassword);

            out.add(String.join(",",
                    id,
                    username,
                    role,
                    hashed,
                    status,
                    lastLogin
            ));
        }

        System.out.println("Saving hashed CSV...");
        Files.write(Paths.get(output), out);

        System.out.println("DONE! Hashed file created:");
        System.out.println(output);
    }
}
