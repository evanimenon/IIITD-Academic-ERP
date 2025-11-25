package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import java.util.List;

public class ViewStats extends JFrame {
    
    private int sectionId;
    private int numStudents;
    private float avgGrade;
    private float medianScore;
    private float highestScore;
    private float lowestScore;

    public ViewStats(int sectionId) {
        this.sectionId = sectionId;
        // loadData(this.sectionId);

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
        List<Float> grades = getAllFinalGrades();

        numStudents = getnumStudents();
        avgGrade = getAvgGrade(grades);
        medianScore = getMedian(grades);
        highestScore = getHighest(grades);
        lowestScore = getLowest(grades);

        addRow(main, gc, 0, "Section ID:", sectionId);
        addRow(main, gc, 1, "Number of Students:", numStudents);
        addRow(main, gc, 3, "Average Grade:", avgGrade + "%");
        addRow(main, gc, 4, "Median Score:", medianScore);
        addRow(main, gc, 5, "Highest Score:", highestScore);
        addRow(main, gc, 6, "Lowest Score:", lowestScore);

        add(main, BorderLayout.CENTER);
        setVisible(true);
    }

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

    int getnumStudents() {
        int num = 0;
        String sql = "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sectionId); 
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    num = rs.getInt(1); 
                }
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
        }
        return num;
    }

    private List<Float> getAllFinalGrades() {
        List<Float> grades = new ArrayList<>();
        
        // Select only the final_grade for the current section, ordered ascendingly.
        String sql = "SELECT final_grade FROM enrollments WHERE section_id = ? AND final_grade IS NOT NULL ORDER BY final_grade ASC";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sectionId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    float grade = rs.getFloat("final_grade");
                    grades.add(grade);
                }
            }
        } 
        catch (SQLException ex) {
            ex.printStackTrace();
            // Return empty list on error
            return new ArrayList<>(); 
        }
        return grades;
    }

    float getAvgGrade(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0.0f;
        }

        double sum = 0.0;
        for (double grade : grades) {
            sum += grade;
        }

        // Calculate the average and cast to float for the return type
        return (float) (sum / grades.size());
    }

    float getMedian(List<Float> grades) {
        int count = grades.size();

        if (count == 0) {
            return 0.0f;
        }

        // If the count is odd, return the middle element.
        if (count % 2 != 0) {
            return grades.get(count / 2);
        } 

        else {
            double middle1 = grades.get(count / 2 - 1);
            double middle2 = grades.get(count / 2);
            return (float) ((middle1 + middle2) / 2.0);
        }
    }

    float getLowest(List<Float> grades) {
        if (grades.isEmpty()) {
            return 0.0f;
        }
        // Since the list is sorted, the first element is the lowest grade.
        return grades.get(0);
    }

    float getHighest(List<Float> grades) {
        int count = grades.size();
        
        if (count == 0) {
            return 0.0f;
        }

        // Since the list is sorted, the last element is the highest grade.
        return (float) grades.get(count - 1);
    }

    private void loadData(){

    }
}
