package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import erp.ui.auth.LoginPage.FontKit;
import erp.ui.common.Dashboard;
import erp.ui.common.Dashboard.NavButton;

public class CourseCatalogue extends JFrame {
    // Palette
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG_LIGHT = new Color(246, 247, 248);
    private static final Color BORDER_COLOR = new Color(230, 233, 236);

    public CourseCatalogue(String userDisplayName) {
        LoginPage.FontKit.init(); 

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

        // Profile block (Top part of sidebar)
        JPanel profile = new JPanel();
        profile.setOpaque(false);
        profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(8, 8, 32, 8));

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
        NavButton homeBtn = new NavButton("  🏠  Home", false);
        nav.add(homeBtn);
        homeBtn.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                new Dashboard(userDisplayName).setVisible(true);
                dispose();
            });
        });
        nav.add(Box.createVerticalStrut(8));
        
        // Highlighted button for current page
        NavButton courseCatalogueBtn = new NavButton("  📚  Course Catalogue", true); // Set to active
        nav.add(courseCatalogueBtn);
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  📜  My Registrations", false));
        nav.add(Box.createVerticalStrut(8));
        nav.add(new NavButton("  🗺️  Time Table", false));
        nav.add(Box.createVerticalStrut(40)); // Increased vertical space
        
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
        nav.add(new NavButton("  🚪  Log Out", false)); // Used door emoji for log out
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Main Content ---
        JPanel mainContent = new JPanel();
        mainContent.setBackground(BG_LIGHT);
        mainContent.setLayout(new BorderLayout());
        mainContent.setBorder(new EmptyBorder(30, 30, 30, 30));

        // Table Data
        String[] columns = {"SNo.", "Course Code", "Course Title", "Credits", "Action"};
        Object[][] data = {
            {"1.", "BIO101", "Foundations of Biology", "4", "Register"},
            {"2.", "CSE101", "Introduction to Programming", "4", "Register"},
            {"3.", "CSE102", "Data Structures and Algorithms", "4", "Register"},
            {"4.", "COM301A", "Technical Communication", "4", "Register"},
            {"5.", "MTH201", "Linear Algebra", "3", "Register"},
            {"6.", "PHY101", "Classical Mechanics", "4", "Register"},
            {"7.", "HIS101", "World History", "3", "Register"},
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(48); // Increased row height for better look
        table.setIntercellSpacing(new Dimension(0, 1)); // Minimal vertical spacing
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER_COLOR);
        table.setFont(LoginPage.FontKit.regular(15f));
        table.setBackground(Color.WHITE); // Ensure table background is white
        
        // --- Header Styling & FIX for Visibility ---
        JTableHeader header = table.getTableHeader();
        header.setBackground(TEAL_DARK);
        header.setForeground(Color.WHITE);
        // FIX: Using a known-good FontKit to prevent rendering failure
        header.setFont(LoginPage.FontKit.bold(16f)); 
        header.setPreferredSize(new Dimension(table.getWidth(), 55)); // Force header height
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setOpaque(true); 

        // Center alignment for table data cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        // Button Column Setup
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Width adjustment
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(BG_LIGHT);
        scroll.getViewport().setBackground(Color.WHITE); // Ensure viewport behind table is white
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); // Light border for the table

        // Correct Placement of Scroll Pane inside mainContent
        mainContent.add(scroll, BorderLayout.CENTER);
        root.add(mainContent, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        LoginPage.FontKit.init();
        SwingUtilities.invokeLater(() -> new CourseCatalogue("Student 123").setVisible(true));
    }
}