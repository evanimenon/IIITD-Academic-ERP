package erp.ui.instructor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
// import erp.ui.admin.EditExistingCourses;
import erp.ui.auth.LoginPage;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedPanel;

public class ManageComponents extends InstructorFrameBase {

    public static class SectionInfo {
        public int sectionID;
        public String courseID;
        public String dayTime;
        public String semester;
        public int year;
        public String room;
        public int capacity;
    }

    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD     = Color.WHITE;
    private static final Color CARD_HOVER = new Color(242, 248, 247);

    private CardLayout cardLayout;
    private JPanel cardPanel;
    private String displayname;
    private static final String VIEW_SECTIONS = "sections-list";

    public ManageComponents(String instrID, String displayName) {
        super(instrID, displayName, Page.COMPONENTS);
        this.displayname = displayName;
        setTitle("IIITD ERP - Grade Students");

        String dept = getDepartment(instrID);
        if (metaLabel != null) {
            metaLabel.setText("Department: " + dept);
        }
    }

    @Override
    protected JComponent buildMainContent() {
        System.out.println(this.instructorId);
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        cardPanel.add(buildSectionsListView(displayname), VIEW_SECTIONS);
        return cardPanel;
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

    private JComponent buildSectionsListView(String displayName) {
        JPanel root = new JPanel(new BorderLayout());
        root.setOpaque(false);

        // Hero header
        RoundedPanel hero = new RoundedPanel(22);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(22, 26, 22, 26));
        hero.setLayout(new BorderLayout());

        JPanel heroLeft = new JPanel();
        heroLeft.setOpaque(false);
        heroLeft.setLayout(new BoxLayout(heroLeft, BoxLayout.Y_AXIS));

        JLabel h1 = new JLabel("📚  My Sections");
        h1.setFont(FontKit.bold(26f));
        h1.setForeground(Color.WHITE);
        heroLeft.add(h1);

        JLabel sub = new JLabel("View and manage only the sections you teach.");
        sub.setFont(FontKit.regular(14f));
        sub.setForeground(new Color(210, 233, 229));
        heroLeft.add(Box.createVerticalStrut(4));
        heroLeft.add(sub);

        hero.add(heroLeft, BorderLayout.WEST);

        JLabel rightLabel = new JLabel("Logged in as " + userDisplayName);
        rightLabel.setFont(FontKit.regular(13f));
        rightLabel.setForeground(new Color(200, 230, 225));
        hero.add(rightLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // Sections as cards
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 8, 24, 8));

        List<SectionInfo> sections = fetchSectionsForInstructor(this.instructorId);

        if (sections.isEmpty()) {
            JLabel empty = new JLabel("You are not assigned to any sections.");
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(TEXT_600);
            JPanel wrap = new JPanel();
            wrap.setOpaque(false);
            wrap.add(empty);
            content.add(wrap, BorderLayout.CENTER);
        } 
        else {
            JPanel grid = new JPanel();
            grid.setOpaque(false);
            grid.setLayout(new GridLayout(0, 2, 16, 16));

            for (SectionInfo s : sections) {
                grid.add(createSectionCard(s, displayName, this.instructorId));
            }

            JScrollPane sc = new JScrollPane(grid);
            sc.setBorder(null);
            sc.getVerticalScrollBar().setUnitIncrement(16);
            sc.getViewport().setBackground(BG);
            sc.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            content.add(sc, BorderLayout.CENTER);
        }

        root.add(content, BorderLayout.CENTER);
        return root;
    }

    private JComponent createSectionCard(SectionInfo s, String displayName, String instID) {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));
        card.setLayout(new BorderLayout(8, 4));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel badge = new JPanel();
        badge.setPreferredSize(new Dimension(6, 1));
        badge.setMaximumSize(new Dimension(6, Integer.MAX_VALUE));
        badge.setBackground(TEAL);
        card.add(badge, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));

        JLabel course = new JLabel((s.courseID != null ? s.courseID : "Course"));
        course.setFont(FontKit.semibold(16f));
        course.setForeground(TEXT_900);
        text.add(course);

        String line2 = "Section " + s.sectionID;
        if (s.dayTime != null && !s.dayTime.isBlank()) {
            line2 += " • " + s.dayTime;
        }
        JLabel secLabel = new JLabel(line2);
        secLabel.setFont(FontKit.regular(13f));
        secLabel.setForeground(TEXT_600);
        text.add(Box.createVerticalStrut(2));
        text.add(secLabel);

        String meta = "";
        if (s.room != null && !s.room.isBlank()) {
            meta += "Room " + s.room;
        }
        if (s.semester != null && !s.semester.isBlank()) {
            if (!meta.isEmpty())
                meta += " • ";
            meta += s.semester + " " + (s.year != 0 ? s.year : "");
        }

        if (!meta.isEmpty()) {
            JLabel metaLabel = new JLabel(meta);
            metaLabel.setFont(FontKit.regular(12f));
            metaLabel.setForeground(TEXT_600);
            text.add(Box.createVerticalStrut(2));
            text.add(metaLabel);
        }

        card.add(text, BorderLayout.CENTER);

        if (s.capacity > 0) {
            JLabel cap = new JLabel("Capacity: " + s.capacity);
            cap.setFont(FontKit.regular(12f));
            cap.setForeground(TEXT_600);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            bottom.setOpaque(false);
            bottom.add(cap);
            card.add(bottom, BorderLayout.SOUTH);
        }

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBackground(CARD_HOVER);
                card.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBackground(CARD);
                card.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                new EditComponents(instID, s.sectionID, displayName).setVisible(true);
                dispose();
            }
        });

        return card;
    }

    private List<SectionInfo> fetchSectionsForInstructor(String instrId) {
        List<SectionInfo> list = new ArrayList<>();
        if (instrId == null || instrId.isBlank())
            return list;

        String sql = "SELECT section_id, course_id, instructor_id, day_time, room, capacity, semester, year " +
                "FROM sections " +
                "WHERE instructor_id = ? " +
                "ORDER BY course_id, section_id";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(instrId));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    SectionInfo s = new SectionInfo();
                    s.sectionID   = rs.getInt("section_id");
                    s.courseID    = rs.getString("course_id");
                    s.dayTime     = rs.getString("day_time");
                    s.room        = rs.getString("room");
                    s.capacity    = rs.getInt("capacity");
                    s.semester    = rs.getString("semester");
                    s.year        = rs.getInt("year");
                    list.add(s);
                }
            }
        } catch (SQLException | NumberFormatException ex) {
            ex.printStackTrace();
        }

        return list;
    }

}
