package erp.ui.student;

import erp.db.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// CSV
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

// PDF (OpenPDF)
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class TranscriptExporter {

    private static final String TRANSCRIPT_SQL =
            "SELECT e.enrollment_id, e.status, " +
            "       c.course_id, c.code, c.title, c.credits, " +
            "       s.semester, s.year, " +
            "       COALESCE(MAX(g.final_grade), '') AS final_grade " +
            "FROM   erp_db.enrollments e " +
            "JOIN   erp_db.sections s ON e.section_id = s.section_id " +
            "JOIN   erp_db.courses c  ON s.course_id = c.course_id " +
            "LEFT JOIN erp_db.grades g ON g.enrollment_id = e.enrollment_id " +
            "WHERE  e.student_id = ? " +
            "GROUP BY e.enrollment_id, e.status, c.course_id, c.code, c.title, " +
            "         c.credits, s.semester, s.year " +
            "ORDER BY s.year, s.semester, c.course_id";

    public static void exportTranscriptCsv(Component parent, String studentId) {
        if (studentId == null || studentId.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Student ID not available.",
                    "Transcript CSV", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transcript_" + studentId + ".csv"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        Path path = chooser.getSelectedFile().toPath();

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(TRANSCRIPT_SQL)) {

            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery();
                 Writer writer = Files.newBufferedWriter(path);
                 CSVPrinter printer = new CSVPrinter(writer,
                         CSVFormat.DEFAULT.withHeader(
                                 "EnrollmentID", "Status", "CourseID", "Code",
                                 "Title", "Credits", "Semester", "Year", "FinalGrade"
                         ))) {

                while (rs.next()) {
                    printer.printRecord(
                            rs.getInt("enrollment_id"),
                            rs.getString("status"),
                            rs.getString("course_id"),
                            rs.getString("code"),
                            rs.getString("title"),
                            rs.getInt("credits"),
                            rs.getString("semester"),
                            rs.getInt("year"),
                            rs.getString("final_grade")
                    );
                }
            }
            JOptionPane.showMessageDialog(parent,
                    "Transcript CSV saved to:\n" + path,
                    "Transcript CSV",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Failed to export CSV: " + ex.getMessage(),
                    "Transcript CSV",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void exportTranscriptPdf(Component parent, String studentId) {
        if (studentId == null || studentId.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Student ID not available.",
                    "Transcript PDF", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("transcript_" + studentId + ".pdf"));
        int result = chooser.showSaveDialog(parent);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();

        try (Connection c = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = c.prepareStatement(TRANSCRIPT_SQL);
             FileOutputStream fos = new FileOutputStream(file)) {

            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            Document doc = new Document();
            PdfWriter.getInstance(doc, fos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph p = new Paragraph("Unofficial Transcript – " + studentId, titleFont);
            p.setAlignment(Element.ALIGN_CENTER);
            p.setSpacingAfter(12f);
            doc.add(p);

            PdfPTable table = new PdfPTable(9); // columns
            table.setWidthPercentage(100);

            String[] headers = {
                    "EnrollmentID", "Status", "CourseID", "Code",
                    "Title", "Credits", "Semester", "Year", "FinalGrade"
            };

            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Paragraph(h, normalFont));
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                table.addCell(cell);
            }

            while (rs.next()) {
                table.addCell(String.valueOf(rs.getInt("enrollment_id")));
                table.addCell(rs.getString("status"));
                table.addCell(rs.getString("course_id"));
                table.addCell(rs.getString("code"));
                table.addCell(rs.getString("title"));
                table.addCell(String.valueOf(rs.getInt("credits")));
                table.addCell(rs.getString("semester"));
                table.addCell(String.valueOf(rs.getInt("year")));
                table.addCell(rs.getString("final_grade"));
            }

            doc.add(table);
            doc.close();

            JOptionPane.showMessageDialog(parent,
                    "Transcript PDF saved to:\n" + file.getAbsolutePath(),
                    "Transcript PDF",
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (DocumentException de) {
            de.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "PDF error: " + de.getMessage(),
                    "Transcript PDF",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(parent,
                    "Failed to export PDF: " + ex.getMessage(),
                    "Transcript PDF",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
