package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.db.DatabaseConnection;
import erp.ui.common.*;

public class AddSection extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color BORDER = new Color(230, 233, 236);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private final String courseId;
    private final String adminName;

    public AddSection(String adminName, String courseId) {
        this.adminName = adminName;
        this.courseId = courseId;

        setTitle("IIITD ERP – Assign Instructor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile block (Top part of sidebar)
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        EmptyBorder br = new EmptyBorder(8, 8, 32, 8);
        profile.setBorder(br);

        // Circular Avatar with rounded corner panel
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                // Draw a circle
                g2.fillOval(0, 0, getWidth(), getHeight()); 
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(adminName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        JLabel meta = new JLabel("Year, Program");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);
        
        // Rounded corners for the entire profile block (visual style enhancement)
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_LIGHT, 1),
            new EmptyBorder(8, 8, 32, 8)
        ));

        sidebar.add(profile, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Navigation Links
        NavButton dashboardBtn = new NavButton("🏠 Home", false);
        dashboardBtn.addActionListener(e -> {
            new AdminDashboard(adminName).setVisible(true);
            AddSection.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            AddSection.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            AddSection.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            AddSection.this.dispose();
        });
        nav.add(assignInstBtn);
        nav.add(Box.createVerticalStrut(8));

        
        // Separator
        nav.add(new JSeparator() {{ 
            setForeground(new Color(60, 120, 116)); 
            setBackground(new Color(60, 120, 116));
            setMaximumSize(new Dimension(240, 1));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }});
        nav.add(Box.createVerticalStrut(40));
        nav.add(new NavButton("  ⚙️  Settings", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🚪  Log Out", false)); // Used door emoji for log out
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("✒️ Adding a Section for Course " + courseId);
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main form ---
        RoundedPanel formCard = new RoundedPanel(20);
        formCard.setBackground(CARD);
        formCard.setBorder(new EmptyBorder(40, 60, 60, 60));
        formCard.setLayout(new GridBagLayout());
        root.add(formCard, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; 
        gbc.gridy = 0;


        // ID
        formCard.add(label("Section ID:"), gbc);

        gbc.gridx = 1;
        RoundedTextField idField = new RoundedTextField(30, "Enter Section ID");
        idField.setFont(FontKit.regular(15f));
        formCard.add(idField, gbc);

        // INSTRUCTOR ID
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Instructor ID:"), gbc);

        gbc.gridx = 1;
        RoundedTextField instrField = new RoundedTextField(30, "Enter Instructor ID (Leave empty if TBA)");
        instrField.setFont(FontKit.regular(15f));
        formCard.add(instrField, gbc);

        // DAY
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Day(s):"), gbc);

        gbc.gridx = 1;
        RoundedTextField dayField = new RoundedTextField(30, "Enter Day(s) (e.g., Mon Wed Fri)");
        dayField.setFont(FontKit.regular(15f));
        formCard.add(dayField, gbc);

        //TIME
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Time:"), gbc);
        gbc.gridx = 1;
        RoundedTextField timeField = new RoundedTextField(30, "Enter Time (e.g., 10:00-11:30)");
        timeField.setFont(FontKit.regular(15f));
        formCard.add(timeField, gbc);

        // ROOM
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Room:"), gbc);
        gbc.gridx = 1;
        RoundedTextField roomField = new RoundedTextField(30, "Enter Room Number (e.g., A101)");
        roomField.setFont(FontKit.regular(15f));
        formCard.add(roomField, gbc);

        // CAPACITY
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Capacity:"), gbc);
        gbc.gridx = 1;
        RoundedTextField capacityField = new RoundedTextField(30, "Enter Capacity (e.g., 60)");
        capacityField.setFont(FontKit.regular(15f));   
        formCard.add(capacityField, gbc);

        // SEMESTER
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Semester:"), gbc);

        gbc.gridx = 1;
        String[] sem = { "Monsoon", "Winter", "Summer" };
        RoundedComboBox<String> creditBox = new RoundedComboBox<>(sem);
        creditBox.setFont(FontKit.regular(15f));
        formCard.add(creditBox, gbc);

        // YEAR
        gbc.gridx = 0; gbc.gridy++;
        formCard.add(label("Year:"), gbc);
        gbc.gridx = 1;
        String[] years = { "2025", "2026" };
        RoundedComboBox<String> yearBox = new RoundedComboBox<>(years);
        yearBox.setFont(FontKit.regular(15f));
        formCard.add(yearBox, gbc);

        // ADD BUTTON
        gbc.gridx = 0; gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        RoundedButton submitBtn = new RoundedButton("Add Section");
        submitBtn.setFont(FontKit.bold(16f));
        submitBtn.setBackground(TEAL_DARK);
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(new EmptyBorder(10, 30, 10, 30));
        submitBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        submitBtn.addActionListener(e -> {
            String sectionID = idField.getText().trim();
            String courseID = this.courseId;
            String instrId = instrField.getText().trim();
            String days = dayField.getText().trim();  
            String time = timeField.getText().trim();
            String dayTime = days + " " + time;  
            String room = roomField.getText().trim();
            int capacity = Integer.parseInt(capacityField.getText().trim());
            String semester = (String) creditBox.getSelectedItem();
            int year = Integer.parseInt((String) yearBox.getSelectedItem());

            //check that all fields are filled
            if (sectionID.isEmpty() || courseID.isEmpty() || days.isEmpty() || time.isEmpty() || room.isEmpty() || capacity <= 0 || instrId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields in correct format.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String errorMessage = addSectionSQL(sectionID, instrId, days, time, dayTime, room, capacity, semester, year);

            if (errorMessage != null) {
                JOptionPane.showMessageDialog(this,errorMessage,"Error",JOptionPane.ERROR_MESSAGE);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Section added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                idField.setText("");
                instrField.setText("");
                dayField.setText("");
                timeField.setText("");
                roomField.setText("");
                capacityField.setText("");
            }
        });

        formCard.add(submitBtn, gbc);
    }

    private String addSectionSQL(String sectionID, String instrId, String days, String time, String dayTime, String room, int capacity, String semester, int year) {
        try (Connection erpConn = DatabaseConnection.erp().getConnection();) {

            // Check ERP duplicates
            try (PreparedStatement checkStmt = erpConn.prepareStatement("SELECT section_id FROM sections WHERE section_id = ?")) {
                checkStmt.setString(1, sectionID);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Section ID already exists
                        String errmsg = "Section ID already exists. Please choose a different Section ID.";
                        return errmsg;
                    }
                }
            }

            //check that instructor exists
            try (PreparedStatement checkInstrStmt = erpConn.prepareStatement("SELECT instructor_id FROM instructors WHERE instructor_id = ?")) {
                checkInstrStmt.setString(1, instrId);
                try (ResultSet rs = checkInstrStmt.executeQuery()) {
                    if (!rs.next()) {
                        // Instructor ID does not exist
                        String errmsg = "Instructor ID does not exist. Please enter a valid Instructor ID.";
                        return errmsg;
                    }
                }
            }

            //check that course exists
            try (PreparedStatement checkCourseStmt = erpConn.prepareStatement("SELECT course_id FROM courses WHERE course_id = ?")) {
                checkCourseStmt.setString(1, courseId);
                try (ResultSet rs = checkCourseStmt.executeQuery()) {
                    if (!rs.next()) {
                        // Course ID does not exist
                        String errmsg = "Course ID does not exist. Please enter a valid Course ID.";
                        return errmsg;
                    }
                }
            }

            //check that days and time are in correct format
            if (!days.matches("^(Mon|Tue|Wed|Thu|Fri|Sat|Sun)( (Mon|Tue|Wed|Thu|Fri|Sat|Sun))*$")) {
                String errmsg = "Days format is incorrect. Please use format like 'Mon Wed Fri'.";
                return errmsg;
            }
            if (!time.matches("^([01]?\\d|2[0-3]):[0-5]\\d-([01]?\\d|2[0-3]):[0-5]\\d$")) {
                String errmsg = "Time format is incorrect. Please use 24-hour format like '10:00-11:30'.";
                return errmsg;
            }

            // Check for room conflicts on ANY overlapping day+time
            try (PreparedStatement st = erpConn.prepareStatement("SELECT section_id, day_time FROM sections WHERE room = ? AND semester = ? AND year = ?")) {
                st.setString(1, room);
                st.setString(2, semester);
                st.setInt(3, year);

                try (ResultSet rs = st.executeQuery()) {
                    String[] newDays = days.split(" ");
                    int[] newTime = parseTimeRange(time);

                    while (rs.next()) {
                        String existingDayTime = rs.getString("day_time");

                        String[] existingDays = extractDays(existingDayTime);
                        int[] existingTime = parseTimeRange(extractTime(existingDayTime));

                        // day intersections
                        boolean dayOverlap = false;
                        for (String d1 : newDays) {
                            for (String d2 : existingDays) {
                                if (d1.equals(d2)) {
                                    dayOverlap = true;
                                    break;
                                }
                            }
                            if (dayOverlap) break;
                        }

                        if (!dayOverlap) continue;

                        // if day intersections exist then only we will check time intersections
                        boolean timeOverlap =
                            (newTime[0] < existingTime[1]) &&
                            (newTime[1] > existingTime[0]);

                        if (timeOverlap) {
                            return "Room conflict: This room is already booked on overlapping days or times.";
                        }
                    }
                }
            }

            // Insert ERP
            try (PreparedStatement insertStmt = erpConn.prepareStatement("INSERT INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                insertStmt.setString(1, sectionID);
                insertStmt.setString(2, courseId);
                insertStmt.setString(3, instrId);
                insertStmt.setString(4,  dayTime);
                insertStmt.setString(5, room);
                insertStmt.setInt(6, capacity);
                insertStmt.setString(7, semester);
                insertStmt.setInt(8, year);
                insertStmt.executeUpdate();
            }
            return null;

        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            String errmsg = "SQL Error: " + ex.getMessage();
            return errmsg;
        }
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FontKit.semibold(16f));
        l.setForeground(TEXT_900);
        return l;
    }

    private int[] parseTimeRange(String timeRange) {
        String[] parts = timeRange.split("-");
        int start = Integer.parseInt(parts[0].replace(":", ""));
        int end = Integer.parseInt(parts[1].replace(":", ""));
        return new int[]{start, end};
    }

    private String extractTime(String dayTime) {
        String[] parts = dayTime.split(" ");
        return parts[parts.length - 1]; // Last token = time range
    }

    private String[] extractDays(String dayTime) {
        String[] parts = dayTime.split(" ");
        String[] days = new String[parts.length - 1];
        System.arraycopy(parts, 0, days, 0, parts.length - 1);
        return days;
    }
}
