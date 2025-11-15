package erp.auth;

public enum Role {
    STUDENT, INSTRUCTOR, ADMIN, UNKNOWN;

    public static Role from(String s) {
        if (s == null) return UNKNOWN;
        return switch (s.trim().toLowerCase()) {
            case "student", "stu", "s" -> STUDENT;
            case "instructor", "inst", "faculty", "teacher" -> INSTRUCTOR;
            case "admin", "administrator", "admins" -> ADMIN;
            default -> UNKNOWN;
        };
    }
}
