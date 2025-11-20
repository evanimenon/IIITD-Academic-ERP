package erp.auth.hash;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import erp.auth.hash.PasswordHasher;

public class GenerateHashedCSV {
    public static void main(String[] args) throws Exception {

        String input = "users_auth.csv";      // your CSV
        String output = "users_auth_hashed.csv";    // new CSV with hashed passwords

        List<String> lines = Files.readAllLines(Paths.get(input));
        List<String> out = new ArrayList<>();

        // copy header
        out.add(lines.get(0));

        for (int i = 1; i < lines.size(); i++) {
            String row = lines.get(i);
            String[] parts = row.split(",", -1);

            if (parts.length < 6) {
                throw new RuntimeException("Bad row: " + row);
            }

            String id = parts[0];
            String username = parts[1];
            String role = parts[2];
            String plainPassword = parts[3];  // "temp123"
            String status = parts[4];
            String lastLogin = parts[5];

            // use YOUR PasswordHasher
            String hashed = PasswordHasher.hash(plainPassword);

            // rebuild row
            out.add(String.join(",",
                    id,
                    username,
                    role,
                    hashed,
                    status,
                    lastLogin
            ));
        }

        Files.write(Paths.get(output), out);
        System.out.println("Hashed CSV created: " + output);
    }
}
