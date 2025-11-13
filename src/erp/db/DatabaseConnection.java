package erp.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;

import javax.sql.DataSource;


public final class DatabaseConnection {
    private static HikariDataSource authDs;
    private static HikariDataSource erpDs;

    public static void init() {
        HikariConfig authCfg = new HikariConfig();
        authCfg.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db");
        authCfg.setJdbcUrl("jdbc:mysql://localhost:3306/auth_db?serverTimezone=Asia/Kolkata");

        authCfg.setUsername("auth_user");
        authCfg.setPassword("secret");
        authCfg.setMaximumPoolSize(5);
        authDs = new HikariDataSource(authCfg);

        HikariConfig erpCfg = new HikariConfig();
        erpCfg.setJdbcUrl("jdbc:mysql://localhost:3306/erp_db");
        erpCfg.setJdbcUrl("jdbc:mysql://localhost:3306/erp_db?serverTimezone=Asia/Kolkata");

        erpCfg.setUsername("erp_user");
        erpCfg.setPassword("secret");
        erpCfg.setMaximumPoolSize(5);
        erpDs = new HikariDataSource(erpCfg);
    }

    public static DataSource auth() { return authDs; }
    public static DataSource erp()  { return erpDs; }



}
