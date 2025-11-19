package erp.ui.admin;
import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import erp.ui.common.RoundedPanel;
import erp.ui.student.CourseCatalog;
import erp.ui.common.NavButton;
import erp.db.Maintenance;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import erp.ui.common.TableHeader;

public class EditExistingCourses extends JFrame{
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;
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

    public EditExistingCourses(String adminName) {
        setTitle("IIITD ERP – Manage Courses");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1220, 840));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // Profile block (Top part of sidebar)
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        EmptyBorder br = new EmptyBorder(8, 8, 32, 8);
        profile.setBorder(br);

        // Circular Avatar with rounded corner panel
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);
        
        JLabel avatar = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                // Draw a circle
                g2.fillOval(0, 0, getWidth(), getHeight()); 
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(100, 100); }
        };
        
        avatarPanel.add(avatar);
        profile.add(avatarPanel);
        profile.add(Box.createVerticalStrut(16));

        JLabel name = new JLabel(adminName);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setForeground(Color.WHITE);
        name.setFont(FontKit.bold(18f));
        profile.add(name);

        JLabel meta = new JLabel("Year, Program");
        meta.setAlignmentX(Component.CENTER_ALIGNMENT);
        meta.setForeground(new Color(210, 225, 221));
        meta.setFont(FontKit.regular(14f));
        profile.add(meta);
        
        // Rounded corners for the entire profile block (visual style enhancement)
        profile.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(TEAL_LIGHT, 1),
            new EmptyBorder(8, 8, 32, 8)
        ));

        sidebar.add(profile, BorderLayout.NORTH);

        // Nav
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        // Navigation Links
        NavButton dashboardBtn = new NavButton("🏠 Home", false);
        dashboardBtn.addActionListener(e -> {
            new AdminDashboard(adminName).setVisible(true);
            EditExistingCourses.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            EditExistingCourses.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            EditExistingCourses.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            EditExistingCourses.this.dispose();
        });
        nav.add(assignInstBtn);
        nav.add(Box.createVerticalStrut(8));
        
        // Separator
        nav.add(new JSeparator() {{ 
            setForeground(new Color(60, 120, 116)); 
            setBackground(new Color(60, 120, 116));
            setMaximumSize(new Dimension(240, 1));
            setAlignmentX(Component.CENTER_ALIGNMENT);
        }});
        nav.add(Box.createVerticalStrut(40));
        nav.add(new NavButton("  ⚙️  Settings", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🚪  Log Out", false));
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("📘 Edit Course");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        //--- Main Area ---
        JPanel main = new JPanel(new BorderLayout());
        main.setOpaque(false);

        // search
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField();
        searchField.setFont(FontKit.regular(15f));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        searchField.setToolTipText("Search by course ID, code, title, or instructor");
        searchPanel.add(new JLabel("🔍"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);
        main.add(searchPanel, BorderLayout.NORTH);

        // table
        String[] columns = {"Course ID", "Code", "Title", "Credits"};
        model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int idx) {
                return idx == 2 ? TitleCell.class : super.getColumnClass(idx);
            }
        };

        table = new JTable(model) {
            // teal row highlight with easy deselect
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                if (isRowSelected(row)) c.setBackground(new Color(220, 241, 239));
                else c.setBackground(Color.WHITE);
                return c;
            }
        };
        table.setFont(FontKit.regular(15f));
        table.setRowHeight(64);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setBackground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader hdr = table.getTableHeader();
        hdr.setDefaultRenderer(new TableHeader());


        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        TableColumnModel cm = table.getColumnModel();
        cm.getColumn(0).setPreferredWidth(110);
        cm.getColumn(1).setPreferredWidth(90);
        cm.getColumn(3).setPreferredWidth(80);
        cm.getColumn(2).setPreferredWidth(700);

        // title+instructor renderer
        TableCellRenderer titleR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(tbl, val, sel, foc, row, col);
                TitleCell cell = (TitleCell) val;
                l.setOpaque(false);
                l.setBorder(new EmptyBorder(10, 10, 10, 10));
                l.setFont(FontKit.regular(14f));
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                String title = cell.title == null ? "" : cell.title;
                String inst  = (cell.instructors == null || cell.instructors.isBlank()) ? "TBA" : cell.instructors;

                l.setText("<html><body style='width:95%;'>"
                        + "<div style='font-weight:600; color:#0066cc; text-decoration:underline; font-size:14px;'>"
                        + esc(title) + "</div>"
                        + "<div style='margin-top:3px; font-size:12px; color:#64748b;'>Instructor: "
                        + esc(inst) + "</div></body></html>");

                // auto height
                int needed = l.getPreferredSize().height + 16;
                if (tbl.getRowHeight(row) != needed) tbl.setRowHeight(row, needed);
                return l;
            }
        };
        cm.getColumn(2).setCellRenderer(titleR);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row < 0) return;

                int modelRow = table.convertRowIndexToModel(row);
                String courseId = String.valueOf(model.getValueAt(modelRow, 0));

                new EditCourse(adminName, courseId).setVisible(true);
                EditExistingCourses.this.dispose();
            }
        });


        JScrollPane sc = new JScrollPane(table);
        sc.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        main.add(sc, BorderLayout.CENTER);

        // search filter
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String t = searchField.getText();
                if (t.isBlank()) sorter.setRowFilter(null);
                else {
                    try { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + t)); }
                    catch (Exception ex) { sorter.setRowFilter(null); }
                }
            }
        });

        loadCourses();
        root.add(main, BorderLayout.CENTER);
    }

    private static String esc(String s) { return s == null ? "" : s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }

    private void loadCourses() {
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
                model.addRow(new Object[] {
                        rs.getString("course_id"),
                        rs.getString("code"),
                        new TitleCell(rs.getString("title"), rs.getString("instructors")),
                        rs.getInt("credits")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading courses:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
