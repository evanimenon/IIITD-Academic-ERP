package erp.db;

public class Maintenance {
    private static boolean maintenanceOn = false;
    public static boolean isOn() { 
        return maintenanceOn; 
    }
    public static void turnOn() {
        maintenanceOn = true; 
    }
    public static void turnOff() {
        maintenanceOn = false; 
    }
}