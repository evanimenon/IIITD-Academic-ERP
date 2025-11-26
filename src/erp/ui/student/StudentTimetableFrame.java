// StudentTimetableFrame.java
package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class StudentTimetableFrame extends StudentFrameBase {

    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD_BG  = Color.WHITE;

    private JPanel gridPanel;   // main timetable grid

    // Columns
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"
    };

    // Rows (time slots) – taken from your real data
    private static final String[] TIME_SLOTS = {
            "08:00-09:00",
            "09:00-10:30",
            "10:45-12:15",
            "11:00-12:30",
            "13:00-14:00",
            "14:00-15:30",
            "16:00-17:30"
    };

    public StudentTimetableFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.TIMETABLE);
        setTitle("IIITD ERP – Time Table");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel title = new JLabel("Class Schedule");
        title.setFont(FontKit.bold(20f));
        title.setForeground(TEXT_900);
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        main.add(title, BorderLayout.NORTH);

        // local map: time slot -> (day -> cell panel)
        Map<String, Map<String, JPanel>> cellMap = new LinkedHashMap<>();

        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;

        // ===== HEADER ROW =====
        gbc.gridy = 0;
        gbc.weighty = 0;

        // top-left empty cell
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel corner = new JLabel("");
        gridPanel.add(corner, gbc);

        // day headers
        for (int d = 0; d < DAYS.length; d++) {
            gbc.gridx = d + 1;
            gbc.weightx = 1;
            JLabel dayLabel = new JLabel(DAYS[d], SwingConstants.CENTER);
            dayLabel.setFont(FontKit.semibold(13f));
            dayLabel.setForeground(TEXT_900);
            gridPanel.add(dayLabel, gbc);
        }

        // ===== TIME ROWS =====
        for (int r = 0; r < TIME_SLOTS.length; r++) {
            String slot = TIME_SLOTS[r];
            gbc.gridy = r + 1;

            // time label at column 0
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.weighty = 1.0 / TIME_SLOTS.length;
            JLabel timeLabel = new JLabel(slot);
            timeLabel.setFont(FontKit.regular(12f));
            timeLabel.setForeground(TEXT_600);
            gridPanel.add(timeLabel, gbc);

            // cells for each day
            Map<String, JPanel> dayToCell = new LinkedHashMap<>();
            for (int d = 0; d < DAYS.length; d++) {
                gbc.gridx = d + 1;
                gbc.weightx = 1;

                JPanel cell = new JPanel();
                cell.setOpaque(false);
                cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
                cell.setBorder(new EmptyBorder(2, 2, 2, 2));

                gridPanel.add(cell, gbc);
                dayToCell.put(DAYS[d], cell);
            }

            cellMap.put(slot, dayToCell);
        }

        main.add(gridPanel, BorderLayout.CENTER);

        // load data into the grid
        loadTimetable(cellMap);

        return main;
    }

    private void loadTimetable(Map<String, Map<String, JPanel>> cellMap) {
        System.out.println("[DEBUG] TimetableFrame loading for studentId = " + studentId);

        // Clear old content
        for (Map<String, JPanel> row : cellMap.values()) {
            for (JPanel cell : row.values()) {
                cell.removeAll();
            }
        }

        String sql =
                "SELECT c.title, c.code, s.day_time, s.room " +
                "FROM erp_db.enrollments e " +
                "JOIN erp_db.sections s ON s.section_id = e.section_id " +
                "JOIN erp_db.courses c ON c.course_id = s.course_id " +
                "WHERE e.student_id = ? AND e.status = 'REGISTERED'";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String title   = rs.getString("title");  // full name
                    String code    = rs.getString("code");   // short acronym
                    String dayTime = rs.getString("day_time"); // e.g. "Mon 09:00-10:30"
                    String room    = rs.getString("room");

                    if (dayTime == null) continue;
                    String[] parts = dayTime.trim().split("\\s+");
                    if (parts.length < 2) continue;

                    String dayToken  = parts[0];        // "Mon"
                    String slotToken = parts[1];        // "09:00-10:30"

                    String dayFull = mapDay(dayToken);  // "Monday"
                    Map<String, JPanel> row = cellMap.get(slotToken);
                    if (row == null) {
                        // time slot not in our fixed list, skip
                        continue;
                    }

                    JPanel cell = row.get(dayFull);
                    if (cell == null) {
                        continue;
                    }

                    cell.add(buildClassCard(code, title, room));
                    cell.add(Box.createVerticalStrut(4));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading timetable:\n" + ex.getMessage(),
                    "Timetable Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JComponent buildClassCard(String code, String title, String room) {
        erp.ui.common.RoundedPanel card = new erp.ui.common.RoundedPanel(12);
        card.setBackground(CARD_BG);
        card.setBorder(new EmptyBorder(6, 8, 6, 8));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // show ONLY course code in the cell
        JLabel codeLbl = new JLabel(code != null ? code : "COURSE");
        codeLbl.setFont(FontKit.semibold(12f));
        codeLbl.setForeground(TEXT_900);

        String roomText = (room == null || room.isBlank()) ? "Room: TBA" : room;
        JLabel roomLbl = new JLabel(roomText);
        roomLbl.setFont(FontKit.regular(11f));
        roomLbl.setForeground(TEXT_600);

        // tooltip = full title
        if (title != null && !title.isBlank()) {
            card.setToolTipText(title);
        }

        card.add(codeLbl);
        card.add(Box.createVerticalStrut(2));
        card.add(roomLbl);

        return card;
    }

    private String mapDay(String token) {
        if (token == null) return "Monday";
        String t = token.toLowerCase();

        if (t.startsWith("mon")) return "Monday";
        if (t.startsWith("tue")) return "Tuesday";
        if (t.startsWith("wed")) return "Wednesday";
        if (t.startsWith("thu")) return "Thursday";
        if (t.startsWith("fri")) return "Friday";
        if (t.startsWith("sat")) return "Saturday";
        if (t.startsWith("sun")) return "Sunday";

        return "Monday";
    }
}
