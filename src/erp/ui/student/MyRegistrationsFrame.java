package erp.ui.student;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

// PDF (example: OpenPDF / iText-style imports; add the JAR to lib/)
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class MyRegistrationsFrame extends StudentFrameBase {

    private JPanel listPanel;

    public MyRegistrationsFrame(String studentId, String userDisplayName) {
        super(studentId, userDisplayName, Page.REGISTRATIONS);
        setTitle("IIITD ERP – My Courses");
    }

    @Override
    protected JComponent buildMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(8, 8, 8, 8));

        // ---- Top bar: Title + Transcript button ----
        JLabel title = new JLabel("My Courses");
        title.setFont(FontKit.bold(22f));
        title.setForeground(new Color(24, 30, 37));

        RoundedButton transcriptBtn = new RoundedButton("Download Transcript (PDF)");
        transcriptBtn.setFont(FontKit.semibold(13f));
        transcriptBtn.setMargin(new Insets(6, 12, 6, 12));
        transcriptBtn.addActionListener(this::handleDownloadTranscript);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.add(title, BorderLayout.WEST);
        topBar.add(transcriptBtn, BorderLayout.EAST);

        main.add(topBar, BorderLayout.NORTH);

        // ---- Vertical list of cards ----
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(new EmptyBorder(12, 0, 12, 0));

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        main.add(scroll, BorderLayout.CENTER);

        loadRegistrations();

        return main;
    }

    private void loadRegistrations() {

        listPanel.removeAll();

        if (this.studentId == null || this.studentId.isBlank()) {
            JOptionPane.showMessageDialog(
                    this,
                    "No student id available in MyRegistrationsFrame.",
                    "Debug",
                    JOptionPane.WARNING_MESSAGE);
            listPanel.revalidate();
            listPanel.repaint();
            return;
        }

        final String sql = "SELECT e.enrollment_id, e.status, e.final_grade, " +
                "       c.course_id, c.code, c.title, c.credits, " +
                "       s.section_id, s.day_time, s.room, s.semester, s.year, " +
                "       i.instructor_name " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s ON s.section_id = e.section_id " +
                "JOIN   erp_db.courses c ON c.course_id = s.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  e.student_id = ? " +
                "  AND  e.status = 'REGISTERED' " +
                "ORDER BY s.year, s.semester, c.course_id";

        int count = 0;

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, this.studentId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int enrollmentId = rs.getInt("enrollment_id");
                    String courseId = rs.getString("course_id");
                    String code = rs.getString("code");
                    String title = rs.getString("title");
                    int credits = rs.getInt("credits");
                    int sectionId = rs.getInt("section_id");
                    String instructor = rs.getString("instructor_name");
                    String dayTime = rs.getString("day_time");
                    String room = rs.getString("room");
                    String semester = rs.getString("semester");
                    int year = rs.getInt("year");
                    String finalGrade = rs.getString("final_grade");
                    String status = rs.getString("status");

                    // Load component rows for this enrollment/section
                    List<ComponentRow> components = fetchComponentRows(conn, sectionId, enrollmentId);

                    JPanel card = createCourseCard(
                            enrollmentId,
                            courseId,
                            code,
                            title,
                            credits,
                            instructor,
                            dayTime,
                            room,
                            semester,
                            year,
                            finalGrade,
                            status,
                            components);

                    listPanel.add(card);
                    listPanel.add(Box.createVerticalStrut(12));
                    count++;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading registrations:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        if (count == 0) {
            JLabel empty = new JLabel("No registrations found for student: " + this.studentId);
            empty.setFont(FontKit.regular(14f));
            empty.setForeground(new Color(100, 116, 139));
            empty.setBorder(new EmptyBorder(16, 8, 0, 0));
            listPanel.add(empty);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Component row for the breakdown: name, score, weight.
     */
    private static class ComponentRow {
        final String name;
        final Double score;  // may be null
        final Double weight; // may be null

        ComponentRow(String name, Double score, Double weight) {
            this.name = name;
            this.score = score;
            this.weight = weight;
        }
    }

    /**
     * Small progress-bar cell for the component table.
     * Uses score/weight to compute a percentage and color (red / yellow / green).
     */
    private static class ProgressBarCell extends JComponent {
        private final Double score;
        private final Double weight;

        ProgressBarCell(Double score, Double weight) {
            this.score = score;
            this.weight = weight;
            setPreferredSize(new Dimension(120, 14));
            setMinimumSize(new Dimension(80, 14));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int trackH = Math.max(6, h - 4);
            int y = (h - trackH) / 2;
            int arc = trackH;

            // track
            g2.setColor(new Color(229, 231, 235)); // light grey
            g2.fillRoundRect(0, y, w, trackH, arc, arc);

            double pct = 0.0;
            if (score != null && weight != null && weight > 0) {
                pct = Math.max(0.0, Math.min(1.0, score / weight));
            } else {
                // no data → leave empty
                g2.dispose();
                return;
            }

            int filled = (int) Math.round(w * pct);
            if (filled <= 0) {
                g2.dispose();
                return;
            }

            // color: 0–40% red, 40–70% yellow, 70–100% green
            Color fill;
            if (pct < 0.4) {
                fill = new Color(239, 68, 68);   // red
            } else if (pct < 0.7) {
                fill = new Color(234, 179, 8);  // yellow
            } else {
                fill = new Color(34, 197, 94);  // green
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, y, filled, trackH, arc, arc);

            g2.dispose();
        }
    }

    /**
     * Fetch component_name, score, weight for one enrollment in a given section.
     *
     * section_components: (id, section_id, component_name, weight)
     * grades: (enrollment_id, component_id, score)
     */
    private List<ComponentRow> fetchComponentRows(Connection conn, int sectionId, int enrollmentId) {
        final String sql = "SELECT sc.component_name, sc.weight, g.score " +
                "FROM   erp_db.section_components sc " +
                "LEFT JOIN erp_db.grades g " +
                "       ON g.component_id = sc.id " +
                "      AND g.enrollment_id = ? " +
                "WHERE  sc.section_id = ? " +
                "ORDER BY sc.id";

        List<ComponentRow> rows = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setInt(2, sectionId);


            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("component_name");
                    Double weight = rs.getObject("weight") == null ? null : rs.getDouble("weight");
                    Double score = rs.getObject("score") == null ? null : rs.getDouble("score");

                    if (name == null || name.isBlank()) {
                        name = "Component";
                    }

                    System.out.println("   [ROW] " + name +
                            " weight=" + weight + " score=" + score);

                    rows.add(new ComponentRow(name, score, weight));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return rows;
    }

    /**
     * Builds a single “card” for a registered course, with inline component table.
     * Functionality is unchanged; only layout/styling is updated.
     */
    private JPanel createCourseCard(
            int enrollmentId,
            String courseId,
            String code,
            String title,
            int credits,
            String instructor,
            String dayTime,
            String room,
            String semester,
            int year,
            String finalGrade,
            String status,
            List<ComponentRow> components) {

        RoundedPanel card = new RoundedPanel(18);
        card.setLayout(new BorderLayout(12, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));

        // ==== TOP AREA: course + section info + grade badge ====
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        // Left block: course, section, term
        JPanel infoLeft = new JPanel();
        infoLeft.setOpaque(false);
        infoLeft.setLayout(new BoxLayout(infoLeft, BoxLayout.Y_AXIS));

        JLabel titleLbl = new JLabel(code + " – " + title);
        titleLbl.setFont(FontKit.semibold(16f));
        titleLbl.setForeground(new Color(24, 30, 37));

        String metaLine = "Course ID " + courseId + " • " + credits + " credits";
        JLabel meta = new JLabel(metaLine);
        meta.setFont(FontKit.regular(12f));
        meta.setForeground(new Color(100, 116, 139));

        String instructorLine = (instructor == null || instructor.isBlank())
                ? "Instructor: TBA"
                : "Instructor: " + instructor;
        JLabel instr = new JLabel(instructorLine);
        instr.setFont(FontKit.regular(12f));
        instr.setForeground(new Color(100, 116, 139));

        String scheduleLine = (dayTime == null || dayTime.isBlank() ? "Time TBA" : dayTime)
                + (room == null || room.isBlank() ? "" : " • Room " + room);
        JLabel schedule = new JLabel(scheduleLine);
        schedule.setFont(FontKit.regular(12f));
        schedule.setForeground(new Color(100, 116, 139));

        String termLine = semester + " " + year;
        JLabel term = new JLabel(termLine);
        term.setFont(FontKit.regular(12f));
        term.setForeground(new Color(148, 163, 184));

        infoLeft.add(titleLbl);
        infoLeft.add(Box.createVerticalStrut(2));
        infoLeft.add(meta);
        infoLeft.add(Box.createVerticalStrut(2));
        infoLeft.add(instr);
        infoLeft.add(Box.createVerticalStrut(2));
        infoLeft.add(schedule);
        infoLeft.add(Box.createVerticalStrut(2));
        infoLeft.add(term);

        top.add(infoLeft, BorderLayout.CENTER);

        // Right: final grade badge
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setAlignmentY(Component.TOP_ALIGNMENT);

        JLabel gradeBadge;
        if (finalGrade != null && !finalGrade.isBlank()) {
            gradeBadge = new JLabel("Final Grade: " + finalGrade);
            gradeBadge.setFont(FontKit.semibold(13f));
            gradeBadge.setForeground(new Color(30, 64, 175));
        } else {
            gradeBadge = new JLabel("Final Grade: –");
            gradeBadge.setFont(FontKit.regular(13f));
            gradeBadge.setForeground(new Color(148, 163, 184));
        }
        gradeBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        right.add(Box.createVerticalStrut(4));
        right.add(gradeBadge);
        right.add(Box.createVerticalGlue());

        top.add(right, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);

        // ==== BOTTOM: component breakdown styled like a compact table ====
        if (components != null && !components.isEmpty()) {

            JPanel compWrapper = new JPanel(new BorderLayout());
            compWrapper.setOpaque(false);
            compWrapper.setBorder(new EmptyBorder(10, 4, 4, 4));

            RoundedPanel tablePanel = new RoundedPanel(12);
            tablePanel.setOpaque(true);
            tablePanel.setBackground(new Color(248, 250, 252)); // light grey
            tablePanel.setLayout(new GridBagLayout());
            tablePanel.setBorder(new EmptyBorder(8, 12, 8, 12));

            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(4, 8, 4, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.anchor = GridBagConstraints.CENTER;

            int row = 0;

            // Header row: Component | Score / Weight | Progress
            gc.gridy = row;

            gc.gridx = 0;
            gc.weightx = 0.4;
            JLabel h1 = new JLabel("Component");
            h1.setFont(FontKit.semibold(12f));
            h1.setForeground(new Color(55, 65, 81));
            tablePanel.add(h1, gc);

            gc.gridx = 1;
            gc.weightx = 0.25;
            JLabel h2 = new JLabel("Score / Weight");
            h2.setFont(FontKit.semibold(12f));
            h2.setForeground(new Color(55, 65, 81));
            tablePanel.add(h2, gc);

            gc.gridx = 2;
            gc.weightx = 0.35;
            JLabel h3 = new JLabel("Progress");
            h3.setFont(FontKit.semibold(12f));
            h3.setForeground(new Color(55, 65, 81));
            h3.setHorizontalAlignment(SwingConstants.RIGHT);
            tablePanel.add(h3, gc);

            row++;

            // Data rows
            for (ComponentRow cr : components) {
                gc.gridy = row;

                // Column 0: component name
                gc.gridx = 0;
                gc.weightx = 0.4;
                JLabel nameLbl = new JLabel(cr.name);
                nameLbl.setFont(FontKit.regular(12f));
                nameLbl.setForeground(new Color(75, 85, 99));
                tablePanel.add(nameLbl, gc);

                // Column 1: score / weight text
                gc.gridx = 1;
                gc.weightx = 0.25;
                String scorePart = (cr.score == null) ? "–" : String.valueOf(cr.score);
                String weightPart = (cr.weight == null) ? "?" : String.valueOf(cr.weight);
                JLabel scoreLbl = new JLabel(scorePart + " / " + weightPart);
                scoreLbl.setFont(FontKit.regular(12f));
                scoreLbl.setForeground(new Color(75, 85, 99));
                tablePanel.add(scoreLbl, gc);

                // Column 2: progress bar (visual)
                gc.gridx = 2;
                gc.weightx = 0.35;
                ProgressBarCell bar = new ProgressBarCell(cr.score, cr.weight);
                JPanel barHolder = new JPanel(new BorderLayout());
                barHolder.setOpaque(false);
                barHolder.add(bar, BorderLayout.CENTER);
                tablePanel.add(barHolder, gc);

                row++;
            }

            compWrapper.add(tablePanel, BorderLayout.CENTER);
            card.add(compWrapper, BorderLayout.CENTER);
        }

        // Hover effect (subtle, same behaviour)
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(new Color(248, 250, 252));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(Color.WHITE);
            }
        });

        return card;
    }

    // --------------------------------------------------------------------
    // Transcript download – unchanged functionality
    // --------------------------------------------------------------------

    private void handleDownloadTranscript(ActionEvent evt) {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transcript_" + studentId + ".pdf"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = chooser.getSelectedFile();
        try (Connection conn = DatabaseConnection.erp().getConnection()) {
            generateTranscriptPdf(conn, file);
            JOptionPane.showMessageDialog(
                    this,
                    "Transcript saved to:\n" + file.getAbsolutePath(),
                    "Transcript",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to generate transcript:\n" + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateTranscriptPdf(Connection conn, File file) throws Exception {

        final String sql = "SELECT e.enrollment_id, e.final_grade, " +
                "       c.course_id, c.code, c.title, c.credits, " +
                "       s.section_id, s.semester, s.year, " +
                "       i.instructor_name " +
                "FROM   erp_db.enrollments e " +
                "JOIN   erp_db.sections s ON s.section_id = e.section_id " +
                "JOIN   erp_db.courses c ON c.course_id = s.course_id " +
                "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
                "WHERE  e.student_id = ? " +
                "  AND  e.status = 'REGISTERED' " +
                "ORDER BY s.year, s.semester, c.course_id";

        Document doc = new Document();
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            PdfWriter.getInstance(doc, fos);
            doc.open();

            // Header
            doc.add(new Paragraph("Unofficial Transcript"));
            doc.add(new Paragraph("Student: " + userDisplayName + " (" + studentId + ")"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph(" "));

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, this.studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int enrollmentId = rs.getInt("enrollment_id");
                        int sectionId = rs.getInt("section_id");

                        String courseId = rs.getString("course_id");
                        String code = rs.getString("code");
                        String title = rs.getString("title");
                        int credits = rs.getInt("credits");
                        String semester = rs.getString("semester");
                        int year = rs.getInt("year");
                        String instructor = rs.getString("instructor_name");
                        String finalGrade = rs.getString("final_grade");

                        // Course heading
                        doc.add(new Paragraph(code + " – " + title));
                        doc.add(new Paragraph(
                                "Course ID: " + courseId +
                                        " | Credits: " + credits +
                                        " | Term: " + semester + " " + year));
                        if (instructor != null && !instructor.isBlank()) {
                            doc.add(new Paragraph("Instructor: " + instructor));
                        }
                        doc.add(new Paragraph("Final Grade: " +
                                (finalGrade == null || finalGrade.isBlank() ? "–" : finalGrade)));
                        doc.add(new Paragraph(" "));

                        // Component table
                        PdfPTable table = new PdfPTable(2);
                        table.setWidthPercentage(100);

                        table.addCell(new PdfPCell(new Paragraph("Component")));
                        table.addCell(new PdfPCell(new Paragraph("Score / Weight")));

                        List<ComponentRow> comps = fetchComponentRows(conn, sectionId, enrollmentId);

                        for (ComponentRow cr : comps) {
                            table.addCell(new Paragraph(cr.name));
                            String scorePart = (cr.score == null) ? "–" : String.valueOf(cr.score);
                            String weightPart = (cr.weight == null) ? "?" : String.valueOf(cr.weight);
                            table.addCell(new Paragraph(scorePart + " / " + weightPart));
                        }

                        doc.add(table);
                        doc.add(new Paragraph(" "));
                        doc.add(new Paragraph(" "));
                    }
                }
            }

        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }

}
