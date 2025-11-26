package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GradesDialog extends JDialog {

    private final int enrollmentId;
    private DefaultTableModel model;
    private JLabel finalGradeLabel;

    private GradesDialog(Frame owner, int enrollmentId) {
        super(owner, "Grades", true);
        this.enrollmentId = enrollmentId;

        FontKit.init();
        setSize(520, 360);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(root);

        JLabel title = new JLabel("Grades for Enrollment #" + enrollmentId);
        title.setFont(FontKit.semibold(16f));
        root.add(title, BorderLayout.NORTH);

        String[] cols = {"Component", "Score", "Final Grade (if set)"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(FontKit.regular(13f));

        JScrollPane sp = new JScrollPane(table);
        root.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);

        finalGradeLabel = new JLabel("Final course grade: –");
        finalGradeLabel.setFont(FontKit.semibold(14f));
        finalGradeLabel.setBorder(new EmptyBorder(4, 0, 0, 0));
        bottom.add(finalGradeLabel, BorderLayout.WEST);

        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        bottom.add(close, BorderLayout.EAST);

        root.add(bottom, BorderLayout.SOUTH);

        loadGrades();
    }

    private void loadGrades() {
        model.setRowCount(0);

        final String gradeSql =
            "SELECT g.component_id, g.score " +
            "FROM erp_db.grades g " +
            "WHERE g.enrollment_id = ? " +
            "ORDER BY g.component_id";

        final String overallSql =
            "SELECT final_grade FROM erp_db.enrollments WHERE enrollment_id = ?";

        try (Connection c = DatabaseConnection.erp().getConnection()) {

            // load component scores
            try (PreparedStatement ps = c.prepareStatement(gradeSql)) {
                ps.setInt(1, enrollmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int compId = rs.getInt("component_id");
                        Double score = rs.getObject("score") == null ? null : rs.getDouble("score");

                        model.addRow(new Object[] {
                            getComponentName(compId),
                            score == null ? "" : score
                        });
                    }
                }
            }

            // load overall course grade
            String overall = null;
            try (PreparedStatement ps2 = c.prepareStatement(overallSql)) {
                ps2.setInt(1, enrollmentId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next())
                        overall = rs2.getString("final_grade");
                }
            }

            if (overall == null || overall.isEmpty())
                overall = "Not yet set";

            finalGradeLabel.setText("Final course grade: " + overall);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                this,
                "Error loading grades:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private String getComponentName(int compID) {
        final String sql = "SELECT component_name FROM section_components WHERE component_id = ?";
        String compName = "NA";

        try (Connection conn = DatabaseConnection.erp().getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, compID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    compName = rs.getString("component_name");
                }
            }

        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return compName;
    }


    public static void showForEnrollment(Frame owner, int enrollmentId) {
        GradesDialog dlg = new GradesDialog(owner, enrollmentId);
        dlg.setVisible(true);
    }
}
