package erp.ui.admin;

import erp.db.DatabaseConnection;
import erp.db.Maintenance;
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

public class MaintenancePanel extends JPanel {

    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);

    private final AdminFrameBase parentFrame;
    protected final String adminId;
    protected final String adminDisplayName;

    private final JToggleButton maintenanceToggle = new JToggleButton();
    private final JLabel maintenanceStatusChip = new JLabel();

    public MaintenancePanel(AdminFrameBase parentFrame, String adminId, String adminDisplayName) {
        this.parentFrame = parentFrame;
        this.adminId = adminId;
        this.adminDisplayName = adminDisplayName;

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(BG);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        loadMaintenanceState();
    }

    // ---------- HEADER ----------

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Maintenance & Backup");
        title.setFont(FontKit.bold(22f));
        title.setForeground(TEXT_900);

        JLabel subtitle = new JLabel("Control maintenance mode and export essential data.");
        subtitle.setFont(FontKit.regular(13f));
        subtitle.setForeground(TEXT_600);

        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        header.add(titleBlock, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        RoundedButton backBtn = new RoundedButton("Back to Dashboard");
        backBtn.setFont(FontKit.semibold(13f));
        backBtn.setBackground(new Color(148, 163, 184));
        backBtn.setForeground(Color.WHITE);
        backBtn.setBorder(new EmptyBorder(8, 14, 8, 8));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new AdminDashboard(adminId, adminDisplayName).setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        right.add(backBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ---------- BODY WRAPPER (CENTERED CARDS) ----------

    private JComponent buildBody() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setMaximumSize(new Dimension(900, Integer.MAX_VALUE));

        column.add(buildMaintenanceCard());
        column.add(Box.createVerticalStrut(16));
        column.add(buildBackupCard());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.NONE;
        wrapper.add(column, gbc);

        return wrapper;
    }

    // ---------- MAINTENANCE MODE CARD ----------

    private JComponent buildMaintenanceCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(16, 20, 16, 20)));
        card.setLayout(new BorderLayout(16, 0));
        card.setMaximumSize(new Dimension(900, 120));

        // Left: text
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("Maintenance Mode");
        h.setFont(FontKit.semibold(16f));
        h.setForeground(TEXT_900);

        JLabel sub = new JLabel(
                "<html>When maintenance is ON, students and instructors can only view data.<br>" +
                        "Use this while performing critical updates or backups.</html>");
        sub.setFont(FontKit.regular(13f));
        sub.setForeground(TEXT_600);

        left.add(h);
        left.add(Box.createVerticalStrut(6));
        left.add(sub);

        card.add(left, BorderLayout.CENTER);

        // Right: status chip + toggle stacked vertically
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Status chip
        maintenanceStatusChip.setFont(FontKit.semibold(12f));
        maintenanceStatusChip.setOpaque(true);
        maintenanceStatusChip.setBorder(new EmptyBorder(4, 10, 4, 10));
        maintenanceStatusChip.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // Toggle styled as pill
        maintenanceToggle.setFocusPainted(false);
        maintenanceToggle.setBorderPainted(false);
        maintenanceToggle.setOpaque(true);
        maintenanceToggle.setFont(FontKit.semibold(13f));
        maintenanceToggle.setPreferredSize(new Dimension(140, 32));
        maintenanceToggle.setMaximumSize(new Dimension(160, 32));
        maintenanceToggle.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        maintenanceToggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        maintenanceToggle.addActionListener(e -> onToggleMaintenance());

        right.add(maintenanceStatusChip);
        right.add(Box.createVerticalStrut(8));
        right.add(maintenanceToggle);

        card.add(right, BorderLayout.EAST);

        return card;
    }

    private void loadMaintenanceState() {
    boolean on = MaintenanceService.isMaintenanceOn();
    updateToggleVisual(on);
}

private void onToggleMaintenance() {
    // Swing has already toggled the button at this point
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
        // Revert visual to previous state
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
        // Revert if DB write failed
        updateToggleVisual(!newState);
    }
}


    private void updateToggleVisual(boolean on) {
        maintenanceToggle.setSelected(on);

        if (on) {
            maintenanceToggle.setText("Turn OFF maintenance");
            maintenanceToggle.setBackground(new Color(22, 163, 74));
            maintenanceToggle.setForeground(Color.WHITE);

            maintenanceStatusChip.setText("Maintenance ON");
            maintenanceStatusChip.setBackground(new Color(220, 252, 231));
            maintenanceStatusChip.setForeground(new Color(21, 128, 61));
        } else {
            maintenanceToggle.setText("Turn ON maintenance");
            maintenanceToggle.setBackground(new Color(248, 250, 252));
            maintenanceToggle.setForeground(TEXT_900);

            maintenanceStatusChip.setText("Maintenance OFF");
            maintenanceStatusChip.setBackground(new Color(239, 246, 255));
            maintenanceStatusChip.setForeground(new Color(37, 99, 235));
        }
    }

    // ---------- BACKUP / EXPORT CARD ----------

    private JComponent buildBackupCard() {
        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(18, 20, 20, 20)));
        card.setLayout(new BorderLayout(0, 14));
        card.setMaximumSize(new Dimension(900, 260));

        // Header block
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel h = new JLabel("Backup & Data Export");
        h.setFont(FontKit.semibold(16f));
        h.setForeground(TEXT_900);

        JLabel sub = new JLabel("Download CSV snapshots of key tables. Keep these in a safe, versioned location.");
        sub.setFont(FontKit.regular(13f));
        sub.setForeground(TEXT_600);

        header.add(h);
        header.add(Box.createVerticalStrut(4));
        header.add(sub);

        card.add(header, BorderLayout.NORTH);

        // Buttons + descriptions in a vertical stack
        JPanel rows = new JPanel();
        rows.setOpaque(false);
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));

        // 1) Export core tables
        RoundedButton exportFull = new RoundedButton("Export core user tables");
        exportFull.setFont(FontKit.semibold(13f));
        exportFull.setBackground(new Color(15, 118, 110));
        exportFull.setForeground(Color.WHITE);
        exportFull.setBorder(new EmptyBorder(8, 14, 8, 14));
        exportFull.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportFull.addActionListener(e -> onExportCoreTables());

        JLabel exportFullText = new JLabel(
                "users_auth, students, instructors → separate CSV files in a chosen folder.");
        exportFullText.setFont(FontKit.regular(12f));
        exportFullText.setForeground(TEXT_600);

        rows.add(buildButtonRow(exportFull, exportFullText));
        rows.add(Box.createVerticalStrut(10));

        // 2) Export selected tables
        RoundedButton exportSelected = new RoundedButton("Export selected tables…");
        exportSelected.setFont(FontKit.semibold(13f));
        exportSelected.setBackground(new Color(37, 99, 235));
        exportSelected.setForeground(Color.WHITE);
        exportSelected.setBorder(new EmptyBorder(8, 14, 8, 14));
        exportSelected.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        exportSelected.addActionListener(e -> onExportSelectedTables());

        JLabel exportSelectedText = new JLabel("Choose any combination of auth_db / erp_db tables to export as CSV.");
        exportSelectedText.setFont(FontKit.regular(12f));
        exportSelectedText.setForeground(TEXT_600);

        rows.add(buildButtonRow(exportSelected, exportSelectedText));
        rows.add(Box.createVerticalStrut(10));

        // 3) Import (future)
        // RoundedButton importBtn = new RoundedButton("Import from CSV (coming soon)");
        // importBtn.setFont(FontKit.semibold(13f));
        // importBtn.setBackground(new Color(148, 163, 184));
        // importBtn.setForeground(Color.WHITE);
        // importBtn.setBorder(new EmptyBorder(8, 14, 8, 14));
        // importBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // importBtn.addActionListener(e -> JOptionPane.showMessageDialog(
        //         this,
        //         "Safe CSV import can be added later.\nRight now only export is implemented.",
        //         "Import",
        //         JOptionPane.INFORMATION_MESSAGE));

        JLabel importText = new JLabel("Planned: validate CSV files and safely merge into the database.");
        importText.setFont(FontKit.regular(12f));
        importText.setForeground(TEXT_600);

        // rows.add(buildButtonRow(importBtn, importText));

        card.add(rows, BorderLayout.CENTER);

        return card;
    }

    private JPanel buildButtonRow(JButton button, JLabel description) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        description.setAlignmentX(Component.LEFT_ALIGNMENT);

        row.add(button, BorderLayout.WEST);
        row.add(description, BorderLayout.CENTER);

        return row;
    }

    // ---------- CSV HELPERS ----------

    private void onExportCoreTables() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder for CSV backup");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

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
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION)
            return;

        java.util.List<String> selected = list.getSelectedValuesList();
        if (selected.isEmpty())
            return;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose folder for CSV export");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File dir = chooser.getSelectedFile();

        try {
            for (String full : selected) {
                String[] parts = full.split("\\.");
                if (parts.length != 2)
                    continue;
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
                if (i > 1)
                    fw.write(",");
                fw.write(escapeCsv(md.getColumnName(i)));
            }
            fw.write("\n");

            // rows
            while (rs.next()) {
                for (int i = 1; i <= cols; i++) {
                    if (i > 1)
                        fw.write(",");
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
