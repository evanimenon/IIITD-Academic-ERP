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
        String overall = null;

        final String sql =
                "SELECT component, score, final_grade " +
                "FROM erp_db.grades " +
                "WHERE enrollment_id = ? " +
                "ORDER BY component";

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String comp = rs.getString("component");
                    Double score = rs.getObject("score") == null ? null : rs.getDouble("score");
                    String fg = rs.getString("final_grade");

                    if (fg != null && !fg.isBlank()) {
                        overall = fg; // last non-null wins; you can refine if needed
                    }

                    model.addRow(new Object[] {
                            comp,
                            score == null ? "" : score,
                            fg == null ? "" : fg
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading grades:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        if (overall == null) overall = "Not yet set";
        finalGradeLabel.setText("Final course grade: " + overall);
    }

    public static void showForEnrollment(Frame owner, int enrollmentId) {
        GradesDialog dlg = new GradesDialog(owner, enrollmentId);
        dlg.setVisible(true);
    }
}
