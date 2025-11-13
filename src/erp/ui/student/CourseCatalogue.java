package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.student.Dashboard.NavButton;

public class CourseCatalogue extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG_LIGHT = new Color(246, 247, 248);
    private static final Color BORDER_COLOR = new Color(230, 233, 236);

    public CourseCatalogue(String userDisplayName) {
        FontKit.init();
        setTitle("Course Catalogue");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_LIGHT);
        setContentPane(root);

        // --- Sidebar ---
        JPanel sidebar = new JPanel();
        sidebar.setBackground(TEAL_DARK);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(new EmptyBorder(24, 16, 24, 16));

        // --- Profile Section ---
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 233, 236));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 100);
            }
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

        // --- Navigation Buttons ---
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(16, 0, 16, 0));

        NavButton homeBtn = new NavButton("  🏠  Home", false);
        homeBtn.addActionListener(e -> {
            new Dashboard(userDisplayName).setVisible(true);
            dispose();
        });
        nav.add(homeBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton catalogueBtn = new NavButton("  📚  Course Catalogue", true);
        nav.add(catalogueBtn);
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
        logoutBtn.addActionListener(e -> {
            new erp.ui.auth.LoginPage().setVisible(true);
            dispose();
        });
        nav.add(logoutBtn);

        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Main Content ---
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_LIGHT);
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Table setup
        String[] columns = { "Course ID", "Code", "Title", "Credits" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        // Column width preferences
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Course ID
        table.getColumnModel().getColumn(1).setPreferredWidth(80); // Code
        table.getColumnModel().getColumn(2).setPreferredWidth(600); // Title
        table.getColumnModel().getColumn(3).setPreferredWidth(80); // Credits

        // Let the table fill remaining space proportionally
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        table.setRowHeight(42);
        table.setFont(FontKit.regular(15f));
        table.setBackground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TEAL_DARK);
        header.setForeground(Color.WHITE);
        header.setFont(FontKit.bold(16f));
        header.setPreferredSize(new Dimension(table.getWidth(), 50));
        header.setReorderingAllowed(true);

        // Sort support
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Hyperlink look for Title
        DefaultTableCellRenderer linkRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                l.setForeground(new Color(0, 102, 204));
                l.setFont(FontKit.semibold(15f));
                l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                l.setText("<html><u>" + value + "</u></html>");
                return l;
            }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(linkRenderer);

        // Click on title to "register"
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 2 && row >= 0) {
                    String courseId = table.getValueAt(row, 0).toString();
                    String title = table.getValueAt(row, 2).toString();
                    JOptionPane.showMessageDialog(
                            CourseCatalogue.this,
                            "You clicked to register for:\n" + courseId + " - " + title,
                            "Register Course", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Renderer that wraps text and adjusts row height dynamically
        DefaultTableCellRenderer wrapRenderer = new DefaultTableCellRenderer() {
            private JTextArea textArea = new JTextArea();

            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                textArea.setText(value == null ? "" : value.toString());
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setOpaque(true);
                textArea.setFont(FontKit.regular(15f));

                // background / foreground
                if (isSelected) {
                    textArea.setBackground(table.getSelectionBackground());
                    textArea.setForeground(table.getSelectionForeground());
                } else {
                    textArea.setBackground(Color.WHITE);
                    textArea.setForeground(new Color(0, 102, 204));
                }

                // adjust row height to fit text
                int preferredHeight = textArea.getPreferredSize().height;
                if (table.getRowHeight(row) != preferredHeight) {
                    table.setRowHeight(row, preferredHeight);
                }

                return textArea;
            }
        };
        table.getColumnModel().getColumn(2).setCellRenderer(wrapRenderer);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        mainContent.add(scrollPane, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);

        // --- Load data from MySQL ---
        loadCourses(model);
    }

    private void loadCourses(DefaultTableModel model) {
        String sql = "SELECT course_id, code, title, credits FROM erp_db.courses ORDER BY course_id ASC";
        try (Connection conn = DatabaseConnection.erp().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = {
                        rs.getString("course_id"),
                        rs.getString("code"),
                        rs.getString("title"),
                        rs.getInt("credits")
                };
                model.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading courses from database:\n" + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FontKit.init();
        erp.db.DatabaseConnection.init(); // ensure pool is up
        SwingUtilities.invokeLater(() -> new CourseCatalogue("Student 123").setVisible(true));
    }
}
