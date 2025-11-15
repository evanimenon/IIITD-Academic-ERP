package erp.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public final class DatabaseConnection {
    private static volatile HikariDataSource authDs;
    private static volatile HikariDataSource erpDs;
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    // Default values (override with properties or env)
    private static final int DEFAULT_MAX_POOL = 10;
    private static final int DEFAULT_MIN_IDLE = 1;
    private static final long DEFAULT_CONN_TIMEOUT = 10_000L; // ms
    private static final long DEFAULT_MAX_LIFETIME = 30 * 60_000L; // 30 min
    private static final long DEFAULT_LEAK_DETECTION_THRESHOLD = 5_000L; // ms (dev)

    private DatabaseConnection() {}

    /**
     * Initialize Hikari pools using db.properties (on classpath) or environment variables as fallback.
     * Safe to call multiple times; initialization is idempotent.
     */
    public static void init() {
        if (initialized.get()) return; // already initialized
        synchronized (DatabaseConnection.class) {
            if (initialized.get()) return;

            Properties props = new Properties();
            // Try to load /db.properties from resources first
            try (InputStream in = DatabaseConnection.class.getResourceAsStream("/db.properties")) {
                if (in != null) {
                    props.load(in);
                }
            } catch (IOException e) {
                // ignore - will fall back to environment
                System.err.println("Warning: couldn't load /db.properties: " + e.getMessage());
            }

            // auth DB config (properties -> env -> default)
            String authJdbc = firstNonNull(
                    props.getProperty("auth.jdbcUrl"),
                    System.getenv("AUTH_JDBC_URL"),
                    System.getenv("AUTH_JDBC")
            );
            String authUser = firstNonNull(
                    props.getProperty("auth.username"),
                    System.getenv("AUTH_DB_USER")
            );
            String authPass = firstNonNull(
                    props.getProperty("auth.password"),
                    System.getenv("AUTH_DB_PASS")
            );

            // erp DB config
            String erpJdbc = firstNonNull(
                    props.getProperty("erp.jdbcUrl"),
                    System.getenv("ERP_JDBC_URL"),
                    System.getenv("ERP_JDBC")
            );
            String erpUser = firstNonNull(
                    props.getProperty("erp.username"),
                    System.getenv("ERP_DB_USER")
            );
            String erpPass = firstNonNull(
                    props.getProperty("erp.password"),
                    System.getenv("ERP_DB_PASS")
            );

            if (authJdbc == null || authUser == null || authPass == null) {
                System.err.println("Warning: auth DB credentials incomplete - auth pool will not be started.");
            } else {
                HikariConfig authCfg = new HikariConfig();
                authCfg.setJdbcUrl(authJdbc);
                authCfg.setUsername(authUser);
                authCfg.setPassword(authPass);
                authCfg.setMaximumPoolSize(getInt(props, "auth.maxPool", DEFAULT_MAX_POOL));
                authCfg.setMinimumIdle(getInt(props, "auth.minIdle", DEFAULT_MIN_IDLE));
                authCfg.setConnectionTimeout(getLong(props, "auth.connTimeout", DEFAULT_CONN_TIMEOUT));
                authCfg.setMaxLifetime(getLong(props, "auth.maxLifetime", DEFAULT_MAX_LIFETIME));
                authCfg.setLeakDetectionThreshold(getLong(props, "auth.leakDetectionMs", DEFAULT_LEAK_DETECTION_THRESHOLD));
                // Optional: set pool name for easier metrics
                authCfg.setPoolName("erp-auth-pool");
                authDs = new HikariDataSource(authCfg);
            }

            if (erpJdbc == null || erpUser == null || erpPass == null) {
                System.err.println("Warning: erp DB credentials incomplete - erp pool will not be started.");
            } else {
                HikariConfig erpCfg = new HikariConfig();
                erpCfg.setJdbcUrl(erpJdbc);
                erpCfg.setUsername(erpUser);
                erpCfg.setPassword(erpPass);
                erpCfg.setMaximumPoolSize(getInt(props, "erp.maxPool", DEFAULT_MAX_POOL));
                erpCfg.setMinimumIdle(getInt(props, "erp.minIdle", DEFAULT_MIN_IDLE));
                erpCfg.setConnectionTimeout(getLong(props, "erp.connTimeout", DEFAULT_CONN_TIMEOUT));
                erpCfg.setMaxLifetime(getLong(props, "erp.maxLifetime", DEFAULT_MAX_LIFETIME));
                erpCfg.setLeakDetectionThreshold(getLong(props, "erp.leakDetectionMs", DEFAULT_LEAK_DETECTION_THRESHOLD));
                erpCfg.setPoolName("erp-main-pool");
                erpDs = new HikariDataSource(erpCfg);
            }

            // Register shutdown hook to close pools on JVM exit
            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseConnection::close, "db-shutdown-hook"));
            initialized.set(true);
        }
    }

    public static DataSource auth() {
        if (!initialized.get()) init();
        return authDs;
    }

    public static DataSource erp() {
        if (!initialized.get()) init();
        return erpDs;
    }

    /** Close pools cleanly. Safe to call multiple times. */
    public static void close() {
        synchronized (DatabaseConnection.class) {
            if (authDs != null) {
                try { authDs.close(); } catch (Exception e) { System.err.println("Failed to close authDs: " + e.getMessage()); }
                authDs = null;
            }
            if (erpDs != null) {
                try { erpDs.close(); } catch (Exception e) { System.err.println("Failed to close erpDs: " + e.getMessage()); }
                erpDs = null;
            }
            initialized.set(false);
        }
    }

    // ---------------- helpers ----------------
    private static String firstNonNull(String... candidates) {
        for (String c : candidates) {
            if (c != null && !c.isBlank()) return c;
        }
        return null;
    }

    private static int getInt(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.trim()); } catch (NumberFormatException ignored) { return def; }
    }
    private static long getLong(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try { return Long.parseLong(v.trim()); } catch (NumberFormatException ignored) { return def; }
    }
}
