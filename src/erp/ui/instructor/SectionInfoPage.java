package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import erp.ui.common.FontKit;

public class SectionInfoPage extends JFrame {

    // --- Section fields ---
    private String sectionId;
    private String courseId;
    private String instructorId;
    private String dayTime;
    private String room;
    private String capacity;
    private String semester;
    private String year;
    
    public SectionInfoPage(String sectionId) {
        this.sectionId = sectionId;

        // TODO: fetch section details from DB using sectionId
        loadMockData(sectionId);  // temporary

        setTitle("Section Details");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridBagLayout());
        main.setBorder(new EmptyBorder(30, 40, 30, 40));
        main.setBackground(Color.WHITE);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 10, 12, 10);
        gc.anchor = GridBagConstraints.WEST;

        addRow(main, gc, 0, "Section ID:", sectionId);
        addRow(main, gc, 1, "Course ID:", courseId);
        addRow(main, gc, 2, "Instructor ID:", instructorId);
        addRow(main, gc, 3, "Day & Time:", dayTime);
        addRow(main, gc, 4, "Room:", room);
        addRow(main, gc, 5, "Capacity:", capacity);
        addRow(main, gc, 6, "Semester:", semester);
        addRow(main, gc, 7, "Year:", year);

        add(main, BorderLayout.CENTER);
        setVisible(true);
    }

    // -----------------------------------
    // OPTIONAL: No-arg constructor (IDE/testing only)
    // -----------------------------------
    public SectionInfoPage() {
        this("SEC-DEMO");  // default placeholder
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int y, String label, String value) {
        gc.gridx = 0;
        gc.gridy = y;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.bold(18f));
        panel.add(l, gc);

        gc.gridx = 1;
        JLabel v = new JLabel(value == null ? "—" : value);
        v.setFont(FontKit.regular(18f));
        panel.add(v, gc);
    }

    // -------------------------
    // TEMP MOCK DATA FOR TESTING
    // -------------------------
    private void loadMockData(String sectionId) {

        // Later:
        // TODO: Query DB using passed sectionId
        // SELECT * FROM sections WHERE section_id = ?
        this.sectionId = sectionId;
        this.courseId = "CSE201";
        this.instructorId = "I1023";
        this.dayTime = "Mon/Wed 2:00 PM – 3:30 PM";
        this.room = "LH-301";
        this.capacity = "60";
        this.semester = "Monsoon";
        this.year = "2025";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SectionInfoPage("SEC101"));
    }
}
