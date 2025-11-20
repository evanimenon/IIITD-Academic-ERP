package erp.ui.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.*;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import erp.db.DatabaseConnection;
import erp.ui.common.FontKit;
import erp.ui.common.NavButton;
import erp.ui.common.RoundedButton;
import erp.ui.common.RoundedPanel;
import erp.ui.common.RoundedTextField;

public class EditCourse extends JFrame {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color TEAL = new Color(28, 122, 120);
    private static final Color TEAL_LIGHT = new Color(55, 115, 110);
    private static final Color BG = new Color(246, 247, 248);
    private static final Color TEXT_900 = new Color(24, 30, 37);
    private static final Color BORDER = new Color(230, 233, 236);
    private static final Color TEXT_600 = new Color(100, 116, 139);
    private static final Color CARD = Color.WHITE;

    private RoundedTextField codeField, titleField, creditField;

    private final String courseId;
    private final String adminName;

    private RoundedPanel actionCard(String title, String desc, Runnable onClick) {
        RoundedPanel card = new RoundedPanel(20);
        card.setBackground(CARD);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FontKit.bold(18f));
        titleLabel.setForeground(TEXT_900);

        JLabel descLabel = new JLabel("<html><p style='width:240px;'>" + desc + "</p></html>");
        descLabel.setFont(FontKit.regular(14f));
        descLabel.setForeground(TEXT_600);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);

        // hover + click behavior
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            Color normal = CARD;
            Color hover = new Color(238, 241, 245);
            @Override public void mouseEntered(MouseEvent e) {
                card.setBackground(hover); card.repaint();
            }
            @Override public void mouseExited(MouseEvent e) {
                card.setBackground(normal); card.repaint();
            }
            @Override public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

        return card;
    }

    public EditCourse(String adminName, String courseId) {
        this.adminName = adminName;
        this.courseId = courseId;

        setTitle("IIITD ERP – Assign Instructor");
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
            EditCourse.this.dispose();
        });
        nav.add(dashboardBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton addUserBtn = new NavButton("👤 Add User", false);
        addUserBtn.addActionListener(e -> {
            new AddUser(adminName).setVisible(true);
            EditCourse.this.dispose();
        });
        nav.add(addUserBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton manageCoursesBtn = new NavButton("📘 Manage Courses", true);
        manageCoursesBtn.addActionListener(e -> {
            new ManageCourses(adminName).setVisible(true);
            EditCourse.this.dispose();
        });
        nav.add(manageCoursesBtn);
        nav.add(Box.createVerticalStrut(8));

        NavButton assignInstBtn = new NavButton("👨 Assign Instructor", false);
        assignInstBtn.addActionListener(e -> {
            new AssignInstructor(adminName).setVisible(true);
            EditCourse.this.dispose();
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
        nav.add(new NavButton("  🚪  Log Out", false)); // Used door emoji for log out
        
        sidebar.add(nav, BorderLayout.CENTER);
        root.add(sidebar, BorderLayout.WEST);

        // --- Top banner ---
        RoundedPanel hero = new RoundedPanel(24);
        hero.setBackground(TEAL_DARK);
        hero.setBorder(new EmptyBorder(24, 28, 24, 28));
        hero.setLayout(new BorderLayout());

        JLabel h1 = new JLabel("✒️ Edit Course");
        h1.setFont(FontKit.bold(28f));
        h1.setForeground(Color.WHITE);
        hero.add(h1, BorderLayout.WEST);

        JLabel adminLabel = new JLabel("Logged in as " + adminName);
        adminLabel.setFont(FontKit.regular(14f));
        adminLabel.setForeground(new Color(200, 230, 225));
        hero.add(adminLabel, BorderLayout.EAST);

        root.add(hero, BorderLayout.NORTH);

        // --- Main content area ---
        RoundedPanel form = new RoundedPanel(20);
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(32, 40, 32, 40));
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 12, 12, 12);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;

        // Fields
        codeField   = new RoundedTextField(20, "Enter course code");
        titleField  = new RoundedTextField(20, "Enter course title");
        creditField = new RoundedTextField(20, "Enter credits");

        addRow(form, gc, 0, "Course Code", codeField);
        addRow(form, gc, 1, "Title", titleField);
        addRow(form, gc, 2, "Credits", creditField);
        gc.gridy = 3;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.EAST;

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        btns.setOpaque(false);

        RoundedButton cancel = new RoundedButton("Cancel");
        cancel.addActionListener(e -> {
            new EditExistingCourses(adminName).setVisible(true);
            dispose();
        });

        RoundedButton save = new RoundedButton("Save Changes");
        save.addActionListener(e -> saveChanges());

        btns.add(cancel);
        btns.add(save);

        form.add(btns, gc);

        // --- Wrap form + action cards ---
        JPanel centerWrap = new JPanel();
        centerWrap.setLayout(new BoxLayout(centerWrap, BoxLayout.Y_AXIS));
        centerWrap.setOpaque(false);

        // form (already a RoundedPanel)
        centerWrap.add(form);
        centerWrap.add(Box.createVerticalStrut(24));

        // --- Action Cards ---
        JPanel cardRow = new JPanel(new GridLayout(1, 3, 20, 0));
        cardRow.setOpaque(false);
        cardRow.setBorder(new EmptyBorder(0, 50, 0, 50));

        cardRow.add(actionCard(
                "📚 Add a Section",
                "Create a new section under this course.",
                () -> {
                    new AddSection(adminName, courseId).setVisible(true);
                    dispose();
                }
        ));

        cardRow.add(actionCard(
                "✏️ Edit Sections",
                "Modify or delete an existing section of this course.",
                () -> {
                    new EditSections(adminName, courseId).setVisible(true);
                    dispose();
                }
        ));

        cardRow.add(actionCard(
                "🗑️ Delete Course",
                "Remove this course and all its sections from the system.",
                () -> {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Are you sure you want to delete this course? This action cannot be undone.",
                            "Confirm Deletion",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.WARNING_MESSAGE);
                    if (confirm == JOptionPane.YES_OPTION) {
                        String query1 = "DELETE FROM courses WHERE course_id = ?";
                        String query2 = "DELETE FROM sections WHERE course_id = ?";
                        try (Connection conn = DatabaseConnection.erp().getConnection();
                                PreparedStatement ps1 = conn.prepareStatement(query1);
                                PreparedStatement ps2 = conn.prepareStatement(query2)) {
    
                                ps2.setString(1, courseId);
                                ps2.executeUpdate();
    
                                ps1.setString(1, courseId);
                                ps1.executeUpdate();
    
                                JOptionPane.showMessageDialog(this,
                                        "Course deleted successfully!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
    
                                new ManageCourses(adminName).setVisible(true);
                                dispose();
    
                            } 
                            catch (Exception e) {
                                JOptionPane.showMessageDialog(this,
                                        "Failed to delete course:\n" + e.getMessage(),
                                        "Database Error",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                    }
                }
        ));

        centerWrap.add(cardRow);
        centerWrap.add(Box.createVerticalStrut(20));

        root.add(centerWrap, BorderLayout.CENTER);

        loadCourse();
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row,String label, JComponent field) {
        gc.gridx = 0; gc.gridy = row;
        gc.weightx = 0;
        JLabel l = new JLabel(label);
        l.setFont(FontKit.semibold(15f));
        p.add(l, gc);

        gc.gridx = 1;
        gc.weightx = 1;
        p.add(field, gc);
    }

    private void loadCourse() {
        String query = "SELECT code, title, credits FROM courses WHERE course_id = ?";
        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, courseId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                codeField.setText(rs.getString("code"));
                titleField.setText(rs.getString("title"));
                creditField.setText(String.valueOf(rs.getInt("credits")));
            }
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Failed to load course:\n" + e.getMessage(),"Database Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        String creditsStr = creditField.getText().trim();

        if (code.isEmpty() || title.isEmpty() || creditsStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsStr);
        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,"Credits must be a number.","Invalid Input",JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "UPDATE courses SET code = ?, title = ?, credits = ? WHERE course_id = ?";

        try (Connection conn = DatabaseConnection.erp().getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, code);
            ps.setString(2, title);
            ps.setInt(3, credits);
            ps.setString(4, courseId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Course updated successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            new EditExistingCourses(adminName).setVisible(true);
            dispose();

        } 
        catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to update course:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
