package erp.ui.admin;

import erp.db.DatabaseConnection;
import erp.db.MaintenanceService;
import erp.ui.common.FontKit;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

public class MaintenancePanel extends JPanel {

    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private final AdminFrameBase parentFrame;
    private final String adminId;
    private final String adminDisplayName;

    private final JToggleButton maintenanceToggle = new JToggleButton();

    public MaintenancePanel(AdminFrameBase parentFrame, String adminId, String adminDisplayName) {
        this.parentFrame = parentFrame;
        this.adminId = adminId;
        this.adminDisplayName = adminDisplayName;

        setLayout(new BorderLayout());
        setOpaque(false);
        setBackground(BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadMaintenanceState();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Maintenance & Backup");
        title.setFont(FontKit.bold(22f));
        title.setForeground(TEXT_900);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        RoundedButton backBtn = new RoundedButton("Back");
        backBtn.setFont(FontKit.semibold(13f));
        backBtn.setBackground(new Color(148, 163, 184));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        backBtn.addActionListener(e -> {
            new AdminDashboard(adminId, adminDisplayName).setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        right.add(backBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        body.add(buildMaintenanceCard());
        body.add(Box.createVerticalStrut(16));
        body.add(buildBackupCard());

        return body;
    }

    // ---------- Maintenance mode card ----------

    private JComponent buildMaintenanceCard() {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 20, 16, 20));
        card.setLayout(new BorderLayout(16, 0));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("Maintenance Mode");
        h.setFont(FontKit.semibold(16f));
        h.setForeground(TEXT_900);

        JLabel sub = new JLabel("When ON, students and instructors cannot modify data; all write operations are disabled.");
        sub.setFont(FontKit.regular(13f));
        sub.setForeground(TEXT_600);

        left.add(h);
        left.add(Box.createVerticalStrut(4));
        left.add(sub);

        card.add(left, BorderLayout.CENTER);

        maintenanceToggle.setText("OFF");
        maintenanceToggle.setFocusPainted(false);
        maintenanceToggle.setBorderPainted(false);
        maintenanceToggle.setOpaque(true);
        maintenanceToggle.setFont(FontKit.semibold(13f));
        maintenanceToggle.setBackground(new Color(248, 250, 252));
        maintenanceToggle.setForeground(TEXT_900);
        maintenanceToggle.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        maintenanceToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        maintenanceToggle.addActionListener(e -> onToggleMaintenance());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(maintenanceToggle);
        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void loadMaintenanceState() {
        boolean on = MaintenanceService.isMaintenanceOn();
        updateToggleVisual(on);
    }

    private void onToggleMaintenance() {
        boolean newState = maintenanceToggle.isSelected();

        int res = JOptionPane.showConfirmDialog(
                this,
                newState
                        ? "Turn ON maintenance mode?\n\nStudents and instructors will become read-only."
                        : "Turn OFF maintenance mode and re-enable editing?",
                "Confirm Maintenance Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (res != JOptionPane.YES_OPTION) {
            // revert visual
            updateToggleVisual(!newState);
            return;
        }

        try {
            MaintenanceService.setMaintenance(newState);
            updateToggleVisual(newState);
            JOptionPane.showMessageDialog(
                    this,
                    "Maintenance mode is now " + (newState ? "ON" : "OFF") + ".",
                    "Maintenance Mode",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to update maintenance mode:\n" + ex.getMessage(),
                    "DB Error",
                    JOptionPane.ERROR_MESSAGE
            );
            // revert toggle
            updateToggleVisual(!newState);
        }
    }

    private void updateToggleVisual(boolean on) {
        maintenanceToggle.setSelected(on);
        if (on) {
            maintenanceToggle.setText("ON");
            maintenanceToggle.setBackground(new Color(22, 163, 74));  // green
            maintenanceToggle.setForeground(Color.WHITE);
        } else {
            maintenanceToggle.setText("OFF");
            maintenanceToggle.setBackground(new Color(248, 250, 252));
            maintenanceToggle.setForeground(TEXT_900);
        }
    }

    // ---------- Backup / export / import card ----------

    private JComponent buildBackupCard() {
        RoundedPanel card = new RoundedPanel(16);
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 20, 20, 20));
        card.setLayout(new BorderLayout(0, 12));

        JLabel h = new JLabel("Backup & Data Export");
        h.setFont(FontKit.semibold(16f));
        h.setForeground(TEXT_900);

        JLabel sub = new JLabel("Create CSV backups of key tables or import from CSV files.");
        sub.setFont(FontKit.regular(13f));
        sub.setForeground(TEXT_600);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(h);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        card.add(header, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);

        RoundedButton exportFull = new RoundedButton("Export (users, students, instructors)");
        exportFull.setFont(FontKit.semibold(13f));
        exportFull.setBackground(new Color(15, 118, 110));
        exportFull.setForeground(Color.WHITE);
        exportFull.setBorder(new EmptyBorder(8, 14, 8, 14));
        exportFull.addActionListener(e -> onExportCoreTables());

        RoundedButton exportSelected = new RoundedButton("Export Selected Tables…");
        exportSelected.setFont(FontKit.semibold(13f));
        exportSelected.setBackground(new Color(37, 99, 235));
        exportSelected.setForeground(Color.WHITE);
        exportSelected.setBorder(new EmptyBorder(8, 14, 8, 14));
        exportSelected.addActionListener(e -> onExportSelectedTables());

        RoundedButton importBtn = new RoundedButton("Import from CSV…");
        importBtn.setFont(FontKit.semibold(13f));
        importBtn.setBackground(new Color(148, 163, 184));
        importBtn.setForeground(Color.WHITE);
        importBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        importBtn.addActionListener(e ->
                JOptionPane.showMessageDialog(
                        this,
                        "Safe CSV import can be added later. For now, only export is implemented.",
                        "Import",
                        JOptionPane.INFORMATION_MESSAGE));

        buttons.add(exportFull);
        buttons.add(exportSelected);
        buttons.add(importBtn);

        card.add(buttons, BorderLayout.CENTER);

        return card;
    }

    private void onExportCoreTables() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder for CSV backup");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File dir = chooser.getSelectedFile();
        try {
            exportAuthTableToCsv("users_auth", new File(dir, "users_auth.csv"));
            exportErpTableToCsv("students", new File(dir, "students.csv"));
            exportErpTableToCsv("instructors", new File(dir, "instructors.csv"));

            JOptionPane.showMessageDialog(
                    this,
                    "Export complete.\nFiles written to:\n" + dir.getAbsolutePath(),
                    "Export",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Export failed:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportSelectedTables() {
        String[] allTables = {
                "auth_db.users_auth",
                "erp_db.students",
                "erp_db.instructors",
                "erp_db.courses",
                "erp_db.sections",
                "erp_db.enrollments",
                "erp_db.grades"
        };

        JList<String> list = new JList<>(allTables);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setVisibleRowCount(8);

        int res = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(list),
                "Select tables to export",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (res != JOptionPane.OK_OPTION) return;

        List<String> selected = list.getSelectedValuesList();
        if (selected.isEmpty()) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder for CSV export");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File dir = chooser.getSelectedFile();

        try {
            for (String full : selected) {
                String[] parts = full.split("\\.");
                if (parts.length != 2) continue;
                String db = parts[0];
                String table = parts[1];

                File out = new File(dir, table + ".csv");
                if ("auth_db".equalsIgnoreCase(db)) {
                    exportAuthTableToCsv(table, out);
                } else {
                    exportErpTableToCsv(table, out);
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Selected tables exported to:\n" + dir.getAbsolutePath(),
                    "Export",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Export failed:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // ----- CSV helpers -----

    private void exportAuthTableToCsv(String table, File outFile) throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.auth().getConnection()) {
            exportTableToCsv(conn, table, outFile);
        }
    }

    private void exportErpTableToCsv(String table, File outFile) throws SQLException, IOException {
        try (Connection conn = DatabaseConnection.erp().getConnection()) {
            exportTableToCsv(conn, table, outFile);
        }
    }

    private void exportTableToCsv(Connection conn, String table, File outFile)
            throws SQLException, IOException {

        String sql = "SELECT * FROM " + table;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             FileWriter fw = new FileWriter(outFile)) {

            ResultSetMetaData md = rs.getMetaData();
            int cols = md.getColumnCount();

            // header
            for (int i = 1; i <= cols; i++) {
                if (i > 1) fw.write(",");
                fw.write(escapeCsv(md.getColumnName(i)));
            }
            fw.write("\n");

            // rows
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1) fw.write(",");
                    Object val = rs.getObject(i);
                    fw.write(escapeCsv(val == null ? "" : val.toString()));
                }
                fw.write("\n");
            }
        }
    }

    private String escapeCsv(String v) {
        if (v.contains("\"") || v.contains(",") || v.contains("\n") || v.contains("\r")) {
            v = v.replace("\"", "\"\"");
            return "\"" + v + "\"";
        }
        return v;
    }
}
