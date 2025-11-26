package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;

public class ClassStats extends InstructorFrameBase {

    // Palette for cards/text (frame colors come from InstructorFrameBase)
    private static final Color TEAL      = new Color(28, 122, 120);
    private static final Color BG        = new Color(246, 247, 248);
    private static final Color TEXT_900  = new Color(24, 30, 37);
    private static final Color TEXT_600  = new Color(100, 116, 139);
    private static final Color CARD      = Color.WHITE;

    public class SectionInfo {
        public int sectionID;
        public String courseID;
        public String instructorID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    public ClassStats(String instrID, String displayName) {
        super(instrID, displayName, Page.STATS);
        setTitle("IIITD ERP – Class Stats");

        String dept = getDepartment(this.instructorId);
        if (metaLabel != null) {
            metaLabel.setText("Department: " + dept);
        }
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // ---- header stack: hero + optional maintenance banner ----
        JPanel headerStack = new JPanel();
        headerStack.setOpaque(false);
        headerStack.setLayout(new BoxLayout(headerStack, BoxLayout.Y_AXIS));

        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("📊  Class Stats");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + userDisplayName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        headerStack.add(hero);

        if (Maintenance.isOn()) {
            headerStack.add(Box.createVerticalStrut(12));
            RoundedPanel banner = new RoundedPanel(12);
            banner.setBackground(new Color(255, 235, 230)); // light red
            banner.setBorder(new EmptyBorder(12, 18, 12, 18));

            JLabel msg = new JLabel("⚠️  Maintenance Mode is ON – Changes are disabled");
            msg.setFont(FontKit.semibold(14f));
            msg.setForeground(new Color(180, 60, 50));
            banner.add(msg);

            headerStack.add(banner);
        }

        main.add(headerStack, BorderLayout.NORTH);

        // ---------- MAIN CONTENT ----------
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 8, 24, 8));

        List<SectionInfo> sections = fetchSectionsForInstructor(instructorId);

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            empty.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(empty);
        } else {
            for (SectionInfo sec : sections) {
                RoundedPanel card = new RoundedPanel(20);
                card.setBackground(CARD);
                card.setBorder(new EmptyBorder(20, 24, 20, 24));
                card.setLayout(new BorderLayout());

                // left column
                JPanel left = new JPanel();
                left.setOpaque(false);
                left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

                JLabel title = new JLabel(sec.courseID + " - Section " + String.format("%02d", sec.sectionID));
                title.setFont(FontKit.bold(20f));
                title.setForeground(TEXT_900);

                JLabel subtitle = new JLabel("Room: " + sec.room + " | Time: " + sec.dayTime);
                subtitle.setFont(FontKit.regular(15f));
                subtitle.setForeground(TEXT_600);

                JLabel sem = new JLabel("Semester: " + sec.semester);
                sem.setFont(FontKit.regular(14f));
                sem.setForeground(TEXT_600);

                left.add(title);
                left.add(Box.createVerticalStrut(6));
                left.add(subtitle);
                left.add(Box.createVerticalStrut(4));
                left.add(sem);

                card.add(left, BorderLayout.WEST);

                // right column – action button
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
                actions.setOpaque(false);

                RoundedButton viewBtn = new RoundedButton("View Stats");
                viewBtn.addActionListener(e -> new ViewStats(sec.sectionID).setVisible(true));
                viewBtn.setBackground(TEAL);
                viewBtn.setForeground(Color.WHITE);
                viewBtn.setFont(FontKit.semibold(14f));
                viewBtn.setFocusPainted(false);
                viewBtn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
                actions.add(viewBtn);

                card.add(actions, BorderLayout.EAST);

                content.add(card);
                content.add(Box.createVerticalStrut(16));
            }
        }

        JScrollPane sc = new JScrollPane(content);
        sc.setBorder(null);
        sc.getViewport().setBackground(BG);
        sc.getVerticalScrollBar().setUnitIncrement(16);

        main.add(sc, BorderLayout.CENTER);
        return main;
    }

    // ---------- DB helpers ----------

    private List<SectionInfo> fetchSectionsForInstructor(String instructorId) {
        List<SectionInfo> list = new ArrayList<>();

        String sql = """
            SELECT 
                s.section_id,
                s.course_id,
                s.instructor_id,
                s.day_time,
                s.room,
                s.capacity,
                s.semester,
                s.year
            FROM sections s
            JOIN courses c ON s.course_id = c.course_id
            WHERE s.instructor_id = ?
        """;

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instructorId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SectionInfo si = new SectionInfo();
                    si.sectionID = rs.getInt("section_id");
                    si.courseID = rs.getString("course_id");
                    si.instructorID = instructorId;
                    si.dayTime = rs.getString("day_time");
                    si.room = rs.getString("room");
                    si.capacity = rs.getInt("capacity");
                    si.semester = rs.getString("semester");
                    si.year = rs.getInt("year");

                    list.add(si);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    private String getDepartment(String instructorId) {
        String dept = "None"; // default

        String sql = "SELECT department FROM instructors WHERE instructor_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, Long.parseLong(instructorId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    dept = rs.getString("department");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return dept;
    }
}
