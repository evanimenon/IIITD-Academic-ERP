package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

public class GradeStudents extends InstructorFrameBase {

    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD     = Color.WHITE;

    public GradeStudents(String instrID, String displayName) {
        super(instrID, displayName, Page.GRADES);
        setTitle("IIITD ERP – Grade Students");

        String dept = getDepartment(instrID);
        if (metaLabel != null) {
            metaLabel.setText("Department: " + dept);
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // ---------- HERO ----------
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout(16, 0));

        JLabel h1 = new JLabel("✒️  Grade Students");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);

        JLabel right = new JLabel("Logged in as " + userDisplayName);
        right.setFont(FontKit.regular(14f));
        right.setForeground(new Color(200, 230, 225));

        hero.add(h1, BorderLayout.WEST);
        hero.add(right, BorderLayout.EAST);

        main.add(hero, BorderLayout.NORTH);

        // ---------- MAIN BODY ----------
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(28, 32, 28, 32));
        card.setLayout(new BorderLayout());

        JLabel title = new JLabel("All grading has moved to My Sections");
        title.setFont(FontKit.semibold(18f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel(
                "<html>To grade students, manage assessments, upload CSVs,<br>" +
                "or view class performance, open <b>My Sections</b> and click on a section card.</html>");
        subtitle.setFont(FontKit.regular(14f));
        subtitle.setForeground(TEXT_600);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.add(title);
        textBox.add(Box.createVerticalStrut(8));
        textBox.add(subtitle);

        card.add(textBox, BorderLayout.NORTH);

        // ---------- ACTION BUTTON ----------
        JButton go = new JButton("Go to My Sections") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(TEAL);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        go.setFocusPainted(false);
        go.setContentAreaFilled(false);
        go.setBorderPainted(false);
        go.setForeground(Color.WHITE);
        go.setFont(FontKit.semibold(15f));
        go.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        go.setBorder(new EmptyBorder(10, 22, 10, 22));

        go.addActionListener(e -> {
            new MySections().setVisible(true);
            SwingUtilities.getWindowAncestor(main).dispose();
        });

        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrap.setOpaque(false);
        btnWrap.add(go);

        card.add(btnWrap, BorderLayout.SOUTH);

        // ---------- WRAP ----------
        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 16, 24, 16));
        body.add(card, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(getBackground());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);
        return main;
    }

    // ---------- DB Helper ----------
    private String getDepartment(String instructorId) {
        if (instructorId == null || instructorId.isBlank()) return "None";

        String dept = "None";
        String sql = "SELECT department FROM instructors WHERE instructor_id = ?";

        try (Connection conn = erp.db.DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, Long.parseLong(instructorId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) dept = rs.getString("department");
            }
        } catch (Exception ignored) {}

        return dept;
    }

}
