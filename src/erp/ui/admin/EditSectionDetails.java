package erp.ui.admin;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedTextField;

import java.awt.*;

public class EditSectionDetails extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color BORDER = new Color(230, 233, 236);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private RoundedTextField courseField, instructorField,dayTimeField, roomField, capacityField, semesterField, yearField;

    private final String sectionID;
    private final String adminName;

    public EditSectionDetails(String adminName, String sectionID) {
        this.adminName = adminName;
        this.sectionID = sectionID;

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
            EditSectionDetails.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            EditSectionDetails.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            EditSectionDetails.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            EditSectionDetails.this.dispose();
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
        nav.add(new NavButton("  🚪  Log Out", false));
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("🏛️ Edit Section " + sectionID);
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main content area ---
        RoundedPanel form = new RoundedPanel(20);
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(32, 40, 32, 40));
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Fields
        courseField   = new RoundedTextField(20, "Enter course code");
        instructorField = new RoundedTextField(20, "Enter instructor name");
        dayTimeField = new RoundedTextField(20, "e.g., Mon Wed Fri 10:00-11:00");
        roomField = new RoundedTextField(20, "Enter room number");  
        capacityField = new RoundedTextField(20, "Enter capacity");
        semesterField = new RoundedTextField(20, "e.g., Fall");
        yearField = new RoundedTextField(20, "e.g., 2024"); 

        addRow(form, gc, 0, "Course Code", courseField);
        addRow(form, gc, 1, "Instructor Name", instructorField);
        addRow(form, gc, 2, "Day & Time", dayTimeField);
        addRow(form, gc, 3, "Room", roomField);
        addRow(form, gc, 4, "Capacity", capacityField);
        addRow(form, gc, 5, "Semester", semesterField);
        addRow(form, gc, 6, "Year", yearField);
        gc.gridy = 7;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.EAST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btns.setOpaque(false);

        RoundedButton cancel = new RoundedButton("Cancel");
        cancel.addActionListener(e -> {
            new EditExistingCourses(adminName).setVisible(true);
            dispose();
        });

        RoundedButton save = new RoundedButton("Save Changes");
        save.addActionListener(e -> saveChanges());

        //button on the left to delete section 
        RoundedButton delete = new RoundedButton("Delete Section");
        delete.addActionListener(e -> {
            // Confirm deletion
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this section?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String query = "DELETE FROM sections WHERE section_id = ?";
                try (Connection conn = DatabaseConnection.erp().getConnection();
                     PreparedStatement ps = conn.prepareStatement(query)) {

                    ps.setString(1, sectionID);
                    ps.executeUpdate();

                    JOptionPane.showMessageDialog(this,
                            "Section deleted successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    new EditExistingCourses(adminName).setVisible(true);
                    dispose();

                } 
                catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Failed to delete section:\n" + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        btns.add(delete);
        btns.add(cancel);
        btns.add(save);
        form.add(btns, gc);
        root.add(form, BorderLayout.CENTER);
        loadSection();
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row,String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row;
        gc.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.semibold(15f));
        p.add(l, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        p.add(field, gc);
    }

    private void loadSection() {
        String query = "SELECT course_id, instructor_id, day_time, room, capacity, semester, year FROM sections WHERE section_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, sectionID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                courseField.setText(rs.getString("course_id"));
                instructorField.setText(rs.getString("instructor_id"));
                dayTimeField.setText(rs.getString("day_time"));
                roomField.setText(rs.getString("room"));
                capacityField.setText(rs.getString("capacity"));
                semesterField.setText(rs.getString("semester"));
                yearField.setText(rs.getString("year"));
            }
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Failed to load course:\n" + e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        String course = courseField.getText().trim();
        String instructor = instructorField.getText().trim();
        String dayTime = dayTimeField.getText().trim();
        String room = roomField.getText().trim();
        String capacitystr = capacityField.getText().trim();
        String semester = semesterField.getText().trim();
        String yearstr = yearField.getText().trim();

        if (course.isEmpty() || instructor.isEmpty() || dayTime.isEmpty() || room.isEmpty() || capacitystr.isEmpty() || semester.isEmpty() || yearstr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearstr);
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Credits must be a number.","Invalid Input",JOptionPane.WARNING_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacitystr);
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Capacity must be a number.","Invalid Input",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "UPDATE sections SET course_id = ?, instructor_id = ?, day_time = ?, room = ?, capacity = ?, semester = ?, year = ? WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, course);
            ps.setString(2, instructor);   
            ps.setString(3, dayTime);
            ps.setString(4, room);
            ps.setInt(5, capacity);
            ps.setString(6, semester);
            ps.setInt(7, year);
            ps.setString(8, sectionID);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Section updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            new EditExistingCourses(adminName).setVisible(true);
            dispose();

        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update section:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
