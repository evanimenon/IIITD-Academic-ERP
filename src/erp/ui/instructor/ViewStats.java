package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import erp.ui.common.FontKit;

public class ViewStats extends JFrame {

    // --- Section stats fields ---
    private String sectionId;
    private int numStudents;
    private float avgPercentage;
    private float avgGrade;
    private float medianScore;
    private float highestScore;
    private float lowestScore;

    public ViewStats(String sectionId) {
        this.sectionId = sectionId;

        // TODO: fetch class statistics from DB
        loadMockData(sectionId);

        setTitle("Class Statistics");
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
        addRow(main, gc, 1, "Number of Students:", numStudents);
        addRow(main, gc, 2, "Average Percentage:", avgPercentage + "%");
        addRow(main, gc, 3, "Average Grade:", avgGrade);
        addRow(main, gc, 4, "Median Score:", medianScore);
        addRow(main, gc, 5, "Highest Score:", highestScore);
        addRow(main, gc, 6, "Lowest Score:", lowestScore);

        add(main, BorderLayout.CENTER);
        setVisible(true);
    }

    // optional for testing
    public ViewStats() {
        this("SEC-DEMO");
    }

    // -----------------------
    // Overloaded addRow()
    // -----------------------
    private void addRow(JPanel panel, GridBagConstraints gc, int y, String label, String value) {
        gc.gridx = 0;
        gc.gridy = y;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.bold(18f));
        panel.add(l, gc);

        gc.gridx = 1;
        JLabel v = new JLabel(value);
        v.setFont(FontKit.regular(18f));
        panel.add(v, gc);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int y, String label, int value) {
        addRow(panel, gc, y, label, String.valueOf(value));
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int y, String label, float value) {
        addRow(panel, gc, y, label, String.valueOf(value));
    }

    // -------------------------
    // TEMP MOCK DATA
    // -------------------------
    private void loadMockData(String sectionId) {
        // TODO: Replace with database query later
        this.numStudents = 42;
        this.avgPercentage = 76.5f;
        this.avgGrade = 8.2f;        // out of 10
        this.medianScore = 78.0f;
        this.highestScore = 95.0f;
        this.lowestScore = 42.0f;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ViewStats("SEC101"));
    }
}
