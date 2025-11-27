package erp.tools;

import erp.db.DatabaseConnection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;

public class ImportTableCsv {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java ... erp.tools.ImportTableCsv <table_name> <path/to/file.csv>");
            System.exit(1);
        }

        String table = args[0];
        Path csv = Path.of(args[1]);

        if (!Files.exists(csv)) {
            System.err.println("File not found: " + csv.toAbsolutePath());
            System.exit(2);
        }

        DatabaseConnection.init();

        try (Connection c = DatabaseConnection.erp().getConnection()) {
            c.setAutoCommit(false);

            try (BufferedReader br = new BufferedReader(new FileReader(csv.toFile()))) {
                String header = br.readLine();
                if (header == null || header.isBlank()) {
                    throw new IllegalArgumentException("Empty CSV: " + csv);
                }

                // column names from header
                String[] cols = Arrays.stream(header.split(",", -1))
                        .map(String::trim)
                        .toArray(String[]::new);

                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ").append(table).append(" (");
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(cols[i]);
                }
                sb.append(") VALUES (");
                for (int i = 0; i < cols.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("?");
                }
                sb.append(")");

                String sql = sb.toString();
                System.out.println("Using SQL: " + sql);

                int ok = 0;
                String line;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    while ((line = br.readLine()) != null) {
                        if (line.isBlank()) continue;

                        String[] f = Arrays.stream(line.split(",", -1))
                                .map(String::trim)
                                .toArray(String[]::new);

                        if (f.length != cols.length) {
                            f = Arrays.copyOf(f, cols.length);
                        }

                        for (int i = 0; i < cols.length; i++) {
                            ps.setString(i + 1, f[i]);
                        }

                        ps.addBatch();
                        ok++;
                    }
                    ps.executeBatch();
                }

                c.commit();
                System.out.println("Imported " + ok + " rows into " + table + " from " + csv.getFileName());
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        }
    }
}
