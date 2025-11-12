package erp.ui.student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import erp.ui.common.FontKit;

public class CourseRegistrationDialog extends JDialog {
    private static final Color TEAL_DARK = new Color(39, 96, 92);
    private static final Color BG_LIGHT = new Color(246, 247, 248);
    private JWindow overlay;

    public CourseRegistrationDialog(JFrame parent, String courseCode, String courseTitle, String capacity, String room, String acronym) {
        super(parent, "Register Course", true); // Modal dialog
        setUndecorated(true); // Remove default window decorations

        // --- Overlay Setup ---
        overlay = new JWindow(parent);
        overlay.setBackground(new Color(0, 0, 0, 120)); // semi-transparent black
        overlay.setSize(parent.getSize());
        overlay.setLocationRelativeTo(parent);
        overlay.setVisible(true);
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 1));
        contentPane.setBackground(Color.WHITE);
        setContentPane(contentPane);

        // --- Header Panel (Dark Teal) ---
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(TEAL_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 24, 16, 24));
        
        JLabel headerLabel = new JLabel(courseCode + ": " + courseTitle);
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setFont(FontKit.bold(18f));
        headerPanel.add(headerLabel);
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // --- Body Panel (White) ---
        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));
        bodyPanel.setBackground(Color.WHITE);
        bodyPanel.setBorder(new EmptyBorder(24, 24, 24, 24));
        
        // Data points
        addDetailRow(bodyPanel, "Course Code:", courseCode, true);
        addDetailRow(bodyPanel, "Course Title:", courseTitle, true);
        addDetailRow(bodyPanel, "Course Acronym:", acronym, true);
        addDetailRow(bodyPanel, "Capacity:", capacity, true);
        addDetailRow(bodyPanel, "Room:", room, true);

        bodyPanel.add(Box.createVerticalStrut(20));

        contentPane.add(bodyPanel, BorderLayout.CENTER);

        // --- Footer Panel (Action Button) ---
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(Color.WHITE);
        footerPanel.setBorder(new EmptyBorder(0, 24, 24, 24));

        
        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(FontKit.semibold(16f));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(TEAL_DARK);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setPreferredSize(new Dimension(150, 45));
        registerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        
        // Custom button style for rounded corners
        registerBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        registerBtn.setMargin(new Insets(0, 0, 0, 0));
        registerBtn.putClientProperty("JButton.buttonType", "roundRect");
        
        registerBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Successfully registered for " + courseCode, "Registration Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JButton closeBtn = new JButton("Cancel");
        closeBtn.setFont(FontKit.semibold(16f));
        closeBtn.setForeground(TEAL_DARK);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> dispose());
        
        footerPanel.add(closeBtn);
        footerPanel.add(Box.createHorizontalStrut(10));
        footerPanel.add(registerBtn);
        
        contentPane.add(footerPanel, BorderLayout.SOUTH);

        // Finalize dialog size and position
        pack();
        setLocationRelativeTo(parent);
    }

    private void disposeOverlay() {
        if (overlay != null) {
            overlay.dispose();
        }
    }
    
    private void addDetailRow(JPanel parent, String label, String value, boolean isBold) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setBackground(Color.WHITE);
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(FontKit.bold(16f)); // Labels always bold
        labelComp.setForeground(TEAL_DARK);
        labelComp.setPreferredSize(new Dimension(150, 25));

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(isBold ? FontKit.semibold(16f) : FontKit.regular(16f));
        
        row.add(labelComp, BorderLayout.WEST);
        row.add(valueComp, BorderLayout.CENTER);
        
        parent.add(row);
        parent.add(Box.createVerticalStrut(8));
    }
    
    // Override paint to draw rounded border
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(TEAL_DARK);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
        g2.dispose();
    }
}