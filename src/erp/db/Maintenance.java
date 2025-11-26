package erp.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Maintenance {

    public static boolean isOn(){
        String sql = "SELECT setting_value from settings where setting_key = 'Maintenance'";
        boolean maintenance = false;

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    maintenance = rs.getBoolean("setting_value");
                }
            }

        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return maintenance;
    }

    
    public static void turnOn() {
        String sql = "UPDATE settings SET setting_value = true WHERE setting_key = 'Maintenance'";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void turnOff() {
        String sql = "UPDATE settings SET setting_value = false WHERE setting_key = 'Maintenance'";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

}