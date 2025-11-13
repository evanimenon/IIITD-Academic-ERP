package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.regex.PatternSyntaxException;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.student.StudentDashboard.NavButton;

public class CourseCatalogue extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG_LIGHT   = new Color(246, 247, 248);
    private static final Color BORDER_COLOR = new Color(230, 233, 236);

    static class TitleCell {
        final String title;
        final String instructors;
        TitleCell(String title, String instructors) { this.title = title; this.instructors = instructors; }
        @Override public String toString() { return title; }
    }

    private TableRowSorter<TableModel> sorter;
    private DefaultTableModel model;
    private JTable table;

    public CourseCatalogue(String userDisplayName) {
        FontKit.init();

        setTitle("Course Catalogue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_LIGHT);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(userDisplayName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        JLabel meta = new JLabel("Year, Program");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);

        sidebar.add(profile, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton homeBtn = new NavButton("  🏠  Home", false);
        homeBtn.addActionListener(e -> { new StudentDashboard(userDisplayName).setVisible(true); dispose(); });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton courseCatalogueBtn = new NavButton("  📚  Course Catalogue", true);
        nav.add(courseCatalogueBtn);
        nav.add(Box.createVerticalStrut(8));

        nav.add(new NavButton("  📜  My Registrations", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🗺️  Time Table", false));
        nav.add(Box.createVerticalStrut(40));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 120, 116));
        sep.setMaximumSize(new Dimension(240, 1));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(40));

        NavButton logoutBtn = new NavButton("  🚪  Log Out", false);
        logoutBtn.addActionListener(e -> { new erp.ui.auth.LoginPage().setVisible(true); dispose(); });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Main content ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        // tried to implement search bar, but its not working yet. 
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField();
        searchField.setFont(FontKit.regular(15f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setToolTipText("Search by course ID, code, title, or instructor");
        searchPanel.add(searchField, BorderLayout.CENTER);
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(FontKit.bold(16f));
        searchPanel.add(searchIcon, BorderLayout.WEST);
        mainContent.add(searchPanel, BorderLayout.NORTH);
        mainContent.add(searchPanel, BorderLayout.NORTH);
        mainContent.revalidate();
        mainContent.repaint();

        searchPanel.setBackground(BG_LIGHT);
        searchField.setBackground(Color.WHITE);



        // Table setup
        String[] columns = {"Course ID", "Code", "Title", "Credits"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 2 ? TitleCell.class : super.getColumnClass(columnIndex);
            }
        };

        table = new JTable(model);
        table.setFont(FontKit.regular(15f));
        table.setRowHeight(64); // taller by default
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TEAL_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(FontKit.bold(16f));
        header.setPreferredSize(new Dimension(table.getWidth(), 50));

        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Column sizing
        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(100);
        cm.getColumn(1).setPreferredWidth(80);
        cm.getColumn(2).setPreferredWidth(700);
        cm.getColumn(3).setPreferredWidth(80);

        // Renderer for title + instructor
        TableCellRenderer titleRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                TitleCell cell = (TitleCell) value;
                l.setOpaque(true);
                l.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);
                l.setBorder(new EmptyBorder(10, 10, 10, 10));
                l.setFont(FontKit.regular(14f));
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                String title = cell.title == null ? "" : cell.title;
                String inst  = cell.instructors == null || cell.instructors.isBlank() ? "TBA" : cell.instructors;

                l.setText("<html><body style='width:95%;'>"
                        + "<div style='font-weight:600; color:#0066cc; text-decoration:underline; font-size:14px;'>"
                        + escapeHtml(title)
                        + "</div>"
                        + "<div style='margin-top:3px; font-size:12px; color:#64748b;'>Instructor: "
                        + escapeHtml(inst)
                        + "</div></body></html>");

                int needed = l.getPreferredSize().height + 16;
                if (tbl.getRowHeight(row) != needed) tbl.setRowHeight(row, needed);
                return l;
            }
        };
        cm.getColumn(2).setCellRenderer(titleRenderer);

        // Clickable titles
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 2) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String courseId = model.getValueAt(modelRow, 0).toString();
                    TitleCell cell = (TitleCell) model.getValueAt(modelRow, 2);
                    JOptionPane.showMessageDialog(
                        CourseCatalogue.this,
                        "Register for: " + courseId + "\n" + cell.title,
                        "Course Selected", JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        mainContent.add(scrollPane, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);

        // Search filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = searchField.getText();
                if (text.trim().isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    try {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                    } catch (PatternSyntaxException ex) {
                        sorter.setRowFilter(null);
                    }
                }
            }
        });

        loadCourses(model);
    }

    private static String escapeHtml(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void loadCourses(DefaultTableModel model) {
        final String sql =
            "SELECT c.course_id, c.code, c.title, c.credits, " +
            "       COALESCE(GROUP_CONCAT(DISTINCT i.instructor_name ORDER BY i.instructor_name SEPARATOR ', '), 'TBA') AS instructors " +
            "FROM   erp_db.courses c " +
            "LEFT JOIN erp_db.sections s    ON s.course_id = c.course_id " +
            "LEFT JOIN erp_db.instructors i ON i.instructor_id = s.instructor_id " +
            "GROUP BY c.course_id, c.code, c.title, c.credits " +
            "ORDER BY c.course_id ASC";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String courseId = rs.getString("course_id");
                String code = rs.getString("code");
                String title = rs.getString("title");
                int credits = rs.getInt("credits");
                String instructors = rs.getString("instructors");

                model.addRow(new Object[]{ courseId, code, new TitleCell(title, instructors), credits });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FontKit.init();
        erp.db.DatabaseConnection.init();
        SwingUtilities.invokeLater(() -> new CourseCatalogue("Student 123").setVisible(true));
    }
}
