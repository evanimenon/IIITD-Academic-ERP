package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.common.*;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;

public class EditComponents extends InstructorFrameBase {

    public static class SectionInfo {
        public int sectionID;
        public String courseID;
        public String instructorID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private String displayName;
    private int sectionID;
    private final List<RoundedTextField[]> fields = new ArrayList<>();
    private final List<RoundedTextField[]> newFields = new ArrayList<>();
    private List<String[]> existing;
    private RoundedPanel form; 
    private JPanel formRows;

    public EditComponents(String instrID, int sectionID, String displayName) {
        super(instrID, displayName, Page.COMPONENTS);
        this.sectionID = sectionID;
        JComponent mainContent = buildMainContent();
        setContentPane(mainContent);
        revalidate();
        repaint();

    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel();
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(32, 40, 32, 40));
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        root.add(new JScrollPane(main), BorderLayout.CENTER);

        // ---------- SECTION TITLE ----------
        SectionInfo section = getSectionDetails(sectionID);
        String courseName = getCourseName(section.courseID);

        JLabel title = new JLabel(courseName + " (" + section.courseID + ")");
        title.setFont(FontKit.bold(22f));
        title.setForeground(TEXT_900);
        main.add(title);

        JLabel sectionmeta = new JLabel(
            "Semester: " + section.semester + " " + section.year +
            " | Schedule: " + section.dayTime +
            " | Room: " + section.room +
            " | Capacity: " + section.capacity
        );
        sectionmeta.setFont(FontKit.regular(15f));
        sectionmeta.setForeground(TEXT_600);
        main.add(sectionmeta);

        // Divider
        main.add(Box.createVerticalStrut(16));
        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(60, 120, 116));
        sep2.setMaximumSize(new Dimension(1500, 1));
        main.add(sep2);
        main.add(Box.createVerticalStrut(24));

        // ---------- EXISTING COMPONENTS ----------
        existing = getComponents(sectionID);

        RoundedPanel existingCard = new RoundedPanel(20);
        existingCard.setBackground(Color.WHITE);
        existingCard.setLayout(new BorderLayout());
        existingCard.setBorder(new EmptyBorder(24, 28, 28, 28));

        JLabel existingTitle = new JLabel("Current Components");
        existingTitle.setOpaque(true);
        existingTitle.setBackground(TEAL_DARK);
        existingTitle.setForeground(Color.WHITE);
        existingTitle.setFont(FontKit.semibold(18f));
        existingTitle.setBorder(new EmptyBorder(10, 14, 10, 14));
        existingCard.add(existingTitle, BorderLayout.NORTH);

        main.add(Box.createVerticalStrut(16));

        // Fields
        JPanel existingRows = new JPanel(new GridBagLayout());
        existingRows.setOpaque(false);
        existingCard.add(existingRows, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 10, 12, 10);
        gc.anchor = GridBagConstraints.WEST;

        int rowNum = 0;
        for (String[] comp : existing) {
            RoundedTextField compField = new RoundedTextField(20, "Enter component name");
            RoundedTextField weightField = new RoundedTextField(20, "Enter weight (%)");
            RoundedTextField[] row = new RoundedTextField[]{compField, weightField};
            fields.add(row);

            gc.gridy = rowNum;
            gc.gridx = 0;
            existingRows.add(compField, gc);

            gc.gridx = 1;
            existingRows.add(weightField, gc);

            RoundedButton deleteBtn = new RoundedButton("Remove");
            gc.gridx = 2;
            existingRows.add(deleteBtn, gc);

            loadcomponent(comp, compField, weightField);

            final String compName = comp[0];
            deleteBtn.addActionListener(e -> {
                fields.remove(row);
                existingRows.remove(compField);
                existingRows.remove(weightField);
                existingRows.remove(deleteBtn);
                existingRows.revalidate();
                existingRows.repaint();

                deletecomponent(sectionID, compName);
            });

            rowNum++;
        }



        gc.gridy = 0;
        gc.gridx = 2;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.EAST;

        main.add(existingCard);
        main.add(Box.createVerticalStrut(32));

        // ---------- ADD COMPONENT FORM ----------
        form = new RoundedPanel(18);
        form.setBackground(Color.WHITE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        formRows = new JPanel();
        formRows.setOpaque(false);
        formRows.setLayout(new BoxLayout(formRows, BoxLayout.Y_AXIS));
        form.add(formRows);
        form.setVisible(false);

        JPanel componentActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        componentActions.setOpaque(false);

        RoundedButton addBtn = new RoundedButton(" + Add Component ");
        addBtn.setBackground(TEAL_DARK);
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> addRow(formRows));

        componentActions.add(addBtn);
        main.add(form);
        main.add(Box.createVerticalStrut(16));
        main.add(componentActions);
        main.add(Box.createVerticalStrut(32));

        // Divider
        JSeparator sep3 = new JSeparator();
        sep3.setForeground(new Color(60, 120, 116));
        sep3.setMaximumSize(new Dimension(1500, 1));
        main.add(sep3);
        main.add(Box.createVerticalStrut(24));

        // ---------- ACTION BUTTONS ----------
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        actions.setOpaque(false);

        RoundedButton cancel = new RoundedButton("Cancel");
        cancel.addActionListener(e -> {
            new ManageComponents(section.instructorID, displayName).setVisible(true);
            dispose();
        });
        actions.add(cancel);

        RoundedButton save = new RoundedButton("Save Changes");
        save.setBackground(TEAL_DARK);
        save.setForeground(Color.WHITE);
        save.addActionListener(e -> saveAll(section.instructorID));
        actions.add(save);

        main.add(actions);
        main.add(Box.createVerticalStrut(32));

        root.add(main, BorderLayout.CENTER);

        // ---------- MAINTENANCE BANNER ----------
        if (Maintenance.isOn()) {
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230));
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️ Maintenance Mode is ON - Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));

            banner.add(msg);
            root.add(banner, BorderLayout.NORTH);
        }
        return main;

    }

    private void loadcomponent(String[] comp, RoundedTextField component, RoundedTextField weight) {
        component.setText(comp[0]);
        weight.setText(comp[1]);
    }

    private void addRow(JPanel formRows) {
        if (!form.isVisible()) {
            form.setVisible(true);
        }

        RoundedTextField comp = new RoundedTextField(20, "Component Name");
        RoundedTextField weight = new RoundedTextField(10, "Weight (%)");

        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.add(comp);
        row.add(weight);

        newFields.add(new RoundedTextField[]{comp, weight}); // keep new fields separate from existing
        formRows.add(row);
        formRows.revalidate();
        formRows.repaint();
    }


    private void deletecomponent(int sectionId, String componentName) {
        String sql = "DELETE FROM section_components WHERE section_id = ? AND component_name = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            stmt.setString(2, componentName);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Component removed!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } 
            else {
                JOptionPane.showMessageDialog(this, "Component not found.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing component", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAll(String instrID) {

    // Gather all fields (existing + new)
    List<RoundedTextField[]> allFields = new ArrayList<>();
        allFields.addAll(fields);     // existing
        allFields.addAll(newFields);  // newly added

        // Delete all existing components for this section
        String deleteSQL = "DELETE FROM section_components WHERE section_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSQL)) {
            deleteStmt.setInt(1, sectionID);
            deleteStmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Insert everything freshly
        String insertSQL = "INSERT INTO section_components (section_id, component_name, weight) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement insertStmt = conn.prepareStatement(insertSQL)) {

            for (RoundedTextField[] row : allFields) {
                String comp = row[0].getText().trim();
                String wStr = row[1].getText().trim();
                if (comp.isEmpty() || wStr.isEmpty()) continue;

                float weight = Float.parseFloat(wStr);
                insertStmt.setInt(1, sectionID);
                insertStmt.setString(2, comp);
                insertStmt.setFloat(3, weight);
                insertStmt.addBatch();
            }

            insertStmt.executeBatch();
            JOptionPane.showMessageDialog(this, "Changes saved!", "Success", JOptionPane.INFORMATION_MESSAGE);

            new EditComponents(instrID, sectionID, displayName).setVisible(true);
            dispose();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving", "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    List<String[]> getComponents(int sectionId) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT component_name, weight FROM section_components WHERE section_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                        rs.getString("component_name"),
                        String.valueOf(rs.getFloat("weight"))
                    });
                }
            }
        } 
        catch (SQLException ex) { 
            ex.printStackTrace(); 
        }
        return list;
    }

    SectionInfo getSectionDetails(int sectionId) {
        SectionInfo section = new SectionInfo();
        String sql = "SELECT * FROM sections WHERE section_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    section.sectionID = rs.getInt("section_id");
                    section.courseID = rs.getString("course_id");
                    section.instructorID = rs.getString("instructor_id");
                    section.dayTime = rs.getString("day_time");
                    section.semester = rs.getString("semester");
                    section.year = rs.getInt("year");
                    section.room = rs.getString("room");
                    section.capacity = rs.getInt("capacity");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return section;
    }

    String getCourseName(String courseId) {
        String name = "Unknown Course";
        String sql = "SELECT title FROM courses WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, courseId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    name = rs.getString("title");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return name;
    }

}
