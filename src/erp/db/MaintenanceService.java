package erp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class MaintenanceService {

    private MaintenanceService() { }

    public static boolean isMaintenanceOn() {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = 'maintenance_mode'";
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String v = rs.getString(1);
                if (v == null) return false;
                v = v.trim();
                return v.equalsIgnoreCase("ON")
                        || v.equalsIgnoreCase("TRUE")
                        || v.equals("1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // fail-open (no table / row → treat as not in maintenance)
        return false;
    }

    public static void setMaintenance(boolean on) throws SQLException {
        String sql = """
            INSERT INTO settings (setting_key, setting_value)
            VALUES ('maintenance_mode', ?)
            ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value)
            """;

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, on ? "ON" : "OFF");
            ps.executeUpdate();
        }
    }
}
